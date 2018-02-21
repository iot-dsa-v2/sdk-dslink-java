package com.acuity.iot.dsa.dslink.protocol.requester;

import com.acuity.iot.dsa.dslink.protocol.message.MessageReader;
import com.acuity.iot.dsa.dslink.protocol.message.OutboundMessage;
import org.iot.dsa.dslink.requester.OutboundRequestHandler;
import org.iot.dsa.dslink.requester.OutboundStream;
import org.iot.dsa.node.DSElement;
import org.iot.dsa.node.DSMap;

/**
 * All stubs manage the lifecycle of a request and are also the outbound stream passed back to the
 * requester.
 *
 * @author Daniel Shapiro, Aaron Hansen
 */
public abstract class DSOutboundStub implements OutboundMessage, OutboundStream {

    ///////////////////////////////////////////////////////////////////////////
    // Fields
    ///////////////////////////////////////////////////////////////////////////

    private String path;
    private boolean open = true;
    private DSRequester requester;
    private Integer requestId;

    ///////////////////////////////////////////////////////////////////////////
    // Constructors
    ///////////////////////////////////////////////////////////////////////////

    DSOutboundStub(DSRequester requester, Integer requestId, String path) {
        this.requester = requester;
        this.requestId = requestId;
        this.path = path;
    }

    ///////////////////////////////////////////////////////////////////////////
    // Methods
    ///////////////////////////////////////////////////////////////////////////

    @Override
    public void closeStream() {
        if (!open) {
            return;
        }
        getRequester().sendClose(getRequestId());
        handleClose();
    }

    public abstract OutboundRequestHandler getHandler();

    public String getPath() {
        return path;
    }

    public DSRequester getRequester() {
        return requester;
    }

    public Integer getRequestId() {
        return requestId;
    }

    public void handleClose() {
        if (!open) {
            return;
        }
        open = false;
        try {
            getHandler().onClose();
        } catch (Exception x) {
            getRequester().error(getRequester().getPath(), x);
        }
        getRequester().removeRequest(getRequestId());
    }

    public void handleError(DSElement details) {
        if (!open) {
            return;
        }
        try {
            String type = null;
            String msg = null;
            String detail = null;
            if (details.isMap()) {
                DSMap map = details.toMap();
                type = map.getString("type");
                msg = map.getString("msg");
                detail = map.getString("detail");
            } else {
                msg = details.toString();
            }
            getHandler().onError(type, msg, detail);
        } catch (Exception x) {
            getRequester().error(getRequester().getPath(), x);
        }
    }

    protected abstract void handleResponse(MessageReader reader);

    public boolean isStreamOpen() {
        return open;
    }

}
