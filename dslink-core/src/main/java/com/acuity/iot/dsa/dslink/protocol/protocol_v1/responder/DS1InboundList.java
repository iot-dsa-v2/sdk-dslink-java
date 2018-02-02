package com.acuity.iot.dsa.dslink.protocol.protocol_v1.responder;

import com.acuity.iot.dsa.dslink.protocol.message.ErrorResponse;
import com.acuity.iot.dsa.dslink.protocol.responder.DSInboundList;
import org.iot.dsa.node.DSMap;

/**
 * List implementation for a responder.
 *
 * @author Aaron Hansen
 */
class DS1InboundList extends DSInboundList {

    private DSMap request;

    @Override
    protected ErrorResponse makeError(Throwable reason) {
        ErrorResponse ret = new ErrorResponse(reason);
        ret.parseRequest(request);
        return ret;
    }

    public DS1InboundList setRequest(DSMap request) {
        this.request = request;
        return this;
    }

}
