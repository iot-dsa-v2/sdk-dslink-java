package com.acuity.iot.dsa.dslink.protocol.protocol_v1.requester;

import com.acuity.iot.dsa.dslink.protocol.message.CloseMessage;
import com.acuity.iot.dsa.dslink.protocol.message.OutboundMessage;
import com.acuity.iot.dsa.dslink.protocol.protocol_v1.DS1Session;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import org.iot.dsa.dslink.DSIRequester;
import org.iot.dsa.dslink.requester.OutboundInvokeHandler;
import org.iot.dsa.dslink.requester.OutboundListHandler;
import org.iot.dsa.dslink.requester.OutboundRequestHandler;
import org.iot.dsa.dslink.requester.OutboundSubscribeHandler;
import org.iot.dsa.node.DSElement;
import org.iot.dsa.node.DSIValue;
import org.iot.dsa.node.DSMap;
import org.iot.dsa.node.DSNode;

/**
 * DSA V1 requester implementation.
 *
 * @author Daniel Shapiro, Aaron Hansen
 */
public class DS1Requester extends DSNode implements DSIRequester {

    ///////////////////////////////////////////////////////////////////////////
    // Fields
    ///////////////////////////////////////////////////////////////////////////

    private final AtomicInteger nextRid = new AtomicInteger();
    private DS1Session session;
    private final Map<Integer, DS1OutboundStub> requests =
            new ConcurrentHashMap<Integer, DS1OutboundStub>();
    private final DS1OutboundSubscriptions subscriptions =
            new DS1OutboundSubscriptions(this);

    ///////////////////////////////////////////////////////////////////////////
    // Constructors
    ///////////////////////////////////////////////////////////////////////////

    public DS1Requester(DS1Session session) {
        this.session = session;
    }

    ///////////////////////////////////////////////////////////////////////////
    // Methods
    ///////////////////////////////////////////////////////////////////////////

    int getNextRid() {
        return nextRid.incrementAndGet();
    }

    DS1Session getSession() {
        return session;
    }

    @Override
    public OutboundInvokeHandler invoke(String path, DSMap params, OutboundInvokeHandler req) {
        DS1OutboundInvokeStub stub = new DS1OutboundInvokeStub(this, getNextRid(),
                                                               path, params, req);
        requests.put(stub.getRequestId(), stub);
        req.onInit(path, params, stub);
        session.enqueueOutgoingRequest(stub);
        return req;
    }

    boolean isError(DSMap message) {
        DSElement e = message.get("error");
        if (e == null) {
            return false;
        }
        return !e.isNull();
    }

    boolean isStreamClosed(DSMap message) {
        return "closed".equals(message.getString("stream"));
    }

    @Override
    public OutboundListHandler list(String path, OutboundListHandler req) {
        DS1OutboundListStub stub = new DS1OutboundListStub(this, getNextRid(), path, req);
        requests.put(stub.getRequestId(), stub);
        req.onInit(path, stub);
        session.enqueueOutgoingRequest(stub);
        return req;
    }

    public void onConnect() {
        subscriptions.onConnect();
    }

    public void onConnectFail() {
        subscriptions.onConnectFail();
    }

    public void onDisconnect() {
        subscriptions.onDisconnect();
    }

    /**
     * Call by the parent session to handle response messages.
     */
    public void processResponse(Integer rid, DSMap map) {
        if (rid == 0) {
            subscriptions.processUpdates(map);
        } else {
            DS1OutboundStub stub = requests.get(rid);
            if (stub != null) {
                stub.handleResponse(map);
                if (isError(map)) {
                    stub.handleError(map.get("error"));
                }
                if (isStreamClosed(map)) {
                    stub.handleClose();
                    requests.remove(rid);
                }
            } else {
                if (!isStreamClosed(map)) {
                    sendClose(rid);
                }
            }
        }
    }

    @Override
    public OutboundRequestHandler remove(String path, OutboundRequestHandler req) {
        DS1OutboundRemoveStub stub = new DS1OutboundRemoveStub(this,
                                                               getNextRid(), path, req);
        requests.put(stub.getRequestId(), stub);
        session.enqueueOutgoingRequest(stub);
        return req;
    }

    void removeRequest(Integer rid) {
        requests.remove(rid);
    }

    void sendClose(Integer rid) {
        requests.remove(rid);
        sendRequest(new CloseMessage(rid));
    }

    void sendRequest(OutboundMessage res) {
        session.enqueueOutgoingRequest(res);
    }

    @Override
    public OutboundRequestHandler set(String path, DSIValue value, OutboundRequestHandler req) {
        DS1OutboundSetStub stub = new DS1OutboundSetStub(this, getNextRid(),
                                                         path, value, req);
        requests.put(stub.getRequestId(), stub);
        session.enqueueOutgoingRequest(stub);
        return req;
    }

    @Override
    public OutboundSubscribeHandler subscribe(String path, int qos, OutboundSubscribeHandler req) {
        return subscriptions.subscribe(path, qos, req);
    }

}
