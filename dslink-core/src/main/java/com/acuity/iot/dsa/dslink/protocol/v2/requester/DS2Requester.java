package com.acuity.iot.dsa.dslink.protocol.v2.requester;

import com.acuity.iot.dsa.dslink.DSSession;
import com.acuity.iot.dsa.dslink.protocol.requester.DSOutboundInvokeStub;
import com.acuity.iot.dsa.dslink.protocol.requester.DSOutboundListStub;
import com.acuity.iot.dsa.dslink.protocol.requester.DSOutboundRemoveStub;
import com.acuity.iot.dsa.dslink.protocol.requester.DSOutboundSetStub;
import com.acuity.iot.dsa.dslink.protocol.requester.DSOutboundStub;
import com.acuity.iot.dsa.dslink.protocol.requester.DSOutboundSubscriptions;
import com.acuity.iot.dsa.dslink.protocol.requester.DSRequester;
import com.acuity.iot.dsa.dslink.protocol.v1.CloseMessage;
import com.acuity.iot.dsa.dslink.protocol.v2.DS2MessageReader;
import com.acuity.iot.dsa.dslink.protocol.v2.MessageConstants;
import java.io.InputStream;
import org.iot.dsa.dslink.requester.ErrorType;
import org.iot.dsa.dslink.requester.OutboundInvokeHandler;
import org.iot.dsa.dslink.requester.OutboundListHandler;
import org.iot.dsa.dslink.requester.OutboundRequestHandler;
import org.iot.dsa.node.DSElement;
import org.iot.dsa.node.DSIValue;
import org.iot.dsa.node.DSList;
import org.iot.dsa.node.DSMap;
import org.iot.dsa.util.DSException;

/**
 * DSA V1 requester implementation.
 *
 * @author Daniel Shapiro, Aaron Hansen
 */
public class DS2Requester extends DSRequester implements MessageConstants {

    ///////////////////////////////////////////////////////////////////////////
    // Fields
    ///////////////////////////////////////////////////////////////////////////

    private byte[] buf;

    ///////////////////////////////////////////////////////////////////////////
    // Constructors
    ///////////////////////////////////////////////////////////////////////////

    public DS2Requester(DSSession session) {
        super(session);
    }

    ///////////////////////////////////////////////////////////////////////////
    // Methods
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Received and error, ignoring the body.
     */
    private void drain(DS2MessageReader reader) {
        try {
            int bufSize = 512;
            int bodyLen = reader.getBodyLength();
            if (bodyLen <= 0) {
                return;
            }
            if (buf == null) {
                buf = new byte[bufSize];
            }
            InputStream in = reader.getBody();
            int len = Math.min(bodyLen, bufSize);
            while (len > 0) {
                in.read(buf, 0, len);
                bodyLen -= len;
                len = Math.min(bodyLen, bufSize);
            }
        } catch (Exception x) {
            DSException.throwRuntime(x);
        }
    }

    private boolean isError(DS2MessageReader reader, Byte status, DSOutboundStub stub) {
        if (status == null) {
            return false;
        }
        ErrorType type = null;
        String message = null;
        switch (status) {
            case STS_ALIAS_LOOP:
                type = ErrorType.badRequest;
                message = "Alias Loop";
                break;
            case STS_INTERNAL_ERR:
                type = ErrorType.internalError;
                message = "Internal Error";
                break;
            case STS_INVALID_AUTH:
                type = ErrorType.permissionDenied;
                message = "Invalid Auth Value";
                break;
            case STS_INVALID_MESSAGE:
                type = ErrorType.badRequest;
                message = "Invalid Message";
                break;
            case STS_INVALID_PARAMETER:
                type = ErrorType.badRequest;
                message = "Invalid Parameter";
                break;
            case STS_NOT_SUPPORTED:
                type = ErrorType.notSupported;
                message = "Not Supported";
                break;
            case STS_PERMISSION_DENIED:
                type = ErrorType.permissionDenied;
                message = "Permission Denied";
                break;
            default:
                return false;
        }
        if (type == null) {
            return false;
        }
        drain(reader);
        String tmp = (String) reader.getHeader(MessageConstants.HDR_ERROR_DETAIL);
        if (tmp != null) {
            message = tmp;
        }
        stub.handleError(type, message);
        return true;
    }

    private boolean isStreamClosed(Byte status) {
        switch (status) {
            case STS_BUSY :
            case STS_CLOSED :
            case STS_DISCONNECTED :
                return true;
        }
        return false;
    }

    public void handleResponse(DS2MessageReader reader) {
        Integer rid = reader.getRequestId();
        DSOutboundStub stub = getRequest(rid);
        if (stub == null) {
            warn(warn() ? "Response for unknown rid: " + rid : null);
            drain(reader);
            sendClose(rid);
            return;
        }
        Byte status = (Byte) reader.getHeader(MessageConstants.HDR_STATUS);
        if (isError(reader, status, stub)) {
            stub.handleClose();
            removeRequest(rid);
            return;
        }
        boolean isSubscription = false;
        if (isSubscription) {
            //todo
        } else {
            ((DS2OutboundStub) stub).handleResponse(reader);
        }
        if (isStreamClosed(status)) {
            stub.handleClose();
            removeRequest(rid);
        }
    }

    @Override
    protected DSOutboundListStub makeList(String path, OutboundListHandler req) {
        return new DS2OutboundListStub(this, getNextRid(), path, req);
    }

    @Override
    protected DSOutboundInvokeStub makeInvoke(String path, DSMap params,
                                              OutboundInvokeHandler req) {
        return new DS2OutboundInvokeStub(this, getNextRid(), path, params, req);
    }

    @Override
    protected DSOutboundRemoveStub makeRemove(String path, OutboundRequestHandler req) {
        return new DS2OutboundRemoveStub(this, getNextRid(), path, req);
    }

    @Override
    protected DSOutboundSetStub makeSet(String path, DSIValue value, OutboundRequestHandler req) {
        return new DS2OutboundSetStub(this, getNextRid(), path, value, req);
    }

    /*
    @Override
    protected DSOutboundSubscriptions makeSubscriptions() {
        return new DS2OutboundSubscriptions(this);
    }
    */

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
