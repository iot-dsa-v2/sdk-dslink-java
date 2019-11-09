package com.acuity.iot.dsa.dslink.protocol.requester;

import org.iot.dsa.node.DSElement;
import org.iot.dsa.node.DSNull;
import org.iot.dsa.node.DSStatus;
import org.iot.dsa.time.DSDateTime;

/**
 * Contains one or more subscription stubs for the same path.
 *
 * @author Daniel Shapiro, Aaron Hansen
 */
class DSOutboundSubscription {

    ///////////////////////////////////////////////////////////////////////////
    // Instance Fields
    ///////////////////////////////////////////////////////////////////////////

    private DSOutboundSubscribeStub first;
    private DSOutboundSubscribeStub last;
    private DSStatus lastStatus;
    private DSDateTime lastTs;
    private DSElement lastValue;
    private String path;
    private int qos = -1;
    private Integer sid;
    private int size;
    private State state = State.INIT;
    private DSOutboundSubscriptions subscriptions;

    ///////////////////////////////////////////////////////////////////////////
    // Constructors
    ///////////////////////////////////////////////////////////////////////////

    public DSOutboundSubscription(
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

    public DSOutboundSubscription setSid(Integer sid) {
        this.sid = sid;
        return this;
    }

    public State getState() {
        return state;
    }

    void setState(State state) {
        this.state = state;
    }

    public DSOutboundSubscriptions getSubscriptions() {
        return subscriptions;
    }

    public boolean hasSid() {
        return sid != null;
    }

    ///////////////////////////////////////////////////////////////////////////
    // Package / Private Methods
    ///////////////////////////////////////////////////////////////////////////

    public void onDisconnect() {
        DSOutboundSubscribeStub cur = first;
        while (cur != null) {
            cur.closeStream();
            cur = cur.getNext();
        }
    }

    /**
     * If already subscribed, will pass the last update to the new subscriber.
     */
    void add(DSOutboundSubscribeStub stub) {
        int prevQos = qos;
        if (stub.getQos() > qos) {
            qos = stub.getQos();
        }
        if (contains(stub)) {
            if (qos > prevQos) {
                getSubscriptions().sendSubscribe(this);
            }
            return;
        }
        stub.setSub(this);
        if (last == null) {
            first = stub;
            last = stub;
        } else {
            last.setNext(stub);
            last = stub;
        }
        size++;
        if (lastValue != null) {
            try {
                stub.update(lastTs, lastValue, lastStatus);
            } catch (Exception x) {
                subscriptions.error(path, x);
            }
        } else if (!getSubscriptions().getRequester().getSession().getConnection().isConnected()) {
            try {
                stub.update(DSDateTime.now(), DSNull.NULL, DSStatus.unknown);
            } catch (Exception x) {
                subscriptions.error(path, x);
            }
        }
        if (qos > prevQos) { //need to resubscribe for new qos
            getSubscriptions().sendSubscribe(this);
        }
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
        } else {
            stub = first;
            int max = 0;
            while (stub != null) {
                if (stub.getQos() > max) {
                    max = stub.getQos();
                }
                stub = stub.getNext();
            }
            if (max != qos) {
                qos = max;
                getSubscriptions().sendSubscribe(this);
            }
        }
    }

    int size() {
        return size;
    }

    void update(DSDateTime ts, DSElement value, DSStatus status) {
        DSOutboundSubscribeStub stub = first;
        while (stub != null) {
            try {
                stub.update(ts, value, status);
            } catch (Exception x) {
                subscriptions.error(path, x);
            }
            stub = stub.getNext();
        }
        lastTs = ts;
        lastValue = value;
        lastStatus = status;
    }

    void updateDisconnected() {
        if (lastStatus == null) {
            lastStatus = DSStatus.down;
        } else {
            lastStatus = lastStatus.add(DSStatus.DOWN);
        }
        lastTs = DSDateTime.now();
        if (lastValue == null) {
            lastValue = DSNull.NULL;
        }
        DSOutboundSubscribeStub cur = first;
        while (cur != null) {
            cur.update(lastTs, lastValue, lastStatus);
            cur = cur.getNext();
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

    ///////////////////////////////////////////////////////////////////////////
    // Inner Classes
    ///////////////////////////////////////////////////////////////////////////

    public enum State {
        INIT,
        PENDING_SUBSCRIBE,
        PENDING_UNSUBSCRIBE,
        SUBSCRIBED,
        UNSUBSCRIBED
    }

}
