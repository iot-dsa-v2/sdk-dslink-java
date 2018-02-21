package com.acuity.iot.dsa.dslink.protocol.v1;

import com.acuity.iot.dsa.dslink.transport.DSBinaryTransport;
import com.acuity.iot.dsa.dslink.transport.DSTextTransport;
import com.acuity.iot.dsa.dslink.transport.DSTransport;
import org.iot.dsa.dslink.DSIRequester;
import org.iot.dsa.dslink.DSLink;
import org.iot.dsa.dslink.DSLinkConfig;
import org.iot.dsa.dslink.DSLinkConnection;
import org.iot.dsa.io.DSIReader;
import org.iot.dsa.io.DSIWriter;
import org.iot.dsa.io.json.JsonReader;
import org.iot.dsa.io.json.JsonWriter;
import org.iot.dsa.io.msgpack.MsgpackReader;
import org.iot.dsa.io.msgpack.MsgpackWriter;
import org.iot.dsa.util.DSException;

/**
 * The DSA V1 connection implementation. Performs connection initialization with the broker, then
 * creates a transport based on the broker response.
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
    private DSIReader reader;
    private DSTransport transport;
    private DS1Session session;
    private DSIWriter writer;

    ///////////////////////////////////////////////////////////////////////////
    // Methods
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Forcefully closes an open connection.  Does not prevent reconnection, intended for
     * problem resolution.
     */
    public void disconnect() {
        if (session != null) {
            session.disconnect();
        }
    }

    public DSIReader getReader() {
        return reader;
    }

    @Override
    public DSIRequester getRequester() {
        return session.getRequester();
    }

    @Override
    public DSTransport getTransport() {
        return transport;
    }

    public DSIWriter getWriter() {
        return writer;
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
        fine(fine() ? "Connection URL = " + uri : null);
        transport.setConnectionUrl(uri);
        transport.setConnection(this);
        transport.setReadTimeout(getLink().getConfig().getConfig(
                DSLinkConfig.CFG_READ_TIMEOUT, 60000));
        setTransport(transport);
        fine(fine() ? "Transport type: " + transport.getClass().getName() : null);
        return transport;
    }

    @Override
    protected void onConnect() {
        try {
            DS1ConnectionInit init = connectionInit;
            //Don't reuse the connection init if there is connection problem.
            connectionInit = null;
            getTransport().open();
            session.onConnect();
            connectionInit = init;
        } catch (Exception x) {
            session.onConnectFail();
            DSException.throwRuntime(x);
        }
    }

    @Override
    protected void onDisconnect() {
        if (session != null) {
            session.onDisconnect();
        }
        reader = null;
        writer = null;
        transport = null;
        remove(TRANSPORT);
    }

    @Override
    protected void onInitialize() {
        DS1ConnectionInit init = connectionInit;
        //Don't reuse the connection init if there is connection problem.
        connectionInit = null;
        if (init == null) {
            init = initializeConnection();
        }
        makeTransport(init);
        put(TRANSPORT, getTransport()).setTransient(true);
        if (session == null) {
            session = new DS1Session(this);
            put(SESSION, session).setTransient(true);
        }
        connectionInit = init;
    }

    @Override
    protected void onRun() {
        session.run();
    }

    @Override
    protected void onStable() {
        this.link = getLink();
        super.onStable();
    }

    public void setRequesterAllowed() {
        session.setRequesterAllowed();
    }

    /**
     * Sets the transport and creates the appropriate reader/writer.
     */
    protected void setTransport(DSTransport transport) {
        if (transport instanceof DSBinaryTransport) {
            final DSBinaryTransport trans = (DSBinaryTransport) transport;
            reader = new MsgpackReader(trans.getInput());
            writer = new MsgpackWriter() {
                @Override
                public void onComplete() {
                    writeTo(trans);
                }
            };
        } else if (transport instanceof DSTextTransport) {
            DSTextTransport trans = (DSTextTransport) transport;
            reader = new JsonReader(trans.getReader());
            writer = new JsonWriter(trans.getWriter());
        } else {
            throw new IllegalStateException(
                    "Unexpected transport type: " + transport.getClass().getName());
        }
        this.transport = transport;
    }

    public void updateSalt(String salt) {
        connectionInit.updateSalt(salt);
    }

}