package com.acuity.iot.dsa.dslink.protocol.protocol_v1.requester;

import org.iot.dsa.dslink.requester.OutboundRemoveRequest;
import org.iot.dsa.io.DSIWriter;
import org.iot.dsa.node.DSMap;

public class DS1OutboundRemoveStub extends DS1OutboundRequestStub {

    ///////////////////////////////////////////////////////////////////////////
    // Fields
    ///////////////////////////////////////////////////////////////////////////

    private OutboundRemoveRequest request;

    ///////////////////////////////////////////////////////////////////////////
    // Constructors
    ///////////////////////////////////////////////////////////////////////////

    public DS1OutboundRemoveStub(OutboundRemoveRequest request) {
        this.request = request;
    }

    ///////////////////////////////////////////////////////////////////////////
    // Methods
    ///////////////////////////////////////////////////////////////////////////

    public OutboundRemoveRequest getRequest() {
        return request;
    }

    @Override
    protected void handleResponse(DSMap map) {
    }

    @Override
    public void write(DSIWriter out) {

        out.beginMap();
        out.key("rid").value(getRequestId());
        out.key("method").value("remove");
        out.key("path").value(request.getPath());
        out.endMap();

    }

}
