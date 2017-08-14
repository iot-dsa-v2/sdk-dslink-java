package org.iot.dsa.dslink.requester;

import org.iot.dsa.node.DSMap;
import org.iot.dsa.security.DSPermission;

public abstract class OutboundInvokeRequest extends OutboundRequest {
    
    private StreamState latestStreamState = StreamState.INITIALIZED;

    public StreamState getLatestStreamState() {
        return latestStreamState;
    }

    public void setLatestStreamState(StreamState latestStreamState) {
        this.latestStreamState = latestStreamState;
    }
    

    /**
     * The maximum permission level of this invoke, or null
     */
    public abstract DSPermission getPermission();

    /**
     * The requested path.
     */
    public abstract String getPath();

    /**
     * Any parameters supplied for the invocation
     */
    public abstract DSMap getParameters();

    /**
     * Called when a response/update to this request comes in from the broker
     *
     * @return true if the stream should be kept open
     */
    public abstract boolean onResponse(InboundInvokeResponse response);

}
