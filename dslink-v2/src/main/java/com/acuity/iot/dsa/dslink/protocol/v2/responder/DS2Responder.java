package com.acuity.iot.dsa.dslink.protocol.v2.responder;

import com.acuity.iot.dsa.dslink.protocol.DSStream;
import com.acuity.iot.dsa.dslink.protocol.responder.DSInboundRequest;
import com.acuity.iot.dsa.dslink.protocol.responder.DSInboundSet;
import com.acuity.iot.dsa.dslink.protocol.responder.DSInboundSubscription;
import com.acuity.iot.dsa.dslink.protocol.responder.DSInboundSubscriptions;
import com.acuity.iot.dsa.dslink.protocol.responder.DSResponder;
import com.acuity.iot.dsa.dslink.protocol.v2.CloseMessage;
import com.acuity.iot.dsa.dslink.protocol.v2.DS2MessageReader;
import com.acuity.iot.dsa.dslink.protocol.v2.DS2Session;
import com.acuity.iot.dsa.dslink.protocol.v2.MessageConstants;
import com.acuity.iot.dsa.dslink.transport.DSBinaryTransport;
import org.iot.dsa.DSRuntime;
import org.iot.dsa.node.DSBytes;
import org.iot.dsa.node.DSElement;
import org.iot.dsa.node.DSMap;
import org.iot.dsa.node.DSPath;
import org.iot.dsa.security.DSPermission;

/**
 * Implementation for DSA v2.
 *
 * @author Aaron Hansen
 */
public class DS2Responder extends DSResponder implements MessageConstants {

    ///////////////////////////////////////////////////////////////////////////
    // Fields
    ///////////////////////////////////////////////////////////////////////////

    private DS2InboundSubscriptions subscriptions = new DS2InboundSubscriptions(this);

    /////////////////////////////////////////////////////////////////
    // Methods - Constructors
    /////////////////////////////////////////////////////////////////

    public DS2Responder() {}

    public DS2Responder(DS2Session session) {
        super(session);
    }

    /////////////////////////////////////////////////////////////////
    // Methods - In alphabetical order by method name.
    /////////////////////////////////////////////////////////////////

    @Override
    protected DSInboundSubscriptions getSubscriptions() {
        return subscriptions;
    }

    public DSBinaryTransport getTransport() {
        return (DSBinaryTransport) getConnection().getTransport();
    }

    @Override
    public boolean isV1() {
        return false;
    }

    /**
     * Process an individual request.
     */
    public void handleRequest(DS2MessageReader reader) {
        switch (reader.getMethod()) {
            case MSG_CLOSE:
                processClose(reader);
                break;
            case MSG_INVOKE_REQ:
                processInvoke(reader);
                break;
            case MSG_LIST_REQ:
                processList(reader);
                break;
            case MSG_OBSERVE_REQ:
                break;
            case MSG_SET_REQ:
                processSet(reader);
                break;
            case MSG_SUBSCRIBE_REQ:
                processSubscribe(reader);
                break;
            default:
                throw new IllegalArgumentException("Unexpected method: " + reader.getMethod());
        }
    }

    public void onConnected() {
    }

    public void onConnectFail() {
    }

    /**
     * Handles an invoke request.
     */
    private void processClose(DS2MessageReader msg) {
        int rid = msg.getRequestId();
        DSStream stream = getRequests().get(rid);
        if (stream != null) {
            stream.onClose(rid);
        } else {
            subscriptions.unsubscribe(rid);
        }
    }

    /**
     * Handles an invoke request.
     */
    private void processInvoke(DS2MessageReader msg) {
        int rid = msg.getRequestId();
        DSMap params = null;
        if (msg.getBodyLength() > 0) {
            params = msg.getBodyReader().getMap();
        }
        DSPermission perm = DSPermission.CONFIG;
        Object obj = msg.getHeader(HDR_MAX_PERMISSION);
        if (obj != null) {
            perm = DSPermission.valueOf(obj.hashCode());
        }
        boolean stream = msg.getHeader(HDR_NO_STREAM) == null;
        DS2InboundInvoke invokeImpl = new DS2InboundInvoke(params, perm);
        invokeImpl.setStream(stream)
                  .setPath((String) msg.getHeader(HDR_TARGET_PATH))
                  .setSession(getSession())
                  .setRequestId(rid)
                  .setResponder(this);
        putRequest(rid, invokeImpl);
        DSRuntime.run(invokeImpl);
    }

    /**
     * Handles a list request.
     */
    private void processList(DS2MessageReader msg) {
        int rid = msg.getRequestId();
        String path = (String) msg.getHeader(HDR_TARGET_PATH);
        boolean stream = msg.getHeader(HDR_NO_STREAM) == null;
        DS2InboundList listImpl = new DS2InboundList();
        listImpl.setStream(stream)
                .setPath(path)
                .setSession(getSession())
                .setRequestId(rid)
                .setResponder(this);
        putRequest(listImpl.getRequestId(), listImpl);
        DSRuntime.run(listImpl);
    }

    /**
     * Handles a set request.
     */
    private void processSet(DS2MessageReader msg) {
        int rid = msg.getRequestId();
        DSPermission perm = DSPermission.CONFIG;
        Object obj = msg.getHeader(HDR_MAX_PERMISSION);
        if (obj != null) {
            perm = DSPermission.valueOf(obj.hashCode());
        }
        int metaLen = DSBytes.readShort(msg.getBody(), false);
        if (metaLen > 0) {
            //what to do with it?
            msg.getBodyReader().getElement();
        }
        DSElement value = msg.getBodyReader().getElement();
        DSInboundSet setImpl = new DSInboundSet(value, perm);
        String path = (String) msg.getHeader(HDR_TARGET_PATH);
        String attr = (String) msg.getHeader(HDR_ATTRIBUTE_FIELD);
        if ((attr != null) && !attr.isEmpty()) {
            path = DSPath.concat(path, attr, null).toString();
        }
        setImpl.setPath(path)
               .setSession(getSession())
               .setRequestId(rid)
               .setResponder(this);
        DSRuntime.run(setImpl);
    }

    /**
     * Handles a subscribe request.
     */
    private void processSubscribe(DS2MessageReader msg) {
        Integer sid = msg.getRequestId();
        String path = (String) msg.getHeader(HDR_TARGET_PATH);
        Number qos = (Number) msg.getHeader(HDR_QOS);
        if (qos == null) {
            qos = Integer.valueOf(0);
        }
        //Integer queueSize = (Integer) msg.getHeader(MessageConstants.HDR_QUEUE_SIZE);
        DSInboundSubscription sub = subscriptions.subscribe(sid, path, qos.intValue());
        if (msg.getHeader(HDR_NO_STREAM) != null) {
            sub.setCloseAfterUpdate(true);
        }
    }

    @Override
    public void sendClose(int rid) {
        sendResponse(new CloseMessage(rid));
    }

    @Override
    public void sendError(DSInboundRequest req, Throwable reason) {
        sendResponse(new ErrorMessage(req, reason));
    }


}
