package org.iot.dsa.dslink.requester;

import java.util.Iterator;

public abstract class OutboundSubscribeRequest extends OutboundRequest {

    public abstract Iterator<OutboundSubscription> getPaths();

}
