package com.acuity.iot.dsa.dslink.protocol.protocol_v1.requester;

import com.acuity.iot.dsa.dslink.protocol.message.CloseMessage;
import com.acuity.iot.dsa.dslink.protocol.message.OutboundMessage;
import org.iot.dsa.dslink.requester.OutboundCloseRequest;
import org.iot.dsa.dslink.requester.OutboundInvokeRequest;
import org.iot.dsa.dslink.requester.OutboundListRequest;
import org.iot.dsa.dslink.requester.OutboundRemoveRequest;
import org.iot.dsa.dslink.requester.OutboundRequest;
import org.iot.dsa.dslink.requester.OutboundSetRequest;
import org.iot.dsa.dslink.requester.OutboundSubscribeRequest;
import org.iot.dsa.dslink.requester.OutboundUnsubscribeRequest;

public abstract class DS1OutboundRequestWrapper implements OutboundMessage {

    protected static String method;

    public static OutboundMessage get(OutboundRequest req) {
        if (req instanceof OutboundCloseRequest) {
            return new CloseMessage(req.getRequestId());
        } else if (req instanceof OutboundInvokeRequest) {
            return new DS1OutboundInvokeWrapper((OutboundInvokeRequest) req);
        } else if (req instanceof OutboundListRequest) {
            return new DS1OutboundListWrapper((OutboundListRequest) req);
        } else if (req instanceof OutboundRemoveRequest) {
            return new DS1OutboundRemoveWrapper((OutboundRemoveRequest) req);
        } else if (req instanceof OutboundSetRequest) {
            return new DS1OutboundSetWrapper((OutboundSetRequest) req);
        } else if (req instanceof OutboundSubscribeRequest) {
            return new DS1OutboundSubscribeWrapper((OutboundSubscribeRequest) req);
        } else if (req instanceof OutboundUnsubscribeRequest) {
            return new DS1OutboundUnsubscribeWrapper((OutboundUnsubscribeRequest) req);
        } else {
            return null;
        }
    }

}
