package com.acuity.iot.dsa.dslink.protocol.requester;

import org.iot.dsa.dslink.requester.OutboundStream;
import org.iot.dsa.dslink.requester.OutboundSubscribeHandler;
import org.iot.dsa.node.DSElement;
import org.iot.dsa.node.DSStatus;
import org.iot.dsa.time.DSDateTime;

/**
 * Manages the lifecycle of a single subscription and is also the outbound stream passed to the
 * requester.
 * <p>
 * <p>
 * <p>
 * There can be multiple subscriptions to a single path.  They are all contained in a
 * DSOutboundSubscribeStubs object.
 *
 * @author Daniel Shapiro, Aaron Hansen
 */
class DSOutboundSubscribeStub implements OutboundStream {

    ///////////////////////////////////////////////////////////////////////////
    // Fields
    ///////////////////////////////////////////////////////////////////////////

    private DSOutboundSubscribeStub next; //linked list in stubs object
    private boolean open = true;
    private String path;
    private int qos = 0;
    private OutboundSubscribeHandler request;
    private DSOutboundSubscribeStubs stubs;

    ///////////////////////////////////////////////////////////////////////////
    // Constructors
    ///////////////////////////////////////////////////////////////////////////

    public DSOutboundSubscribeStub(String path, int qos, OutboundSubscribeHandler request) {
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
        stubs.remove(this);
        try {
            request.onClose();
        } catch (Exception x) {
            stubs.getSubscriptions().error(path, x);
        }
    }

    public DSOutboundSubscribeStub getNext() {
        return next;
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
        if (!open) {
            return;
        }
        try {
            request.onUpdate(ts, value, status);
        } catch (Exception x) {
            stubs.getSubscriptions().error(path, x);
        }
    }

    /**
     * The next stub in the parents stubs object.
     */
    void setNext(DSOutboundSubscribeStub next) {
        this.next = next;
    }

    void setStubs(DSOutboundSubscribeStubs stubs) {
        this.stubs = stubs;
    }

}
