package com.acuity.iot.dsa.dslink.protocol.v2;

import com.acuity.iot.dsa.dslink.io.DSByteBuffer;
import com.acuity.iot.dsa.dslink.transport.DSBinaryTransport;
import com.acuity.iot.dsa.dslink.transport.DSTransport;
import com.acuity.iot.dsa.dslink.transport.SocketTransport;
import java.io.IOException;
import java.io.InputStream;
import java.security.SecureRandom;
import org.iot.dsa.dslink.DSIRequester;
import org.iot.dsa.dslink.DSLink;
import org.iot.dsa.dslink.DSLinkConfig;
import org.iot.dsa.dslink.DSLinkConnection;
import org.iot.dsa.node.DSBool;
import org.iot.dsa.node.DSBytes;
import org.iot.dsa.node.DSInfo;
import org.iot.dsa.node.DSStatus;
import org.iot.dsa.node.DSString;
import org.iot.dsa.security.DSKeys;
import org.iot.dsa.time.DSDateTime;
import org.iot.dsa.util.DSException;

/**
 * The DSA V2 connection implementation. Performs connection initialization with the broker,
 * then creates a transport and session based on the broker response.
 *
 * @author Aaron Hansen
 */
public class DS2LinkConnection extends DSLinkConnection {

    ///////////////////////////////////////////////////////////////////////////
    // Constants
    ///////////////////////////////////////////////////////////////////////////

    private static final String BROKER_AUTH = "Broker Auth";
    private static final String BROKER_ID = "Broker DSID";
    private static final String BROKER_PATH = "Broker Path";
    private static final String BROKER_PUB_KEY = "Broker Public Key";
    private static final String BROKER_SALT = "Broker Salt";
    private static final String BROKER_URI = "Broker URI";
    private static final String LAST_CONNECT_OK = "Last Connect Ok";
    private static final String LAST_CONNECT_FAIL = "Last Connect Fail";
    private static final String LINK_SALT = "Link Salt";
    private static final String FAIL_CAUSE = "Fail Cause";
    private static final String REQUESTER_ALLOWED = "Requester Allowed";
    private static final String SESSION = "Status";
    private static final String STATUS = "Status";
    private static final String TRANSPORT = "Transport";

    ///////////////////////////////////////////////////////////////////////////
    // Fields
    ///////////////////////////////////////////////////////////////////////////

    private DSInfo brokerAuth = getInfo(BROKER_AUTH);
    private DSInfo brokerDsId = getInfo(BROKER_ID);
    private DSInfo brokerPath = getInfo(BROKER_PATH);
    private DSInfo brokerPubKey = getInfo(BROKER_PUB_KEY);
    private DSInfo brokerSalt = getInfo(BROKER_SALT);
    private DSInfo brokerUri = getInfo(BROKER_URI);
    private DSInfo linkSalt = getInfo(LINK_SALT);
    private DSInfo requesterAllowed = getInfo(REQUESTER_ALLOWED);
    private DS2Session session;
    private DSBinaryTransport transport;

    ///////////////////////////////////////////////////////////////////////////
    // Methods
    ///////////////////////////////////////////////////////////////////////////

    @Override
    public void declareDefaults() {
        declareDefault(STATUS, DSStatus.down).setTransient(true).setReadOnly(true);
        declareDefault(LAST_CONNECT_OK, DSDateTime.NULL).setTransient(true).setReadOnly(true);
        declareDefault(LAST_CONNECT_FAIL, DSDateTime.NULL).setTransient(true).setReadOnly(true);
        declareDefault(FAIL_CAUSE, DSString.NULL).setTransient(true).setReadOnly(true);
        declareDefault(BROKER_URI, DSString.NULL).setTransient(true).setReadOnly(true);
        declareDefault(BROKER_PATH, DSString.NULL).setTransient(true).setReadOnly(true);
        declareDefault(BROKER_ID, DSString.NULL).setTransient(true).setReadOnly(true);
        declareDefault(BROKER_AUTH, DSBytes.NULL)
                .setTransient(true).setReadOnly(true).setAdmin(true);
        declareDefault(BROKER_PUB_KEY, DSBytes.NULL)
                .setTransient(true).setReadOnly(true).setAdmin(true);
        declareDefault(BROKER_SALT, DSBytes.NULL)
                .setTransient(true).setReadOnly(true).setAdmin(true);
        declareDefault(LINK_SALT, DSBytes.NULL)
                .setTransient(true).setReadOnly(true).setAdmin(true);
        declareDefault(REQUESTER_ALLOWED, DSBool.FALSE).setTransient(true).setReadOnly(true);
    }

    @Override
    public void disconnect() {
        if (session != null) {
            session.disconnect();
        }
    }

    private byte[] getLinkSalt() {
        if (linkSalt.getObject().isNull()) {
            byte[] tmp = new byte[32];
            SecureRandom random = new SecureRandom();
            random.nextBytes(tmp);
            put(linkSalt, DSBytes.valueOf(tmp));
        }
        return linkSalt.getElement().toBytes();
    }

    @Override
    public DSIRequester getRequester() {
        return session.getRequester();
    }

    @Override
    public DSBinaryTransport getTransport() {
        return transport;
    }

    /**
     * Looks at the connection initialization response to determine the type of transport then
     * instantiates the correct type fom the config.
     */
    protected void makeTransport() {
        DSTransport.Factory factory = null;
        String uri = getLink().getConfig().getBrokerUri();
        put(brokerUri, DSString.valueOf(uri));
        transport = null;
        if (uri.startsWith("ws")) {
            try {
                String type = getLink().getConfig().getConfig(
                        DSLinkConfig.CFG_WS_TRANSPORT_FACTORY,
                        "org.iot.dsa.dslink.websocket.StandaloneTransportFactory");
                factory = (DSTransport.Factory) Class.forName(type).newInstance();
                transport = factory.makeBinaryTransport(this);
            } catch (Exception x) {
                DSException.throwRuntime(x);
            }
        } else if (uri.startsWith("ds")) {
            transport = new SocketTransport();
        }
        put(TRANSPORT, transport);
        fine(fine() ? "Connection URL = " + uri : null);
        transport.setConnectionUrl(uri);
        transport.setConnection(this);
    }

    @Override
    protected void onConnect() {
        transport.open();
        session.onConnect();
        try {
            performHandshake();
        } catch (Exception x) {
            session.onConnectFail();
            DSException.throwRuntime(x);
        }
    }

    @Override
    protected void onDisconnect() {
        session.onDisconnect();
        put(STATUS, DSStatus.down);
    }

    @Override
    protected void onInitialize() {
        if (session == null) {
            session = new DS2Session(this);
            put(SESSION, session);
        }
        makeTransport();
    }

    /**
     * The long term management of the connection (reading and writing).  When this
     * returns, onDisconnect will be called.
     */
    @Override
    protected void onRun() {
        session.run();
    }

    private void performHandshake() {
        try {
            sendF0();
            recvF1();
            sendF2();
            recvF3();
            put(LAST_CONNECT_OK, DSDateTime.currentTime());
            put(STATUS, DSStatus.ok);
        } catch (Exception io) {
            put(STATUS, DSStatus.fault);
            put(LAST_CONNECT_FAIL, DSDateTime.currentTime());
            put(FAIL_CAUSE, DSString.valueOf(DSException.makeMessage(io)));
            DSException.throwRuntime(io);
        }
    }

    private void recvF1() throws IOException {
        InputStream in = transport.getInput();
        DS2MessageReader reader = new DS2MessageReader();
        reader.init(in);
        if (reader.getMethod() != 0xf1) {
            throw new IllegalStateException("Expecting handshake method 0xF1 not 0x" +
                                                    Integer.toHexString(reader.getMethod()));
        }
        //TODO check for header status
        put(brokerDsId, DSString.valueOf(reader.readString(in)));
        byte[] tmp = new byte[65];
        int len = in.read(tmp);
        if (len != 65) {
            throw new IllegalStateException("Broker pub key not 65 bytes: " + len);
        }
        put(BROKER_PUB_KEY, DSBytes.valueOf(tmp));
        tmp = new byte[32];
        len = in.read(tmp);
        if (len != 32) {
            throw new IllegalStateException("Broker salt not 32 bytes: " + len);
        }
        put(BROKER_SALT, DSBytes.valueOf(tmp));
    }

    private void recvF3() throws IOException {
        InputStream in = transport.getInput();
        DS2MessageReader reader = new DS2MessageReader();
        reader.init(in);
        if (reader.getMethod() != 0xf3) {
            throw new IllegalStateException("Expecting handshake method 0xF3 not 0x" +
                                                    Integer.toHexString(reader.getMethod()));
        }
        //TODO check for header status
        boolean allowed = in.read() == 1;
        put(requesterAllowed, DSBool.valueOf(allowed));
        if (allowed) {
            session.setRequesterAllowed();
        }
        String pathOnBroker = reader.readString(in);
        put(brokerPath, DSString.valueOf(pathOnBroker));
        byte[] tmp = new byte[32];
        in.read(tmp);
        put(brokerAuth, DSBytes.valueOf(tmp));
    }

    private void sendF0() {
        DSLink link = getLink();
        String dsId = link.getDsId();
        DSKeys dsKeys = link.getKeys();
        DS2MessageWriter writer = new DS2MessageWriter();
        writer.setMethod((byte) 0xf0);
        DSByteBuffer buffer = writer.getBody();
        buffer.put((byte) 2).put((byte) 0); //dsa version
        writer.writeString(dsId, buffer);
        buffer.put(dsKeys.encodePublic());
        buffer.put(getLinkSalt());
        writer.write(transport);
    }

    private void sendF2() throws Exception {
        DS2MessageWriter writer = new DS2MessageWriter();
        writer.setMethod((byte) 0xf2);
        DSByteBuffer buffer = writer.getBody();
        String token = getLink().getConfig().getToken();
        if (token == null) {
            token = "";
        }
        writer.writeString(token, buffer);
        buffer.put((byte) 0x01); //isResponder
        writer.writeString("", buffer); //blank server path
        byte[] sharedSecret = getLink().getKeys().generateSharedSecret(
                brokerPubKey.getElement().toBytes());
        byte[] tmp = brokerSalt.getElement().toBytes();
        byte[] authBytes = DSKeys.generateHmacSHA256Signature(tmp, sharedSecret);
        /*
        byte[] authBytes = new byte[tmp.length + sharedSecret.length];
        System.arraycopy(tmp, 0, authBytes, 0, tmp.length);
        System.arraycopy(sharedSecret, 0, authBytes, tmp.length, sharedSecret.length);
        MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
        messageDigest.update(authBytes);
        authBytes = messageDigest.digest();
        */
        buffer.put(authBytes);
        writer.write(transport);
    }

}