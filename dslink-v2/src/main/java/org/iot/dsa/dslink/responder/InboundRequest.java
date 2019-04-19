package org.iot.dsa.dslink.responder;

import org.iot.dsa.node.DSIValue;

/**
 * Common to all incoming requests.
 *
 * @author Aaron Hansen
 */
public interface InboundRequest {

    /**
     * The target of the request.
     */
    public String getPath();

    /**
     * Any parameters accompanying the request, possibly null.
     */
    public default DSIValue getParams() {
        return null;
    }

    /**
     * Unique ID of the request, or 0 for subscriptions.
     */
    public Integer getRequestId();

}
