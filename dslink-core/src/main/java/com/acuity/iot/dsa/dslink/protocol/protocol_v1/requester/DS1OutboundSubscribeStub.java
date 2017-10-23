package com.acuity.iot.dsa.dslink.protocol.protocol_v1.requester;

import org.iot.dsa.dslink.requester.OutboundSubscribeRequest;
import org.iot.dsa.dslink.requester.OutboundSubscribeStub;
import org.iot.dsa.node.DSElement;
import org.iot.dsa.node.DSStatus;
import org.iot.dsa.time.DSDateTime;

public class DS1OutboundSubscribeStub implements OutboundSubscribeStub {

    ///////////////////////////////////////////////////////////////////////////
    // Fields
    ///////////////////////////////////////////////////////////////////////////

    private DS1OutboundSubscribeStub next;
    private OutboundSubscribeRequest request;
    private DS1OutboundSubscriptions subscriptions;

    ///////////////////////////////////////////////////////////////////////////
    // Constructors
    ///////////////////////////////////////////////////////////////////////////

    public DS1OutboundSubscribeStub(DS1OutboundSubscriptions subscriptions,
                                    OutboundSubscribeRequest request) {
        this.subscriptions = subscriptions;
        this.request = request;
    }

    ///////////////////////////////////////////////////////////////////////////
    // Methods
    ///////////////////////////////////////////////////////////////////////////

    @Override
    public void close() {
        subscriptions.unsubscribe(this);
    }

    public DS1OutboundSubscribeStub getNext() {
        return next;
    }

    public OutboundSubscribeRequest getRequest() {
        return request;
    }

    public int getQos() {
        return request.getQos();
    }

    public void process(DSDateTime ts, DSElement value, DSStatus status) {
        try {
            request.update(ts, value, status);
        } catch (Exception x) {
            subscriptions.severe(request.getPath(), x);
        }
    }

    void setNext(DS1OutboundSubscribeStub next) {
        this.next = next;
    }

}
