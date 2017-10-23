package com.acuity.iot.dsa.dslink.protocol.protocol_v1.requester;

import com.acuity.iot.dsa.dslink.protocol.message.OutboundMessage;
import org.iot.dsa.dslink.requester.OutboundRequest;
import org.iot.dsa.node.DSElement;
import org.iot.dsa.node.DSMap;

/**
 * Common to all return values from DSIRequester.
 *
 * @author Daniel Shapiro, Aaron Hansen
 */
abstract class DS1OutboundRequestStub implements OutboundMessage {

    ///////////////////////////////////////////////////////////////////////////
    // Fields
    ///////////////////////////////////////////////////////////////////////////

    private long created = System.currentTimeMillis();
    private boolean open = true;
    private DS1Requester requester;
    private Integer requestId;

    ///////////////////////////////////////////////////////////////////////////
    // Methods
    ///////////////////////////////////////////////////////////////////////////

    public void close() {
        if (!open) {
            return;
        }
        getRequester().sendClose(getRequestId());
        handleClose();
    }

    /**
     * Can be used to clean up very old requests that should have been short lived.
     */
    public long getCreatedTime() {
        return created;
    }

    public abstract OutboundRequest getRequest();

    public DS1Requester getRequester() {
        return requester;
    }

    public Integer getRequestId() {
        return requestId;
    }

    protected void handleClose() {
        if (!open) {
            return;
        }
        open = false;
        try {
            getRequest().onClose();
        } catch (Exception x) {
            getRequester().severe(getRequester().getPath(), x);
        }
        getRequester().removeRequest(getRequestId());
    }

    protected void handleError(DSElement details) {
        if (!open) {
            return;
        }
        try {
            getRequest().onError(details);
        } catch (Exception x) {
            getRequester().severe(getRequester().getPath(), x);
        }
    }

    protected abstract void handleResponse(DSMap map);

    public boolean isOpen() {
        return open;
    }

    public DS1OutboundRequestStub setRequester(DS1Requester requester) {
        this.requester = requester;
        return this;
    }

    public DS1OutboundRequestStub setRequestId(Integer requestId) {
        this.requestId = requestId;
        return this;
    }

}
