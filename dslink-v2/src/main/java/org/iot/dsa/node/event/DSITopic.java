package org.iot.dsa.node.event;

import org.iot.dsa.node.DSIObject;
import org.iot.dsa.node.DSIValue;
import org.iot.dsa.node.DSInfo;

/**
 * DSISubscribers subscribe to DSITopics on DSNodes.
 * <p>
 * Topics can be mounted children of a node or simply implied like the default set of topics
 * available on every node.
 *
 * @see DSIEvent
 * @see DSISubscriber
 * @see DSNodeTopic
 * @see org.iot.dsa.node.DSNode#subscribe(DSITopic, DSInfo, DSIValue, DSISubscriber)
 */
public interface DSITopic extends DSIObject {

}
