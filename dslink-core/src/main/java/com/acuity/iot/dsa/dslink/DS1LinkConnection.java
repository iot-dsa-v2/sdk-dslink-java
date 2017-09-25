package com.acuity.iot.dsa.dslink;

import com.acuity.iot.dsa.dslink.protocol.protocol_v1.DS1Protocol;
import java.util.logging.Logger;
import org.iot.dsa.dslink.DSLink;
import org.iot.dsa.dslink.DSLinkConfig;
import org.iot.dsa.dslink.DSLinkConnection;
import org.iot.dsa.logging.DSLogging;
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

    ///////////////////////////////////////////////////////////////////////////
    // Fields
    ///////////////////////////////////////////////////////////////////////////

    private String connectionId;
    private DS1ConnectionInit connectionInit;
    private DSLink link;
    private Logger logger;
    private Object mutex = new Object();
    private DSProtocol protocol;
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
            DSProtocol ptl = protocol;
            if (ptl != null) {
                if (link.getRequester() != null) {
                    link.getRequester().onDisconnected(ptl.getRequesterSession());
                }
                ptl.close();
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
    protected DSProtocol makeProtocol() {
        String version = connectionInit.getResponse().get("version", "");
        if (!version.startsWith("1.1.2")) {
            throw new IllegalStateException("Unsupported version: " + version);
        }
        return new DS1Protocol();
    }

    /**
     * Looks at the connection initialization response to determine the type of transport then
     * instantiates the correct type fom the config.
     */
    protected DSTransport makeTransport() {
        DSTransport.Factory factory = null;
        DSTransport transport = null;
        try {
            String type = link.getConfig().getConfig(
                    DSLinkConfig.CFG_TRANSPORT_FACTORY,
                    "org.iot.dsa.dslink.websocket.StandaloneTransportFactory");
            factory = (DSTransport.Factory) Class.forName(type).newInstance();
            transport = factory.makeTransport(connectionInit.getResponse());
        } catch (Exception x) {
            DSException.throwRuntime(x);
        }
        String wsUri = connectionInit.getResponse().get("wsUri", null);
        if (wsUri == null) {
            throw new IllegalStateException("Only websocket transports are supported.");
        }
        String uri = connectionInit.makeWsUrl(wsUri);
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
    }

    public void setRequesterAllowed() {
        protocol.setRequesterAllowed();
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
                    if (connectionInit == null) {
                        connectionInit = new DS1ConnectionInit();
                        put("Connection Init", connectionInit);
                        connectionInit.initializeConnection();
                    }
                    transport = makeTransport();
                    config(config() ? "Transport type: " + transport.getClass().getName() : null);
                    transport.open();
                    if (protocol != null) {
                        if (!connectionInit.canReuseSession()) {
                            protocol = null;
                        }
                    }
                    protocol = makeProtocol()
                            .setConnection(DS1LinkConnection.this)
                            .setTransport(transport);
                    config(config() ? "Protocol type: " + protocol.getClass().getName() : null);
                    protocol.run();
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
                protocol = null;
                transport = null;
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
