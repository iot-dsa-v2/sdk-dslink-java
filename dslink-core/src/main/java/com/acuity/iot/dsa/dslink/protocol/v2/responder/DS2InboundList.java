package com.acuity.iot.dsa.dslink.protocol.v2.responder;

import com.acuity.iot.dsa.dslink.io.DSByteBuffer;
import com.acuity.iot.dsa.dslink.protocol.message.MessageWriter;
import com.acuity.iot.dsa.dslink.protocol.v2.DS2MessageWriter;
import com.acuity.iot.dsa.dslink.protocol.v2.MessageConstants;
import com.acuity.iot.dsa.dslink.protocol.responder.DSInboundList;
import com.acuity.iot.dsa.dslink.transport.DSBinaryTransport;
import org.iot.dsa.io.msgpack.MsgpackWriter;
import org.iot.dsa.node.DSElement;

/**
 * List implementation for a responder.
 *
 * @author Aaron Hansen
 */
class DS2InboundList extends DSInboundList implements MessageConstants {

    @Override
    protected void beginMessage(MessageWriter writer) {
    }

    @Override
    protected void beginUpdates(MessageWriter writer) {
    }

    @Override
    protected void encode(String key, DSElement value, MessageWriter writer) {
        DS2MessageWriter out = (DS2MessageWriter) writer;
        if (out.isDebug()) {
            out.getDebug().append('\n').append(key).append(" : ").append(value);
        }
        DSByteBuffer buf = out.getBody();
        MsgpackWriter mp = out.getWriter();
        buf.skip(2);
        int start = buf.length();
        mp.writeUTF8(key);
        int end = buf.length();
        buf.replaceShort(start - 2, (short) (end - start), false);
        mp.reset();
        buf.skip(2);
        start = buf.length();
        mp.value(value);
        end = buf.length();
        buf.replaceShort(start - 2, (short) (end - start), false);
    }

    @Override
    protected void encode(String key, String value, MessageWriter writer) {
        DS2MessageWriter out = (DS2MessageWriter) writer;
        if (out.isDebug()) {
            out.getDebug().append('\n').append(key).append(" : ").append(value);
        }
        DSByteBuffer buf = out.getBody();
        MsgpackWriter mp = out.getWriter();
        buf.skip(2);
        int start = buf.length();
        mp.writeUTF8(key);
        int end = buf.length();
        buf.replaceShort(start - 2, (short) (end - start), false);
        out.getWriter().reset();
        buf.skip(2);
        start = buf.length();
        out.getWriter().value(value);
        end = buf.length();
        buf.replaceShort(start - 2, (short) (end - start), false);
    }

    @Override
    protected String encodeName(String name) {
        return name;
    }

    @Override
    protected void encodeUpdate(Update update, MessageWriter writer) {
        if (update.added) {
            encodeChild(update.child, writer);
        } else {
            DS2MessageWriter out = (DS2MessageWriter) writer;
            out.writeString(encodeName(update.child.getName()));
            out.getBody().put((byte) 0, (byte) 0);
        }
    }

    @Override
    protected void endMessage(MessageWriter writer, Boolean streamOpen) {
        DS2MessageWriter out = (DS2MessageWriter) writer;
        if (streamOpen == null) {
            out.addHeader((byte) HDR_STATUS, MessageConstants.STS_INITIALIZING);
        } else if (streamOpen) {
            out.addHeader((byte) HDR_STATUS, MessageConstants.STS_OK);
        } else {
            out.addHeader((byte) HDR_STATUS, MessageConstants.STS_CLOSED);
        }
    }

    @Override
    protected void endUpdates(MessageWriter writer) {
    }

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
