package com.acuity.iot.dsa.dslink.protocol.v2;

import com.acuity.iot.dsa.dslink.protocol.message.MessageWriter;
import com.acuity.iot.dsa.dslink.protocol.message.OutboundMessage;
import com.acuity.iot.dsa.dslink.protocol.v2.responder.DS2Responder;

/**
 * Responder uses to close streams without errors.
 *
 * @author Aaron Hansen
 */
public class CloseMessage implements MessageConstants, OutboundMessage {

    private byte method = MSG_CLOSE;
    private DS2Responder responder;
    private int rid;

    public CloseMessage(DS2Responder responder, int requestId) {
        this.responder = responder;
        this.rid = requestId;
    }

    public CloseMessage(DS2Responder responder, int requestId, byte method) {
        this.method = method;
        this.responder = responder;
        this.rid = requestId;
    }

    @Override
    public void write(MessageWriter writer) {
        DS2MessageWriter out = (DS2MessageWriter) writer;
        out.init(rid, responder.getSession().getNextAck());
        out.setMethod(method);
        out.write(responder.getTransport());
    }

}
