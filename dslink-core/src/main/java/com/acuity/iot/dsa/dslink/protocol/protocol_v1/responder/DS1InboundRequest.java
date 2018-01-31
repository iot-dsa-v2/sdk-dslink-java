package com.acuity.iot.dsa.dslink.protocol.protocol_v1.responder;

import com.acuity.iot.dsa.dslink.protocol.protocol_v1.DS1Session;
import com.acuity.iot.dsa.dslink.protocol.responder.DSInboundRequest;
import org.iot.dsa.dslink.DSLink;
import org.iot.dsa.dslink.responder.InboundRequest;
import org.iot.dsa.logging.DSLogger;
import org.iot.dsa.node.DSMap;

/**
 * Getters and setters common to most requests.
 *
 * @author Aaron Hansen
 */
class DS1InboundRequest extends DSInboundRequest implements InboundRequest {

    private DSMap request;

    public DSMap getRequest() {
        return request;
    }

    public DS1InboundRequest setRequest(DSMap request){
        this.request = request;
        return this;
    }

    public DS1Responder getResponder() {
        return (DS1Responder) super.getResponder();
    }

}
