package org.iot.dsa.dslink.responder;

import org.iot.dsa.node.DSIValue;

/**
 * Common to all incoming requests.
 *
 * @author Aaron Hansen
 */
public interface InboundRequest {

    /**
     * Any parameters accompanying the request, possibly null.
     */
    default DSIValue getParameters() {
        return null;
    }

    /**
     * The target of the request.
     */
    String getPath();

    /**
     * Unique ID of the request, or 0 for subscriptions.
     */
    Integer getRequestId();

}
