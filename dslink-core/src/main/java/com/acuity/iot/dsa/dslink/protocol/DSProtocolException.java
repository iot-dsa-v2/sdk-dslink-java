package com.acuity.iot.dsa.dslink.protocol;

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

    /**
     * Optional.
     */
    public DSProtocolException setDetail(String arg) {
        detail = arg;
        return this;
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

}
