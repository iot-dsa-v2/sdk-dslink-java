package com.acuity.iot.dsa.dslink.protocol.requester;

import org.iot.dsa.node.DSElement;
import org.iot.dsa.node.DSStatus;
import org.iot.dsa.time.DSDateTime;

/**
 * Contains one or more subscription stubs for the same path.
 *
 * @author Daniel Shapiro, Aaron Hansen
 */
class DSOutboundSubscribeStubs {

    ///////////////////////////////////////////////////////////////////////////
    // Instance Fields
    ///////////////////////////////////////////////////////////////////////////

    private DSOutboundSubscribeStub first;
    private DSOutboundSubscribeStub last;
    private DSStatus lastStatus;
    private DSDateTime lastTs;
    private DSElement lastValue;
    private String path;
    private int qos = 0;
    private Integer sid;
    private int size;
    private State state = State.PENDING_SUBSCRIBE;
    private DSOutboundSubscriptions subscriptions;

    ///////////////////////////////////////////////////////////////////////////
    // Constructors
    ///////////////////////////////////////////////////////////////////////////

    public DSOutboundSubscribeStubs(
            String path,
            DSOutboundSubscriptions subscriptions) {
        this.path = path;
        this.subscriptions = subscriptions;
    }

    ///////////////////////////////////////////////////////////////////////////
    // Public Methods
    ///////////////////////////////////////////////////////////////////////////

    public String getPath() {
        return path;
    }

    public int getQos() {
        return qos;
    }

    public Integer getSid() {
        return sid;
    }

    public State getState() {
        return state;
    }

    public DSOutboundSubscriptions getSubscriptions() {
        return subscriptions;
    }

    public boolean hasSid() {
        return sid != null;
    }

    public void onDisconnect() {
        DSOutboundSubscribeStub cur = first;
        while (cur != null) {
            cur.closeStream();
            cur = cur.getNext();
        }
    }

    public DSOutboundSubscribeStubs setSid(Integer sid) {
        this.sid = sid;
        return this;
    }

    ///////////////////////////////////////////////////////////////////////////
    // Package / Private Methods
    ///////////////////////////////////////////////////////////////////////////

    /**
     * If already subscribed, will pass the last update to the new subscriber.
     */
    void add(DSOutboundSubscribeStub stub) {
        if (stub.getQos() > qos) {
            qos = stub.getQos();
        }
        if (contains(stub)) {
            return;
        }
        stub.setStubs(this);
        if (last == null) {
            first = stub;
            last = stub;
        } else {
            last.setNext(stub);
            last = stub;
        }
        //Send the last update to the new subscription
        if (++size > 1) {
            if (lastValue != null) {
                try {
                    stub.process(lastTs, lastValue, lastStatus);
                } catch (Exception x) {
                    subscriptions.error(path, x);
                }
            }
        }
    }

    private boolean contains(DSOutboundSubscribeStub stub) {
        if (stub == first) {
            return true;
        }
        return predecessor(stub) != last;
    }

    /**
     * Null if the arg is the first in the list, last if stub is not contained.
     */
    private DSOutboundSubscribeStub predecessor(DSOutboundSubscribeStub stub) {
        if (first == null) {
            return null;
        }
        if (stub == first) {
            return null;
        }
        DSOutboundSubscribeStub cur = first;
        while (cur.getNext() != null) {
            if (cur.getNext() == stub) {
                return cur;
            }
            cur = cur.getNext();
        }
        return cur;
    }

    void process(DSDateTime ts, DSElement value, DSStatus status) {
        DSOutboundSubscribeStub stub = first;
        while (stub != null) {
            try {
                stub.process(ts, value, status);
            } catch (Exception x) {
                subscriptions.error(path, x);
            }
            stub = stub.getNext();
        }
        lastTs = ts;
        lastValue = value;
        lastStatus = status;
    }

    void remove(DSOutboundSubscribeStub stub) {
        DSOutboundSubscribeStub pred = predecessor(stub);
        if (pred == last) { //not contained
            return;
        }
        if (stub == first) {
            if (first == last) {
                first = last = null;
            } else {
                first = first.getNext();
            }
        }
        if (stub == last) {
            last = pred;
            pred.setNext(null);
        }
        if (--size == 0) {
            subscriptions.unsubscribe(this);
        }
    }

    void setState(State state) {
        this.state = state;
    }

    int size() {
        return size;
    }

    ///////////////////////////////////////////////////////////////////////////
    // Inner Classes
    ///////////////////////////////////////////////////////////////////////////

    public enum State {
        PENDING_SUBSCRIBE,
        PENDING_UNSUBSCRIBE,
        SUBSCRIBED,
        UNSUBSCRIBED
    }

}
