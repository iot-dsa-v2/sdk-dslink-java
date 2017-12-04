package com.acuity.iot.dsa.dslink.protocol.protocol_v1;

import com.acuity.iot.dsa.dslink.transport.DSBinaryTransport;
import com.acuity.iot.dsa.dslink.transport.DSTextTransport;
import com.acuity.iot.dsa.dslink.transport.DSTransport;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;
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
import org.iot.dsa.time.DSTime;
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

    private String connectionId;
    private DS1ConnectionInit connectionInit;
    private DSLink link;
    private ConcurrentHashMap<Listener, Listener> listeners;
    private Logger logger;
    private Object mutex = new Object();
    private DS1Session session;
    private DSIReader reader;
    private long reconnectRate = 1000;
    private DSTransport transport;
    private DSIWriter writer;

    ///////////////////////////////////////////////////////////////////////////
    // Constructors
    ///////////////////////////////////////////////////////////////////////////

    ///////////////////////////////////////////////////////////////////////////
    // Methods
    ///////////////////////////////////////////////////////////////////////////

    @Override
    public void addListener(Listener listener) {
        synchronized (this) {
            if (listeners == null) {
                listeners = new ConcurrentHashMap<Listener, Listener>();
            }
        }
        listeners.put(listener, listener);
    }

    @Override
    public String getConnectionId() {
        if (connectionId == null) {
            StringBuilder builder = new StringBuilder();
            builder.append(link.getLinkName()).append("-");
            String uri = link.getConfig().getBrokerUri();
            if ((uri != null) && !uri.isEmpty()) {
                int idx = uri.indexOf("://") + 3;
                if ((idx > 0) && (uri.length() > idx)) {
                    int end = uri.indexOf("/", idx);
                    if (end > idx) {
                        builder.append(uri.substring(idx, end));
                    } else {
                        builder.append(uri.substring(idx));
                    }
                }
            } else {
                builder.append(Integer.toHexString(hashCode()));
            }
            connectionId = builder.toString();
            info(info() ? "Connection ID: " + connectionId : null);
        }
        return connectionId;
    }

    @Override
    public DSLink getLink() {
        return link;
    }

    /**
     * The default logger for this connection, the logging name will be the connection ID.
     */
    @Override
    public Logger getLogger() {
        if (logger == null) {
            logger = Logger.getLogger(getLink().getLinkName() + ".connection");
        }
        return logger;
    }

    public DSIReader getReader() {
        return reader;
    }

    @Override
    public DSIRequester getRequester() {
        return session.getRequester();
    }

    public DSTransport getTransport() {
        return transport;
    }

    public DSIWriter getWriter() {
        return writer;
    }

    protected DS1ConnectionInit initializeConnection() throws Exception {
        DS1ConnectionInit init = new DS1ConnectionInit();
        put(CONNECTION_INIT, init).setTransient(true);
        init.initializeConnection();
        return init;
    }

    @Override
    public boolean isOpen() {
        DSTransport tpt = transport;
        if (tpt == null) {
            return false;
        }
        return tpt.isOpen();
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
        DSTransport transport = null;
        String wsUri = init.getResponse().get("wsUri", null);
        if (wsUri == null) {
            throw new IllegalStateException("Only websocket transports are supported.");
        }
        try {
            String type = link.getConfig().getConfig(
                    DSLinkConfig.CFG_WS_TRANSPORT_FACTORY,
                    "org.iot.dsa.dslink.websocket.StandaloneTransportFactory");
            factory = (DSTransport.Factory) Class.forName(type).newInstance();
            String format = init.getResponse().getString("format");
            if ("msgpack".equals(format)) {
                setTransport(factory.makeBinaryTransport(this));
            } else if ("json".equals(format)) {
                setTransport(factory.makeTextTransport(this));
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
        return transport;
    }

    /**
     * Called by the transport when the network connection is closed.
     */
    public void onClose() {
        if (session != null) {
            session.close();
        }
    }

    /**
     * Spawns a thread to manage opening the connection and subsequent reconnections.
     */
    @Override
    public void onStable() {
        if (isOpen()) {
            throw new IllegalStateException("Connection already open");
        }
        this.link = (DSLink) getParent();
        new ConnectionRunThread(new ConnectionRunner()).start();
    }

    @Override
    public void removeListener(Listener listener) {
        if (listeners != null) {
            listeners.remove(listener);
        }
    }

    public void setRequesterAllowed() {
        session.setRequesterAllowed();
    }

    protected void setTransport(DSTransport transport) {
        if (transport instanceof DSBinaryTransport) {
            final DSBinaryTransport trans = (DSBinaryTransport) transport;
            reader = new MsgpackReader(trans.getInput());
            writer = new MsgpackWriter() {
                @Override
                public void onComplete() {
                    trans.write(byteBuffer, true);
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

    ///////////////////////////////////////////////////////////////////////////
    // Inner Classes
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Daemon thread that manages connection and reconnection.
     */
    private class ConnectionRunner implements Runnable {

        /**
         * Runs until stop is called.
         */
        public void run() {
            while (isRunning()) {
                try {
                    synchronized (mutex) {
                        try {
                            mutex.wait(reconnectRate);
                        } catch (Exception x) {
                            warn(warn() ? getConnectionId() : null, x);
                        }
                    }
                    //We need to reinitialize if there are any connection failures, so
                    //only hold the init reference after successfully connected.
                    DS1ConnectionInit init = connectionInit;
                    connectionInit = null;
                    if (init == null) {
                        init = initializeConnection();
                    }
                    transport = makeTransport(init);
                    put(TRANSPORT, transport).setTransient(true);
                    config(config() ? "Transport type: " + transport.getClass().getName() : null);
                    if (session == null) {
                        session = makeSession(init);
                        config(config() ? "Session type: " + session.getClass().getName() : null);
                        put(SESSION, session).setTransient(true);
                        session.setConnection(DS1LinkConnection.this);
                    }
                    try {
                        transport.open();
                        connectionInit = init;
                        session.onConnect();
                    } catch (Exception x) {
                        session.onConnectFail();
                        throw x;
                    }
                    if (listeners != null) {
                        for (Listener listener : listeners.keySet()) {
                            try {
                                listener.onConnect(DS1LinkConnection.this);
                            } catch (Exception x) {
                                severe(listener.toString(), x);
                            }
                        }
                    }
                    session.run();
                    reconnectRate = 1000;
                } catch (Throwable x) {
                    reconnectRate = Math.min(reconnectRate * 2, DSTime.MILLIS_MINUTE);
                    severe(getConnectionId(), x);
                }
                for (Listener listener : listeners.keySet()) {
                    try {
                        listener.onDisconnect(DS1LinkConnection.this);
                    } catch (Exception x) {
                        severe(listener.toString(), x);
                    }
                }
                if (session != null) {
                    session.onDisconnect();
                }
                if (transport != null) {
                    remove(TRANSPORT);
                    transport = null;
                    reader = null;
                    writer = null;
                }
            }
        }
    } //ConnectionRunner

    /**
     * Daemon thread that manages connection and reconnection.
     */
    private class ConnectionRunThread extends Thread {

        public ConnectionRunThread(ConnectionRunner runner) {
            super(runner);
            setName(getConnectionId() + " Runner");
            setDaemon(true);
        }
    }

}
