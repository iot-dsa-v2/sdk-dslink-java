package org.iot.dsa.node.event;

import org.iot.dsa.node.DSIValue;
import org.iot.dsa.node.DSInfo;
import org.iot.dsa.node.DSNode;

/**
 * DSISubscribers subscribe to DSEvents on DSNodes.
 * <p>
 * The two most important events are built into every node, they are: DSValueEvent and DSInfoEvent.
 *
 * @author Aaron Hansen
 * @see DSEvent
 * @see DSNode#subscribe(DSISubscriber)
 */
public interface DSISubscriber {

    /**
     * Called no matter how the unsubscribe happens, whether by the subscriber or the
     * the subscribee. Does nothing by default.
     */
    public default void onClosed(DSISubscription subscription) {
    }

    /**
     * Subscription callback.
     *
     * @param event Required, the event of the event.
     * @param node  Required, the node firing the event.
     * @param child Optional, if the event concerns a child.
     * @param data  Optional, if the event supplies any data.
     */
    public void onEvent(DSEvent event, DSNode node, DSInfo child, DSIValue data);

}
