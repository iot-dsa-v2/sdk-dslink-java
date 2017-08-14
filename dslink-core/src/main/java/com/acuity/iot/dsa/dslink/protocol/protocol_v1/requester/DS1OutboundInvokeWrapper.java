package com.acuity.iot.dsa.dslink.protocol.protocol_v1.requester;

import org.iot.dsa.dslink.requester.OutboundInvokeRequest;
import org.iot.dsa.io.DSWriter;
import org.iot.dsa.node.DSMap;
import org.iot.dsa.security.DSPermission;

public class DS1OutboundInvokeWrapper extends DS1OutboundRequestWrapper {

    private OutboundInvokeRequest request;
    protected static final String method = "invoke";

    public DS1OutboundInvokeWrapper(OutboundInvokeRequest request) {
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
        DSMap params = request.getParameters();
        if (params == null) {
            params = new DSMap();
        }
        out.key("params").value(params);
        out.endMap();

    }

}
