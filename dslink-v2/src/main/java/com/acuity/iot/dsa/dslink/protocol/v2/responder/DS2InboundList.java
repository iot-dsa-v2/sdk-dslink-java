package com.acuity.iot.dsa.dslink.protocol.v2.responder;

import com.acuity.iot.dsa.dslink.io.DSByteBuffer;
import com.acuity.iot.dsa.dslink.io.msgpack.MsgpackWriter;
import com.acuity.iot.dsa.dslink.protocol.DSSession;
import com.acuity.iot.dsa.dslink.protocol.DSUpstreamConnection;
import com.acuity.iot.dsa.dslink.protocol.message.MessageWriter;
import com.acuity.iot.dsa.dslink.protocol.responder.DSInboundList;
import com.acuity.iot.dsa.dslink.protocol.v2.DS2MessageWriter;
import com.acuity.iot.dsa.dslink.protocol.v2.MessageConstants;
import com.acuity.iot.dsa.dslink.protocol.v2.MultipartWriter;
import com.acuity.iot.dsa.dslink.transport.DSBinaryTransport;
import org.iot.dsa.node.DSElement;
import org.iot.dsa.node.DSPath;

/**
 * List implementation for a responder.
 *
 * @author Aaron Hansen
 */
class DS2InboundList extends DSInboundList implements MessageConstants {

    private MultipartWriter multipart;
    private int seqId = 0;

    @Override
    public void write(DSSession session, MessageWriter writer) {
        DS2MessageWriter out = (DS2MessageWriter) writer;
        if (multipart != null) {
            if (multipart.update(out, getSession().getAckToSend())) {
                getResponder().sendResponse(this);
            }
            return;
        }
        int ack = getSession().getAckToSend();
        out.init(getRequestId(), ack);
        out.setMethod(MSG_LIST_RES);
        out.addIntHeader(HDR_SEQ_ID, seqId);
        seqId++;
        super.write(session, writer);
        if (out.requiresMultipart()) {
            multipart = out.makeMultipart();
            multipart.update(out, ack);
            getResponder().sendResponse(this);
        } else {
            DSUpstreamConnection up = (DSUpstreamConnection) getResponder().getConnection();
            out.write((DSBinaryTransport) up.getTransport());
        }
    }

    @Override
    protected void beginMessage(MessageWriter writer) {
    }

    @Override
    protected void beginUpdates(MessageWriter writer) {
    }

    @Override
    protected void encode(String key, DSElement value, MessageWriter writer) {
        DS2MessageWriter out = (DS2MessageWriter) writer;
        /*
        if (out.isDebug()) {
            out.getDebug().append('\n').append(key).append(" : ").append(value);
        }
        */
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
        /*
        if (out.isDebug()) {
            out.getDebug().append('\n').append(key).append(" : ").append(value);
        }
        */
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
    protected String encodeName(String name, StringBuilder buf) {
        buf.setLength(0);
        if (DSPath.encodeName(name, buf)) {
            return buf.toString();
        }
        return name;
    }

    @Override
    protected void encodeUpdate(Update update, MessageWriter writer, StringBuilder buf) {
        if (update instanceof AddUpdate) {
            encodeChild(((AddUpdate) update).child, writer);
        } else {
            DS2MessageWriter out = (DS2MessageWriter) writer;
            out.writeString(encodeName(((RemoveUpdate) update).name, buf));
            out.getBody().put((byte) 0, (byte) 0);
        }
    }

    @Override
    protected void endMessage(MessageWriter writer, Boolean streamOpen) {
        DS2MessageWriter out = (DS2MessageWriter) writer;
        if (streamOpen == null) {
            out.addByteHeader(HDR_STATUS, STS_INITIALIZING);
        } else if (streamOpen) {
            out.addByteHeader(HDR_STATUS, STS_OK);
        } else {
            out.addByteHeader(HDR_STATUS, STS_CLOSED);
        }
    }

    @Override
    protected void endUpdates(MessageWriter writer) {
    }

}
