package com.acuity.iot.dsa.dslink.protocol.protocol_v1.requester;

import java.util.Iterator;
import org.iot.dsa.dslink.requester.OutboundSubscribeRequest;
import org.iot.dsa.dslink.requester.OutboundSubscription;
import org.iot.dsa.io.DSWriter;
import org.iot.dsa.node.DSList;
import org.iot.dsa.node.DSMap;

public class DS1OutboundSubscribeWrapper extends DS1OutboundRequestWrapper {

    private OutboundSubscribeRequest request;
    protected static final String method = "subscribe";

    public DS1OutboundSubscribeWrapper(OutboundSubscribeRequest request) {
        this.request = request;
    }

    @Override
    public void write(DSWriter out) {
        out.beginMap();
        out.key("rid").value(request.getRequestId());
        out.key("method").value(method);
        Iterator<OutboundSubscription> it = request.getPaths();
        DSList paths = new DSList();
        while (it.hasNext()) {
            OutboundSubscription sub = it.next();
            DSMap m = paths.addMap().put("path", sub.getPath()).put("sid", sub.getSubscriptionId());
            Integer qos = sub.getQos();
            if (qos != null) {
                m.put("qos", qos);
            }
        }
        out.key("paths").value(paths);
        out.endMap();
    }

}
