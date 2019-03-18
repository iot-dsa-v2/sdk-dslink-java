package com.acuity.iot.dsa.dslink.protocol.v1;

import com.acuity.iot.dsa.dslink.protocol.DSSession;
import com.acuity.iot.dsa.dslink.protocol.message.MessageWriter;
import com.acuity.iot.dsa.dslink.protocol.message.OutboundMessage;
import org.iot.dsa.io.DSIWriter;

/**
 * Responder uses to close streams without errors.
 *
 * @author Aaron Hansen
 */
public class CloseMessage implements OutboundMessage {

    private boolean method = false;
    private Integer rid;

    public CloseMessage(Integer requestId) {
        this.rid = requestId;
    }

    public CloseMessage(Integer requestId, boolean method) {
        this.rid = requestId;
        this.method = method;
    }

    @Override
    public boolean canWrite(DSSession session) {
        return true;
    }

    @Override
    public boolean write(DSSession session, MessageWriter writer) {
        DSIWriter out = writer.getWriter();
        out.beginMap().key("rid").value(rid);
        if (method) {
            out.key("method").value("close");
        } else {
            out.key("stream").value("closed");
        }
        out.endMap();
        return true;
    }

}
