package org.iot.dsa.node.event;

import org.iot.dsa.node.DSInfo;
import org.iot.dsa.node.DSNode;

/**
 * DSISubscribers subscribe to DSTopics on DSNodes.
 * <p>
 * A single topic can emanate multiple types of events.  The event object is an empty interface,
 * each topic will define it's own events.
 * <p>
 * Events can also supply parameters.  The parameters specific to each kind of event.
 * <p>
 * See the class documentation of a specific topic to understand the possible events and their
 * parameters.
 * <p>
 * The two most important topics are built into every node, they are: DSValueTopic and DSInfoTopic.
 *
 * @see DSIEvent
 * @see DSTopic
 * @see org.iot.dsa.node.DSNode#subscribe(DSInfo, DSISubscriber, DSTopic)
 */
public interface DSISubscriber {

    /**
     * Subscription callback.
     *
     * @param node   Required, node subscribed to.
     * @param child  Optional, if the event concerns a child.
     * @param topic  Required, the topic emanating the event.
     * @param event  Required, the actual event.
     * @param params Can be null, only used if the event defines it.
     */
    public void onEvent(DSNode node, DSInfo child, DSTopic topic, DSIEvent event, Object... params);

    /**
     * Called no matter how the unsubscribe happens, whether explicitly or if the node
     * unsubscribes itself.
     *
     * @param node  Node that was passed to DSNode.subscribe, never null.
     * @param child The child that was passed to DSNode.subscribe, may be null.
     * @param topic The topic that was passed to DSNode.subscribe.
     */
    public void onUnsubscribed(DSNode node, DSInfo child, DSTopic topic);

}
