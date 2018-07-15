package com.acuity.iot.dsa.dslink.protocol.v1.responder;

import com.acuity.iot.dsa.dslink.protocol.DSSession;
import com.acuity.iot.dsa.dslink.protocol.message.MessageWriter;
import com.acuity.iot.dsa.dslink.protocol.message.OutboundMessage;
import java.io.PrintWriter;
import java.io.StringWriter;
import org.iot.dsa.dslink.DSInvalidPathException;
import org.iot.dsa.dslink.DSPermissionException;
import org.iot.dsa.dslink.DSRequestException;
import org.iot.dsa.io.DSIWriter;

/**
 * Responder uses to close streams without errors.
 *
 * @author Aaron Hansen
 */
class ErrorMessage implements OutboundMessage {

    private static String SERVER_ERROR;
    private String message;
    private Integer rid;
    private String type = SERVER_ERROR;

    public ErrorMessage(Integer requestId, String message) {
        this.rid = requestId;
        this.message = message;
    }

    public ErrorMessage(Integer requestId, Throwable reason) {
        this.rid = requestId;
        this.message = toString(reason);
        if (reason instanceof DSRequestException) {
            if (reason instanceof DSInvalidPathException) {
                setType("invalidPath");
            } else if (reason instanceof DSPermissionException) {
                setType("permissionDenied");
            } else {
                setType("invalidRequest");
            }
        } else {
            setType("serverError");
        }
    }

    @Override
    public boolean canWrite(DSSession session) {
        return true;
    }

    public ErrorMessage setType(String type) {
        this.type = type;
        return this;
    }

    @Override
    public void write(DSSession session, MessageWriter writer) {
        DSIWriter out = writer.getWriter();
        out.beginMap()
           .key("rid").value(rid)
           .key("stream").value("closed");
        out.key("error").beginMap()
           .key("type").value(type)
           .key("msg").value(message)
           .endMap();
        out.endMap();
    }

    private String toString(Throwable arg) {
        String msg = arg.getMessage();
        if ((msg != null) && (msg.length() > 0)) {
            return msg;
        }
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        arg.printStackTrace(pw);
        pw.close();
        return sw.toString();
    }

}
