package com.acuity.iot.dsa.dslink.protocol.requester;

import com.acuity.iot.dsa.dslink.protocol.DSSession;
import com.acuity.iot.dsa.dslink.protocol.message.OutboundMessage;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import org.iot.dsa.DSRuntime;
import org.iot.dsa.dslink.DSIRequester;
import org.iot.dsa.dslink.DSITransport;
import org.iot.dsa.dslink.requester.OutboundInvokeHandler;
import org.iot.dsa.dslink.requester.OutboundListHandler;
import org.iot.dsa.dslink.requester.OutboundRequestHandler;
import org.iot.dsa.dslink.requester.OutboundSubscribeHandler;
import org.iot.dsa.node.DSIValue;
import org.iot.dsa.node.DSLong;
import org.iot.dsa.node.DSMap;
import org.iot.dsa.node.DSNode;

/**
 * DSA requester abstraction.
 *
 * @author Daniel Shapiro, Aaron Hansen
 */
public abstract class DSRequester extends DSNode implements DSIRequester {

    ///////////////////////////////////////////////////////////////////////////
    // Class Fields
    ///////////////////////////////////////////////////////////////////////////

    private static final String LIST_COUNT = "Lists";
    private static final String REQ_COUNT = "Total Requests";
    private static final String SUB_COUNT = "Subscriptions";

    ///////////////////////////////////////////////////////////////////////////
    // Instance Fields
    ///////////////////////////////////////////////////////////////////////////

    private Map<String, DSOutboundListStub> lists = new HashMap<>();
    private AtomicInteger nextRid = new AtomicInteger();
    private Map<Integer, DSOutboundStub> requests = new ConcurrentHashMap<>();
    private DSSession session;
    private DSOutboundSubscriptions subscriptions = makeSubscriptions();
    private DSRuntime.Timer updateTimer;

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
        if (!getSession().getConnection().isConnected()) {
            throw new IllegalStateException("Not connected");
        }
        DSOutboundInvokeStub stub = makeInvoke(path, params, req);
        requests.put(stub.getRequestId(), stub);
        req.onInit(path, params, stub);
        session.enqueueOutgoingRequest(stub);
        return req;
    }

    @Override
    public OutboundListHandler list(String path, final OutboundListHandler req) {
        DSOutboundListStub stub;
        boolean existing = false;
        synchronized (lists) {
            stub = lists.get(path);
            if (stub == null) {
                stub = makeList(path, req);
                requests.put(stub.getRequestId(), stub);
                lists.put(path, stub);
            } else {
                existing = true;
                stub.addHandler(req);
            }
        }
        if (!getSession().getConnection().isConnected()) {
            stub.disconnected();
        } else if (!existing) {
            session.enqueueOutgoingRequest(stub);
        }
        return req;
    }

    public void onConnected() {
        subscriptions.onConnected();
        synchronized (lists) {
            for (DSOutboundListStub stub : lists.values()) {
                requests.remove(stub.getRequestId());
                stub.setRequestId(getNextRid());
                requests.put(stub.getRequestId(), stub);
                session.enqueueOutgoingRequest(stub);
            }
        }
    }

    public void onDisconnected() {
        Iterator<Entry<Integer, DSOutboundStub>> it = requests.entrySet().iterator();
        Map.Entry<Integer, DSOutboundStub> me;
        while (it.hasNext()) {
            me = it.next();
            if (!(me.getValue() instanceof DSOutboundListStub)) {
                try {
                    me.getValue().getHandler().onClose();
                } catch (Exception x) {
                    error(getPath(), x);
                }
                it.remove();
            }
        }
        subscriptions.onDisconnected();
        synchronized (lists) {
            disconnectLists();
        }
    }

    @Override
    public OutboundRequestHandler remove(String path, OutboundRequestHandler req) {
        if (!getSession().getConnection().isConnected()) {
            throw new IllegalStateException("Not connected");
        }
        DSOutboundRemoveStub stub = makeRemove(path, req);
        requests.put(stub.getRequestId(), stub);
        session.enqueueOutgoingRequest(stub);
        return req;
    }

    public void removeRequest(Integer rid) {
        Object obj = requests.remove(rid);
        if (obj instanceof DSOutboundListStub) {
            DSOutboundListStub stub = (DSOutboundListStub) obj;
            lists.remove(stub.getPath());
        }
    }

    public void sendRequest(OutboundMessage res) {
        session.enqueueOutgoingRequest(res);
    }

    @Override
    public OutboundRequestHandler set(String path, DSIValue value, OutboundRequestHandler req) {
        if (!getSession().getConnection().isConnected()) {
            throw new IllegalStateException("Not connected");
        }
        DSOutboundSetStub stub = makeSet(path, value, req);
        requests.put(stub.getRequestId(), stub);
        session.enqueueOutgoingRequest(stub);
        return req;
    }

    @Override
    public OutboundSubscribeHandler subscribe(String path, DSIValue qos,
                                              OutboundSubscribeHandler req) {
        return subscriptions.subscribe(path, qos, req);
    }

    ///////////////////////////////////////////////////////////////////////////
    // Protected Methods
    ///////////////////////////////////////////////////////////////////////////

    @Override
    protected void declareDefaults() {
        super.declareDefaults();
        declareDefault(LIST_COUNT, DSLong.valueOf(0)).setReadOnly(true).setTransient(true);
        declareDefault(SUB_COUNT, DSLong.valueOf(0)).setReadOnly(true).setTransient(true);
        declareDefault(REQ_COUNT, DSLong.valueOf(0)).setReadOnly(true).setTransient(true);
    }

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

    @Override
    protected void onStopped() {
        super.onStopped();
        if (updateTimer != null) {
            updateTimer.cancel();
            updateTimer = null;
        }
    }

    @Override
    protected void onSubscribed() {
        super.onSubscribed();
        if (updateTimer == null) {
            updateTimer = DSRuntime.runAfterDelay(this::updateStats, 1000, 2000);
        }
    }

    @Override
    protected void onUnsubscribed() {
        super.onUnsubscribed();
        if (updateTimer != null) {
            updateTimer.cancel();
            updateTimer = null;
        }
    }

    protected abstract void sendClose(Integer rid);

    ///////////////////////////////////////////////////////////////////////////
    // Private Methods
    ///////////////////////////////////////////////////////////////////////////

    private void disconnectLists() {
        for (DSOutboundListStub stub : lists.values()) {
            stub.disconnected();
        }
    }

    private void updateStats() {
        put(LIST_COUNT, DSLong.valueOf(lists.size()));
        put(SUB_COUNT, DSLong.valueOf(subscriptions.size()));
        put(REQ_COUNT, DSLong.valueOf(requests.size()));
    }

}
