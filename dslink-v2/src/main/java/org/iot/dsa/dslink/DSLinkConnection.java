package org.iot.dsa.dslink;

import com.acuity.iot.dsa.dslink.protocol.DSSession;
import com.acuity.iot.dsa.dslink.protocol.v2.DS2Session;
import com.acuity.iot.dsa.dslink.transport.DSBinaryTransport;
import com.acuity.iot.dsa.dslink.transport.DSTransport;
import java.util.concurrent.ConcurrentHashMap;
import org.iot.dsa.DSRuntime;
import org.iot.dsa.conn.DSConnection;
import org.iot.dsa.node.DSBool;
import org.iot.dsa.node.DSInfo;
import org.iot.dsa.node.DSNode;
import org.iot.dsa.node.DSPath;
import org.iot.dsa.node.DSString;

/**
 * Represents an upstream connection to a broker.
 *
 * @author Aaron Hansen
 */
public abstract class DSLinkConnection extends DSConnection {

    ///////////////////////////////////////////////////////////////////////////
    // Class Fields
    ///////////////////////////////////////////////////////////////////////////

    protected static final String BROKER_URI = "Broker URI";
    protected static final String BROKER_PATH = "Path In Broker";

    ///////////////////////////////////////////////////////////////////////////
    // Instance Fields
    ///////////////////////////////////////////////////////////////////////////

    private DSInfo brokerPath = getInfo(BROKER_PATH);
    private String connectionId;
    private DSLink link;
    private ConcurrentHashMap<Listener, Listener> listeners;

    ///////////////////////////////////////////////////////////////////////////
    // Public Methods
    ///////////////////////////////////////////////////////////////////////////

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
        if (link == null) {
            link = (DSLink) getAncestor(DSLink.class);
        }
        return link;
    }

    /**
     * The path representing the link node in the broker.
     */
    public String getPathInBroker() {
        return brokerPath.getElement().toString();
    }

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

    public abstract DSTransport getTransport();

    /**
     * Removes a listener for connection events.
     */
    public void removeListener(Listener listener) {
        if (listeners != null) {
            listeners.remove(listener);
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // Protected Methods
    ///////////////////////////////////////////////////////////////////////////

    @Override
    protected void declareDefaults() {
        super.declareDefaults();
        declareDefault(BROKER_URI, DSString.NULL).setTransient(true).setReadOnly(true);
        declareDefault(BROKER_PATH, DSString.NULL).setTransient(true).setReadOnly(true);
    }

    @Override
    protected String getLogName() {
        return getLogName("connection");
    }

    protected DSSysNode getSys() {
        return (DSSysNode) getParent();
    }

    @Override
    protected void onConnected() {
        super.onConnected();
        if (listeners != null) {
            for (Listener l : listeners.keySet()) {
                try {
                    l.onConnect(this);
                } catch (Exception x) {
                    error(getPath(), x);
                }
            }
        }
    }

    @Override
    protected void onDisconnect() {
        try {
            if (getTransport() != null) {
                getTransport().close();
            }
        } catch (Exception x) {
            debug(getPath(), x);
        }
    }

    @Override
    protected void onDisconnected() {
        super.onDisconnected();
        if (listeners != null) {
            for (Listener l : listeners.keySet()) {
                try {
                    l.onDisconnect(this);
                } catch (Exception x) {
                    error(getPath(), x);
                }
            }
        }
    }

    /**
     * Creates and starts a thread for running the connection lifecycle.
     */
    @Override
    protected void onStable() {
        Thread t = new Thread(this, "Connection " + getName() + " Runner");
        t.setDaemon(true);
        t.start();
    }

    protected void setPathInBroker(String path) {
        put(brokerPath, DSString.valueOf(path));
    }

    ///////////////////////////////////////////////////////////////////////////
    // Inner Classes
    ///////////////////////////////////////////////////////////////////////////

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
