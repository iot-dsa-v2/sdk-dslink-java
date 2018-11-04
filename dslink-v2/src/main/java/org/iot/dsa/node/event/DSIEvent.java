package org.iot.dsa.node.event;

import org.iot.dsa.node.DSInfo;
import org.iot.dsa.node.DSMap;
import org.iot.dsa.node.DSNode;

/**
 * Subscribers subscribe to topics on nodes, and nodes notify subscribers of events related to
 * the topics they've subscribed.
 * <p>
 * The whole event api is designed to minimize object instantiation.  Often events and topics
 * are the same instance such as the VALUE_TOPIC available on all nodes.
 *
 * @author Aaron Hansen
 * @see DSNode#subscribe(DSTopic, DSInfo, DSISubscriber, DSMap)
 * @see DSISubscriber#onEvent(DSNode, DSInfo, DSIEvent)
 * @see DSNode#INFO_TOPIC
 * @see DSNode#VALUE_TOPIC
 */
public interface DSIEvent {

    /**
     * Possibly null, can be used to provide more data about an event.  The topic should document
     * whether or not it provides any data.
     */
    public DSMap getData();

    public DSTopic getTopic();

}
