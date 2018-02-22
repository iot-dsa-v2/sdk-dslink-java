package com.acuity.iot.dsa.dslink.protocol.v1.requester;

import com.acuity.iot.dsa.dslink.protocol.requester.DSOutboundStub;
import com.acuity.iot.dsa.dslink.protocol.requester.DSRequester;
import com.acuity.iot.dsa.dslink.protocol.v1.CloseMessage;
import com.acuity.iot.dsa.dslink.protocol.v1.DS1Session;
import org.iot.dsa.node.DSElement;
import org.iot.dsa.node.DSList;
import org.iot.dsa.node.DSMap;

/**
 * DSA V1 requester implementation.
 *
 * @author Daniel Shapiro, Aaron Hansen
 */
public class DS1Requester extends DSRequester {

    ///////////////////////////////////////////////////////////////////////////
    // Constructors
    ///////////////////////////////////////////////////////////////////////////

    public DS1Requester(DS1Session session) {
        super(session);
    }

    ///////////////////////////////////////////////////////////////////////////
    // Methods
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Called by the parent session to handle response messages.
     */
    public void handleResponse(Integer rid, DSMap map) {
        if (rid == 0) {
            processUpdates(map);
        } else {
            DSOutboundStub stub = getRequest(rid);
            if (stub != null) {
                stub.handleResponse(map);
                if (isError(map)) {
                    stub.handleError(map.get("error"));
                }
                if (isStreamClosed(map)) {
                    stub.handleClose();
                    removeRequest(rid);
                }
            } else {
                if (!isStreamClosed(map)) {
                    sendClose(rid);
                }
            }
        }
    }

    private boolean isError(DSMap message) {
        DSElement e = message.get("error");
        if (e == null) {
            return false;
        }
        return !e.isNull();
    }

    private boolean isStreamClosed(DSMap message) {
        return "closed".equals(message.getString("stream"));
    }

    private void processUpdate(DSElement updateElement) {
        int sid = -1;
        DSElement value;
        String ts, sts = null;
        if (updateElement instanceof DSList) {
            DSList updateList = (DSList) updateElement;
            int cols = updateList.size();
            if (cols < 3) {
                trace(trace() ? "Update incomplete: " + updateList.toString() : null);
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
            debug(debug() ? "Update missing sid: " + updateElement.toString() : null);
            return;
        }
        getSubscriptions().handleUpdate(sid, ts, sts, value);
    }

    private void processUpdates(DSMap map) {
        DSList updates = map.getList("updates");
        for (int i = 0; i < updates.size(); i++) {
            DSElement update = updates.get(i);
            processUpdate(update);
        }
    }

    @Override
    public void sendClose(Integer rid) {
        removeRequest(rid);
        sendRequest(new CloseMessage(rid, true));
    }

}
