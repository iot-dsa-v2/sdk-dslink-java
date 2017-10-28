package com.acuity.iot.dsa.dslink.protocol.protocol_v1.requester;

import org.iot.dsa.dslink.requester.OutboundStream;
import org.iot.dsa.dslink.requester.OutboundSubscribeHandler;
import org.iot.dsa.node.DSElement;
import org.iot.dsa.node.DSStatus;
import org.iot.dsa.time.DSDateTime;

/**
 * Manages the lifecycle of a single subscription to a path and is also the close handler passed to
 * the requester.
 *
 * <p>
 *
 * There can be multiple subscriptions to a single path.  They are all contained in a
 * DS1OutboundSubscribeStubs object.
 *
 * @author Daniel Shapiro, Aaron Hansen
 */
class DS1OutboundSubscribeStub implements OutboundStream {

    ///////////////////////////////////////////////////////////////////////////
    // Fields
    ///////////////////////////////////////////////////////////////////////////

    private DS1OutboundSubscribeStub next; //linked list in stubs object
    private boolean open = true;
    private String path;
    private int qos = 0;
    private OutboundSubscribeHandler request;
    private DS1OutboundSubscriptions subscriptions;

    ///////////////////////////////////////////////////////////////////////////
    // Constructors
    ///////////////////////////////////////////////////////////////////////////

    public DS1OutboundSubscribeStub(DS1OutboundSubscriptions subscriptions,
                                    String path,
                                    int qos,
                                    OutboundSubscribeHandler request) {
        this.subscriptions = subscriptions;
        this.path = path;
        this.qos = qos;
        this.request = request;
    }

    ///////////////////////////////////////////////////////////////////////////
    // Methods
    ///////////////////////////////////////////////////////////////////////////

    @Override
    public void closeStream() {
        synchronized (this) {
            if (!open) {
                return;
            }
            open = false;
        }
        subscriptions.unsubscribe(this);
    }

    public DS1OutboundSubscribeStub getNext() {
        return next;
    }

    public OutboundSubscribeHandler getHandler() {
        return request;
    }

    public String getPath() {
        return path;
    }

    public int getQos() {
        return qos;
    }

    @Override
    public boolean isStreamOpen() {
        return open;
    }

    public void process(DSDateTime ts, DSElement value, DSStatus status) {
        try {
            request.onUpdate(ts, value, status);
        } catch (Exception x) {
            subscriptions.severe(path, x);
        }
    }

    /**
     * The next stub in the parents stubs object.
     */
    void setNext(DS1OutboundSubscribeStub next) {
        this.next = next;
    }

}
