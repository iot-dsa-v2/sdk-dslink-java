package com.acuity.iot.dsa.dslink.protocol.protocol_v2.responder;

import com.acuity.iot.dsa.dslink.protocol.message.ErrorResponse;
import com.acuity.iot.dsa.dslink.protocol.message.MessageWriter;
import com.acuity.iot.dsa.dslink.protocol.responder.DSInboundList;

/**
 * List implementation for a responder.
 *
 * @author Aaron Hansen
 */
class DS2InboundList extends DSInboundList {

    @Override
    protected ErrorResponse makeError(Throwable reason) {
        ErrorResponse ret = new ErrorResponse(reason);
        //TODO
        return ret;
    }

    @Override
    public void write(MessageWriter writer) {
        //Prep response
        //if multipart message, send next part
        super.write(writer);
        //get bytes determine if multi part message
    }

}
