package com.acuity.iot.dsa.dslink.protocol.v2.requester;

import com.acuity.iot.dsa.dslink.protocol.v2.DS2MessageReader;

public interface DS2OutboundStub {

    void handleResponse(DS2MessageReader response);

}
