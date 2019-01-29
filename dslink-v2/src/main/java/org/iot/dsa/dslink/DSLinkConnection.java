package org.iot.dsa.dslink;

import com.acuity.iot.dsa.dslink.transport.DSTransport;
import org.iot.dsa.conn.DSConnection;

/**
 * Represents an upstream connection to a broker.
 *
 * @author Aaron Hansen
 */
public abstract class DSLinkConnection extends DSConnection {

    ///////////////////////////////////////////////////////////////////////////
    // Class Fields
    ///////////////////////////////////////////////////////////////////////////

    ///////////////////////////////////////////////////////////////////////////
    // Instance Fields
    ///////////////////////////////////////////////////////////////////////////

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
    public abstract String getPathInBroker();

    public abstract DSIRequester getRequester();

    public abstract DSTransport getTransport();

    ///////////////////////////////////////////////////////////////////////////
    // Protected Methods
    ///////////////////////////////////////////////////////////////////////////

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

}
