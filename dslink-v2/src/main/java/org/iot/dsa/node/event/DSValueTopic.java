package org.iot.dsa.node.event;

import org.iot.dsa.node.DSMap;

/**
 * This topic is for change of value events on DSNodes.  Events will not have any data.
 * If a node implements DSIValue, it can fire an event without a child info.
 *
 * @author Aaron Hansen
 */
public class DSValueTopic extends DSTopic implements DSIEvent {

    /**
     * The only instance of this topic.
     *
     * @see org.iot.dsa.node.DSNode#VALUE_TOPIC
     */
    public static final DSValueTopic INSTANCE = new DSValueTopic();

    // Prevent instantiation
    private DSValueTopic() {
    }

    /**
     * Returns null.
     */
    @Override
    public DSMap getData() {
        return null;
    }

    @Override
    public DSTopic getTopic() {
        return this;
    }

}
