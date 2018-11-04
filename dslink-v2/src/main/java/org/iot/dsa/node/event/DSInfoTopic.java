package org.iot.dsa.node.event;

import org.iot.dsa.node.DSMap;

/**
 * This topic is for info related events on DSNodes.
 * <p>
 * Events will be on of the enum defined in the Event inner class.  The parameters for each event
 * are defined in the documentation for each event.
 *
 * @author Aaron Hansen
 */
public class DSInfoTopic extends DSTopic {

    /**
     * The only instance of this topic.
     *
     * @see org.iot.dsa.node.DSNode#INFO_TOPIC
     */
    public static final DSInfoTopic INSTANCE = new DSInfoTopic();

    // Prevent instantiation
    private DSInfoTopic() {
    }

    /**
     * The possible events for this topic.
     */
    public enum Event implements DSIEvent {

        CHILD_ADDED,

        CHILD_REMOVED,

        METADATA_CHANGED;

        /**
         * Returns null.
         */
        @Override
        public DSMap getData() {
            return null;
        }

        @Override
        public DSTopic getTopic() {
            return INSTANCE;
        }

    }

}
