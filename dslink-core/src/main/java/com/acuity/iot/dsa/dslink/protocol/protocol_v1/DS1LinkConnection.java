package com.acuity.iot.dsa.dslink.protocol.protocol_v1;

import com.acuity.iot.dsa.dslink.transport.DSTransport;
import org.iot.dsa.dslink.DSIRequester;
import org.iot.dsa.dslink.DSLink;
import org.iot.dsa.dslink.DSLinkConfig;
import org.iot.dsa.dslink.DSLinkConnection;
import org.iot.dsa.util.DSException;

/**
 * The default connection implementation. Performs connection initialization with the broker, then
 * creates a transport and a protocol based on the broker response.
 *
 * @author Aaron Hansen
 */
public class DS1LinkConnection extends DSLinkConnection {

    ///////////////////////////////////////////////////////////////////////////
    // Constants
    ///////////////////////////////////////////////////////////////////////////

    static final String CONNECTION_INIT = "Initialization";
    static final String SESSION = "Session";
    static final String TRANSPORT = "Transport";

    ///////////////////////////////////////////////////////////////////////////
    // Fields
    ///////////////////////////////////////////////////////////////////////////

    private DS1ConnectionInit connectionInit;
    private DSLink link;
    private DS1Session session;

    ///////////////////////////////////////////////////////////////////////////
    // Constructors
    ///////////////////////////////////////////////////////////////////////////

    ///////////////////////////////////////////////////////////////////////////
    // Methods
    ///////////////////////////////////////////////////////////////////////////

    @Override
    public DSLink getLink() {
        return link;
    }

    @Override
    public DSIRequester getRequester() {
        return session.getRequester();
    }

    protected DS1ConnectionInit initializeConnection() {
        DS1ConnectionInit init = new DS1ConnectionInit();
        put(CONNECTION_INIT, init).setTransient(true);
        try {
            init.initializeConnection();
        } catch (Exception x) {
            DSException.throwRuntime(x);
        }
        return init;
    }

    /**
     * Looks at the connection initialization response to determine the protocol implementation.
     */
    protected DS1Session makeSession(DS1ConnectionInit init) {
        String version = init.getResponse().get("version", "");
        if (!version.startsWith("1.1.2")) {
            throw new IllegalStateException("Unsupported version: " + version);
        }
        return new DS1Session();
    }

    /**
     * Looks at the connection initialization response to determine the type of transport then
     * instantiates the correct type fom the config.
     */
    protected DSTransport makeTransport(DS1ConnectionInit init) {
        DSTransport.Factory factory = null;
        String wsUri = init.getResponse().get("wsUri", null);
        if (wsUri == null) {
            throw new IllegalStateException("Only websocket transports are supported.");
        }
        DSTransport transport = null;
        try {
            String type = link.getConfig().getConfig(
                    DSLinkConfig.CFG_WS_TRANSPORT_FACTORY,
                    "org.iot.dsa.dslink.websocket.StandaloneTransportFactory");
            factory = (DSTransport.Factory) Class.forName(type).newInstance();
            String format = init.getResponse().getString("format");
            if ("msgpack".equals(format)) {
                transport = factory.makeBinaryTransport(this);
            } else if ("json".equals(format)) {
                transport = factory.makeTextTransport(this);
            } else {
                throw new IllegalStateException("Unknown format: " + format);
            }
        } catch (Exception x) {
            DSException.throwRuntime(x);
        }
        String uri = init.makeWsUrl(wsUri);
        config(config() ? "Connection URL = " + uri : null);
        transport.setConnectionUrl(uri);
        transport.setConnection(this);
        transport.setReadTimeout(getLink().getConfig().getConfig(
                DSLinkConfig.CFG_READ_TIMEOUT, 60000));
        setTransport(transport);
        config(config() ? "Transport type: " + transport.getClass().getName() : null);
        return transport;
    }

    @Override
    protected void onConnect() {
        //If there is a failure, then we want connection init to happen again.
        DS1ConnectionInit init = connectionInit;
        connectionInit = null;
        try {
            getTransport().open();
            connectionInit = init;
            session.onConnect();
        } catch (Exception x) {
            session.onConnectFail();
            DSException.throwRuntime(x);
        }
    }

    @Override
    protected void onDisconnect() {
        if (session != null) {
            session.close();
        }
        if (session != null) {
            session.onDisconnect();
        }
        remove(TRANSPORT);
    }

    @Override
    protected void onInitialize() {
        //We need to reinitialize if there are any connection failures, so
        //only hold the init reference after successfully connected.
        DS1ConnectionInit init = connectionInit;
        connectionInit = null;
        if (init == null) {
            init = initializeConnection();
        }
        makeTransport(init);
        put(TRANSPORT, getTransport()).setTransient(true);
        if (session == null) {
            session = makeSession(init);
            config(config() ? "Session type: " + session.getClass().getName() : null);
            put(SESSION, session).setTransient(true);
            session.setConnection(DS1LinkConnection.this);
        }
        connectionInit = init;
    }

    @Override
    protected void onRun() {
        session.run();
    }

    @Override
    protected void onStable() {
        this.link = (DSLink) getParent();
        super.onStable();
    }

    public void setRequesterAllowed() {
        session.setRequesterAllowed();
    }

    public void updateSalt(String salt) {
        connectionInit.updateSalt(salt);
    }

}
