package com.acuity.iot.dsa.dslink.protocol.protocol_v1.requester;

import org.iot.dsa.dslink.requester.InboundListResponse;
import org.iot.dsa.dslink.requester.StreamState;
import org.iot.dsa.node.DSList;

public class DS1InboundListResponse implements InboundListResponse {

    private StreamState streamState;
    private DSList updates;

    @Override
    public StreamState getStreamState() {
        return streamState;
    }

    @Override
    public DSList getUpdates() {
        return updates;
    }

    public void setStreamState(StreamState streamState) {
        this.streamState = streamState;
    }

    public void setUpdates(DSList updates) {
        this.updates = updates;
    }

}
