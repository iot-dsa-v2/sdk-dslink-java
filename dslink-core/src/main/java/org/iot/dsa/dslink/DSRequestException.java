package org.iot.dsa.dslink;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * Indicates something was wrong with a request.
 *
 * @author Aaron Hansen
 */
public class DSRequestException extends RuntimeException {

    /////////////////////////////////////////////////////////////////
    // Fields
    /////////////////////////////////////////////////////////////////

    private String detail;

    /////////////////////////////////////////////////////////////////
    // Constructors
    /////////////////////////////////////////////////////////////////

    public DSRequestException() {
    }

    public DSRequestException(String message) {
        super(message);
    }

    /////////////////////////////////////////////////////////////////
    // Methods - Public and in alphabetical order by method name.
    /////////////////////////////////////////////////////////////////

    /**
     * Additional information to supply to the remote endpoint.
     */
    public String getDetail() {
        if (detail == null) {
            setDetail(this);
        }
        return detail;
    }

    /**
     * Overrides the default detail which is the stack trace of this exception.
     */
    public void setDetail(String detail) {
        this.detail = detail;
    }

    /**
     * Overrides the default detail which is the stack trace of this exception.
     */
    public void setDetail(Throwable arg) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        arg.printStackTrace(pw);
        pw.close();
        detail = sw.toString();
    }

}
