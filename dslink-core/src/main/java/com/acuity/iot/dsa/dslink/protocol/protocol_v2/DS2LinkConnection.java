package com.acuity.iot.dsa.dslink.protocol.protocol_v2;

import com.acuity.iot.dsa.dslink.transport.DSBinaryTransport;
import com.acuity.iot.dsa.dslink.transport.DSTransport;
import com.acuity.iot.dsa.dslink.transport.SocketTransport;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.SecureRandom;
import org.iot.dsa.dslink.DSIRequester;
import org.iot.dsa.dslink.DSLink;
import org.iot.dsa.dslink.DSLinkConfig;
import org.iot.dsa.dslink.DSLinkConnection;
import org.iot.dsa.io.DSBase64;
import org.iot.dsa.node.DSBool;
import org.iot.dsa.node.DSBytes;
import org.iot.dsa.node.DSStatus;
import org.iot.dsa.node.DSString;
import org.iot.dsa.security.DSKeys;
import org.iot.dsa.time.DSDateTime;
import org.iot.dsa.util.DSException;

/**
 * The default connection implementation. Performs connection initialization with the broker, then
 * creates a transport and a protocol based on the broker response.
 *
 * @author Aaron Hansen
 */
public class DS2LinkConnection extends DSLinkConnection {

    ///////////////////////////////////////////////////////////////////////////
    // Fields
    ///////////////////////////////////////////////////////////////////////////

    private byte[] brokerAuth;
    private String brokerDsId;
    private byte[] brokerPubKey;
    private byte[] brokerSalt;
    private DSLink link;
    private byte[] linkSalt;
    private boolean requesterAllowed = false;
    private DSBinaryTransport transport;

    ///////////////////////////////////////////////////////////////////////////
    // Methods
    ///////////////////////////////////////////////////////////////////////////

    @Override
    public DSLink getLink() {
        return link;
    }

    private byte[] getLinkSalt() {
        if (linkSalt == null) {
            linkSalt = new byte[32];
            SecureRandom random = new SecureRandom();
            random.nextBytes(linkSalt);
            put("Link Salt", DSString.valueOf(DSBase64.encodeUrl(linkSalt)))
                    .setReadOnly(true)
                    .setTransient(true);
        }
        return linkSalt;
    }

    @Override
    public DSIRequester getRequester() {
        return null;//session.getRequester();
    }

    /**
     * Looks at the connection initialization response to determine the type of transport then
     * instantiates the correct type fom the config.
     */
    protected DSTransport makeTransport() {
        DSTransport.Factory factory = null;
        String uri = link.getConfig().getBrokerUri();
        put("Broker URI", DSString.valueOf(uri)).setReadOnly(true).setTransient(true);
        transport = null;
        if (uri.startsWith("ws")) {
            try {
                String type = link.getConfig().getConfig(
                        DSLinkConfig.CFG_WS_TRANSPORT_FACTORY,
                        "org.iot.dsa.dslink.websocket.StandaloneTransportFactory");
                factory = (DSTransport.Factory) Class.forName(type).newInstance();
                transport = factory.makeBinaryTransport(this);
            } catch (Exception x) {
                DSException.throwRuntime(x);
            }
        } else {
            transport = new SocketTransport();
        }
        config(config() ? "Connection URL = " + uri : null);
        transport.setConnectionUrl(uri);
        transport.setConnection(this);
        transport.setReadTimeout(getLink().getConfig().getConfig(
                DSLinkConfig.CFG_READ_TIMEOUT, 60000));
        setTransport(makeTransport());
        return transport;
    }

    @Override
    protected void onConnect() {
        put("Last Connect Attempt", DSDateTime.currentTime())
                .setReadOnly(true).setTransient(true);
        makeTransport().open();
        performHandshake();
        put("Status", DSStatus.ok).setReadOnly(true).setTransient(true);
    }

    @Override
    protected void onDisconnect() {
        put("Status", DSStatus.down).setReadOnly(true).setTransient(true);
    }

    @Override
    protected void onInitialize() {
    }

    /**
     * The long term management of the connection (reading and writing).  When this
     * returns, onDisconnect will be called.
     */
    @Override
    protected void onRun() {
        //session.run();
    }

    @Override
    protected void onStable() {
        this.link = (DSLink) getParent();
        super.onStable();
    }

    private void performHandshake() {
        try {
            sendF0();
            recvF1();
            sendF2();
            recvF3();
            put("Last Connect", DSDateTime.currentTime()).setReadOnly(true).setTransient(true);
            put("Status", DSStatus.ok).setReadOnly(true).setTransient(true);
        } catch (Exception io) {
            put("Status", DSStatus.fault).setReadOnly(true).setTransient(true);
            put("Last Fail", DSDateTime.currentTime()).setReadOnly(true).setTransient(true);
            put("Fail Cause", DSString.valueOf(DSException.makeMessage(io)))
                    .setReadOnly(true).setTransient(true);
            DSException.throwRuntime(io);
        }
    }

    private void recvF1() throws IOException {
        InputStream in = transport.getInput();
        MessageReader reader = new MessageReader();
        reader.init(in);
        if (reader.getMethod() != 0xf1) {
            throw new IllegalStateException("Expecting handshake method 0xF1 not 0x" +
                                                    Integer.toHexString(reader.getMethod()));
        }
        //TODO check for header status
        brokerDsId = reader.readString(in);
        put("Broker DSID", DSString.valueOf(brokerDsId)).setReadOnly(true).setTransient(true);
        brokerPubKey = new byte[65];
        in.read(brokerPubKey);
        put("Broker Public Key", DSString.valueOf(DSBase64.encodeUrl(brokerPubKey)))
                .setReadOnly(true).setTransient(true);
        brokerSalt = new byte[32];
        in.read(brokerSalt);
        put("Broker Salt", DSString.valueOf(DSBase64.encodeUrl(brokerSalt)))
                .setReadOnly(true).setTransient(true);
    }

    private void recvF3() throws IOException {
        InputStream in = transport.getInput();
        MessageReader reader = new MessageReader();
        reader.init(in);
        if (reader.getMethod() != 0xf3) {
            throw new IllegalStateException("Expecting handshake method 0xF3 not 0x" +
                                                    Integer.toHexString(reader.getMethod()));
        }
        //TODO check for header status
        requesterAllowed = (in.read() == 1);
        put("Requester Allowed", DSBool.valueOf(requesterAllowed))
                .setReadOnly(true).setTransient(true);
        String sessionId = reader.readString(in);
        put("Session ID", DSString.valueOf(sessionId)).setTransient(true);
        int lastAck = DSBytes.readInt(in, false);
        String pathOnBroker = reader.readString(in);
        put("Broker Path", DSString.valueOf(pathOnBroker))
                .setReadOnly(true).setTransient(true);
        if (brokerAuth == null) {
            brokerAuth = new byte[32];
        }
        in.read(brokerAuth);
        put("Broker Auth", DSString.valueOf(DSBase64.encodeUrl(brokerAuth)))
                .setReadOnly(true).setTransient(true);
    }

    private void sendF0() {
        DSLink link = getLink();
        String dsId = link.getDsId();
        DSKeys dsKeys = link.getKeys();
        MessageWriter writer = new MessageWriter();
        writer.setMethod((byte) 0xf0);
        ByteBuffer buffer = writer.getBody();
        buffer.put((byte) 2).put((byte) 0); //dsa version
        writer.writeString(dsId, buffer);
        buffer.put(dsKeys.encodePublic());
        buffer.put(getLinkSalt());
        writer.write(transport);
    }

    private void sendF2() throws Exception {
        MessageWriter writer = new MessageWriter();
        writer.setMethod((byte) 0xf2);
        ByteBuffer buffer = writer.getBody();
        String token = link.getConfig().getToken();
        if (token == null) {
            token = "";
        }
        writer.writeString(token, buffer);
        buffer.put((byte) 0x01); //isResponder
        writer.writeString("", buffer); //blank session string
        writer.writeIntLE(0, buffer); //last ack
        writer.writeString("", buffer); //blank server path
        byte[] sharedSecret = getLink().getKeys().generateSharedSecret(brokerPubKey);
        byte[] authBytes = new byte[brokerSalt.length + sharedSecret.length];
        System.arraycopy(brokerSalt, 0, authBytes, 0, brokerSalt.length);
        System.arraycopy(sharedSecret, 0, authBytes, brokerSalt.length, sharedSecret.length);
        MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
        messageDigest.update(authBytes);
        authBytes = messageDigest.digest();
        buffer.put(authBytes);
    }

}
