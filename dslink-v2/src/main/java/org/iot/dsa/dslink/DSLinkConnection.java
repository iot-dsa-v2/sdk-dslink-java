package org.iot.dsa.dslink;

import com.acuity.iot.dsa.dslink.protocol.DSSession;
import com.acuity.iot.dsa.dslink.transport.DSTransport;
import java.util.concurrent.ConcurrentHashMap;
import org.iot.dsa.node.DSNode;
import org.iot.dsa.node.DSPath;
import org.iot.dsa.time.DSTime;

/**
 * Represents an upstream connection with a broker.
 * <p>
 * Implementations must have a no-arg public constructor.  It will be dynamically added as
 * a child of the DSLink.
 *
 * @author Aaron Hansen
 */
public abstract class DSLinkConnection extends DSNode {

    // Fields
    // ------

    private boolean connected = false;
    private String connectionId;
    private ConcurrentHashMap<Listener, Listener> listeners;

    // Methods
    // -------

    /**
     * Adds a listener for connection events.  If already connected, onConnect
     * will be called on the listener.
     */
    public void addListener(Listener listener) {
        synchronized (this) {
            if (listeners == null) {
                listeners = new ConcurrentHashMap<Listener, Listener>();
            }
        }
        listeners.put(listener, listener);
        if (isConnected()) {
            try {
                listener.onConnect(this);
            } catch (Exception x) {
                error(getPath(), x);
            }
        }
    }

    /**
     * Forcefully closes an open connection.  Does not prevent reconnection, intended for
     * dealing with problems.
     */
    public abstract void disconnect();

    /**
     * A unique descriptive tag such as a combination of the link name and the broker host.
     */
    public String getConnectionId() {
        if (connectionId == null) {
            StringBuilder builder = new StringBuilder();
            builder.append(getLink().getLinkName()).append("-");
            String uri = getLink().getConfig().getBrokerUri();
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


    /**
     * The link using this connection.
     */
    public DSLink getLink() {
        return (DSLink) getSys().getParent();
    }

    @Override
    protected String getLogName() {
        return getClass().getSimpleName();
    }

    /**
     * The path representing the link node in the broker.
     */
    public abstract String getPathInBroker();

    /**
     * Concatenates the path in broker with the path of the node.
     */
    public String getPathInBroker(DSNode node) {
        StringBuilder buf = new StringBuilder();
        String localPath = DSPath.encodePath(node, buf).toString();
        buf.setLength(0);
        return DSPath.concat(getPathInBroker(), localPath, buf).toString();
    }

    public abstract DSIRequester getRequester();

    public abstract DSSession getSession();

    public DSSysNode getSys() {
        return (DSSysNode) getParent();
    }

    public abstract DSTransport getTransport();

    /**
     * True when a connection is established with the remote endpoint.
     */
    public boolean isConnected() {
        return connected;
    }

    /**
     * Called after onInitialize and before onRun.  This should open the connection,
     * maybe do some preamble messaging, but should return relatively quickly.  Use
     * onRun for the long running of the connection.
     */
    protected abstract void onConnect();

    /**
     * Called when this network connection has been closed.  This will only be called
     * if the connection is has been established and onRun was called.
     */
    protected abstract void onDisconnect();

    /**
     * Always called before onConnect.  If an exception is thrown onConnect and onDisconnect
     * will not be called.
     */
    protected abstract void onInitialize();

    /**
     * The long term management of the connection (reading and writing).  When this
     * returns, onDisconnect will be called.
     */
    protected abstract void onRun();

    /**
     * Starts the connection.
     */
    @Override
    protected void onStable() {
        new ConnectionRunThread(new ConnectionRunner()).start();
    }

    /**
     * Removes a listener for connection events.
     */
    public void removeListener(Listener listener) {
        if (listeners != null) {
            listeners.remove(listener);
        }
    }


    // Inner Classes
    // -------------

    /**
     * Daemon thread that manages connection and reconnection.
     */
    private class ConnectionRunner implements Runnable {

        private long reconnectRate = 1000;

        /**
         * Runs until stop is called.
         */
        public void run() {
            while (isRunning()) {
                synchronized (this) {
                    try {
                        wait(reconnectRate);
                    } catch (Exception x) {
                        warn(warn() ? getConnectionId() : null, x);
                    }
                }
                reconnectRate = Math.min(reconnectRate * 2, DSTime.MILLIS_MINUTE);
                try {
                    onInitialize();
                } catch (Exception x) {
                    error(getPath(), x);
                    continue;
                }
                try {
                    onConnect();
                    connected = true;
                } catch (Exception x) {
                    error(getPath(), x);
                    continue;
                }
                if (listeners != null) {
                    for (Listener listener : listeners.keySet()) {
                        try {
                            listener.onConnect(DSLinkConnection.this);
                        } catch (Exception x) {
                            error(listener.toString(), x);
                        }
                    }
                }
                try {
                    onRun();
                    reconnectRate = 1000;
                } catch (Throwable x) {
                    reconnectRate = Math.min(reconnectRate * 2, DSTime.MILLIS_MINUTE);
                    error(getConnectionId(), x);
                }
                try {
                    onDisconnect();
                } catch (Exception x) {
                    error(getPath(), x);
                }
                if (listeners != null) {
                    for (Listener listener : listeners.keySet()) {
                        try {
                            listener.onDisconnect(DSLinkConnection.this);
                        } catch (Exception x) {
                            error(listener.toString(), x);
                        }
                    }
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

    /**
     * Intended for requester functionality so that requesters can know when to
     * start and stop making requests.
     */
    public interface Listener {

        /**
         * Called asynchronously after the connection with the endpoint is opened.
         */
        public void onConnect(DSLinkConnection connection);

        /**
         * Called synchronously after the connection with the endpoint is closed.
         */
        public void onDisconnect(DSLinkConnection connection);

    }

}
