package org.iot.dsa.node.topic;

import org.iot.dsa.node.DSNode;

/**
 * Represents a subscription to one or more topics on a node.
 *
 * @author Aaron Hansen
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
