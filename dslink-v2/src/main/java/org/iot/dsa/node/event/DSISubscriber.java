package org.iot.dsa.node.event;

import org.iot.dsa.node.DSInfo;
import org.iot.dsa.node.DSIValue;
import org.iot.dsa.node.DSNode;

/**
 * DSISubscribers subscribe to DSTopics on DSNodes.
 * <p>
 * The two most important topics are built into every node, they are: DSValueTopic and DSInfoTopic.
 *
 * @see DSIEvent
 * @see DSNode#subscribe(DSTopic, DSInfo, DSIValue, DSISubscriber)
 * @see DSNode#INFO_TOPIC
 * @see DSNode#VALUE_TOPIC
 */
public interface DSISubscriber {

    /**
     * Subscription callback.
     *
     * @param node  Required, node subscribed to.
     * @param child Optional, if the event concerns a child.
     * @param event Required, the actual event.
     */
    public void onEvent(DSNode node, DSInfo child, DSIEvent event);

    /**
     * Called no matter how the unsubscribe happens, whether explicitly or if the node
     * unsubscribes itself.
     *
     * @param topic The topic that was passed to DSNode.subscribe.
     * @param node  Node that was passed to DSNode.subscribe, never null.
     * @param child The child that was passed to DSNode.subscribe, may be null.
     */
    public void onUnsubscribed(DSTopic topic, DSNode node, DSInfo child);

}
