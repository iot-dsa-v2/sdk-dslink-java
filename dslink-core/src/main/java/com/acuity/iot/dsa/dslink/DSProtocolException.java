package com.acuity.iot.dsa.dslink;

import com.acuity.iot.dsa.dslink.protocol.message.ErrorResponse.Phase;
import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * For encoding errors at the protocol level.
 *
 * @author Aaron Hansen
 */
public class DSProtocolException extends RuntimeException {

    /////////////////////////////////////////////////////////////////
    // Fields
    /////////////////////////////////////////////////////////////////

    private String detail;
    private Phase phase = Phase.REQUEST;
    private String type;

    /////////////////////////////////////////////////////////////////
    // Constructors
    /////////////////////////////////////////////////////////////////

    public DSProtocolException(String msg) {
        super(msg);
    }

    /////////////////////////////////////////////////////////////////
    // Methods - Public and in alphabetical order by method name.
    /////////////////////////////////////////////////////////////////

    public String getDetail() {
        return detail;
    }

    public Phase getPhase() {
        return phase;
    }

    public String getType() {
        return type;
    }

    /**
     * Optional.
     */
    public DSProtocolException setDetail(Throwable arg) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        arg.printStackTrace(pw);
        pw.close();
        detail = sw.toString();
        return this;
    }

    /**
     * Only needs called for responses, the default is request.
     */
    public DSProtocolException setPhase(Phase arg) {
        phase = arg;
        return this;
    }

    /**
     * Optional.
     */
    public DSProtocolException setType(String arg) {
        type = arg;
        return this;
    }

    /////////////////////////////////////////////////////////////////
    // Methods - Protected and in alphabetical order by method name.
    /////////////////////////////////////////////////////////////////

    /////////////////////////////////////////////////////////////////
    // Methods - Package and in alphabetical order by method name.
    /////////////////////////////////////////////////////////////////

    /////////////////////////////////////////////////////////////////
    // Methods - Private and in alphabetical order by method name.
    /////////////////////////////////////////////////////////////////

    /////////////////////////////////////////////////////////////////
    // Inner Classes - in alphabetical order by class name.
    /////////////////////////////////////////////////////////////////

    /////////////////////////////////////////////////////////////////
    // Facets - in alphabetical order by field name.
    /////////////////////////////////////////////////////////////////

    /////////////////////////////////////////////////////////////////
    // Initialization
    /////////////////////////////////////////////////////////////////

}
