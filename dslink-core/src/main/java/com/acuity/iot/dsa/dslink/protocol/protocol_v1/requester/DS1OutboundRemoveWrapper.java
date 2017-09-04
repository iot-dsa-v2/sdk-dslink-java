package com.acuity.iot.dsa.dslink.protocol.protocol_v1.requester;

import org.iot.dsa.dslink.requester.OutboundRemoveRequest;
import org.iot.dsa.io.DSIWriter;

public class DS1OutboundRemoveWrapper extends DS1OutboundRequestWrapper {

    private OutboundRemoveRequest request;
    protected static final String method = "remove";

    public DS1OutboundRemoveWrapper(OutboundRemoveRequest request) {
        this.request = request;
    }

    @Override
    public void write(DSIWriter out) {

        out.beginMap();
        out.key("rid").value(request.getRequestId());
        out.key("method").value(method);
        out.key("path").value(request.getPath());
        out.endMap();

    }

}
