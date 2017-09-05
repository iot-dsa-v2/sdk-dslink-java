package org.iot.dsa.dslink;

import org.iot.dsa.node.DSNode;

/**
 * Represents an upstream connection with a broker.  Implementations must have a no-arg public
 * constructor. The default connection type can be overridden by specifying the config
 * connectionType as a Java class name dslink.json.
 *
 * @author Aaron Hansen
 */
public abstract class DSLinkConnection extends DSNode {

    /**
     * Closes the current established connection, does not stop this object.  There are many places
     * and reasons for closing the actual connection (such as errors and timeouts),  all of them
     * should just call this for comprehensive cleanup.
     */
    public abstract void close();

    /**
     * A unique descriptive tag such as a combination of the link name and the broker host.
     */
    public abstract String getConnectionId();

    /**
     * The parent link.
     */
    public abstract DSLink getLink();

    /**
     * True when a connection is established with the remote endpoint.
     */
    public abstract boolean isOpen();

    /**
     * Called by the link after creation.
     */
    public abstract void setLink(DSLink link);

}
