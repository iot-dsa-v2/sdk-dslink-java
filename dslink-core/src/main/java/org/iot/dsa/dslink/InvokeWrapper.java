package org.iot.dsa.dslink;

import org.iot.dsa.dslink.responder.InboundInvokeRequest;
import org.iot.dsa.node.DSList;
import org.iot.dsa.node.DSMap;
import org.iot.dsa.security.DSPermission;

/**
 * Used by DSRootNode to handle an invoke request.
 *
 * @author Aaron Hansen
 */
class InvokeWrapper implements InboundInvokeRequest {

    ///////////////////////////////////////////////////////////////////////////
    // Constants
    ///////////////////////////////////////////////////////////////////////////

    ///////////////////////////////////////////////////////////////////////////
    // Fields
    ///////////////////////////////////////////////////////////////////////////

    private RequestPath path;
    private InboundInvokeRequest request;

    ///////////////////////////////////////////////////////////////////////////
    // Constructors
    ///////////////////////////////////////////////////////////////////////////

    InvokeWrapper(RequestPath path, InboundInvokeRequest request) {
        this.path = path;
        this.request = request;
    }

    ///////////////////////////////////////////////////////////////////////////
    // Methods
    ///////////////////////////////////////////////////////////////////////////

    @Override
    public void clearAllRows() {
        request.clearAllRows();
    }

    @Override
    public void close() {
        request.close();
    }

    @Override
    public void close(Exception reason) {
        request.close(reason);
    }

    public void onClose() {
    }

    @Override
    public DSMap getParameters() {
        return request.getParameters();
    }

    @Override
    public String getPath() {
        return path.getPath();
    }

    @Override
    public DSPermission getPermission() {
        return request.getPermission();
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
    public void insert(int index, DSList... rows) {
        request.insert(index, rows);
    }

    @Override
    public boolean isOpen() {
        return request.isOpen();
    }

    @Override
    public void send(DSList row) {
        request.send(row);
    }

    @Override
    public void replace(int idx, int len, DSList... rows) {
        request.replace(idx, len, rows);
    }

    ///////////////////////////////////////////////////////////////////////////
    // Inner Classes
    ///////////////////////////////////////////////////////////////////////////

}
