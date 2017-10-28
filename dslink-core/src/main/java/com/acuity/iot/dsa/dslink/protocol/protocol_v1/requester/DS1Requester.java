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
import org.iot.dsa.dslink.requester.OutboundRemoveRequest;
import org.iot.dsa.dslink.requester.OutboundSetRequest;
import org.iot.dsa.dslink.requester.OutboundSubscribeRequest;
import org.iot.dsa.dslink.requester.OutboundSubscribeStub;
import org.iot.dsa.node.DSElement;
import org.iot.dsa.node.DSMap;
import org.iot.dsa.node.DSNode;

public class DS1Requester extends DSNode implements DSIRequester {

    ///////////////////////////////////////////////////////////////////////////
    // Fields
    ///////////////////////////////////////////////////////////////////////////

    private final AtomicInteger nextRid = new AtomicInteger();
    private DS1Session session;
    private final Map<Integer, DS1OutboundRequestStub> requests =
            new ConcurrentHashMap<Integer, DS1OutboundRequestStub>();
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
        DS1OutboundInvokeStub stub = new DS1OutboundInvokeStub(this,
                                                               getNextRid(),
                                                               path,
                                                               params,
                                                               req);
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
        return stub;
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
            DS1OutboundRequestStub stub = requests.get(rid);
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
    public void remove(OutboundRemoveRequest req) {
        DS1OutboundRemoveStub stub = new DS1OutboundRemoveStub(req);
        stub.setRequester(this).setRequestId(getNextRid());
        requests.put(stub.getRequestId(), stub);
        session.enqueueOutgoingRequest(stub);
    }

    void removeRequest(Integer rid) {
        requests.remove(rid);
    }

    void sendClose(int rid) {
        requests.remove(rid);
        sendRequest(new CloseMessage(rid));
    }

    void sendRequest(OutboundMessage res) {
        session.enqueueOutgoingRequest(res);
    }

    @Override
    public void set(OutboundSetRequest req) {
        DS1OutboundSetStub stub = new DS1OutboundSetStub(req);
        stub.setRequester(this).setRequestId(getNextRid());
        requests.put(stub.getRequestId(), stub);
        session.enqueueOutgoingRequest(stub);
    }

    @Override
    public OutboundSubscribeStub subscribe(OutboundSubscribeRequest request) {
        return subscriptions.subscribe(request);
    }

}
