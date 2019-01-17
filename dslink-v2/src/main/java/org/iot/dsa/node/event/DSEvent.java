package org.iot.dsa.node.event;

import org.iot.dsa.node.DSIObject;
import org.iot.dsa.node.DSNode;

/**
 * DSISubscribers subscribe to DSNodes and can receive a variety of events.
 *
 * @author Aaron Hansen
 * @see DSISubscriber
 * @see DSISubscription
 * @see DSNode#subscribe(DSISubscriber)
 */
public class DSEvent implements DSIObject {

    private String id;

    public DSEvent(String id) {
        if (id == null) {
            throw new NullPointerException("ID cannot be null");
        }
        this.id = id;
    }

    /**
     * Returns this.
     */
    @Override
    public DSEvent copy() {
        return this;
    }

    /**
     * True if is a event IDs match.
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj instanceof DSEvent) {
            return ((DSEvent) obj).getEventId().equals(id);
        }
        return false;
    }

    /**
     *
     */
    public String getEventId() {
        return id;
    }

    /**
     * Returns the hashCode of the ID.
     */
    @Override
    public int hashCode() {
        return id.hashCode();
    }

    /**
     * Only tests instance equality.
     */
    @Override
    public boolean isEqual(Object obj) {
        return obj == this;
    }

    /**
     * False
     */
    @Override
    public boolean isNull() {
        return false;
    }

    /**
     * Returns the event ID.
     */
    @Override
    public String toString() {
        return id;
    }

}
