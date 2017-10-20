package org.iot.dsa.dslink.requester;

import org.iot.dsa.node.DSMap;
import org.iot.dsa.security.DSPermission;

/**
 * Parameter to invoke method on DSIRequester.  Provides details about the invocation as well as
 * callbacks for various state changes.
 *
 * @author Daniel Shapiro, Aaron Hansen
 */
public abstract class OutboundListRequest extends OutboundRequest {

    /**
     * Callback - single entry point for all responses to the request.
     */
    public abstract void onResponse(DSMap response);

}
