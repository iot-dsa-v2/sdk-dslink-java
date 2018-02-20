package com.acuity.iot.dsa.dslink.protocol.message;

import com.acuity.iot.dsa.dslink.DSProtocolException;
import java.io.PrintWriter;
import java.io.StringWriter;
import org.iot.dsa.dslink.DSInvalidPathException;
import org.iot.dsa.dslink.DSPermissionException;
import org.iot.dsa.dslink.DSRequestException;
import org.iot.dsa.io.DSIWriter;
import org.iot.dsa.util.DSException;

/**
 * Encapsulates a full error response message.
 *
 * @author Aaron Hansen
 */
public class ErrorResponse extends AbstractMessage implements OutboundMessage {

    /////////////////////////////////////////////////////////////////
    // Fields
    /////////////////////////////////////////////////////////////////

    private String detail;
    private String msg;
    private Phase phase = Phase.REQUEST;
    private String type;

    /////////////////////////////////////////////////////////////////
    // Constructors
    /////////////////////////////////////////////////////////////////

    public ErrorResponse() {
        setStream("closed");
    }

    public ErrorResponse(Throwable exception) {
        setStream("closed");
        parse(exception);
    }

    /////////////////////////////////////////////////////////////////
    // Methods - In alphabetical order by method name.
    /////////////////////////////////////////////////////////////////

    /**
     * Extracts information from the exception and returns this.
     */
    public ErrorResponse parse(Throwable reason) {
        if (reason instanceof DSException) {
            Throwable tmp = reason.getCause();
            if (tmp != null) {
                reason = tmp;
            }
        }
        msg = reason.getMessage();
        if (reason instanceof DSProtocolException) {
            DSProtocolException x = (DSProtocolException) reason;
            detail = x.getDetail();
            phase = x.getPhase();
            type = x.getType();
        } else if (reason instanceof DSRequestException) {
            DSRequestException x = (DSRequestException) reason;
            setMethod(x.getMessage());
            setDetail(x.getDetail());
            if (reason instanceof DSInvalidPathException) {
                setType("invalidPath");
            } else if (reason instanceof DSPermissionException) {
                setType("permissionDenied");
            } else {
                setType("invalidRequest");
            }
        } else {
            setType("serverError");
            setDetail(reason);
            setPhase(Phase.RESPONSE);
        }
        return this;
    }

    /**
     * Optional.
     */
    public ErrorResponse setDetail(String arg) {
        detail = arg;
        return this;
    }

    /**
     * Optional.
     */
    public ErrorResponse setDetail(Throwable arg) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        arg.printStackTrace(pw);
        pw.close();
        detail = sw.toString();
        return this;
    }

    /**
     * Only needs to be called for responses, the default is request.
     */
    public ErrorResponse setPhase(Phase arg) {
        phase = arg;
        return this;
    }

    /**
     * Optional.
     */
    public ErrorResponse setType(String arg) {
        type = arg;
        return this;
    }

    /**
     * Calls the super implementation then writes the error object and closes the entire response
     * object.
     */
    public void write(MessageWriter writer) {
        super.write(writer);
        DSIWriter out = writer.getWriter();
        out.key("error").beginMap();
        if (type != null) {
            out.key("type").value(type);
        }
        if (phase == Phase.RESPONSE) {
            out.key("phase").value("response");
        }
        if (msg != null) {
            out.key("msg").value(msg);
        }
        if (detail != null) {
            out.key("detail").value(detail);
        }
        out.endMap().endMap();
    }

    /////////////////////////////////////////////////////////////////
    // Inner Classes - in alphabetical order by class name.
    /////////////////////////////////////////////////////////////////

    public enum Phase {
        REQUEST,
        RESPONSE
    }

}
