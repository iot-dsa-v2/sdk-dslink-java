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
    void close();

    /**
     * The node being subscribed to.
     */
    DSNode getNode();

    /**
     * Event sink.
     */
    DSISubscriber getSubscriber();

    boolean isOpen();

}
