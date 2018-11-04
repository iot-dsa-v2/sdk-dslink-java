package org.iot.dsa.node.event;

import org.iot.dsa.node.DSMap;

/**
 * This topic is for change of value events on DSNodes.  There are two types of events,
 * NODE_CHANGED for nodes that implement DSIValue and CHILD_CHANGED for child values of the
 * subscribed node.
 * <p>
 * Events will be one of the enums defined in the Event inner class.
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
