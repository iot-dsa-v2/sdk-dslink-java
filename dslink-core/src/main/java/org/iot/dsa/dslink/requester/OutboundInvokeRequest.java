package org.iot.dsa.dslink.requester;

import org.iot.dsa.node.DSMap;
import org.iot.dsa.security.DSPermission;

/**
 * Parameter to invoke method on DSIRequester.  Provides details about the invocation as well as
 * callbacks for various state changes.
 */
public abstract class OutboundInvokeRequest extends OutboundRequest {


    ///////////////////////////////////////////////////////////////////////////
    // Fields
    ///////////////////////////////////////////////////////////////////////////

    private String path;
    private DSMap parameters;
    private DSPermission permission;

    ///////////////////////////////////////////////////////////////////////////
    // Constructors
    ///////////////////////////////////////////////////////////////////////////

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
     * The requested path.
     */
    public String getPath() {
        return path;
    }

    /**
     * The maximum permission level of this invoke, or null
     */
    public DSPermission getPermission() {
        return permission;
    }

    /**
     * Called no matter how the stream is closed.  Does nothing by default.
     */
    public void onClose() {
    }

    /**
     * Callback - single entry point for all responses to the request.
     */
    public abstract void onResponse(DSMap response);

    public OutboundInvokeRequest setParameters(DSMap parameters) {
        this.parameters = parameters;
        return this;
    }

    public OutboundInvokeRequest setPath(String path) {
        this.path = path;
        return this;
    }

    public OutboundInvokeRequest setPermission(DSPermission permission) {
        this.permission = permission;
        return this;
    }

}
