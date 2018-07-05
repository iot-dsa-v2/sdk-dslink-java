package org.iot.dsa.node.event;

import org.iot.dsa.node.DSInfo;
import org.iot.dsa.node.DSNode;

/**
 * This is an empty interface, DSTopics are allowed to define events however they wish.
 *
 * @author Aaron Hansen
 * @see DSISubscriber#onEvent(DSTopic, DSIEvent, DSNode, DSInfo, Object...)
 * @see DSTopic
 */
public interface DSIEvent {

}
