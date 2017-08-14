package org.iot.dsa.dslink.requester;

import org.iot.dsa.node.DSList;

public interface InboundListResponse {

    public StreamState getStreamState();

    public DSList getUpdates();

}
