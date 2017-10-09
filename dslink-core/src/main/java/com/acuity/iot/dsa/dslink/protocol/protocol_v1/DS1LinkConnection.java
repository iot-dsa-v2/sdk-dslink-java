package com.acuity.iot.dsa.dslink.protocol.protocol_v1;

import com.acuity.iot.dsa.dslink.DSSession;
import com.acuity.iot.dsa.dslink.DSTransport;
import java.util.logging.Logger;
import org.iot.dsa.dslink.DSLink;
import org.iot.dsa.dslink.DSLinkConfig;
import org.iot.dsa.dslink.DSLinkConnection;
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
    private Logger logger;
    private Object mutex = new Object();
    private DS1Session session;
    private long reconnectRate = 1000;
    private DSTransport transport;

    ///////////////////////////////////////////////////////////////////////////
    // Constructors
    ///////////////////////////////////////////////////////////////////////////

    ///////////////////////////////////////////////////////////////////////////
    // Methods
    ///////////////////////////////////////////////////////////////////////////

    @Override
    public void close() {
        if (isOpen()) {
            DSTransport tpt = transport;
            if (tpt != null) {
                tpt.close();
            }
            DSSession ses = session;
            if (ses != null) {
                ses.pause();
            }
        }
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
    protected DS1Session makeSession() {
        String version = connectionInit.getResponse().get("version", "");
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
        try {
            String type = link.getConfig().getConfig(
                DSLinkConfig.CFG_TRANSPORT_FACTORY,
                "org.iot.dsa.dslink.websocket.StandaloneTransportFactory");
            factory = (DSTransport.Factory) Class.forName(type).newInstance();
            transport = factory.makeTransport(init.getResponse());
        } catch (Exception x) {
            DSException.throwRuntime(x);
        }
        String wsUri = init.getResponse().get("wsUri", null);
        if (wsUri == null) {
            throw new IllegalStateException("Only websocket transports are supported.");
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

    /**
     * Terminates the connection runner.
     */
    @Override
    public void onStopped() {
        close();
        if (session != null) {
            session.close();
            session = null;
        }
        transport = null;
    }

    public void setRequesterAllowed() {
        session.setRequesterAllowed();
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
                    DS1ConnectionInit init = connectionInit;
                    connectionInit = null;
                    if (init == null) {
                        init = new DS1ConnectionInit();
                        put(CONNECTION_INIT, init).setTransient(true);
                        init.initializeConnection();
                    }
                    transport = makeTransport(init);
                    put(TRANSPORT, transport).setTransient(true);
                    config(config() ? "Transport type: " + transport.getClass().getName() : null);
                    transport.open();
                    connectionInit = init;
                    if (session != null) {
                        if (!session.canReuse()) {
                            session.close();
                            remove(SESSION);
                            session = null;
                        }
                    }
                    if (session == null) {
                        session = makeSession();
                        put(SESSION, session).setTransient(true);
                    }
                    session.setConnection(DS1LinkConnection.this).setTransport(transport);
                    config(config() ? "Protocol type: " + session.getClass().getName() : null);
                    session.run();
                    reconnectRate = 1000;
                } catch (Throwable x) {
                    reconnectRate = Math.min(reconnectRate * 2, DSTime.MILLIS_MINUTE);
                    severe(getConnectionId(), x);
                }
                try {
                    close();
                } catch (Exception x) {
                    fine(fine() ? getConnectionId() : null, x);
                }
                if (session != null) {
                    session.pause();
                }
                transport = null;
            }
            if (session != null) {
                session.close();
                remove(SESSION);
                session = null;
            }
        }
    }

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
