package com.acuity.iot.dsa.dslink.protocol.v2;

import com.acuity.iot.dsa.dslink.protocol.DSSession;
import com.acuity.iot.dsa.dslink.protocol.message.MessageWriter;
import com.acuity.iot.dsa.dslink.protocol.message.OutboundMessage;

/**
 * @author Aaron Hansen
 */
class PingMessage implements MessageConstants, OutboundMessage {

    private DS2Session session;

    public PingMessage(DS2Session session) {
        this.session = session;
    }

    @Override
    public boolean canWrite(DSSession session) {
        return true;
    }

    @Override
    public void write(DSSession sess, MessageWriter writer) {
        DS2MessageWriter out = (DS2MessageWriter) writer;
        out.init(-1, session.getAckToSend());
        out.setMethod(MSG_PING);
        out.write(session.getTransport());
    }

}
