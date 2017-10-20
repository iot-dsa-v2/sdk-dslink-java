package com.acuity.iot.dsa.dslink.protocol.protocol_v1.requester;

import com.acuity.iot.dsa.dslink.protocol.message.CloseMessage;
import com.acuity.iot.dsa.dslink.protocol.message.OutboundMessage;
import com.acuity.iot.dsa.dslink.protocol.protocol_v1.DS1Session;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import org.iot.dsa.dslink.DSIRequester;
import org.iot.dsa.dslink.requester.OutboundInvokeRequest;
import org.iot.dsa.dslink.requester.OutboundListRequest;
import org.iot.dsa.dslink.requester.OutboundRemoveRequest;
import org.iot.dsa.dslink.requester.OutboundSetRequest;
import org.iot.dsa.dslink.requester.OutboundSubscribeRequest;
import org.iot.dsa.dslink.requester.OutboundSubscription;
import org.iot.dsa.dslink.requester.OutboundUnsubscribeRequest;
import org.iot.dsa.node.DSElement;
import org.iot.dsa.node.DSList;
import org.iot.dsa.node.DSMap;
import org.iot.dsa.node.DSNode;

public abstract class DS1Requester extends DSNode implements DSIRequester {

    ///////////////////////////////////////////////////////////////////////////
    // Fields
    ///////////////////////////////////////////////////////////////////////////

    private AtomicInteger nextRid = new AtomicInteger();
    private AtomicInteger nextSid = new AtomicInteger();
    private DS1Session session;
    private final Map<Integer, DS1OutboundRequestStub> requests =
            new ConcurrentHashMap<Integer, DS1OutboundRequestStub>();
    private final Map<Integer, OutboundSubscription> subscriptions =
            new ConcurrentHashMap<Integer, OutboundSubscription>();

    ///////////////////////////////////////////////////////////////////////////
    // Constructors
    ///////////////////////////////////////////////////////////////////////////

    public DS1Requester() {
    }

    public DS1Requester(DS1Session session) {
        this.session = session;
    }

    ///////////////////////////////////////////////////////////////////////////
    // Methods
    ///////////////////////////////////////////////////////////////////////////

    private Double getDoubleOrNull(DSMap map, String key) {
        DSElement elem = map.get(key);
        if (elem == null || !elem.isNumber()) {
            return null;
        }
        return elem.toDouble();
    }

    private int getNextSid() {
        return nextSid.incrementAndGet();
    }

    private int getNextRid() {
        return nextRid.incrementAndGet();
    }

    DS1Session getSession() {
        return session;
    }

    @Override
    public DS1OutboundInvokeStub invoke(OutboundInvokeRequest req) {
        DS1OutboundInvokeStub stub = new DS1OutboundInvokeStub(req);
        stub.setRequester(this).setRequestId(getNextRid());
        requests.put(stub.getRequestId(), stub);
        session.enqueueOutgoingRequest(stub);
        return stub;
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
    public DS1OutboundListStub list(OutboundListRequest req) {
        DS1OutboundListStub stub = new DS1OutboundListStub(req);
        stub.setRequester(this).setRequestId(getNextRid());
        requests.put(stub.getRequestId(), stub);
        session.enqueueOutgoingRequest(stub);
        return stub;
    }

    /**
     * Call by the parent session to handle response messages.
     */
    public void processResponse(Integer rid, DSMap map) {
        if (rid == 0) {
            processSubscriptionUpdates(map);
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
                requests.remove(rid);
            }
        }
    }

    private void processSubscriptionUpdate(DSElement updateElement) {
        int sid;
        DSElement value;
        String ts, status = null;
        int count = -1;
        Double sum = null, max = null, min = null;
        if (updateElement instanceof DSList) {
            DSList updateList = (DSList) updateElement;
            if (updateList.size() < 3) {
                finest(finest() ? "Update incomplete: " + updateList.toString() : null);
                return;
            }
            sid = updateList.get(0, -1);
            value = updateList.get(1);
            ts = updateList.getString(2);
        } else if (updateElement instanceof DSMap) {
            DSMap updateMap = (DSMap) updateElement;
            sid = updateMap.get("sid", -1);
            value = updateMap.get("value");
            ts = updateMap.getString("ts");
            count = updateMap.get("count", -1);
            sum = getDoubleOrNull(updateMap, "sum");
            max = getDoubleOrNull(updateMap, "max");
            min = getDoubleOrNull(updateMap, "min");
        } else {
            return;
        }
        if (sid < 0) {
            finest(finest() ? "Update missing sid: " + updateElement.toString() : null);
            return;
        }

        OutboundSubscription sub = subscriptions.get(sid);
        if (sub == null) {
            return;
        }

        DS1SubscriptionUpdate update = new DS1SubscriptionUpdate();
        update.setValue(value);
        update.setTimestamp(ts);
        update.setStatus(status);
        if (count >= 0) {
            update.setCount(count);
        }
        update.setSum(sum);
        update.setMax(max);
        update.setMin(min);

        sub.onUpdate(update);
    }

    private void processSubscriptionUpdates(DSMap map) {
        DSList updates = map.getList("updates");
        for (int i = 0; i < updates.size(); i++) {
            DSElement update = updates.get(i);
            processSubscriptionUpdate(update);
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

    public void sendClose(int rid) {
        requests.remove(rid);
        sendRequest(new CloseMessage(rid));
    }

    public void sendRequest(OutboundMessage res) {
        session.enqueueOutgoingRequest(res);
    }

    private void sendSubscribeRequest(OutboundSubscribeRequest req) {
        Iterator<OutboundSubscription> iter = req.getPaths();
        while (iter.hasNext()) {
            OutboundSubscription sub = iter.next();
            int sid = getNextSid();
            sub.setSubscriptionId(sid);
            subscriptions.put(sid, sub);
        }
    }

    private void sendUnsubscribeRequest(OutboundUnsubscribeRequest req) {
        Iterator<OutboundSubscription> iter = req.getSids();
        while (iter.hasNext()) {
            OutboundSubscription sub = iter.next();
            int sid = sub.getSubscriptionId();
            subscriptions.remove(sid);
        }
    }

    @Override
    public void set(OutboundSetRequest req) {
        DS1OutboundSetStub stub = new DS1OutboundSetStub(req);
        stub.setRequester(this).setRequestId(getNextRid());
        requests.put(stub.getRequestId(), stub);
        session.enqueueOutgoingRequest(stub);
    }

}
