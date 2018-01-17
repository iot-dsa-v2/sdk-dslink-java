package org.iot.dsa.node.event;

import org.iot.dsa.node.DSInfo;
import org.iot.dsa.node.DSNode;

public interface DSISubscriber {

    /**
     * Subscription callback.
     *
     * @param node  Required, node subscribed to.
     * @param child Optional, if the event concerns a child.
     * @param topic Required, the topic emanating the event.
     * @param event Required, the actual event.
     * @param param1 Can be null, only used if the event defines it.
     * @param param2 Can be null, only used if the event defines it.
     */
    public void onEvent(DSNode node,
                        DSInfo child,
                        DSTopic topic,
                        DSIEvent event,
                        Object param1,
                        Object param2);

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
