package org.iot.dsa.dslink;

import org.iot.dsa.dslink.responder.InboundSubscribeRequest;
import org.iot.dsa.node.DSIValue;
import org.iot.dsa.node.DSStatus;

/**
 * Used by DSRootNode to handle subscribe requests.
 *
 * @author Aaron Hansen
 */
class SubscribeWrapper implements InboundSubscribeRequest {

    ///////////////////////////////////////////////////////////////////////////
    // Constants
    ///////////////////////////////////////////////////////////////////////////

    ///////////////////////////////////////////////////////////////////////////
    // Fields
    ///////////////////////////////////////////////////////////////////////////

    private String path;
    private InboundSubscribeRequest request;

    ///////////////////////////////////////////////////////////////////////////
    // Constructors
    ///////////////////////////////////////////////////////////////////////////

    SubscribeWrapper(String path, InboundSubscribeRequest request) {
        this.path = path;
        this.request = request;
    }

    ///////////////////////////////////////////////////////////////////////////
    // Methods
    ///////////////////////////////////////////////////////////////////////////

    @Override
    public void close() {
        request.close();
    }

    @Override
    public String getPath() {
        return path;
    }

    @Override
    public Integer getRequestId() {
        return request.getRequestId();
    }

    @Override
    public DSLinkSession getResponder() {
        return request.getResponder();
    }

    @Override
    public Integer getSubscriptionId() {
        return request.getSubscriptionId();
    }

    @Override
    public void update(long timestamp, DSIValue value, DSStatus quality) {
        request.update(timestamp, value, quality);
    }

    ///////////////////////////////////////////////////////////////////////////
    // Inner Classes
    ///////////////////////////////////////////////////////////////////////////

}
