package org.iot.dsa.node.event;

import org.iot.dsa.node.DSIValue;
import org.iot.dsa.node.DSInfo;
import org.iot.dsa.node.DSNode;

/**
 * DSISubscribers subscribe to DSTopics on DSNodes.
 * <p>
 * The two most important topics are built into every node, they are: DSValueTopic and DSInfoTopic.
 *
 * @author Aaron Hansen
 * @see DSIEvent
 * @see DSITopic
 * @see DSNode#subscribe(DSITopic, DSInfo, DSIValue, DSISubscriber)
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
     * @param node  Required, the node firing the event.
     * @param child Optional, if the event concerns a child.
     * @param event Required, the actual event.
     */
    public void onEvent(DSNode node, DSInfo child, DSIEvent event);

}
