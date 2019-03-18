package com.acuity.iot.dsa.dslink.protocol.requester;

import com.acuity.iot.dsa.dslink.protocol.DSSession;
import com.acuity.iot.dsa.dslink.protocol.message.OutboundMessage;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import org.iot.dsa.dslink.DSIRequester;
import org.iot.dsa.dslink.DSITransport;
import org.iot.dsa.dslink.requester.OutboundInvokeHandler;
import org.iot.dsa.dslink.requester.OutboundListHandler;
import org.iot.dsa.dslink.requester.OutboundRequestHandler;
import org.iot.dsa.dslink.requester.OutboundSubscribeHandler;
import org.iot.dsa.node.DSIValue;
import org.iot.dsa.node.DSMap;
import org.iot.dsa.node.DSNode;

/**
 * DSA requester abstraction.
 *
 * @author Daniel Shapiro, Aaron Hansen
 */
public abstract class DSRequester extends DSNode implements DSIRequester {

    ///////////////////////////////////////////////////////////////////////////
    // Instance Fields
    ///////////////////////////////////////////////////////////////////////////

    private AtomicInteger nextRid = new AtomicInteger();
    private Map<Integer, DSOutboundStub> requests = new ConcurrentHashMap<Integer, DSOutboundStub>();
    private DSSession session;
    private DSOutboundSubscriptions subscriptions = makeSubscriptions();

    ///////////////////////////////////////////////////////////////////////////
    // Constructors
    ///////////////////////////////////////////////////////////////////////////

    public DSRequester() {
    }

    public DSRequester(DSSession session) {
        this.session = session;
    }

    ///////////////////////////////////////////////////////////////////////////
    // Public Methods
    ///////////////////////////////////////////////////////////////////////////

    public DSSession getSession() {
        return session;
    }

    public DSITransport getTransport() {
        return getSession().getTransport();
    }

    @Override
    public OutboundInvokeHandler invoke(String path, DSMap params, OutboundInvokeHandler req) {
        DSOutboundInvokeStub stub = makeInvoke(path, params, req);
        requests.put(stub.getRequestId(), stub);
        req.onInit(path, params, stub);
        session.enqueueOutgoingRequest(stub);
        return req;
    }

    @Override
    public OutboundListHandler list(String path, OutboundListHandler req) {
        DSOutboundListStub stub = makeList(path, req);
        requests.put(stub.getRequestId(), stub);
        req.onInit(path, null, stub);
        session.enqueueOutgoingRequest(stub);
        return req;
    }

    public void onConnected() {
        subscriptions.onConnected();
    }

    public void onDisconnected() {
        Iterator<Entry<Integer, DSOutboundStub>> it = requests.entrySet().iterator();
        Map.Entry<Integer, DSOutboundStub> me;
        while (it.hasNext()) {
            me = it.next();
            try {
                me.getValue().getHandler().onClose();
            } catch (Exception x) {
                error(getPath(), x);
            }
            it.remove();
        }
        subscriptions.onDisconnected();
    }

    @Override
    public OutboundRequestHandler remove(String path, OutboundRequestHandler req) {
        DSOutboundRemoveStub stub = makeRemove(path, req);
        requests.put(stub.getRequestId(), stub);
        session.enqueueOutgoingRequest(stub);
        return req;
    }

    public void removeRequest(Integer rid) {
        requests.remove(rid);
    }

    public void sendRequest(OutboundMessage res) {
        session.enqueueOutgoingRequest(res);
    }

    @Override
    public OutboundRequestHandler set(String path, DSIValue value, OutboundRequestHandler req) {
        DSOutboundSetStub stub = makeSet(path, value, req);
        requests.put(stub.getRequestId(), stub);
        session.enqueueOutgoingRequest(stub);
        return req;
    }

    @Override
    public OutboundSubscribeHandler subscribe(String path, int qos, OutboundSubscribeHandler req) {
        return subscriptions.subscribe(path, qos, req);
    }

    ///////////////////////////////////////////////////////////////////////////
    // Protected Methods
    ///////////////////////////////////////////////////////////////////////////

    protected int getNextRid() {
        return nextRid.incrementAndGet();
    }

    protected DSOutboundStub getRequest(Integer rid) {
        return requests.get(rid);
    }

    protected DSOutboundSubscriptions getSubscriptions() {
        return subscriptions;
    }

    /**
     * For V2 override.
     */
    protected DSOutboundInvokeStub makeInvoke(String path, DSMap params,
                                              OutboundInvokeHandler req) {
        return new DSOutboundInvokeStub(this, getNextRid(), path, params, req);
    }

    /**
     * For V2 override.
     */
    protected DSOutboundListStub makeList(String path, OutboundListHandler req) {
        return new DSOutboundListStub(this, getNextRid(), path, req);
    }

    /**
     * For V2 override.
     */
    protected DSOutboundRemoveStub makeRemove(String path, OutboundRequestHandler req) {
        return new DSOutboundRemoveStub(this, getNextRid(), path, req);
    }

    /**
     * For V2 override.
     */
    protected DSOutboundSetStub makeSet(String path, DSIValue value, OutboundRequestHandler req) {
        return new DSOutboundSetStub(this, getNextRid(), path, value, req);
    }

    /**
     * V2 Override point.
     */
    protected DSOutboundSubscriptions makeSubscriptions() {
        return new DSOutboundSubscriptions(this);
    }

    protected abstract void sendClose(Integer rid);

}
