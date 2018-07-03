package com.acuity.iot.dsa.dslink.protocol.v2.responder;

import com.acuity.iot.dsa.dslink.io.DSByteBuffer;
import com.acuity.iot.dsa.dslink.protocol.message.MessageWriter;
import com.acuity.iot.dsa.dslink.protocol.responder.DSInboundSubscription;
import com.acuity.iot.dsa.dslink.protocol.responder.DSInboundSubscriptions;
import com.acuity.iot.dsa.dslink.protocol.v2.DS2MessageWriter;
import com.acuity.iot.dsa.dslink.protocol.v2.MessageConstants;
import com.acuity.iot.dsa.dslink.transport.DSBinaryTransport;
import org.iot.dsa.io.DSIWriter;
import org.iot.dsa.time.DSTime;

/**
 * Subscribe implementation for the responder.
 *
 * @author Aaron Hansen
 */
public class DS2InboundSubscription extends DSInboundSubscription implements MessageConstants {

    private int seqId = 0;

    protected DS2InboundSubscription(DSInboundSubscriptions manager,
                                     Integer sid, String path, int qos) {
        super(manager, sid, path, qos);
    }

    @Override
    protected void write(Update update, MessageWriter writer, StringBuilder buf) {
        DS2MessageWriter out = (DS2MessageWriter) writer;
        out.init(getSubscriptionId(), getSession().getNextAck());
        out.setMethod(MSG_SUBSCRIBE_RES);
        out.addIntHeader(HDR_SEQ_ID, seqId);
        seqId++;
        DSIWriter dsiWriter = out.getWriter();
        DSByteBuffer byteBuffer = out.getBody();
        byteBuffer.skip(2);
        int start = byteBuffer.length();
        dsiWriter.beginMap();
        buf.setLength(0);
        DSTime.encode(update.timestamp, true, buf);
        dsiWriter.key("timestamp").value(buf.toString());
        if (!update.status.isOk()) {
            dsiWriter.key("status").value(update.status.toElement());
        }
        dsiWriter.endMap();
        int end = byteBuffer.length();
        byteBuffer.replaceShort(start - 2, (short) (end - start), false);
        dsiWriter.reset();
        dsiWriter.value(update.value.toElement());
        out.write((DSBinaryTransport) getResponder().getTransport());
    }

}
