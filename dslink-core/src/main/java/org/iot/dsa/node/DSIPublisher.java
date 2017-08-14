package org.iot.dsa.node;

/**
 * DSEvent producer.
 *
 * <p>
 *
 * Containers do not notify parent containers of any events.  However, if a container subtype
 * implements this interface, parent containers will treat all events emitted as a child changed
 * event.
 *
 * <p>
 *
 * Mutable DSIValues such as DSFlexEnum should implement this interface. However, DSList and DSMap
 * do not.
 *
 * <p>
 *
 * Containers will not publish events if they are stopped.
 *
 * @author Aaron Hansen
 * @see DSISubscriber
 */
public interface DSIPublisher {

    /**
     * Subscribes the argument, has no effect if the argument is already subscribed.
     */
    public void subscribe(DSISubscriber subscriber);

    /**
     * Unsubscribes the argument, has no effect if the argument is not currently subscribed.
     */
    public void unsubscribe(DSISubscriber subscriber);

    /**
     * Passed to DSISubscriber onEvent to describe the event.
     */
    public enum Event {

        /**
         * The event will have a source (parent) and an info.
         */
        CHILD_ADDED,

        /**
         * The event will have a source (parent) and an info.
         */
        CHILD_CHANGED,

        /**
         * The event will have a source (parent) and an info.
         */
        CHILD_REMOVED,

        /**
         * The info of a child changed.  The event will have a source (parent) and an info.
         */
        INFO_CHANGED,

        /**
         * The publisher was mutated in such a way that did not involve a child.
         */
        PUBLISHER_CHANGED,

    }
}
