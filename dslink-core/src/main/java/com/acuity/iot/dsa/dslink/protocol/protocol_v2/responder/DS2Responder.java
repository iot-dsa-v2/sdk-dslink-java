package com.acuity.iot.dsa.dslink.protocol.protocol_v2.responder;

import com.acuity.iot.dsa.dslink.protocol.DSStream;
import com.acuity.iot.dsa.dslink.protocol.message.OutboundMessage;
import com.acuity.iot.dsa.dslink.protocol.protocol_v2.DS2Session;
import com.acuity.iot.dsa.dslink.protocol.protocol_v2.MessageConstants;
import com.acuity.iot.dsa.dslink.protocol.protocol_v2.MessageReader;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;
import org.iot.dsa.dslink.DSLinkConnection;
import org.iot.dsa.node.DSNode;

/**
 * Implements DSA 1.1.2
 *
 * @author Aaron Hansen
 */
public class DS2Responder extends DSNode implements MessageConstants {

    ///////////////////////////////////////////////////////////////////////////
    // Constants
    ///////////////////////////////////////////////////////////////////////////

    ///////////////////////////////////////////////////////////////////////////
    // Fields
    ///////////////////////////////////////////////////////////////////////////

    private ConcurrentHashMap<Integer, DSStream> inboundRequests =
            new ConcurrentHashMap<Integer, DSStream>();
    private Logger logger;
    private DS2Session session;
    private DS2Responder responder;
    //private DS2InboundSubscriptions subscriptions =
    //new DS2InboundSubscriptions(this);

    /////////////////////////////////////////////////////////////////
    // Methods - Constructors
    /////////////////////////////////////////////////////////////////

    public DS2Responder(DS2Session session) {
        this.session = session;
    }

    /////////////////////////////////////////////////////////////////
    // Methods - In alphabetical order by method name.
    /////////////////////////////////////////////////////////////////

    public DSLinkConnection getConnection() {
        return session.getConnection();
    }

    @Override
    public Logger getLogger() {
        if (logger == null) {
            logger = Logger.getLogger(
                    getConnection().getLink().getLinkName() + ".responderSession");
        }
        return logger;
    }

    /*
    public DS2InboundSubscriptions getSubscriptions() {
        return subscriptions;
    }
    */

    public void onConnect() {
    }

    public void onConnectFail() {
    }

    /*
    public void onDisconnect() {
        finer(finer() ? "Close" : null);
        subscriptions.close();
        for (Map.Entry<Integer, DSStream> entry : inboundRequests.entrySet()) {
            try {
                entry.getValue().onClose(entry.getKey());
            } catch (Exception x) {
                finer(finer() ? "Close" : null, x);
            }
        }
        inboundRequests.clear();
    }
    */

    /**
     * Handles an invoke request.
     private void processInvoke(Integer rid, DSMap req) {
     DS2InboundInvoke invokeImpl = new DS2InboundInvoke(req);
     invokeImpl.setPath(getPath(req))
     .setSession(session)
     .setRequestId(rid)
     .setResponderImpl(responder)
     .setResponder(this);
     inboundRequests.put(rid, invokeImpl);
     DSRuntime.run(invokeImpl);
     }
     */

    /**
     * Handles a list request.
     private void processList(Integer rid, DSMap req) {
     DS2InboundList listImpl = new DS2InboundList();
     listImpl.setPath(getPath(req))
     .setSession(session)
     .setRequest(req)
     .setRequestId(rid)
     .setResponderImpl(responder)
     .setResponder(this);
     inboundRequests.put(listImpl.getRequestId(), listImpl);
     DSRuntime.run(listImpl);
     }
     */

    /**
     * Process an individual request.
     */
    public void processRequest(MessageReader reader) {
        switch (reader.getMethod()) {
            case MSG_INVOKE_REQ :
                break;
            case MSG_LIST_REQ :
                //processList()
                break;
            case MSG_OBSERVE_REQ :
                break;
            case MSG_SET_REQ :
                break;
            case MSG_SUBSCRIBE_REQ :
                break;
            default :
                throw new IllegalArgumentException("Unexpected method: " + reader.getMethod());
        }
    }

    /**
     * Handles a set request.
     private void processSet(Integer rid, DSMap req) {
     DS2InboundSet setImpl = new DS2InboundSet(req);
     setImpl.setPath(getPath(req))
     .setSession(session)
     .setRequestId(rid)
     .setResponderImpl(responder)
     .setResponder(this);
     DSRuntime.run(setImpl);
     }
     */

    /**
     * Handles a subscribe request.
     private void processSubscribe(int rid, DSMap req) {
     DSList list = req.getList("paths");
     if (list == null) {
     return;
     }
     String path;
     Integer sid;
     Integer qos;
     DSMap subscribe;
     for (int i = 0, len = list.size(); i < len; i++) {
     subscribe = list.getMap(i);
     path = subscribe.getString("path");
     sid = subscribe.getInt("sid");
     qos = subscribe.get("qos", 0);
     try {
     subscriptions.subscribe(responder, sid, path, qos);
     } catch (Exception x) {
     //invalid paths are very common
     fine(path, x);
     }
     }
     }
     */

    /**
     * Handles an unsubscribe request.
     * private void processUnsubscribe(int rid, DSMap req) {
     * DSList list = req.getList("sids");
     * Integer sid = null;
     * if (list != null) {
     * for (int i = 0, len = list.size(); i < len; i++) {
     * try {
     * sid = list.getInt(i);
     * subscriptions.unsubscribe(sid);
     * } catch (Exception x) {
     * fine(fine() ? "Unsubscribe: " + sid : null, x);
     * }
     * }
     * }
     * }
     */

    void removeInboundRequest(Integer requestId) {
        inboundRequests.remove(requestId);
    }

    public boolean shouldEndMessage() {
        return session.shouldEndMessage();
    }

    public void sendResponse(OutboundMessage res) {
        session.enqueueOutgoingResponse(res);
    }

    /**
     * Used throughout processRequest.
     private void throwInvalidMethod(String methodName, DSMap request) {
     String msg = "Invalid method name " + methodName;
     finest(finest() ? (msg + ": " + request.toString()) : null);
     throw new DSProtocolException(msg).setType("invalidMethod");
     }
     */

}
