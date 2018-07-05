package com.acuity.iot.dsa.dslink.protocol.v2.requester;

import com.acuity.iot.dsa.dslink.protocol.DSSession;
import com.acuity.iot.dsa.dslink.protocol.requester.*;
import com.acuity.iot.dsa.dslink.protocol.v2.CloseMessage;
import com.acuity.iot.dsa.dslink.protocol.v2.DS2MessageReader;
import com.acuity.iot.dsa.dslink.protocol.v2.MessageConstants;
import java.io.InputStream;
import org.iot.dsa.dslink.requester.ErrorType;
import org.iot.dsa.dslink.requester.OutboundInvokeHandler;
import org.iot.dsa.dslink.requester.OutboundListHandler;
import org.iot.dsa.dslink.requester.OutboundRequestHandler;
import org.iot.dsa.io.DSIReader;
import org.iot.dsa.node.DSBytes;
import org.iot.dsa.node.DSElement;
import org.iot.dsa.node.DSIValue;
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

    private boolean handleError(DS2MessageReader reader, Byte status, DSOutboundStub stub) {
        if (status == null) {
            return false;
        }
        ErrorType type;
        String message;
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
        String tmp = (String) reader.getHeader(HDR_ERROR_DETAIL);
        if (tmp != null) {
            message = tmp;
        }
        stub.handleError(type, message);
        return true;
    }


    public void handleResponse(DS2MessageReader reader) {
        if (handleSubscription(reader)) {
            return;
        }
        Integer rid = reader.getRequestId();
        DSOutboundStub stub = getRequest(rid);
        if (stub == null) {
            warn(warn() ? "Response for unknown rid: " + rid : null);
            drain(reader);
            sendClose(rid);
            return;
        }
        Byte status = (Byte) reader.getHeader(HDR_STATUS);
        if (handleError(reader, status, stub)) {
            stub.handleClose();
            removeRequest(rid);
            return;
        }
        ((DS2OutboundStub) stub).handleResponse(reader);
        if (isStreamClosed(status)) {
            stub.handleClose();
            removeRequest(rid);
        }
    }

    private boolean handleSubscription(DS2MessageReader reader) {
        if (reader.getMethod() != MSG_SUBSCRIBE_RES) {
            return false;
        }
        String ts = null;
        String sts = null;
        InputStream in = reader.getBody();
        DSIReader dsiReader = reader.getBodyReader();
        int len = DSBytes.readShort(reader.getBody(), false);
        if (len > 0) {
            DSMap map = dsiReader.getMap();
            ts = map.getString("timestamp");
            sts = map.getString("status");
            dsiReader.reset();
        }
        DSElement value = dsiReader.getElement();
        getSubscriptions().handleUpdate(reader.getRequestId(), ts, sts, value);
        return true;
    }

    private boolean isStreamClosed(Byte status) {
        if (status == null) {
            return false;
        }
        switch (status) {
            case STS_BUSY:
            case STS_CLOSED:
            case STS_DISCONNECTED:
                return true;
        }
        return false;
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

    @Override
    protected DSOutboundSubscriptions makeSubscriptions() {
        return new DS2OutboundSubscriptions(this);
    }

    @Override
    public void sendClose(Integer rid) {
        removeRequest(rid);
        sendRequest(new CloseMessage(getSession(), rid));
    }

}
