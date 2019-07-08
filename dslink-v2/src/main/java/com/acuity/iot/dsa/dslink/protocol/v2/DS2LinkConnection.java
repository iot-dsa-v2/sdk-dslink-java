package com.acuity.iot.dsa.dslink.protocol.v2;

import static com.acuity.iot.dsa.dslink.protocol.v2.MessageConstants.HDR_STATUS;
import static com.acuity.iot.dsa.dslink.protocol.v2.MessageConstants.STS_INITIALIZING;
import static com.acuity.iot.dsa.dslink.protocol.v2.MessageConstants.STS_INVALID_AUTH;
import static com.acuity.iot.dsa.dslink.protocol.v2.MessageConstants.STS_OK;

import com.acuity.iot.dsa.dslink.io.DSByteBuffer;
import com.acuity.iot.dsa.dslink.protocol.DSBrokerConnection;
import com.acuity.iot.dsa.dslink.transport.DSTransport;
import com.acuity.iot.dsa.dslink.transport.SocketTransport;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.security.SecureRandom;
import org.iot.dsa.dslink.DSLink;
import org.iot.dsa.dslink.DSLinkOptions;
import org.iot.dsa.dslink.DSPermissionException;
import org.iot.dsa.io.DSBase64;
import org.iot.dsa.node.DSBytes;
import org.iot.dsa.node.DSInfo;
import org.iot.dsa.node.DSString;
import org.iot.dsa.security.DSKeys;
import org.iot.dsa.util.DSException;

/**
 * The DSA V2 connection implementation. Performs connection initialization with the broker,
 * then creates a transport and session based on the broker response.
 *
 * @author Aaron Hansen
 */
public class DS2LinkConnection extends DSBrokerConnection {

    ///////////////////////////////////////////////////////////////////////////
    // Class Fields
    ///////////////////////////////////////////////////////////////////////////

    private static final String BROKER_AUTH = "Broker Auth";
    private static final String LINK_SALT = "Link Salt";

    ///////////////////////////////////////////////////////////////////////////
    // Instance Fields
    ///////////////////////////////////////////////////////////////////////////

    private DSInfo brokerAuth = getInfo(BROKER_AUTH);
    private DSInfo linkSalt = getInfo(LINK_SALT);

    ///////////////////////////////////////////////////////////////////////////
    // Constructors
    ///////////////////////////////////////////////////////////////////////////

    public DS2LinkConnection() {
        getSession();
    }

    ///////////////////////////////////////////////////////////////////////////
    // Public Methods
    ///////////////////////////////////////////////////////////////////////////

    @Override
    public void declareDefaults() {
        super.declareDefaults();
        declareDefault(BROKER_AUTH, DSBytes.NULL).setTransient(true).setReadOnly(true);
        declareDefault(LINK_SALT, DSBytes.NULL).setTransient(true).setReadOnly(true);
        getInfo(ENABLED).setPrivate(true);
    }

    public DS2Session getSession() {
        return (DS2Session) super.getSession();
    }

    ///////////////////////////////////////////////////////////////////////////
    // Protected Methods
    ///////////////////////////////////////////////////////////////////////////

    @Override
    protected void checkConfig() {
    }

    @Override
    protected void initializeConnection() {
        try {
            if (DSString.isNotEmpty(getBrokerSalt()) && DSString.isNotEmpty(getBrokerKey())) {
                return;
            }
            sendF0();
            recvF1();
            sendF2();
            recvF3();
        } catch (IOException x) {
            throw new UncheckedIOException(x);
        }
    }

    @Override
    protected DS2Session makeSession() {
        return new DS2Session();
    }

    @Override
    protected DSTransport makeTransport() {
        DSTransport.Factory factory = null;
        String uri = getLink().getOptions().getBrokerUri();
        DSTransport transport = null;
        if (uri.startsWith("ws")) {
            try {
                String type = getLink().getOptions().getConfig(
                        DSLinkOptions.CFG_WS_TRANSPORT_FACTORY,
                        "org.iot.dsa.dslink.websocket.StandaloneTransportFactory");
                factory = (DSTransport.Factory) Class.forName(type).newInstance();
                transport = factory.makeTransport(this);
            } catch (Exception x) {
                DSException.throwRuntime(x);
            }
        } else if (uri.startsWith("ds")) {
            transport = new SocketTransport().setText(false);
        }
        debug(debug() ? "Connection URL = " + uri : null);
        return transport;
    }

    ///////////////////////////////////////////////////////////////////////////
    // Package / Private Methods
    ///////////////////////////////////////////////////////////////////////////

    private byte[] getLinkSalt() {
        if (linkSalt.get().isNull()) {
            byte[] tmp = new byte[32];
            SecureRandom random = new SecureRandom();
            random.nextBytes(tmp);
            put(linkSalt, DSBytes.valueOf(tmp));
        }
        return linkSalt.getElement().toBytes();
    }

    private void recvF1() throws IOException {
        InputStream in = getTransport().getBinaryInput();
        DS2MessageReader reader = new DS2MessageReader();
        reader.init(in);
        if (reader.getMethod() != 0xf1) {
            throw new IllegalStateException("Expecting handshake method 0xF1 not 0x" +
                                                    Integer.toHexString(reader.getMethod()));
        }
        Byte status = (Byte) reader.getHeader(HDR_STATUS);
        if (status != null) {
            switch (status.intValue()) {
                case STS_OK:
                case STS_INITIALIZING:
                    break;
                case STS_INVALID_AUTH:
                    throw new DSPermissionException("Invalid Auth");
                default:
                    throw new IllegalStateException("Unexpected status: 0x" +
                                                            DSBytes.toHex(status, null));
            }
        }
        setBrokerId(reader.readString(in));
        byte[] tmp = new byte[65];
        int len = in.read(tmp);
        if (len != 65) {
            throw new IllegalStateException("Broker pub key not 65 bytes: " + len);
        }
        setBrokerKey(DSBase64.encodeUrl(tmp));
        tmp = new byte[32];
        len = in.read(tmp);
        if (len != 32) {
            throw new IllegalStateException("Broker salt not 32 bytes: " + len);
        }
        setBrokerSalt(DSBase64.encodeUrl(tmp));
    }

    private void recvF3() throws IOException {
        InputStream in = getTransport().getBinaryInput();
        DS2MessageReader reader = new DS2MessageReader();
        reader.init(in);
        if (reader.getMethod() != 0xf3) {
            throw new IllegalStateException("Expecting handshake method 0xF3 not 0x" +
                                                    Integer.toHexString(reader.getMethod()));
        }
        Byte status = (Byte) reader.getHeader(HDR_STATUS);
        if (status != null) {
            switch (status.intValue()) {
                case STS_OK:
                case STS_INITIALIZING:
                    break;
                case STS_INVALID_AUTH:
                    throw new DSPermissionException("Invalid Auth");
                default:
                    throw new IllegalStateException("Unexpected status: 0x" +
                                                            DSBytes.toHex(status, null));
            }
        }
        boolean allowed = in.read() == 1;
        getSession().setRequesterAllowed(allowed);
        String pathOnBroker = reader.readString(in);
        setPathInBroker(pathOnBroker);
        byte[] tmp = new byte[32];
        in.read(tmp);
        put(brokerAuth, DSString.valueOf(DSBase64.encodeUrl(tmp)));
    }

    private void sendF0() {
        DSLink link = getLink();
        String dsId = link.getDsId();
        DSKeys dsKeys = link.getOptions().getKeys();
        DS2MessageWriter writer = new DS2MessageWriter();
        writer.setMethod(0xf0);
        DSByteBuffer buffer = writer.getBody();
        buffer.put((byte) 2).put((byte) 0); //dsa version
        writer.writeString(dsId, buffer);
        buffer.put(dsKeys.encodePublic());
        buffer.put(getLinkSalt());
        writer.write(getTransport());
    }

    private void sendF2() {
        DS2MessageWriter writer = new DS2MessageWriter();
        writer.setMethod(0xf2);
        DSByteBuffer buffer = writer.getBody();
        String token = getLink().getOptions().getToken();
        if (token == null) {
            token = "";
        }
        writer.writeString(token, buffer);
        buffer.put((byte) 0x01); //isResponder
        writer.writeString("", buffer); //blank server path
        byte[] brokerKey = DSBase64.decode(getBrokerKey());
        byte[] sharedSecret = getLink().getOptions().getKeys().generateSharedSecret(brokerKey);
        byte[] tmp = DSBase64.decode(getBrokerSalt());
        byte[] authBytes = DSKeys.generateHmacSHA256Signature(tmp, sharedSecret);
        buffer.put(authBytes);
        writer.write(getTransport());
    }

}
