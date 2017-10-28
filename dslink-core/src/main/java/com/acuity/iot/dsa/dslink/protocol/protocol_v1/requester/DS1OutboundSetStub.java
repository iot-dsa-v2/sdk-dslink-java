package com.acuity.iot.dsa.dslink.protocol.protocol_v1.requester;

import org.iot.dsa.dslink.requester.OutboundRequestHandler;
import org.iot.dsa.io.DSIWriter;
import org.iot.dsa.node.DSElement;
import org.iot.dsa.node.DSMap;

/**
 * @author Daniel Shapiro, Aaron Hansen
 */
class DS1OutboundSetStub extends DS1OutboundRequestStub {

    ///////////////////////////////////////////////////////////////////////////
    // Fields
    ///////////////////////////////////////////////////////////////////////////

    private OutboundRequestHandler request;
    private DSElement value;

    ///////////////////////////////////////////////////////////////////////////
    // Constructors
    ///////////////////////////////////////////////////////////////////////////

    public DS1OutboundSetStub(DS1Requester requester,
                              Integer requestId,
                              String path,
                              DSElement value,
                              OutboundRequestHandler request) {
        super(requester, requestId, path);
        this.value = value;
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
        out.key("method").value("set");
        out.key("permit").value("config");
        out.key("path").value(getPath());
        out.key("value").value(value);
        out.endMap();
    }

}
