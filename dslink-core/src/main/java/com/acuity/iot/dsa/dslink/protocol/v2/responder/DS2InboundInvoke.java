package com.acuity.iot.dsa.dslink.protocol.v2.responder;

import com.acuity.iot.dsa.dslink.protocol.message.MessageWriter;
import com.acuity.iot.dsa.dslink.protocol.v2.DS2MessageWriter;
import com.acuity.iot.dsa.dslink.protocol.v2.MessageConstants;
import com.acuity.iot.dsa.dslink.protocol.responder.DSInboundInvoke;
import com.acuity.iot.dsa.dslink.transport.DSBinaryTransport;
import org.iot.dsa.node.DSMap;
import org.iot.dsa.security.DSPermission;

/**
 * List implementation for a responder.
 *
 * @author Aaron Hansen
 */
class DS2InboundInvoke extends DSInboundInvoke implements MessageConstants {

    DS2InboundInvoke(DSMap parameters, DSPermission permission) {
        super(parameters, permission);
    }

    @Override
    public void write(MessageWriter writer) {
        //if has remaining multipart, send that
        DS2MessageWriter out = (DS2MessageWriter) writer;
        out.init(getRequestId(), getSession().getNextAck());
        out.setMethod((byte) MSG_INVOKE_RES);
        super.write(writer);
        out.write((DSBinaryTransport) getResponder().getTransport());
        //if has multipart
    }

    @Override
    protected void writeBegin(MessageWriter writer) {
        writer.getWriter().beginMap();
    }

    @Override
    protected void writeClose(MessageWriter writer) {
        DS2MessageWriter out = (DS2MessageWriter) writer;
        out.addHeader((byte) HDR_STATUS, STS_CLOSED);
    }

    @Override
    protected void writeOpen(MessageWriter writer) {
        DS2MessageWriter out = (DS2MessageWriter) writer;
        out.addHeader((byte) HDR_STATUS, STS_OK);
    }


}
