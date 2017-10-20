package org.iot.dsa.dslink.requester;

import org.iot.dsa.node.DSElement;
import org.iot.dsa.security.DSPermission;

/**
 * @author Daniel Shapiro, Aaron Hansen
 */
public class OutboundSetRequest extends OutboundRequest {

    ///////////////////////////////////////////////////////////////////////////
    // Fields
    ///////////////////////////////////////////////////////////////////////////

    private DSElement value;
    private DSPermission permission;

    ///////////////////////////////////////////////////////////////////////////
    // Methods in alphabetical order
    ///////////////////////////////////////////////////////////////////////////

    /**
     * The maximum permission level of the requester, or null.
     */
    public DSPermission getPermission() {
        return permission;
    }

    public DSElement getValue() {
        return value;
    }

    public OutboundSetRequest setPermission(DSPermission permission) {
        this.permission = permission;
        return this;
    }

    public OutboundSetRequest setValue(DSElement value) {
        this.value = value;
        return this;
    }

}
