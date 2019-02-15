package org.iot.dsa.dslink;

import org.iot.dsa.conn.DSConnection;
import org.iot.dsa.node.DSNode;

/**
 * Abstract representation of a DSA connection.
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

    ///////////////////////////////////////////////////////////////////////////
    // Protected Methods
    ///////////////////////////////////////////////////////////////////////////

}
