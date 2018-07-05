package org.iot.dsa.dslink.responder;

import org.iot.dsa.node.DSElement;
import org.iot.dsa.security.DSPermission;

public interface InboundSetRequest extends InboundRequest {

    /**
     * The value to set.
     */
    public DSElement getValue();

    /**
     * The permission to set with.
     */
    public DSPermission getPermission();

}
