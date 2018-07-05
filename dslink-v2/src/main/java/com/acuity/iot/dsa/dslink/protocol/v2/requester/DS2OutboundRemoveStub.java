package com.acuity.iot.dsa.dslink.protocol.v2.requester;

import com.acuity.iot.dsa.dslink.protocol.requester.DSOutboundRemoveStub;
import com.acuity.iot.dsa.dslink.protocol.requester.DSRequester;
import com.acuity.iot.dsa.dslink.protocol.v2.DS2MessageReader;
import org.iot.dsa.dslink.requester.OutboundRequestHandler;

public class DS2OutboundRemoveStub extends DSOutboundRemoveStub implements DS2OutboundStub {

    protected DS2OutboundRemoveStub(DSRequester requester,
                                    Integer requestId,
                                    String path,
                                    OutboundRequestHandler handler) {
        super(requester, requestId, path, handler);
    }

    public void handleResponse(DS2MessageReader response) {
    }

}
