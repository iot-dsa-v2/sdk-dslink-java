package org.iot.dsa.node;

import org.iot.dsa.node.DSIPublisher.Event;

/**
 * Interface for receiving events from DSIPublishers.
 *
 * @author Aaron Hansen
 * @see DSIPublisher
 */
public interface DSISubscriber {

    /**
     * @param publisher The object that was subscribed to, will never be null (could be a
     *                  DSIPublisher or a DSContainer).
     * @param child     If the event does not involve a child, this will be null.
     * @param event     Describes the event, will never be null.
     */
    public void onEvent(DSIObject publisher, DSInfo child, Event event);

}
