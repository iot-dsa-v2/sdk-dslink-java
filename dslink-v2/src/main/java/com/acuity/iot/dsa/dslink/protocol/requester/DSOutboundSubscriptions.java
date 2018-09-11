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
import org.iot.dsa.logging.DSLogger;
import org.iot.dsa.node.DSElement;
import org.iot.dsa.node.DSNull;
import org.iot.dsa.node.DSStatus;
import org.iot.dsa.time.DSDateTime;

/**
 * Manages all subscriptions for a requester.
 *
 * @author Aaron Hansen
 */
public class DSOutboundSubscriptions extends DSLogger implements OutboundMessage {

    ///////////////////////////////////////////////////////////////////////////
    // Class Fields
    ///////////////////////////////////////////////////////////////////////////

    static final int MAX_SID = 2147483647;

    ///////////////////////////////////////////////////////////////////////////
    // Instance Fields
    ///////////////////////////////////////////////////////////////////////////

    private final ConcurrentLinkedQueue<DSOutboundSubscription> pendingSubscribe =
            new ConcurrentLinkedQueue<DSOutboundSubscription>();
    private final ConcurrentLinkedQueue<DSOutboundSubscription> pendingUnsubscribe =
            new ConcurrentLinkedQueue<DSOutboundSubscription>();
    private final Map<String, DSOutboundSubscription> pathMap =
            new ConcurrentHashMap<String, DSOutboundSubscription>();
    private final Map<Integer, DSOutboundSubscription> sidMap =
            new ConcurrentHashMap<Integer, DSOutboundSubscription>();
    private boolean connected = false;
    private boolean enqueued = false;
    private int nextSid = 1;
    private DSRequester requester;

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
            timestamp = DSDateTime.currentTime();
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
        stub.process(timestamp, value, status);
    }

    @Override
    public void write(DSSession session, MessageWriter writer) {
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
                    doWriteSubscribe(writer, sub.getPath(), sub.getSid(), sub.getQos());
                    sub.setState(State.SUBSCRIBED);
                }
                it.remove();
            }
            doEndMessage(writer);
        }
        if (!pendingUnsubscribe.isEmpty() && !session.shouldEndMessage()) {
            debug(debug() ? "Sending unsubscribe requests" : null);
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
        synchronized (pathMap) {
            for (DSOutboundSubscription sub : pathMap.values()) {
                sub.setState(State.PENDING_SUBSCRIBE);
                    pendingSubscribe.add(sub);
            }
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
    }

    ///////////////////////////////////////////////////////////////////////////
    // Package / Private Methods
    ///////////////////////////////////////////////////////////////////////////

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

    /**
     * Create or update a subscription.
     */
    OutboundSubscribeHandler subscribe(String path, int qos, OutboundSubscribeHandler req) {
        trace(trace() ? String.format("Subscribe (qos=%s) %s", qos, path) : null);
        DSOutboundSubscribeStub stub = new DSOutboundSubscribeStub(path, qos, req);
        DSOutboundSubscription sub = null;
        synchronized (pathMap) {
            sub = pathMap.get(path);
            if (sub == null) {
                sub = new DSOutboundSubscription(path, this);
                sub.add(stub);
                pathMap.put(path, sub);
                if (connected) {
                    pendingSubscribe.add(sub);
                }
            } else {
                sub.add(stub);
                sub.setState(State.PENDING_SUBSCRIBE);
                if (connected) {
                    pendingSubscribe.add(sub);
                }
            }
        }
        try {
            req.onInit(path, sub.getQos(), stub);
        } catch (Exception x) {
            error(path, x);
        }
        if (connected) {
            sendMessage();
        }
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

}
