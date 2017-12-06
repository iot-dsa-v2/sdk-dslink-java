package org.iot.dsa.node;

import org.iot.dsa.node.DSIPublisher.Event;

/**
 * Interface for receiving event notification.
 *
 * @author Aaron Hansen
 * @see DSIPublisher
 */
public interface DSISubscriber {

    /* Future
    public DSInfo getChild();

    public Type getType();
    */

    /**
     * Notification from the publisher that the subscription is no longer valid.
     *
     * @param publisher Who is being unsubscribed.
     */
    public void onClose(DSIObject publisher);

    /**
     * @param publisher The object that was subscribed to, will never be null.
     * @param child     If the event does not involve a child, this will be null.
     * @param event     Describes the event, will never be null.
     */
    public void onEvent(DSIObject publisher, DSInfo child, Event event);

    /* Future
    public enum Event {
        CHILD_ADDED,
        CHILD_CHANGED,
        INFO_CHANGED,
        CHILD_REMOVED,
        PUBLISHER_CHANGED,
    }

    public enum Type {
        LIST,
        VALUE,
    }
    */

}
