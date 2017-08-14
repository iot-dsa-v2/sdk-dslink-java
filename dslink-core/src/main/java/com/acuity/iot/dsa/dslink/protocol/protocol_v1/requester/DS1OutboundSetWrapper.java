package com.acuity.iot.dsa.dslink.protocol.protocol_v1.requester;

import org.iot.dsa.dslink.requester.OutboundSetRequest;
import org.iot.dsa.io.DSWriter;
import org.iot.dsa.security.DSPermission;

public class DS1OutboundSetWrapper extends DS1OutboundRequestWrapper {

    private OutboundSetRequest request;
    protected static final String method = "set";

    public DS1OutboundSetWrapper(OutboundSetRequest request) {
        this.request = request;
    }

    @Override
    public void write(DSWriter out) {

        out.beginMap();
        out.key("rid").value(request.getRequestId());
        out.key("method").value(method);
        DSPermission permit = request.getPermission();
        if (permit != null) {
            out.key("permit").value(permit.toString());
        }
        out.key("path").value(request.getPath());
        out.key("value").value(request.getValue());
        out.endMap();

    }

}
