package org.iot.dsa.dslink;

import org.iot.dsa.dslink.responder.InboundSubscribeRequest;
import org.iot.dsa.dslink.responder.SubscriptionCloseHandler;
import org.iot.dsa.node.DSIStatus;
import org.iot.dsa.node.DSIValue;
import org.iot.dsa.node.DSInfo;
import org.iot.dsa.node.DSNode;
import org.iot.dsa.node.DSStatus;
import org.iot.dsa.node.DSString;
import org.iot.dsa.node.event.DSIEvent;
import org.iot.dsa.node.event.DSISubscriber;
import org.iot.dsa.node.event.DSTopic;
import org.iot.dsa.node.event.DSValueTopic.Event;

/**
 * Used process value subscriptions.
 *
 * @author Aaron Hansen
 */
class ValueSubscriber implements DSISubscriber, SubscriptionCloseHandler {

    ///////////////////////////////////////////////////////////////////////////
    // Fields
    ///////////////////////////////////////////////////////////////////////////

    private DSInfo info;
    private DSNode node;
    private InboundSubscribeRequest request;

    ///////////////////////////////////////////////////////////////////////////
    // Constructors
    ///////////////////////////////////////////////////////////////////////////

    ValueSubscriber(RequestPath path, InboundSubscribeRequest request) {
        this.request = request;
        this.info = path.getInfo();
        if (info.isNode()) {
            this.node = info.getNode();
            this.info = null;
            onEvent(node, info, DSNode.VALUE_TOPIC, Event.NODE_CHANGED, null, node);
        } else {
            this.node = path.getParent();
            onEvent(node, info, DSNode.VALUE_TOPIC, Event.CHILD_CHANGED, info.getValue(),
                    info.getValue());
        }
        node.subscribe(info, this, DSNode.VALUE_TOPIC);
    }

    ///////////////////////////////////////////////////////////////////////////
    // Methods
    ///////////////////////////////////////////////////////////////////////////

    @Override
    public void onClose(Integer subscriptionId) {
        node.unsubscribe(info, this, DSNode.VALUE_TOPIC);
    }

    @Override
    public void onEvent(DSNode node,
                        DSInfo child,
                        DSTopic topic,
                        DSIEvent event,
                        Object... params) {
        DSIValue value;
        if (info == null) {
            if (this.node instanceof DSIValue) {
                value = (DSIValue) this.node;
            } else {
                value = DSString.valueOf(this.node.toString());
            }
        } else {
            if (info.isValue()) {
                value = info.getValue();
            } else {
                value = DSString.valueOf(info.getObject().toString());
            }
        }
        DSStatus quality = DSStatus.ok;
        if (value instanceof DSIStatus) {
            quality = ((DSIStatus) value).toStatus();
        }
        request.update(System.currentTimeMillis(), value, quality);
    }

    @Override
    public void onUnsubscribed(DSNode node, DSInfo info, DSTopic topic) {
        request.close();
    }


}
