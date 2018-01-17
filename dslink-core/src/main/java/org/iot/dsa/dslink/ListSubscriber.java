package org.iot.dsa.dslink;

import org.iot.dsa.dslink.responder.ApiObject;
import org.iot.dsa.dslink.responder.InboundListRequest;
import org.iot.dsa.dslink.responder.OutboundListResponse;
import org.iot.dsa.node.DSInfo;
import org.iot.dsa.node.DSNode;
import org.iot.dsa.node.event.DSIEvent;
import org.iot.dsa.node.event.DSISubscriber;
import org.iot.dsa.node.event.DSInfoTopic;
import org.iot.dsa.node.event.DSTopic;

/**
 * Used by DSRootNode to subscribe to the target of a list request and update the stream when
 * changes are detected.
 *
 * @author Aaron Hansen
 */
class ListSubscriber implements DSISubscriber, OutboundListResponse {

    ///////////////////////////////////////////////////////////////////////////
    // Fields
    ///////////////////////////////////////////////////////////////////////////

    private DSInfo info;
    private DSNode node;
    private InboundListRequest request;

    ///////////////////////////////////////////////////////////////////////////
    // Constructors
    ///////////////////////////////////////////////////////////////////////////

    ListSubscriber(RequestPath path, InboundListRequest request) {
        this.request = request;
        this.info = path.getInfo();
        if (info.isNode()) {
            this.node = info.getNode();
            node.subscribe(null, this, DSNode.INFO_TOPIC);
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
        if (node != null) {
            node.unsubscribe(null, this, DSNode.INFO_TOPIC);
        }
    }

    @Override
    public void onEvent(DSNode node,
                        DSInfo child,
                        DSTopic topic,
                        DSIEvent event,
                        Object p1,
                        Object p2) {
        switch ((DSInfoTopic.Event) event) {
            case CHILD_ADDED:
                request.childAdded(child);
                break;
            case CHILD_REMOVED:
                request.childRemoved(child);
                break;
            case METADATA_CHANGED: //TODO
                break;
            default:
        }
    }

    @Override
    public void onUnsubscribed(DSNode node, DSInfo child, DSTopic topic) {
        request.close();
    }

}
