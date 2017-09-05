package com.acuity.iot.dsa.dslink.protocol.protocol_v1.requester;

import com.acuity.iot.dsa.dslink.DSRequesterSession;
import com.acuity.iot.dsa.dslink.protocol.message.CloseMessage;
import com.acuity.iot.dsa.dslink.protocol.message.OutboundMessage;
import com.acuity.iot.dsa.dslink.protocol.protocol_v1.DS1Protocol;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import org.iot.dsa.dslink.DSLinkConnection;
import org.iot.dsa.dslink.requester.OutboundCloseRequest;
import org.iot.dsa.dslink.requester.OutboundInvokeRequest;
import org.iot.dsa.dslink.requester.OutboundListRequest;
import org.iot.dsa.dslink.requester.OutboundRequest;
import org.iot.dsa.dslink.requester.OutboundSubscribeRequest;
import org.iot.dsa.dslink.requester.OutboundSubscription;
import org.iot.dsa.dslink.requester.OutboundUnsubscribeRequest;
import org.iot.dsa.dslink.requester.StreamState;
import org.iot.dsa.logging.DSLogger;
import org.iot.dsa.node.DSElement;
import org.iot.dsa.node.DSList;
import org.iot.dsa.node.DSMap;

public class DS1RequesterSession extends DSLogger implements DSRequesterSession {

    private DS1Protocol protocol;

    private final Map<Integer, OutboundRequest> requests = new HashMap<Integer, OutboundRequest>();
    private final Map<Integer, OutboundSubscription> subscriptions = new HashMap<Integer, OutboundSubscription>();
    private AtomicInteger nextRid = new AtomicInteger();
    private AtomicInteger nextSid = new AtomicInteger();

    public DS1RequesterSession(DS1Protocol protocol) {
        this.protocol = protocol;
    }

    private int getNextSid() {
        return nextSid.incrementAndGet();
    }

    private int getNextRid() {
        return nextRid.incrementAndGet();
    }

    @Override
    public DSLinkConnection getConnection() {
        return protocol.getConnection();
    }

    @Override
    public boolean shouldEndMessage() {
        return protocol.shouldEndMessage();
    }

    @Override
    public void sendRequest(OutboundMessage res) {
        protocol.enqueueOutgoingRequest(res);
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

    private void sendCloseRequest(OutboundCloseRequest req) {
        int rid = req.getRequestId();
        sendClose(rid);
    }

    public void sendClose(int rid) {
        requests.remove(rid);
        sendRequest(new CloseMessage(rid));
    }

    @Override
    public void sendRequest(OutboundRequest req) {
        if (req instanceof OutboundCloseRequest) {
            sendCloseRequest((OutboundCloseRequest) req);
            return;
        } else if (req instanceof OutboundSubscribeRequest) {
            sendSubscribeRequest((OutboundSubscribeRequest) req);
        } else if (req instanceof OutboundUnsubscribeRequest) {
            sendUnsubscribeRequest((OutboundUnsubscribeRequest) req);
        }
        int rid = getNextRid();
        req.setRequestId(rid);
        requests.put(rid, req);
        sendRequest(DS1OutboundRequestWrapper.get(req));
    }

    public void processResponse(int rid, DSMap map) {
        if (rid == 0) {
            processSubscriptionUpdates(map);
        } else {
            OutboundRequest req = requests.get(rid);
            if (req instanceof OutboundInvokeRequest) {
                processInvokeResponse(rid, map, (OutboundInvokeRequest) req);
            } else if (req instanceof OutboundListRequest) {
                processListResponse(rid, map, (OutboundListRequest) req);
            } else {
                requests.remove(rid);
            }
        }
    }

    private void processListResponse(int rid, DSMap map, OutboundListRequest req) {
        String stream = map.getString("stream");
        if (stream != null) {
            req.setLatestStreamState(StreamState.valueOf(stream.toUpperCase()));
        }
        StreamState streamState = req.getLatestStreamState();
        if (streamState.isClosed()) {
            requests.remove(rid);
        }
        DSList updates = map.getList("updates");
        DS1InboundListResponse response = new DS1InboundListResponse();
        response.setStreamState(streamState);
        response.setUpdates(updates);
        boolean keepOpen = req.onResponse(response);
        if (!streamState.isClosed() && !keepOpen) {
            sendClose(rid);
        }
    }

    private void processInvokeResponse(int rid, DSMap map, OutboundInvokeRequest req) {
        String stream = map.getString("stream");
        if (stream != null) {
            req.setLatestStreamState(StreamState.valueOf(stream.toUpperCase()));
        }
        StreamState streamState = req.getLatestStreamState();
        if (streamState.isClosed()) {
            requests.remove(rid);
        }
        DSList columns = map.getList("columns");
        DSList updates = map.getList("updates");
        DSMap meta = map.getMap("meta");
        DS1InboundInvokeResponse response = new DS1InboundInvokeResponse();
        response.setStreamState(streamState);
        response.setMetadata(meta);
        if (columns != null) {
            for (int i = 0; i < columns.size(); i++) {
                response.addColumn(columns.getMap(i));
            }
        }
        if (updates != null) {
            for (int i = 0; i < updates.size(); i++) {
                DSList row = updates.getList(i);
                response.addRow(row);
            }
        }
        boolean keepOpen = req.onResponse(response);
        if (!streamState.isClosed() && !keepOpen) {
            sendClose(rid);
        }
    }

    private void processSubscriptionUpdates(DSMap map) {
        DSList updates = map.getList("updates");
        for (int i = 0; i < updates.size(); i++) {
            DSElement update = updates.get(i);
            processASubscriptionUpdate(update);
        }
    }

    private void processASubscriptionUpdate(DSElement updateElement) {
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

    private Double getDoubleOrNull(DSMap map, String key) {
        DSElement elem = map.get(key);
        if (elem == null || !elem.isNumber()) {
            return null;
        }
        return elem.toDouble();
    }


}
