package org.iot.dsa.dslink;

import org.iot.dsa.DSRuntime;
import org.iot.dsa.dslink.responder.InboundInvokeRequest;
import org.iot.dsa.dslink.responder.InboundListRequest;
import org.iot.dsa.dslink.responder.InboundSetRequest;
import org.iot.dsa.dslink.responder.InboundSubscribeRequest;
import org.iot.dsa.dslink.responder.OutboundListResponse;
import org.iot.dsa.dslink.responder.SubscriptionCloseHandler;
import org.iot.dsa.node.DSIValue;
import org.iot.dsa.node.DSInfo;
import org.iot.dsa.node.DSNode;
import org.iot.dsa.node.action.ActionInvocation;
import org.iot.dsa.node.action.ActionResult;
import org.iot.dsa.node.action.DSAction;

/**
 * A DSNode and DSResponder that converts the node model into DSA.  Most links will subclass this
 * and override declareDefaults() to bind their application logic.
 *
 * @author Aaron Hansen
 */
public class DSRootNode extends DSNode implements DSResponder {

    ///////////////////////////////////////////////////////////////////////////
    // Constants
    ///////////////////////////////////////////////////////////////////////////

    static final String SAVE = "Save";

    ///////////////////////////////////////////////////////////////////////////
    // Fields
    ///////////////////////////////////////////////////////////////////////////

    private DSInfo save = getInfo(SAVE);

    ///////////////////////////////////////////////////////////////////////////
    // Constructors
    ///////////////////////////////////////////////////////////////////////////

    ///////////////////////////////////////////////////////////////////////////
    // Methods
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Adds the save action, overrides should call super if they want this action.
     */
    @Override
    protected void declareDefaults() {
        declareDefault(SAVE, new DSAction()).setConfig(true);
    }

    /**
     * The parent link or null.
     */
    public DSLink getLink() {
        return (DSLink) getParent();
    }

    public ActionResult onInvoke(DSInfo actionInfo, ActionInvocation invocation) {
        if (actionInfo == save) {
            DSRuntime.run(new Runnable() {
                @Override
                public void run() {
                    getLink().saveNodes();
                }
            });
            return null;
        }
        return super.onInvoke(actionInfo, invocation);
    }

    /**
     * Responder implementation.  If one of the children in the path implements DSResponder, it will
     * be given responsibility for completing the request.
     */
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

    /**
     * Responder implementation.  If one of the children in the path implements DSResponder, it will
     * be given responsibility for completing the request.
     */
    @Override
    public OutboundListResponse onList(InboundListRequest request) {
        RequestPath path = new RequestPath(request.getPath(), this);
        if (path.isResponder()) {
            DSResponder responder = (DSResponder) path.getTarget();
            return responder.onList(new ListWrapper(path.getPath(), request));
        }
        return new ListSubscriber(path, request);
    }

    /**
     * Responder implementation.  If one of the children in the path implements DSResponder, it will
     * be given responsibility for completing the request.
     */
    @Override
    public SubscriptionCloseHandler onSubscribe(InboundSubscribeRequest request) {
        RequestPath path = new RequestPath(request.getPath(), this);
        if (path.isResponder()) {
            DSResponder responder = (DSResponder) path.getTarget();
            return responder.onSubscribe(new SubscribeWrapper(path.getPath(), request));
        }
        return new ValueSubscriber(path, request);
    }


    /**
     * Responder implementation.  If one of the children in the path implements DSResponder, it will
     * be given responsibility for completing the request.
     */
    @Override
    public void onSet(InboundSetRequest request) {
        RequestPath path = new RequestPath(request.getPath(), this);
        if (path.isResponder()) {
            DSResponder responder = (DSResponder) path.getTarget();
            responder.onSet(new SetWrapper(path.getPath(), request));
        }
        DSNode parent = path.getParent();
        DSInfo info = path.getInfo();
        if (info.isReadOnly()) {
            throw new DSRequestException("Not writable: " + getPath());
        }
        //TODO verify incoming permission
        DSIValue value = info.getValue();
        if (value == null) {
            if (info.getDefaultObject() instanceof DSIValue) {
                value = (DSIValue) info.getDefaultObject();
            }
        }
        if (value != null) {
            value = value.valueOf(request.getValue());
        } else if (request.getValue() instanceof DSIValue) {
            value = (DSIValue) request.getValue();
        } else {
            throw new DSRequestException("Cannot decode value: " + parent.getPath());
        }
        parent.onSet(info, value);
    }

    ///////////////////////////////////////////////////////////////////////////
    // Inner Classes
    ///////////////////////////////////////////////////////////////////////////

}
