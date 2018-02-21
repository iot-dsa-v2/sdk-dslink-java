package com.acuity.iot.dsa.dslink.protocol.requester;

import com.acuity.iot.dsa.dslink.DSSession;
import com.acuity.iot.dsa.dslink.protocol.message.OutboundMessage;
import com.acuity.iot.dsa.dslink.protocol.v1.CloseMessage;
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
 * DSA requester abstraction.
 *
 * @author Daniel Shapiro, Aaron Hansen
 */
public class DSRequester extends DSNode implements DSIRequester {

    ///////////////////////////////////////////////////////////////////////////
    // Fields
    ///////////////////////////////////////////////////////////////////////////

    private final AtomicInteger nextRid = new AtomicInteger();
    private DSSession session;
    private final Map<Integer, DSOutboundStub> requests =
            new ConcurrentHashMap<Integer, DSOutboundStub>();
    private final DSOutboundSubscriptions subscriptions =
            new DSOutboundSubscriptions(this);

    ///////////////////////////////////////////////////////////////////////////
    // Constructors
    ///////////////////////////////////////////////////////////////////////////

    public DSRequester(DSSession session) {
        this.session = session;
    }

    ///////////////////////////////////////////////////////////////////////////
    // Methods
    ///////////////////////////////////////////////////////////////////////////

    int getNextRid() {
        return nextRid.incrementAndGet();
    }

    DSSession getSession() {
        return session;
    }

    @Override
    public OutboundInvokeHandler invoke(String path, DSMap params, OutboundInvokeHandler req) {
        /*
        DS1OutboundInvokeStub stub = new DS1OutboundInvokeStub(this, getNextRid(),
                                                               path, params, req);
        requests.put(stub.getRequestId(), stub);
        req.onInit(path, params, stub);
        session.enqueueOutgoingRequest(stub);
        */
        return req;
    }

    @Override
    public OutboundListHandler list(String path, OutboundListHandler req) {
        /*
        DS1OutboundListStub stub = new DS1OutboundListStub(this, getNextRid(), path, req);
        requests.put(stub.getRequestId(), stub);
        req.onInit(path, stub);
        session.enqueueOutgoingRequest(stub);
        */
        return req;
    }

    public void onConnect() {
        subscriptions.onConnect();
        session.setRequesterAllowed();
    }

    public void onConnectFail() {
        subscriptions.onConnectFail();
    }

    public void onDisconnect() {
        subscriptions.onDisconnect();
    }

    @Override
    public OutboundRequestHandler remove(String path, OutboundRequestHandler req) {
        /*
        DS1OutboundRemoveStub stub = new DS1OutboundRemoveStub(this,
                                                               getNextRid(), path, req);
        requests.put(stub.getRequestId(), stub);
        session.enqueueOutgoingRequest(stub);
        */
        return req;
    }

    void removeRequest(Integer rid) {
        requests.remove(rid);
    }

    void sendClose(Integer rid) {
        requests.remove(rid);
        sendRequest(new CloseMessage(rid, true));
    }

    void sendRequest(OutboundMessage res) {
        session.enqueueOutgoingRequest(res);
    }

    @Override
    public OutboundRequestHandler set(String path, DSIValue value, OutboundRequestHandler req) {
        /*
        DS1OutboundSetStub stub = new DS1OutboundSetStub(this, getNextRid(),
                                                         path, value, req);
        requests.put(stub.getRequestId(), stub);
        session.enqueueOutgoingRequest(stub);
        */
        return req;
    }

    @Override
    public OutboundSubscribeHandler subscribe(String path, int qos, OutboundSubscribeHandler req) {
        return subscriptions.subscribe(path, qos, req);
    }

}
