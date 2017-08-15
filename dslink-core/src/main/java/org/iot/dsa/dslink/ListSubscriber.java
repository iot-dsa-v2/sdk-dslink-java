package org.iot.dsa.dslink;

import org.iot.dsa.dslink.responder.ApiObject;
import org.iot.dsa.dslink.responder.InboundListRequest;
import org.iot.dsa.dslink.responder.OutboundListResponse;
import org.iot.dsa.node.DSIObject;
import org.iot.dsa.node.DSIPublisher;
import org.iot.dsa.node.DSISubscriber;
import org.iot.dsa.node.DSInfo;
import org.iot.dsa.node.DSNode;

/**
 * Used by DSRootNode to subscribe to the target of a list request and update the stream when
 * changes are detected.
 *
 * @author Aaron Hansen
 */
class ListSubscriber implements DSISubscriber, OutboundListResponse {

    ///////////////////////////////////////////////////////////////////////////
    // Constants
    ///////////////////////////////////////////////////////////////////////////

    ///////////////////////////////////////////////////////////////////////////
    // Fields
    ///////////////////////////////////////////////////////////////////////////

    private DSInfo info;
    private DSNode parent;
    private InboundListRequest request;
    private ListType subscriptionType;
    private DSIObject target;

    ///////////////////////////////////////////////////////////////////////////
    // Constructors
    ///////////////////////////////////////////////////////////////////////////

    ListSubscriber(RequestPath path, InboundListRequest request) {
        this.info = path.getInfo();
        this.parent = path.getParent();
        DSIObject obj = path.getTarget();
        this.target = obj;
        this.request = request;
        if (obj instanceof DSIPublisher) {
            ((DSIPublisher) obj).subscribe(this);
            subscriptionType = ListType.PUBLISHER;
        } else if (obj instanceof DSNode) {
            ((DSNode) obj).subscribe(this);
            subscriptionType = ListType.CONTAINER;
        } else {
            subscriptionType = ListType.VALUE;
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // Methods
    ///////////////////////////////////////////////////////////////////////////

    @Override
    public ApiObject getTarget() {
        return info;
    }

    @Override
    public void onClose() {
        switch (subscriptionType) {
            case CONTAINER:
                ((DSNode) target).unsubscribe(this);
                break;
            case PUBLISHER:
                ((DSIPublisher) target).unsubscribe(this);
                break;
            default:
                ;
        }
    }

    @Override
    public void onEvent(DSIObject publisher, DSInfo child, DSIPublisher.Event event) {
        switch (event) {
            case CHILD_ADDED:
                request.childAdded(child);
                break;
            case CHILD_REMOVED:
                request.childRemoved(child);
                break;
            default:
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // Inner Classes
    ///////////////////////////////////////////////////////////////////////////

    enum ListType {
        CONTAINER,
        PUBLISHER,
        VALUE,
    }
}
