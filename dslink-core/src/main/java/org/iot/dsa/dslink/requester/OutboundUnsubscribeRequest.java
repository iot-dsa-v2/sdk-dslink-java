package org.iot.dsa.dslink.requester;

import java.util.Iterator;

public abstract class OutboundUnsubscribeRequest extends OutboundRequest {

    public abstract Iterator<OutboundSubscription> getSids();
}
