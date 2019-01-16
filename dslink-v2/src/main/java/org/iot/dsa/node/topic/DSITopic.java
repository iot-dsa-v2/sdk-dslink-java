package org.iot.dsa.node.topic;

import org.iot.dsa.node.DSIObject;
import org.iot.dsa.node.DSNode;

/**
 * DSISubscribers subscribe to DSITopics on DSNodes.
 * <p>
 * Topics should be distinguished using getTopicId().  A node can fire multiple types of topics so
 * do not cast topics without first checking if it is safe.
 * <p>
 * The topic ID should be the simple class name except enums, which should be Enum.name().
 *
 * @see DSISubscriber
 * @see DSNode#subscribe(DSISubscriber)
 * @see DSTopic
 */
public interface DSITopic extends DSIObject {

    /**
     * If the implementation is an enum, this returns name(), otherwise it returns
     * getClass().getSimpleName()
     *
     * @see Enum#name()
     * @see Class#getSimpleName()
     */
    public default String getTopicId() {
        if (this instanceof Enum) {
            return ((Enum) this).name();
        }
        return getClass().getSimpleName();
    }

}
