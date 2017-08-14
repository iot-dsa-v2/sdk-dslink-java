package org.iot.dsa.dslink;

import org.iot.dsa.dslink.responder.InboundInvokeRequest;
import org.iot.dsa.dslink.responder.InboundListRequest;
import org.iot.dsa.dslink.responder.InboundSetRequest;
import org.iot.dsa.dslink.responder.InboundSubscribeRequest;
import org.iot.dsa.dslink.responder.OutboundListResponse;
import org.iot.dsa.dslink.responder.SubscriptionCloseHandler;
import org.iot.dsa.node.DSIValue;
import org.iot.dsa.node.DSInfo;
import org.iot.dsa.node.DSNode;
import org.iot.dsa.node.action.ActionResult;
import org.iot.dsa.node.action.DSAction;

/**
 * A responder that converts the node model into DSA.  Most link root nodes will subclass this.
 *
 * @author Aaron Hansen
 */
public class DSRootNode extends DSNode implements DSResponder {

    ///////////////////////////////////////////////////////////////////////////
    // Constants
    ///////////////////////////////////////////////////////////////////////////

    ///////////////////////////////////////////////////////////////////////////
    // Fields
    ///////////////////////////////////////////////////////////////////////////

    ///////////////////////////////////////////////////////////////////////////
    // Constructors
    ///////////////////////////////////////////////////////////////////////////

    ///////////////////////////////////////////////////////////////////////////
    // Methods
    ///////////////////////////////////////////////////////////////////////////

    @Override
    public ActionResult onInvoke(InboundInvokeRequest request) {
        RequestPath path = new RequestPath(request.getPath(), this);
        if (path.isResponder()) {
            DSResponder responder = (DSResponder) path.getTarget();
            return responder.onInvoke(new InvokeWrapper(path, request));
        }
        DSInfo info = path.getInfo();
        if (!info.isAction()) {
            throw new DSRequestException("Not an action " + path.getPath());
        }
        DSAction action = info.getAction();
        return action.invoke(info, request);
    }

    @Override
    public OutboundListResponse onList(InboundListRequest request) {
        RequestPath path = new RequestPath(request.getPath(), this);
        if (path.isResponder()) {
            DSResponder responder = (DSResponder) path.getTarget();
            return responder.onList(new ListWrapper(path.getPath(), request));
        }
        return new ListSubscriber(path, request);
    }

    @Override
    public SubscriptionCloseHandler onSubscribe(InboundSubscribeRequest request) {
        RequestPath path = new RequestPath(request.getPath(), this);
        if (path.isResponder()) {
            DSResponder responder = (DSResponder) path.getTarget();
            return responder.onSubscribe(new SubscribeWrapper(path.getPath(), request));
        }
        return new ValueSubscriber(path, request);
    }


    @Override
    public void onSet(InboundSetRequest request) {
        RequestPath path = new RequestPath(request.getPath(), this);
        if (path.isResponder()) {
            DSResponder responder = (DSResponder) path.getTarget();
            responder.onSet(new SetWrapper(path.getPath(), request));
        }
        DSNode parent = path.getParent();
        DSInfo info = path.getInfo();
        DSIValue value = info.getValue();
        if (value == null) {
            if (info.getDefaultObject() instanceof DSIValue) {
                value = (DSIValue) info.getDefaultObject();
            }
        }
        if (value != null) {
            value = value.decode(request.getValue());
        } else if (request.getValue() instanceof DSIValue) {
            value = (DSIValue) request.getValue();
        } else {
            throw new DSRequestException("Cannot decode value: " + parent.getPath());
        }
        parent.put(info.getName(), value);
    }

    ///////////////////////////////////////////////////////////////////////////
    // Inner Classes
    ///////////////////////////////////////////////////////////////////////////

}
