package org.iot.dsa.dslink;

import org.iot.dsa.dslink.requester.OutboundRequest;

public interface DSRequesterInterface extends DSLinkSession {

    public void sendRequest(OutboundRequest req);

}
