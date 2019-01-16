package org.iot.dsa.node.topic;

import org.iot.dsa.node.DSIValue;
import org.iot.dsa.node.DSInfo;
import org.iot.dsa.node.DSNode;

/**
 * A convenience for filtering events based on topic and/or child info.  If the topic is null, then
 * all topics pass the filter, same with children.  Non-null values are compare using the
 * equals method.
 *
 * @author Aaron Hansen
 */
public class EventFilter implements DSISubscriber {

    ///////////////////////////////////////////////////////////////////////////
    // Instance Fields
    ///////////////////////////////////////////////////////////////////////////

    protected DSInfo child;
    protected DSISubscriber subscriber;
    protected DSITopic topic;

    ///////////////////////////////////////////////////////////////////////////
    // Constructors
    ///////////////////////////////////////////////////////////////////////////

    /**
     * @param subscriber Required.
     */
    public EventFilter(DSISubscriber subscriber) {
        if (subscriber == null) {
            throw new NullPointerException("Subscriber cannot be null");
        }
        this.subscriber = subscriber;
    }

    /**
     * If the topic or child are non-null, then the event values must == them.
     *
     * @param subscriber Required.
     * @param topic      Optional.
     * @param child      Optional.
     */
    public EventFilter(DSISubscriber subscriber, DSITopic topic, DSInfo child) {
        this.subscriber = subscriber;
        this.topic = topic;
        this.child = child;
    }

    ///////////////////////////////////////////////////////////////////////////
    // Public Methods
    ///////////////////////////////////////////////////////////////////////////

    @Override
    public void onEvent(DSITopic topic, DSNode node, DSInfo child, DSIValue data) {
        if (this.topic != null) {
            if (!this.topic.equals(topic)) {
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
        subscriber.onEvent(topic, node, child, data);
    }

}
