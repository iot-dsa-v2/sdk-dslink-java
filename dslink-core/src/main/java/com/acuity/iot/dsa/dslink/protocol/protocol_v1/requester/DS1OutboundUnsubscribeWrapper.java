package com.acuity.iot.dsa.dslink.protocol.protocol_v1.requester;

import java.util.Iterator;
import org.iot.dsa.dslink.requester.OutboundSubscription;
import org.iot.dsa.dslink.requester.OutboundUnsubscribeRequest;
import org.iot.dsa.io.DSIWriter;
import org.iot.dsa.node.DSList;

public class DS1OutboundUnsubscribeWrapper extends DS1OutboundRequestWrapper {

    private OutboundUnsubscribeRequest request;
    protected static final String method = "unsubscribe";

    public DS1OutboundUnsubscribeWrapper(OutboundUnsubscribeRequest request) {
        this.request = request;
    }

    @Override
    public void write(DSIWriter out) {
        out.beginMap();
        out.key("rid").value(request.getRequestId());
        out.key("method").value(method);
        Iterator<OutboundSubscription> it = request.getSids();
        DSList unsubs = new DSList();
        while (it.hasNext()) {
            OutboundSubscription sub = it.next();
            unsubs.add(sub.getSubscriptionId());
        }
        out.key("sids").value(unsubs);
        out.endMap();

    }

}
