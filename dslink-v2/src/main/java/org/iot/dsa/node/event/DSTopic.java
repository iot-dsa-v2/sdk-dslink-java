package org.iot.dsa.node.event;

import org.iot.dsa.node.DSIObject;
import org.iot.dsa.node.DSInfo;

/**
 * DSISubscribers subscribe to DSTopics on DSNodes.
 * <p>
 * A single topic can emanate multiple types of events.  See the class documentation of a specific
 * topic to understand the possible events and their parameters.
 * <p>
 * The two most important topics are built into every node, they are: DSValueTopic and DSInfoTopic.
 *
 * @see DSIEvent
 * @see DSISubscriber
 * @see org.iot.dsa.node.DSNode#subscribe(DSTopic, DSInfo, DSISubscriber)
 * @see org.iot.dsa.node.DSNode#INFO_TOPIC
 * @see org.iot.dsa.node.DSNode#VALUE_TOPIC
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
