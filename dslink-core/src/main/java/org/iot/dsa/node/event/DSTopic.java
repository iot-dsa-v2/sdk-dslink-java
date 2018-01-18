package org.iot.dsa.node.event;

import org.iot.dsa.node.DSIObject;
import org.iot.dsa.node.DSInfo;

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
 * @see DSISubscriber
 * @see DSInfoTopic
 * @see DSValueTopic
 * @see org.iot.dsa.node.DSNode#subscribe(DSInfo, DSISubscriber, DSTopic)
 */
public class DSTopic implements DSIObject {

    /**
     * Returns this.
     */
    @Override
    public DSTopic copy() {
        return this;
    }

    /**
     * Returns false, override to return true if the topic is child specific.
     */
    public boolean isChildSpecific() {
        return false;
    }

    /**
     * Only test instance equality.
     */
    @Override
    public boolean isEqual(Object obj) {
        return obj == this;
    }

    /**
     * False
     */
    @Override
    public boolean isNull() {
        return false;
    }

}
