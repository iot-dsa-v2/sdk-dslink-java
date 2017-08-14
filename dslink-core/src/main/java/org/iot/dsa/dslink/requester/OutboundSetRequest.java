package org.iot.dsa.dslink.requester;

import org.iot.dsa.node.DSElement;
import org.iot.dsa.security.DSPermission;

public abstract class OutboundSetRequest extends OutboundRequest {

    /**
     * The maximum permission level of this invoke, or null
     */
    public abstract DSPermission getPermission();

    /**
     * The requested path.
     */
    public abstract String getPath();

    /**
     * The value being written
     */
    public abstract DSElement getValue();
}
