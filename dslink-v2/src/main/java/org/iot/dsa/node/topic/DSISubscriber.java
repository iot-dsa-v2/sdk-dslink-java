package org.iot.dsa.node.topic;

import org.iot.dsa.node.DSIValue;
import org.iot.dsa.node.DSInfo;
import org.iot.dsa.node.DSNode;

/**
 * DSISubscribers subscribe to DSTopics on DSNodes.
 * <p>
 * The two most important topics are built into every node, they are: DSValueTopic and DSInfoTopic.
 *
 * @author Aaron Hansen
 * @see DSITopic
 * @see DSNode#subscribe(DSISubscriber)
 */
public interface DSISubscriber {

    /**
     * Called no matter how the unsubscribe happens, whether explicitly or if the node
     * closes it itself.  Does nothing by default.
     */
    public default void onClosed(DSISubscription subscription) {
    }

    /**
     * Subscription callback.
     *
     * @param topic Required, the topic of the event.
     * @param node  Required, the node firing the event.
     * @param child Optional, if the event concerns a child.
     * @param data  Optional, if the topic supplies any data.
     */
    public void onEvent(DSITopic topic, DSNode node, DSInfo child, DSIValue data);

}
