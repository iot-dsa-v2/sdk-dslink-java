package com.acuity.iot.dsa.dslink.protocol.protocol_v1.requester;

import java.util.Iterator;
import org.iot.dsa.dslink.requester.OutboundSubscribeRequest;
import org.iot.dsa.dslink.requester.OutboundSubscription;
import org.iot.dsa.io.DSIWriter;
import org.iot.dsa.node.DSList;
import org.iot.dsa.node.DSMap;

public class DS1OutboundSubscribeStub extends DS1OutboundRequestStub {

    ///////////////////////////////////////////////////////////////////////////
    // Fields
    ///////////////////////////////////////////////////////////////////////////

    private DS1OutboundSubscribeStub next;
    private OutboundSubscribeRequest request;

    ///////////////////////////////////////////////////////////////////////////
    // Constructors
    ///////////////////////////////////////////////////////////////////////////

    public DS1OutboundSubscribeStub(OutboundSubscribeRequest request) {
        this.request = request;
    }

    ///////////////////////////////////////////////////////////////////////////
    // Methods
    ///////////////////////////////////////////////////////////////////////////

    @Override
    public void close() {
        //TODO send unsubscribe
    }

    DS1OutboundSubscribeStub getNext() {
        return next;
    }

    @Override
    public OutboundSubscribeRequest getRequest() {
        return request;
    }

    @Override
    public void handleClose() {
        //TODO ???
    }

    @Override
    public void handleResponse(DSMap map) {
        try {
            request.onUpdate(map);
        } catch (Exception x) {
            getRequester().severe(getRequester().getPath(), x);
        }
    }

    void setNext(DS1OutboundSubscribeStub next) {
        this.next = next;
    }

    @Override
    public void write(DSIWriter out) {
        out.beginMap();
        out.key("rid").value(getRequestId());
        out.key("method").value("subscribe");
        Iterator<OutboundSubscription> it = request.getPath();
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
