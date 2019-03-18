package com.acuity.iot.dsa.dslink.protocol.v2;

import com.acuity.iot.dsa.dslink.protocol.DSSession;
import com.acuity.iot.dsa.dslink.protocol.message.MessageWriter;
import com.acuity.iot.dsa.dslink.protocol.message.OutboundMessage;

/**
 * Responder uses to close streams without errors.
 *
 * @author Aaron Hansen
 */
public class CloseMessage implements MessageConstants, OutboundMessage {

    private byte method = MSG_CLOSE;
    private int rid;

    public CloseMessage(int requestId) {
        this.rid = requestId;
    }

    public CloseMessage(int requestId, byte method) {
        this.rid = requestId;
        this.method = method;
    }

    @Override
    public boolean canWrite(DSSession session) {
        return true;
    }

    @Override
    public boolean write(DSSession session, MessageWriter writer) {
        DS2MessageWriter out = (DS2MessageWriter) writer;
        out.init(rid, session.getAckToSend());
        out.setMethod(method);
        out.write(session.getTransport());
        return true;
    }

}
