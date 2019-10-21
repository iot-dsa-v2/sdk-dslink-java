package com.acuity.iot.dsa.dslink.protocol.v1.requester;

import com.acuity.iot.dsa.dslink.protocol.DSSession;
import com.acuity.iot.dsa.dslink.protocol.requester.DSOutboundStub;
import com.acuity.iot.dsa.dslink.protocol.requester.DSRequester;
import com.acuity.iot.dsa.dslink.protocol.v1.CloseMessage;
import org.iot.dsa.dslink.requester.ErrorType;
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

    public DS1Requester() {
    }

    public DS1Requester(DSSession session) {
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
                if (isError(map)) {
                    handleError(stub, map.get("error"));
                    stub.handleClose();
                    removeRequest(rid);
                } else {
                    stub.handleResponse(map);
                    if (isStreamClosed(map)) {
                        stub.handleClose();
                        removeRequest(rid);
                    }
                }
            } else {
                if (!isStreamClosed(map)) {
                    sendClose(rid);
                }
            }
        }
    }

    @Override
    public void sendClose(Integer rid) {
        removeRequest(rid);
        sendRequest(new CloseMessage(rid, true));
    }

    private void handleError(DSOutboundStub stub, DSElement details) {
        try {
            ErrorType type;
            String msg;
            if (details.isMap()) {
                String detail;
                DSMap map = details.toMap();
                String tmp = map.getString("type");
                if ("permissionDenied".equals(tmp)) {
                    type = ErrorType.permissionDenied;
                } else if ("invalidRequest".equals(tmp)) {
                    type = ErrorType.badRequest;
                } else if ("invalidPath".equals(tmp)) {
                    type = ErrorType.badRequest;
                } else if ("notSupported".equals(tmp)) {
                    type = ErrorType.notSupported;
                } else {
                    type = ErrorType.internalError;
                }
                msg = map.getString("msg");
                detail = map.getString("detail");
                if (msg == null) {
                    msg = detail;
                }
                if (msg == null) {
                    msg = details.toString();
                }
            } else {
                type = ErrorType.internalError;
                msg = details.toString();
            }
            if (msg == null) {
                msg = "";
            }
            stub.handleError(type, msg);
        } catch (Exception x) {
            error(getPath(), x);
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
        int sid;
        DSElement value;
        String ts, sts;
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
            sts = updateList.get(3, null);
        } else if (updateElement instanceof DSMap) {
            DSMap updateMap = (DSMap) updateElement;
            sid = updateMap.get("sid", -1);
            value = updateMap.get("value");
            ts = updateMap.getString("ts");
            sts = updateMap.get("status", null);
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

}
