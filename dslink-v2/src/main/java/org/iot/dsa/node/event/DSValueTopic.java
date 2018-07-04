package org.iot.dsa.node.event;

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
     * The possible events from this topic.
     */
    public enum Event implements DSIEvent {
        /**
         * For DSNodes that implement DSIValue.  The DSInfo arg to onEvent must be null.
         */
        NODE_CHANGED,
        /**
         * For node value children (who are not also nodes).  The DSInfo arg must not be null.
         */
        CHILD_CHANGED
    }

}
