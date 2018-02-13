package com.acuity.iot.dsa.dslink.protocol.protocol_v2.responder;

import com.acuity.iot.dsa.dslink.protocol.message.MessageWriter;
import com.acuity.iot.dsa.dslink.protocol.protocol_v2.DS2MessageWriter;
import com.acuity.iot.dsa.dslink.protocol.protocol_v2.MessageConstants;
import com.acuity.iot.dsa.dslink.protocol.responder.DSInboundList;
import com.acuity.iot.dsa.dslink.transport.DSBinaryTransport;

/**
 * List implementation for a responder.
 *
 * @author Aaron Hansen
 */
class DS2InboundList extends DSInboundList implements MessageConstants {

    @Override
    public void write(MessageWriter writer) {
        //if has remaining multipart, send that
        DS2MessageWriter out = (DS2MessageWriter) writer;
        out.init(getRequestId(), getSession().getNextAck());
        out.setMethod((byte) MSG_LIST_RES);
        super.write(writer);
        out.write((DSBinaryTransport) getResponder().getTransport());
        //if has multipart
    }

}
