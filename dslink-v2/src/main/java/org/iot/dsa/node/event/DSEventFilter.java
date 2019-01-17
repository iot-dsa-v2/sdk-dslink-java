package org.iot.dsa.node.event;

import org.iot.dsa.node.DSIValue;
import org.iot.dsa.node.DSInfo;
import org.iot.dsa.node.DSNode;

/**
 * A convenience for filtering events based on event and/or child info.  If the event is null, then
 * all events pass the filter, same with children.  Non-null values are compare using the
 * equals method.
 *
 * @author Aaron Hansen
 */
public class DSEventFilter implements DSISubscriber {

    ///////////////////////////////////////////////////////////////////////////
    // Instance Fields
    ///////////////////////////////////////////////////////////////////////////

    protected DSInfo child;
    protected DSISubscriber subscriber;
    protected DSEvent event;

    ///////////////////////////////////////////////////////////////////////////
    // Constructors
    ///////////////////////////////////////////////////////////////////////////

    /**
     * @param subscriber Required.
     */
    public DSEventFilter(DSISubscriber subscriber) {
        if (subscriber == null) {
            throw new NullPointerException("Subscriber cannot be null");
        }
        this.subscriber = subscriber;
    }

    /**
     * If the event or child are non-null, then the event values must == them.
     *
     * @param subscriber Required.
     * @param event      Optional.
     * @param child      Optional.
     */
    public DSEventFilter(DSISubscriber subscriber, DSEvent event, DSInfo child) {
        this.subscriber = subscriber;
        this.event = event;
        this.child = child;
    }

    ///////////////////////////////////////////////////////////////////////////
    // Public Methods
    ///////////////////////////////////////////////////////////////////////////

    @Override
    public void onEvent(DSEvent event, DSNode node, DSInfo child, DSIValue data) {
        if (this.event != null) {
            if (!this.event.equals(event)) {
                return;
            }
        }
        if (this.child != null) {
            if (child == null) {
                return;
            }
            if (!this.child.equals(child)) {
                return;
            }
        }
        subscriber.onEvent(event, node, child, data);
    }

}
