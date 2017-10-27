package org.iot.dsa.dslink;

import org.iot.dsa.node.DSNode;

/**
 * Represents an upstream connection with a broker.
 *
 * Implementations must have a no-arg public constructor.  It will be dynamically added as a child
 * of the DSLink.
 *
 * The default connection type can be overridden by specifying the config connectionType as a Java
 * class name dslink.json.
 *
 * @author Aaron Hansen
 */
public abstract class DSLinkConnection extends DSNode {

    /**
     * A unique descriptive tag such as a combination of the link name and the broker host.
     */
    public abstract String getConnectionId();

    /**
     * The link using this connection.
     */
    public abstract DSLink getLink();

    public abstract DSIRequester getRequester();

    /**
     * True when a connection is established with the remote endpoint.
     */
    public abstract boolean isOpen();

    /**
     * Called the when network connection has been closed.
     */
    public abstract void onClose();


}
