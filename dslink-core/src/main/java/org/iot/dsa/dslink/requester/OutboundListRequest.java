package org.iot.dsa.dslink.requester;

public abstract class OutboundListRequest extends OutboundRequest {

    private StreamState latestStreamState = StreamState.INITIALIZED;

    public StreamState getLatestStreamState() {
        return latestStreamState;
    }

    public void setLatestStreamState(StreamState latestStreamState) {
        this.latestStreamState = latestStreamState;
    }
    
    
    /**
     * The requested path.
     */
    public abstract String getPath();

    /**
     * Called when a response/update to this request comes in from the broker
     *
     * @return true if the stream should be kept open
     */
    public abstract boolean onResponse(InboundListResponse response);
}
