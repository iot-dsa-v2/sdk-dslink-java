package com.acuity.iot.dsa.dslink.protocol.protocol_v1.requester;

import org.iot.dsa.dslink.requester.OutboundListRequest;
import org.iot.dsa.io.DSWriter;

public class DS1OutboundListWrapper extends DS1OutboundRequestWrapper {

    private OutboundListRequest request;
    protected static final String method = "list";

    public DS1OutboundListWrapper(OutboundListRequest request) {
        this.request = request;
    }

    @Override
    public void write(DSWriter out) {

        out.beginMap();
        out.key("rid").value(request.getRequestId());
        out.key("method").value(method);
        out.key("path").value(request.getPath());
        out.endMap();

    }

}
