package org.iot.dsa.dslink;

import org.iot.dsa.dslink.responder.ApiObject;
import org.iot.dsa.dslink.responder.InboundListRequest;

/**
 * Used by DSRootNode to handle and list request.
 *
 * @author Aaron Hansen
 */
class ListWrapper implements InboundListRequest {

    ///////////////////////////////////////////////////////////////////////////
    // Constants
    ///////////////////////////////////////////////////////////////////////////

    ///////////////////////////////////////////////////////////////////////////
    // Fields
    ///////////////////////////////////////////////////////////////////////////

    private String path;
    private InboundListRequest request;

    ///////////////////////////////////////////////////////////////////////////
    // Constructors
    ///////////////////////////////////////////////////////////////////////////

    ListWrapper(String path, InboundListRequest request) {
        this.path = path;
        this.request = request;
    }

    ///////////////////////////////////////////////////////////////////////////
    // Methods
    ///////////////////////////////////////////////////////////////////////////

    @Override
    public void childAdded(ApiObject obj) {
        request.childAdded(obj);
    }

    @Override
    public void childRemoved(ApiObject obj) {
        request.childRemoved(obj);
    }

    @Override
    public void close() {
        request.close();
    }

    @Override
    public void close(Exception reason) {
        request.close(reason);
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
    public boolean isOpen() {
        return request.isOpen();
    }

    ///////////////////////////////////////////////////////////////////////////
    // Inner Classes
    ///////////////////////////////////////////////////////////////////////////

}
