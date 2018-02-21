package com.acuity.iot.dsa.dslink.protocol.v1.requester;

import com.acuity.iot.dsa.dslink.protocol.message.MessageWriter;
import org.iot.dsa.dslink.requester.OutboundRequestHandler;
import org.iot.dsa.io.DSIWriter;
import org.iot.dsa.node.DSMap;

/**
 * Manages the lifecycle of a remove request and is also the outbound stream passed to the
 * requester.
 *
 * @author Daniel Shapiro, Aaron Hansen
 */
class DS1OutboundRemoveStub extends DS1OutboundStub {

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
    public void write(MessageWriter writer) {
        DSIWriter out = writer.getWriter();
        out.beginMap();
        out.key("rid").value(getRequestId());
        out.key("method").value("remove");
        out.key("path").value(getPath());
        out.endMap();
    }

}
