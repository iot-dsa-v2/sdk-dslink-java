package org.iot.dsa.node.event;

import org.iot.dsa.node.DSIValue;
import org.iot.dsa.node.DSInfo;
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
     * If the subscription specified a specific child, otherwise null.
     */
    public DSInfo getChild();

    /**
     * The node subscribed to.
     */
    public DSNode getNode();

    /**
     * Any args supplied to subscription creation, can be null.
     */
    public DSIValue getParameters();

    /**
     * Event sink.
     */
    public DSISubscriber getSubscriber();

    /**
     * Null for all topics, a topic, or possibly a topic matcher.
     */
    public DSITopic getTopic();

    public boolean isOpen();

}
