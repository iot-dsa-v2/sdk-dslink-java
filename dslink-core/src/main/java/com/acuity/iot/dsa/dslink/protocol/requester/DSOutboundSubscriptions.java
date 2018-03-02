package com.acuity.iot.dsa.dslink.protocol.requester;

import com.acuity.iot.dsa.dslink.DSSession;
import com.acuity.iot.dsa.dslink.protocol.message.MessageWriter;
import com.acuity.iot.dsa.dslink.protocol.message.OutboundMessage;
import com.acuity.iot.dsa.dslink.protocol.requester.DSOutboundSubscribeStubs.State;
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
    // Constants
    ///////////////////////////////////////////////////////////////////////////

    static final int MAX_SID = 2147483647;

    ///////////////////////////////////////////////////////////////////////////
    // Fields
    ///////////////////////////////////////////////////////////////////////////

    private boolean enqueued = false;
    private int nextSid = 1;
    private final ConcurrentLinkedQueue<DSOutboundSubscribeStubs> pendingSubscribe =
            new ConcurrentLinkedQueue<DSOutboundSubscribeStubs>();
    private final ConcurrentLinkedQueue<DSOutboundSubscribeStubs> pendingUnsubscribe =
            new ConcurrentLinkedQueue<DSOutboundSubscribeStubs>();
    private final Map<String, DSOutboundSubscribeStubs> pathMap =
            new ConcurrentHashMap<String, DSOutboundSubscribeStubs>();
    private DSRequester requester;
    private final Map<Integer, DSOutboundSubscribeStubs> sidMap =
            new ConcurrentHashMap<Integer, DSOutboundSubscribeStubs>();

    ///////////////////////////////////////////////////////////////////////////
    // Constructors
    ///////////////////////////////////////////////////////////////////////////

    protected DSOutboundSubscriptions(DSRequester requester) {
        this.requester = requester;
    }

    ///////////////////////////////////////////////////////////////////////////
    // Methods in alphabetical order
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

    protected DSRequester getRequester() {
        return requester;
    }

    public void onConnect() {
    }

    public void onConnectFail() {
    }

    public void onDisconnect() {
    }

    public void handleUpdate(int sid, String ts, String sts, DSElement value) {
        if (sid < 0) {
            debug(debug() ? "Update missing sid" : null);
            return;
        }
        DSOutboundSubscribeStubs stub = sidMap.get(sid);
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
        DSOutboundSubscribeStub stub = new DSOutboundSubscribeStub(path, qos, req);
        DSOutboundSubscribeStubs stubs = null;
        synchronized (pathMap) {
            stubs = pathMap.get(path);
            if (stubs == null) {
                stubs = new DSOutboundSubscribeStubs(path, this);
                stubs.add(stub);
                pathMap.put(path, stubs);
                pendingSubscribe.add(stubs);
            } else {
                stubs.add(stub);
                stubs.setState(State.PENDING_SUBSCRIBE);
                pendingSubscribe.add(stubs);
            }
        }
        try {
            req.onInit(path, stubs.getQos(), stub);
        } catch (Exception x) {
            error(path, x);
        }
        sendMessage();
        return req;
    }

    /**
     * Remove the subscription and call onClose.
     */
    void unsubscribe(DSOutboundSubscribeStubs stubs) {
        synchronized (pathMap) {
            if (stubs.size() == 0) {
                stubs.setState(State.PENDING_UNSUBSCRIBE);
                pendingUnsubscribe.add(stubs);
                sendMessage();
            }
        }
    }

    @Override
    public void write(MessageWriter writer) {
        DSSession session = requester.getSession();
        if (!pendingSubscribe.isEmpty()) {
            doBeginSubscribe(writer);
            Iterator<DSOutboundSubscribeStubs> it = pendingSubscribe.iterator();
            while (it.hasNext() && !session.shouldEndMessage()) {
                DSOutboundSubscribeStubs stubs = it.next();
                if (!stubs.hasSid()) {
                    synchronized (pathMap) {
                        stubs.setSid(getNextSid());
                        sidMap.put(stubs.getSid(), stubs);
                    }
                }
                if (stubs.getState() == State.PENDING_SUBSCRIBE) {
                    doWriteSubscribe(writer, stubs.getPath(), stubs.getSid(), stubs.getQos());
                    stubs.setState(State.SUBSCRIBED);
                }
                it.remove();
            }
            doEndMessage(writer);
        }
        if (!pendingUnsubscribe.isEmpty() && !session.shouldEndMessage()) {
            doBeginUnsubscribe(writer);
            Iterator<DSOutboundSubscribeStubs> it = pendingUnsubscribe.iterator();
            while (it.hasNext() && !session.shouldEndMessage()) {
                DSOutboundSubscribeStubs stubs = it.next();
                synchronized (pathMap) {
                    if (stubs.size() == 0) {
                        pathMap.remove(stubs.getPath());
                        sidMap.remove(stubs.getSid());
                        doWriteUnsubscribe(writer, stubs.getSid());
                    }
                }
                it.remove();
            }
            doEndMessage(writer);
        }
        synchronized (this) {
            enqueued = false;
        }
        if (!pendingSubscribe.isEmpty() || !pendingUnsubscribe.isEmpty()) {
            sendMessage();
        }
    }

}
