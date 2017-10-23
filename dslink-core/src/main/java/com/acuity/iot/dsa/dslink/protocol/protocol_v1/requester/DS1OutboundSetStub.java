package com.acuity.iot.dsa.dslink.protocol.protocol_v1.requester;

import org.iot.dsa.dslink.requester.OutboundSetRequest;
import org.iot.dsa.io.DSIWriter;
import org.iot.dsa.node.DSElement;
import org.iot.dsa.node.DSMap;
import org.iot.dsa.security.DSPermission;

/**
 * @author Daniel Shapiro, Aaron Hansen
 */
class DS1OutboundSetStub extends DS1OutboundRequestStub {

    ///////////////////////////////////////////////////////////////////////////
    // Fields
    ///////////////////////////////////////////////////////////////////////////

    private boolean open = true;
    private OutboundSetRequest request;

    ///////////////////////////////////////////////////////////////////////////
    // Constructors
    ///////////////////////////////////////////////////////////////////////////

    public DS1OutboundSetStub(OutboundSetRequest request) {
        this.request = request;
    }

    ///////////////////////////////////////////////////////////////////////////
    // Methods
    ///////////////////////////////////////////////////////////////////////////

    public OutboundSetRequest getRequest() {
        return request;
    }

    @Override
    protected void handleResponse(DSMap map) {
    }

    @Override
    public void write(DSIWriter out) {
        out.beginMap();
        out.key("rid").value(getRequestId());
        out.key("method").value("set");
        DSPermission permit = request.getPermission();
        if (permit != null) {
            out.key("permit").value(permit.toString());
        }
        out.key("path").value(request.getPath());
        out.key("value").value(request.getValue());
        out.endMap();
    }

}
