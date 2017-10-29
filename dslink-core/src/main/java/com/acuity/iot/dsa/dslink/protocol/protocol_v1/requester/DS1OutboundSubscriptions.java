package com.acuity.iot.dsa.dslink.protocol.protocol_v1.requester;

import com.acuity.iot.dsa.dslink.protocol.message.OutboundMessage;
import com.acuity.iot.dsa.dslink.protocol.protocol_v1.DS1Session;
import com.acuity.iot.dsa.dslink.protocol.protocol_v1.requester.DS1OutboundSubscribeStubs.State;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Logger;
import org.iot.dsa.dslink.requester.OutboundSubscribeHandler;
import org.iot.dsa.io.DSIWriter;
import org.iot.dsa.logging.DSLogger;
import org.iot.dsa.node.DSElement;
import org.iot.dsa.node.DSList;
import org.iot.dsa.node.DSMap;
import org.iot.dsa.node.DSNull;
import org.iot.dsa.node.DSStatus;
import org.iot.dsa.time.DSDateTime;

/**
 * Manages all subscriptions for a requester.
 *
 * @author Aaron Hansen
 */
class DS1OutboundSubscriptions extends DSLogger implements OutboundMessage {

    ///////////////////////////////////////////////////////////////////////////
    // Constants
    ///////////////////////////////////////////////////////////////////////////

    private static final Integer ZERO = Integer.valueOf(0);
    static final int MAX_SID = 2147483647;

    ///////////////////////////////////////////////////////////////////////////
    // Fields
    ///////////////////////////////////////////////////////////////////////////

    private boolean enqueued = false;
    private Logger logger;
    private int nextSid = 1;
    private final ConcurrentLinkedQueue<DS1OutboundSubscribeStubs> pendingSubscribe =
            new ConcurrentLinkedQueue<DS1OutboundSubscribeStubs>();
    private final ConcurrentLinkedQueue<DS1OutboundSubscribeStubs> pendingUnsubscribe =
            new ConcurrentLinkedQueue<DS1OutboundSubscribeStubs>();
    private final Map<String, DS1OutboundSubscribeStubs> pathMap =
            new ConcurrentHashMap<String, DS1OutboundSubscribeStubs>();
    private DS1Requester requester;
    private final Map<Integer, DS1OutboundSubscribeStubs> sidMap =
            new ConcurrentHashMap<Integer, DS1OutboundSubscribeStubs>();

    ///////////////////////////////////////////////////////////////////////////
    // Constructors
    ///////////////////////////////////////////////////////////////////////////

    DS1OutboundSubscriptions(DS1Requester requester) {
        this.requester = requester;
    }

    ///////////////////////////////////////////////////////////////////////////
    // Methods in alphabetical order
    ///////////////////////////////////////////////////////////////////////////

    @Override
    public Logger getLogger() {
        if (logger == null) {
            logger = requester.getLogger();
        }
        return logger;
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

    DS1Requester getRequester() {
        return requester;
    }

    public void onConnect() {
    }

    public void onConnectFail() {
    }

    public void onDisconnect() {
    }

    void processUpdate(DSElement updateElement) {
        int sid = -1;
        DSElement value;
        String ts, sts = null;
        if (updateElement instanceof DSList) {
            DSList updateList = (DSList) updateElement;
            int cols = updateList.size();
            if (cols < 3) {
                finest(finest() ? "Update incomplete: " + updateList.toString() : null);
                return;
            }
            sid = updateList.get(0, -1);
            value = updateList.get(1);
            ts = updateList.getString(2);
            sts = updateList.get(3, (String) null);
        } else if (updateElement instanceof DSMap) {
            DSMap updateMap = (DSMap) updateElement;
            sid = updateMap.get("sid", -1);
            value = updateMap.get("value");
            ts = updateMap.getString("ts");
            sts = updateMap.get("status", (String) null);
        } else {
            return;
        }
        if (sid < 0) {
            finer(finer() ? "Update missing sid: " + updateElement.toString() : null);
            return;
        }
        DS1OutboundSubscribeStubs stub = sidMap.get(sid);
        if (stub == null) {
            finer(finer() ? ("Unexpected subscription update " + sidMap.toString()) : null);
            return;
        }
        DSDateTime timestamp = null;
        if (ts == null) {
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

    void processUpdates(DSMap map) {
        DSList updates = map.getList("updates");
        for (int i = 0; i < updates.size(); i++) {
            DSElement update = updates.get(i);
            processUpdate(update);
        }
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
        DS1OutboundSubscribeStub stub = new DS1OutboundSubscribeStub(path, qos, req);
        boolean send = true;
        synchronized (pathMap) {
            DS1OutboundSubscribeStubs stubs = pathMap.get(path);
            if (stubs == null) {
                stubs = new DS1OutboundSubscribeStubs(path, getNextSid(), this);
                stubs.add(stub);
                pendingSubscribe.add(stubs);
                pathMap.put(path, stubs);
                sidMap.put(stubs.getSid(), stubs);
            } else {
                send = qos > stubs.getQos();
                stubs.add(stub);
                if (send) {
                    stubs.setState(State.PENDING_SUBSCRIBE);
                    pendingSubscribe.add(stubs);
                }
            }
        }
        if (send) {
            sendMessage();
        }
        try {
            req.onInit(path, qos, stub);
        } catch (Exception x) {
            severe(path, x);
        }
        return req;
    }

    /**
     * Remove the subscription and call onClose.
     */
    void unsubscribe(DS1OutboundSubscribeStubs stubs) {
        synchronized (pathMap) {
            if (stubs.size() == 0) {
                stubs.setState(State.PENDING_UNSUBSCRIBE);
                pendingUnsubscribe.add(stubs);
                sendMessage();
            }
        }
    }

    @Override
    public void write(DSIWriter out) {
        DS1Session session = requester.getSession();
        if (!pendingSubscribe.isEmpty()) {
            out.beginMap();
            out.key("rid").value(requester.getNextRid());
            out.key("method").value("subscribe");
            Iterator<DS1OutboundSubscribeStubs> it = pendingSubscribe.iterator();
            out.key("paths").beginList();
            while (it.hasNext() && !session.shouldEndMessage()) {
                DS1OutboundSubscribeStubs stubs = it.next();
                if (stubs.getState() == State.PENDING_SUBSCRIBE) {
                    out.beginMap();
                    out.key("path").value(stubs.getPath());
                    out.key("sid").value(stubs.getSid());
                    if (stubs.getQos() > 0) {
                        out.key("qos").value(stubs.getQos());
                    }
                    stubs.setState(State.SUBSCRIBED);
                    out.endMap();
                }
                it.remove();
            }
            out.endList();
            out.endMap();
        }
        if (!pendingUnsubscribe.isEmpty() && !session.shouldEndMessage()) {
            out.beginMap();
            out.key("rid").value(requester.getNextRid());
            out.key("method").value("unsubscribe");
            out.key("sids").beginList();
            Iterator<DS1OutboundSubscribeStubs> it = pendingUnsubscribe.iterator();
            while (it.hasNext() && !session.shouldEndMessage()) {
                DS1OutboundSubscribeStubs stubs = it.next();
                synchronized (pathMap) {
                    if (stubs.size() == 0) {
                        pathMap.remove(stubs.getPath());
                        sidMap.remove(stubs.getSid());
                        out.value(stubs.getSid());
                    }
                }
                it.remove();
            }
            out.endList();
            out.endMap();
        }
        synchronized (this) {
            enqueued = false;
        }
        if (!pendingSubscribe.isEmpty() || !pendingUnsubscribe.isEmpty()) {
            sendMessage();
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // Inner Classes
    ///////////////////////////////////////////////////////////////////////////

    ///////////////////////////////////////////////////////////////////////////
    // Initialization
    ///////////////////////////////////////////////////////////////////////////

}
