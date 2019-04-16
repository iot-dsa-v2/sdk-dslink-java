package com.acuity.iot.dsa.dslink.protocol.requester;

import org.iot.dsa.dslink.requester.OutboundStream;
import org.iot.dsa.dslink.requester.OutboundSubscribeHandler;
import org.iot.dsa.node.DSElement;
import org.iot.dsa.node.DSIValue;
import org.iot.dsa.node.DSLong;
import org.iot.dsa.node.DSStatus;
import org.iot.dsa.time.DSDateTime;

/**
 * Manages the lifecycle of a single subscription and is also the outbound stream passed to the
 * requester.
 * <p>
 * There can be multiple subscriptions to a single path.  They are all contained in a
 * DSOutboundSubscription object.
 *
 * @author Daniel Shapiro, Aaron Hansen
 */
class DSOutboundSubscribeStub implements OutboundStream {

    private DSOutboundSubscribeStub next; //linked list in subscription object
    private boolean open = true;
    private String path;
    private DSIValue qos;
    private OutboundSubscribeHandler request;
    private DSOutboundSubscription sub;

    public DSOutboundSubscribeStub(String path, DSIValue qos, OutboundSubscribeHandler request) {
        this.path = path;
        if (qos == null) {
            this.qos = DSLong.valueOf(0);
        } else {
            this.qos = qos;
        }
        this.request = request;
    }

    @Override
    public void closeStream() {
        synchronized (this) {
            if (!open) {
                return;
            }
            open = false;
        }
        sub.remove(this);
        try {
            request.onClose();
        } catch (Exception x) {
            sub.getSubscriptions().error(path, x);
        }
    }

    public DSOutboundSubscribeStub getNext() {
        return next;
    }

    public String getPath() {
        return path;
    }

    public int getQos() {
        return qos.toElement().toInt();
    }

    @Override
    public boolean isStreamOpen() {
        return open;
    }

    public void update(DSDateTime ts, DSElement value, DSStatus status) {
        if (!open) {
            return;
        }
        try {
            request.onUpdate(ts, value, status);
        } catch (Exception x) {
            sub.getSubscriptions().error(path, x);
        }
    }

    /**
     * The next stub in the parents stubs object.
     */
    void setNext(DSOutboundSubscribeStub next) {
        this.next = next;
    }

    void setSub(DSOutboundSubscription sub) {
        this.sub = sub;
    }

}
