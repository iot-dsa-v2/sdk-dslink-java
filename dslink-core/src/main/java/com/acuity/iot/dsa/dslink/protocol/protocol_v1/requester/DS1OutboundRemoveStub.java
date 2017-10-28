package com.acuity.iot.dsa.dslink.protocol.protocol_v1.requester;

import org.iot.dsa.dslink.requester.OutboundRequestHandler;
import org.iot.dsa.io.DSIWriter;
import org.iot.dsa.node.DSMap;

/**
 * @author Daniel Shapiro, Aaron Hansen
 */
class DS1OutboundRemoveStub extends DS1OutboundRequestStub {

    ///////////////////////////////////////////////////////////////////////////
    // Fields
    ///////////////////////////////////////////////////////////////////////////

    private OutboundRequestHandler request;

    ///////////////////////////////////////////////////////////////////////////
    // Constructors
    ///////////////////////////////////////////////////////////////////////////

    public DS1OutboundRemoveStub(DS1Requester requester,
                                 Integer requestId,
                                 String path,
                                 OutboundRequestHandler request) {
        super(requester, requestId, path);
        this.request = request;
    }

    ///////////////////////////////////////////////////////////////////////////
    // Methods
    ///////////////////////////////////////////////////////////////////////////

    public OutboundRequestHandler getHandler() {
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
        out.key("path").value(getPath());
        out.endMap();

    }

}
