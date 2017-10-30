package com.acuity.iot.dsa.dslink.protocol.protocol_v1.requester;

import org.iot.dsa.node.DSElement;
import org.iot.dsa.node.DSStatus;
import org.iot.dsa.time.DSDateTime;

/**
 * Contains one or more subscription stubs for the same path.
 *
 * @author Daniel Shapiro, Aaron Hansen
 */
class DS1OutboundSubscribeStubs {

    ///////////////////////////////////////////////////////////////////////////
    // Fields
    ///////////////////////////////////////////////////////////////////////////

    private DS1OutboundSubscribeStub first;
    private DS1OutboundSubscribeStub last;
    private DSStatus lastStatus;
    private DSDateTime lastTs;
    private DSElement lastValue;
    private String path;
    private int qos = 0;
    private Integer sid;
    private int size;
    private State state = State.PENDING_SUBSCRIBE;
    private DS1OutboundSubscriptions subscriptions;

    ///////////////////////////////////////////////////////////////////////////
    // Constructors
    ///////////////////////////////////////////////////////////////////////////

    public DS1OutboundSubscribeStubs(
            String path,
            Integer sid,
            DS1OutboundSubscriptions subscriptions) {
        this.path = path;
        this.sid = sid;
        this.subscriptions = subscriptions;
    }

    ///////////////////////////////////////////////////////////////////////////
    // Methods
    ///////////////////////////////////////////////////////////////////////////

    /**
     * If already subscribed, will pass the last update to the new subscriber.
     */
    void add(DS1OutboundSubscribeStub stub) {
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
                    subscriptions.severe(path, x);
                }
            }
        }
    }

    public void close() {
        //TODO who calls this and for what purpose
        /*
        DS1OutboundSubscribeStub cur = first;
        while (cur != null) {
            cur.close();
            cur = cur.getNext();
        }
        */
    }

    private boolean contains(DS1OutboundSubscribeStub stub) {
        if (stub == first) {
            return true;
        }
        return predecessor(stub) != last;
    }

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

    public DS1OutboundSubscriptions getSubscriptions() {
        return subscriptions;
    }

    /**
     * Null if the arg is the first in the list, last if stub is not contained.
     */
    private DS1OutboundSubscribeStub predecessor(DS1OutboundSubscribeStub stub) {
        if (first == null) {
            return null;
        }
        if (stub == first) {
            return null;
        }
        DS1OutboundSubscribeStub cur = first;
        while (cur.getNext() != null) {
            if (cur.getNext() == stub) {
                return cur;
            }
            cur = cur.getNext();
        }
        return cur;
    }

    void process(DSDateTime ts, DSElement value, DSStatus status) {
        DS1OutboundSubscribeStub stub = first;
        while (stub != null) {
            try {
                stub.process(ts, value, status);
            } catch (Exception x) {
                subscriptions.severe(path, x);
            }
            stub = stub.getNext();
        }
        lastTs = ts;
        lastValue = value;
        lastStatus = status;
    }

    void remove(DS1OutboundSubscribeStub stub) {
        DS1OutboundSubscribeStub pred = predecessor(stub);
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
