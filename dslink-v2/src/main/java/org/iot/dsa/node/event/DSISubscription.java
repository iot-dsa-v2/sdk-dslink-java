package org.iot.dsa.node.event;

import org.iot.dsa.node.DSNode;

/**
 * Represents a subscription to a node.
 *
 * @author Aaron Hansen
 * @see DSNode#subscribe(DSISubscriber)
 */
public interface DSISubscription {

    /**
     * Closes the subscription.
     */
    public void close();

    /**
     * The node subscribed to.
     */
    public DSNode getNode();

    /**
     * Event sink.
     */
    public DSISubscriber getSubscriber();

    public boolean isOpen();

}
