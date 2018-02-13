package com.acuity.iot.dsa.dslink.protocol.protocol_v2.responder;

import com.acuity.iot.dsa.dslink.io.DSByteBuffer;
import com.acuity.iot.dsa.dslink.protocol.message.MessageWriter;
import com.acuity.iot.dsa.dslink.protocol.protocol_v2.DS2MessageWriter;
import com.acuity.iot.dsa.dslink.protocol.protocol_v2.MessageConstants;
import com.acuity.iot.dsa.dslink.protocol.responder.DSInboundSubscription;
import com.acuity.iot.dsa.dslink.protocol.responder.DSInboundSubscriptions;
import com.acuity.iot.dsa.dslink.transport.DSBinaryTransport;
import org.iot.dsa.io.msgpack.MsgpackWriter;
import org.iot.dsa.time.DSTime;

/**
 * Subscribe implementation for the responder.
 *
 * @author Aaron Hansen
 */
public class DS2InboundSubscription extends DSInboundSubscription implements MessageConstants {

    ///////////////////////////////////////////////////////////////////////////
    // Fields
    ///////////////////////////////////////////////////////////////////////////

    ///////////////////////////////////////////////////////////////////////////
    // Constructors
    ///////////////////////////////////////////////////////////////////////////

    protected DS2InboundSubscription(DSInboundSubscriptions manager,
                                     Integer sid, String path, int qos) {
        super(manager, sid, path, qos);
    }

    ///////////////////////////////////////////////////////////////////////////
    // Methods in alphabetical order
    ///////////////////////////////////////////////////////////////////////////

    @Override
    protected void write(Update update, MessageWriter writer, StringBuilder buf) {
        DS2MessageWriter out = (DS2MessageWriter) writer;
        out.init(getSubscriptionId(), getSession().getNextAck());
        out.setMethod((byte) MSG_SUBSCRIBE_RES);
        DSByteBuffer byteBuffer = out.getBody();
        byteBuffer.skip(2);
        MsgpackWriter msgpackWriter = new MsgpackWriter(byteBuffer);
        msgpackWriter.beginMap();
        buf.setLength(0);
        DSTime.encode(update.timestamp, true, buf);
        msgpackWriter.key("timestamp").value(buf.toString());
        if (!update.quality.isOk()) {
            msgpackWriter.key("status").value(update.quality.toElement());
        }
        msgpackWriter.endMap();
        byteBuffer.replaceShort(0, (short) msgpackWriter.length(), false);
        msgpackWriter.value(update.value.toElement());
        out.write((DSBinaryTransport) getResponder().getTransport());
    }

}
