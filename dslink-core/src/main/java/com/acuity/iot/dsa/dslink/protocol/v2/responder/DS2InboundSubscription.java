package com.acuity.iot.dsa.dslink.protocol.v2.responder;

import com.acuity.iot.dsa.dslink.io.DSByteBuffer;
import com.acuity.iot.dsa.dslink.protocol.message.MessageWriter;
import com.acuity.iot.dsa.dslink.protocol.v2.DS2MessageWriter;
import com.acuity.iot.dsa.dslink.protocol.v2.MessageConstants;
import com.acuity.iot.dsa.dslink.protocol.responder.DSInboundSubscription;
import com.acuity.iot.dsa.dslink.protocol.responder.DSInboundSubscriptions;
import com.acuity.iot.dsa.dslink.transport.DSBinaryTransport;
import org.iot.dsa.io.DSIWriter;
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
        DS2MessageWriter messageWriter = (DS2MessageWriter) writer;
        messageWriter.init(getSubscriptionId(), getSession().getNextAck());
        messageWriter.setMethod((byte) MSG_SUBSCRIBE_RES);
        DSIWriter dsiWriter = messageWriter.getWriter();
        DSByteBuffer byteBuffer = messageWriter.getBody();
        byteBuffer.skip(2);
        int start = byteBuffer.length();
        dsiWriter.beginMap();
        buf.setLength(0);
        DSTime.encode(update.timestamp, true, buf);
        dsiWriter.key("timestamp").value(buf.toString());
        if (!update.quality.isOk()) {
            dsiWriter.key("status").value(update.quality.toElement());
        }
        dsiWriter.endMap();
        int end = byteBuffer.length();
        byteBuffer.replaceShort(start - 2, (short) (end - start), false);
        dsiWriter.reset();
        dsiWriter.value(update.value.toElement());
        messageWriter.write((DSBinaryTransport) getResponder().getTransport());
    }

}
