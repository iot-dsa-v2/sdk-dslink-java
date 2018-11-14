package org.iot.dsa.dslink;

import com.acuity.iot.dsa.dslink.protocol.DSSession;
import com.acuity.iot.dsa.dslink.transport.DSTransport;
import org.iot.dsa.conn.DSConnection;
import org.iot.dsa.node.DSInfo;
import org.iot.dsa.node.DSNode;
import org.iot.dsa.node.DSPath;
import org.iot.dsa.node.DSString;
import org.iot.dsa.node.action.ActionInvocation;
import org.iot.dsa.node.action.ActionResult;
import org.iot.dsa.node.action.DSAction;

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
    protected static final String RECONNECT = "Reconnect";

    ///////////////////////////////////////////////////////////////////////////
    // Instance Fields
    ///////////////////////////////////////////////////////////////////////////

    private DSInfo brokerPath = getInfo(BROKER_PATH);
    private String connectionId;
    private DSLink link;

    ///////////////////////////////////////////////////////////////////////////
    // Public Methods
    ///////////////////////////////////////////////////////////////////////////

    /**
     * A unique descriptive tag such as a combination of the link name and the broker host.
     */
    public String getConnectionId() {
        if (connectionId == null) {
            StringBuilder builder = new StringBuilder();
            builder.append(getLink().getLinkName()).append("-");
            String uri = getLink().getOptions().getBrokerUri();
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

    ///////////////////////////////////////////////////////////////////////////
    // Protected Methods
    ///////////////////////////////////////////////////////////////////////////

    @Override
    protected void declareDefaults() {
        super.declareDefaults();
        declareDefault(BROKER_URI, DSString.NULL).setTransient(true).setReadOnly(true);
        declareDefault(BROKER_PATH, DSString.NULL).setTransient(true).setReadOnly(true);
        declareDefault(RECONNECT, new DSAction.Parameterless() {
            @Override
            public ActionResult invoke(DSInfo target, ActionInvocation invocation) {
                ((DSLinkConnection)target.getObject()).disconnect();
                return null;
            }
        });
    }

    protected DSSysNode getSys() {
        return (DSSysNode) getParent();
    }

    @Override
    protected void doDisconnect() {
        try {
            if (getTransport() != null) {
                getTransport().close();
            }
        } catch (Exception x) {
            error(getPath(), x);
        }
    }

    /**
     * Creates and starts a thread for running the connection lifecycle.
     */
    @Override
    protected void onStable() {
        super.onStable();
        Thread t = new Thread(this, "Connection " + getName() + " Runner");
        t.setDaemon(true);
        t.start();
    }

    protected void setPathInBroker(String path) {
        put(brokerPath, DSString.valueOf(path));
    }

}
