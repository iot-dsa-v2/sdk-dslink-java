package org.iot.dsa.node;

import java.util.concurrent.ConcurrentLinkedQueue;
import org.iot.dsa.logging.DSLogger;

/**
 * Multiplexes events to multiple subscribers.
 *
 * @author Aaron Hansen
 */
class SubscriberAdapter extends DSLogger implements DSISubscriber {

    ///////////////////////////////////////////////////////////////////////////
    // Constants
    ///////////////////////////////////////////////////////////////////////////

    ///////////////////////////////////////////////////////////////////////////
    // Fields
    ///////////////////////////////////////////////////////////////////////////

    private ConcurrentLinkedQueue<DSISubscriber> subscribers =
            new ConcurrentLinkedQueue<DSISubscriber>();

    ///////////////////////////////////////////////////////////////////////////
    // Constructors
    ///////////////////////////////////////////////////////////////////////////

    SubscriberAdapter() {
    }

    SubscriberAdapter(DSISubscriber first) {
        subscribers.add(first);
    }

    ///////////////////////////////////////////////////////////////////////////
    // Methods in alphabetical order
    ///////////////////////////////////////////////////////////////////////////

    public boolean isEmpty() {
        return subscribers.isEmpty();
    }

    public void onEvent(DSIObject publisher, DSInfo child, DSIPublisher.Event event) {
        for (DSISubscriber subscriber : subscribers) {
            try {
                subscriber.onEvent(publisher, child, event);
            } catch (Exception x) {
                severe(subscriber.toString(), x);
            }
        }
    }

    public void subscribe(DSISubscriber subscriber) {
        if (!subscribers.contains(subscriber)) {
            subscribers.add(subscriber);
        }
    }

    public void unsubscribe(DSISubscriber subscriber) {
        subscribers.remove(subscriber);
    }

    ///////////////////////////////////////////////////////////////////////////
    // Inner Classes
    ///////////////////////////////////////////////////////////////////////////

    ///////////////////////////////////////////////////////////////////////////
    // Initialization
    ///////////////////////////////////////////////////////////////////////////

}
