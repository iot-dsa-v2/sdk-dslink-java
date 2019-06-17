package com.acuity.iot.dsa.dslink.protocol.requester;

import com.acuity.iot.dsa.dslink.protocol.DSSession;
import com.acuity.iot.dsa.dslink.protocol.message.MessageWriter;
import com.acuity.iot.dsa.dslink.protocol.message.OutboundMessage;
import com.acuity.iot.dsa.dslink.protocol.requester.DSOutboundSubscription.State;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import org.iot.dsa.dslink.requester.OutboundSubscribeHandler;
import org.iot.dsa.io.DSIWriter;
import org.iot.dsa.node.DSElement;
import org.iot.dsa.node.DSIValue;
import org.iot.dsa.node.DSNode;
import org.iot.dsa.node.DSNull;
import org.iot.dsa.node.DSStatus;
import org.iot.dsa.time.DSDateTime;

/**
 * Manages all subscriptions for a requester.
 *
 * @author Aaron Hansen
 */
public class DSOutboundSubscriptions extends DSNode implements OutboundMessage {

    ///////////////////////////////////////////////////////////////////////////
    // Class Fields
    ///////////////////////////////////////////////////////////////////////////

    static final int MAX_SID = 2147483646;

    ///////////////////////////////////////////////////////////////////////////
    // Instance Fields
    ///////////////////////////////////////////////////////////////////////////

    private boolean connected = false;
    private boolean enqueued = false;
    private int nextSid = 1;
    private final Map<String, DSOutboundSubscription> pathMap =
            new ConcurrentHashMap<String, DSOutboundSubscription>();
    private final ConcurrentLinkedQueue<DSOutboundSubscription> pendingSubscribe =
            new ConcurrentLinkedQueue<DSOutboundSubscription>();
    private final ConcurrentLinkedQueue<DSOutboundSubscription> pendingUnsubscribe =
            new ConcurrentLinkedQueue<DSOutboundSubscription>();
    private DSRequester requester;
    private final Map<Integer, DSOutboundSubscription> sidMap =
            new ConcurrentHashMap<Integer, DSOutboundSubscription>();

    ///////////////////////////////////////////////////////////////////////////
    // Constructors
    ///////////////////////////////////////////////////////////////////////////

    protected DSOutboundSubscriptions(DSRequester requester) {
        this.requester = requester;
    }

    ///////////////////////////////////////////////////////////////////////////
    // Public Methods
    ///////////////////////////////////////////////////////////////////////////

    @Override
    public boolean canWrite(DSSession session) {
        return true;
    }

    public DSRequester getRequester() {
        return requester;
    }

    public void handleUpdate(int sid, String ts, String sts, DSElement value) {
        if (sid < 0) {
            debug(debug() ? "Update missing sid" : null);
            return;
        }
        DSOutboundSubscription stub = sidMap.get(sid);
        if (stub == null) {
            debug(debug() ? ("Unexpected subscription sid " + sid) : null);
            return;
        }
        DSDateTime timestamp = null;
        if ((ts == null) || ts.isEmpty()) {
            timestamp = DSDateTime.now();
        } else {
            timestamp = DSDateTime.valueOf(ts);
        }
        DSStatus status = DSStatus.ok;
        if (sts != null) {
            status = DSStatus.valueOf(sts);
        }
        if (value == null) {
            value = DSNull.NULL;
        }
        stub.update(timestamp, value, status);
    }

    @Override
    public boolean write(DSSession session, MessageWriter writer) {
        boolean wrote = false;
        if (!pendingSubscribe.isEmpty()) {
            debug(debug() ? "Sending subscribe requests" : null);
            doBeginSubscribe(writer);
            Iterator<DSOutboundSubscription> it = pendingSubscribe.iterator();
            while (it.hasNext() && !session.shouldEndMessage()) {
                DSOutboundSubscription sub = it.next();
                if (!sub.hasSid()) {
                    synchronized (pathMap) {
                        sub.setSid(getNextSid());
                        sidMap.put(sub.getSid(), sub);
                    }
                }
                if (sub.getState() == State.PENDING_SUBSCRIBE) {
                    wrote = true;
                    doWriteSubscribe(writer, sub.getPath(), sub.getSid(), sub.getQos());
                    sub.setState(State.SUBSCRIBED);
                }
                it.remove();
            }
            doEndMessage(writer);
        }
        if (!pendingUnsubscribe.isEmpty() && !session.shouldEndMessage()) {
            debug(debug() ? "Sending unsubscribe requests" : null);
            wrote = true;
            doBeginUnsubscribe(writer);
            Iterator<DSOutboundSubscription> it = pendingUnsubscribe.iterator();
            while (it.hasNext() && !session.shouldEndMessage()) {
                DSOutboundSubscription sub = it.next();
                synchronized (pathMap) {
                    if (sub.size() == 0) {
                        pathMap.remove(sub.getPath());
                        sidMap.remove(sub.getSid());
                        doWriteUnsubscribe(writer, sub.getSid());
                    }
                }
                it.remove();
            }
            doEndMessage(writer);
        }
        enqueued = false;
        if (!pendingSubscribe.isEmpty() || !pendingUnsubscribe.isEmpty()) {
            sendMessage();
        }
        return wrote;
    }

    ///////////////////////////////////////////////////////////////////////////
    // Protected Methods
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Override point for v2.
     */
    protected void doBeginSubscribe(MessageWriter writer) {
        DSIWriter out = writer.getWriter();
        out.beginMap();
        out.key("rid").value(requester.getNextRid());
        out.key("method").value("subscribe");
        out.key("paths").beginList();
    }

    /**
     * Override point for v2.
     */
    protected void doBeginUnsubscribe(MessageWriter writer) {
        DSIWriter out = writer.getWriter();
        out.beginMap();
        out.key("rid").value(requester.getNextRid());
        out.key("method").value("unsubscribe");
        out.key("sids").beginList();
    }

    /**
     * Override point for v2.
     */
    protected void doEndMessage(MessageWriter writer) {
        DSIWriter out = writer.getWriter();
        out.endList();
        out.endMap();
    }

    /**
     * Override point for v2.
     */
    protected void doWriteSubscribe(MessageWriter writer, String path, Integer sid, int qos) {
        DSIWriter out = writer.getWriter();
        out.beginMap();
        out.key("path").value(path);
        out.key("sid").value(sid);
        if (qos > 0) {
            out.key("qos").value(qos);
        }
        out.endMap();
    }

    /**
     * Override point for v2.
     */
    protected void doWriteUnsubscribe(MessageWriter writer, Integer sid) {
        DSIWriter out = writer.getWriter();
        out.value(sid);
    }

    protected void onConnected() {
        connected = true;
        for (DSOutboundSubscription sub : pathMap.values()) {
            sub.setState(State.PENDING_SUBSCRIBE);
            pendingSubscribe.add(sub);
        }
        if (!pendingSubscribe.isEmpty()) {
            sendMessage();
        }
    }

    protected void onDisconnected() {
        connected = false;
        synchronized (pathMap) {
            pendingSubscribe.clear();
            for (DSOutboundSubscription sub : pendingUnsubscribe) {
                pathMap.remove(sub.getPath());
                sidMap.remove(sub.getSid());
            }
            pendingUnsubscribe.clear();
        }
        enqueued = false;
        updateDisconnected();
    }

    ///////////////////////////////////////////////////////////////////////////
    // Package / Private Methods
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Called by the subscription add method when a subscribe message needs to be sent because
     * it is a new subscription or there change in qos.
     */
    void sendSubscribe(DSOutboundSubscription sub) {
        if (connected && (sub.getState() != State.PENDING_SUBSCRIBE)) {
            sub.setState(State.PENDING_SUBSCRIBE);
            pendingSubscribe.add(sub);
            sendMessage();
        }
    }

    /**
     * Number of subscriptions.
     */
    int size() {
        synchronized (pathMap) {
            return pathMap.size();
        }
    }

    /**
     * Create or update a subscription.
     */
    OutboundSubscribeHandler subscribe(String path, DSIValue qos, OutboundSubscribeHandler req) {
        debug(debug() ? String.format("Subscribe (qos=%s) %s", qos, path) : null);
        DSOutboundSubscribeStub stub = new DSOutboundSubscribeStub(path, qos, req);
        DSOutboundSubscription sub = null;
        synchronized (pathMap) {
            sub = pathMap.get(path);
            if (sub == null) {
                sub = new DSOutboundSubscription(path, this);
                pathMap.put(path, sub);
            }
        }
        try {
            req.onInit(path, qos, stub);
        } catch (Exception x) {
            error(path, x);
        }
        sub.add(stub);
        return req;
    }

    /**
     * Remove the subscription and call onClose.
     */
    void unsubscribe(DSOutboundSubscription sub) {
        synchronized (pathMap) {
            if (connected) {
                if (sub.size() == 0) {
                    sub.setState(State.PENDING_UNSUBSCRIBE);
                    pendingUnsubscribe.add(sub);
                    sendMessage();
                }
            } else {
                pathMap.remove(sub.getPath());
                sidMap.remove(sub.getSid());
            }
        }
    }

    /**
     * This is already synchronized by the subscribe method.
     */
    private int getNextSid() {
        int ret = nextSid;
        if (++nextSid > MAX_SID) {
            nextSid = 1;
        }
        return ret;
    }

    private void sendMessage() {
        synchronized (this) {
            if (enqueued) {
                return;
            }
            enqueued = true;
        }
        requester.sendRequest(this);
    }

    private void updateDisconnected() {
        for (Map.Entry<String, DSOutboundSubscription> entry : pathMap.entrySet()) {
            entry.getValue().updateDisconnected();
        }
    }


}
