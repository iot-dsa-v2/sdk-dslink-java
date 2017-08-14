package org.iot.dsa.dslink.requester;

public abstract class OutboundSubscription {

    private int sid;

    public int getSubscriptionId() {
        return sid;
    }

    public void setSubscriptionId(int sid) {
        this.sid = sid;
    }

    /**
     * The requested path.
     */
    public abstract String getPath();

    /**
     * the qos of the subscription, or null 0: default, responder/broker won't cache value for
     * requester, if responder's updating speed is faster than requester's reading speed, broker
     * will merge value and only send requester the last state with the rollup of all skipped
     * values. 1: durable, responder/broker cache values for the requester, but would drop the queue
     * as soon as the requester is disconnected. 2: durable, responder/broker cache values for the
     * requester, makes sure it doesn't miss data if requester's connection is slow, or when
     * requester is offline for a while. 3: durable and persist, both 1 and 2, responder/broker will
     * backup the whole cache queue
     */
    public abstract Integer getQos();

    /**
     * Called when an update for this subscription comes in from the broker
     */
    public abstract void onUpdate(InboundSubscriptionUpdate update);


}
