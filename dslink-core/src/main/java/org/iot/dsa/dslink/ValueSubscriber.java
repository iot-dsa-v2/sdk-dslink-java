package org.iot.dsa.dslink;

import org.iot.dsa.dslink.responder.InboundSubscribeRequest;
import org.iot.dsa.dslink.responder.SubscriptionCloseHandler;
import org.iot.dsa.node.*;
import org.iot.dsa.node.DSIPublisher.Event;

/**
 * Used by DSRootNode to handle value subscriptions.
 *
 * @author Aaron Hansen
 */
class ValueSubscriber implements DSISubscriber, SubscriptionCloseHandler {

    ///////////////////////////////////////////////////////////////////////////
    // Constants
    ///////////////////////////////////////////////////////////////////////////

    ///////////////////////////////////////////////////////////////////////////
    // Fields
    ///////////////////////////////////////////////////////////////////////////

    private DSInfo info;
    private DSNode parent;
    private InboundSubscribeRequest request;
    private SubscriptionType subscriptionType;
    private DSIObject target;

    ///////////////////////////////////////////////////////////////////////////
    // Constructors
    ///////////////////////////////////////////////////////////////////////////

    ValueSubscriber(RequestPath path, InboundSubscribeRequest request) {
        this.info = path.getInfo();
        this.parent = path.getParent();
        DSIObject obj = path.getTarget();
        this.target = obj;
        this.request = request;
        if (info.isNode()) {
            this.parent = info.getNode();
            info.getNode().subscribe(this);
            subscriptionType = SubscriptionType.CONTAINER;
            onEvent(obj, info, Event.PUBLISHER_CHANGED);
        } else if (info.isValue()) {
            parent.subscribe(this);
            subscriptionType = SubscriptionType.VALUE;
            onEvent(obj, info, Event.CHILD_CHANGED);
        } else if (obj instanceof DSIPublisher) {
            ((DSIPublisher) obj).subscribe(this);
            subscriptionType = SubscriptionType.PUBLISHER;
            onEvent(obj, info, Event.PUBLISHER_CHANGED);
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // Methods
    ///////////////////////////////////////////////////////////////////////////

    @Override
    public void onClose(Integer subscriptionId) {
        switch (subscriptionType) {
            case CONTAINER:
                parent.unsubscribe(this);
                break;
            case PUBLISHER:
                ((DSIPublisher) target).unsubscribe(this);
                break;
            case VALUE:
                parent.unsubscribe(this);
                break;
        }
    }

    @Override
    public void onClose(DSIObject publisher) {
        request.close();
    }

    private void onContainerEvent(DSIObject publisher, DSInfo child, DSIPublisher.Event event) {
        if (event == Event.PUBLISHER_CHANGED) {
            DSIValue value = null;
            if (publisher instanceof DSIValue) {
                value = (DSIValue) publisher;
            } else {
                value = DSString.valueOf(publisher.toString());
            }
            DSStatus quality = DSStatus.ok;
            if (value instanceof DSIStatus) {
                quality = ((DSIStatus) value).toStatus();
            }
            request.update(System.currentTimeMillis(), value, quality);
        }
    }

    @Override
    public void onEvent(DSIObject publisher, DSInfo child, DSIPublisher.Event event) {
        if (child == info) {
            switch (subscriptionType) {
                case CONTAINER:
                    onContainerEvent(publisher, child, event);
                    break;
                case PUBLISHER:
                    onPublisherEvent(publisher, child, event);
                    break;
                case VALUE:
                    onValueEvent(publisher, child, event);
                    break;
            }
        }
    }

    public void onPublisherEvent(DSIObject publisher, DSInfo child, DSIPublisher.Event event) {
        if (event == Event.PUBLISHER_CHANGED) {
            DSIValue value = null;
            if (publisher instanceof DSIValue) {
                value = (DSIValue) publisher;
            } else {
                value = DSString.valueOf(publisher.toString());
            }
            DSStatus quality = DSStatus.ok;
            if (value instanceof DSIStatus) {
                quality = ((DSIStatus) value).toStatus();
            }
            request.update(System.currentTimeMillis(), value, quality);
        }
    }

    private void onValueEvent(DSIObject publisher, DSInfo child, DSIPublisher.Event event) {
        if (event == Event.CHILD_CHANGED) {
            DSIValue value = child.getValue();
            DSStatus quality = DSStatus.ok;
            if (value instanceof DSIStatus) {
                quality = ((DSIStatus) value).toStatus();
            }
            request.update(System.currentTimeMillis(), value, quality);
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // Inner Classes
    ///////////////////////////////////////////////////////////////////////////

    enum SubscriptionType {
        CONTAINER,
        PUBLISHER,
        VALUE,
    }
}
