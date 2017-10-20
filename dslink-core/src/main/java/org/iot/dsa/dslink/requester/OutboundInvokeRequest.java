package org.iot.dsa.dslink.requester;

import org.iot.dsa.node.DSMap;
import org.iot.dsa.security.DSPermission;

/**
 * Parameter to invoke method on DSIRequester.  Provides details about the invocation as well as
 * callbacks for various state changes.
 *
 * @author Daniel Shapiro, Aaron Hansen
 */
public abstract class OutboundInvokeRequest extends OutboundRequest {

    ///////////////////////////////////////////////////////////////////////////
    // Fields
    ///////////////////////////////////////////////////////////////////////////

    private DSMap parameters;
    private DSPermission permission;

    ///////////////////////////////////////////////////////////////////////////
    // Methods in alphabetical order
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Any parameters supplied for the invocation
     */
    public DSMap getParameters() {
        return parameters;
    }

    /**
     * The maximum permission level of the invoker, or null.
     */
    public DSPermission getPermission() {
        return permission;
    }

    /**
     * Callback - single entry point for all responses to the request.
     */
    public abstract void onResponse(DSMap response);

    public OutboundInvokeRequest setParameters(DSMap parameters) {
        this.parameters = parameters;
        return this;
    }

    public OutboundInvokeRequest setPermission(DSPermission permission) {
        this.permission = permission;
        return this;
    }

}
