package org.iot.dsa.dslink.requester;

public abstract class OutboundRequest {

    private Integer rid;

    /**
     * Unique ID of the request, or 0 for subscriptions.
     */
    public Integer getRequestId() {
        return rid;
    }

    ;

    public void setRequestId(Integer rid) {
        this.rid = rid;
    }


}
