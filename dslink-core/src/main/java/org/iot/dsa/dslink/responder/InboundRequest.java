package org.iot.dsa.dslink.responder;

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
     * Unique ID of the request, or 0 for subscriptions.
     */
    public Integer getRequestId();

}
