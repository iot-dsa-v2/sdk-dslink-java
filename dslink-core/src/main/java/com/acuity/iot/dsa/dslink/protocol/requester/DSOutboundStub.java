package com.acuity.iot.dsa.dslink.protocol.requester;

import com.acuity.iot.dsa.dslink.protocol.DSSession;
import com.acuity.iot.dsa.dslink.protocol.message.OutboundMessage;
import org.iot.dsa.dslink.requester.ErrorType;
import org.iot.dsa.dslink.requester.OutboundRequestHandler;
import org.iot.dsa.dslink.requester.OutboundStream;
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

    public DSSession getSession() {
        return requester.getSession();
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

    public void handleError(ErrorType type, String message) {
        if (!open) {
            return;
        }
        try {
            getHandler().onError(type, message);
        } catch (Exception x) {
            getRequester().error(getRequester().getPath(), x);
        }
    }

    /**
     * Handle the V1 response map.
     */
    public abstract void handleResponse(DSMap map);

    public boolean isStreamOpen() {
        return open;
    }

}
