package com.acuity.iot.dsa.dslink.protocol.v2;

import com.acuity.iot.dsa.dslink.protocol.DSSession;
import com.acuity.iot.dsa.dslink.protocol.message.MessageWriter;
import com.acuity.iot.dsa.dslink.protocol.message.OutboundMessage;
import com.acuity.iot.dsa.dslink.transport.DSBinaryTransport;

/**
 * Responder uses to close streams without errors.
 *
 * @author Aaron Hansen
 */
public class CloseMessage implements MessageConstants, OutboundMessage {

    private byte method = MSG_CLOSE;
    private DSSession session;
    private int rid;

    public CloseMessage(DSSession session, int requestId) {
        this.session = session;
        this.rid = requestId;
    }

    public CloseMessage(DSSession session, int requestId, byte method) {
        this.method = method;
        this.session = session;
        this.rid = requestId;
    }

    @Override
    public void write(MessageWriter writer) {
        DS2MessageWriter out = (DS2MessageWriter) writer;
        out.init(rid, session.getAckToSend());
        out.setMethod(method);
        out.write((DSBinaryTransport) session.getTransport());
    }

}
