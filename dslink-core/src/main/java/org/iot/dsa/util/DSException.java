package org.iot.dsa.util;

import java.io.PrintStream;
import java.io.PrintWriter;

/**
 * An runtime exception that forwards most calls to the inner exception.
 * This is to exclude itself from reporting and expose the real issue as soon as possible.
 * <p>
 * The throwRuntime method is a convenience for converting checked exceptions into runtime
 * exceptions.
 *
 * @author Aaron Hansen
 */
public class DSException extends RuntimeException {

    /////////////////////////////////////////////////////////////////
    // Constructors
    /////////////////////////////////////////////////////////////////

    public DSException(Exception inner) {
        this.inner = inner;
    }

    /////////////////////////////////////////////////////////////////
    // Methods - Public and in alphabetical order by method name.
    /////////////////////////////////////////////////////////////////

    @Override
    public Throwable getCause() {
        return null;
    }

    @Override
    public String getMessage() { return inner.getMessage(); }

    @Override
    public String getLocalizedMessage() {
        return inner.getLocalizedMessage();
    }

    @Override
    public StackTraceElement[] getStackTrace() {
        return inner.getStackTrace();
    }

    @Override
    public void printStackTrace() {
        inner.printStackTrace();
    }

    @Override
    public void printStackTrace(PrintStream out) {
        inner.printStackTrace(out);
    }

    @Override
    public void printStackTrace(PrintWriter out) {
        inner.printStackTrace(out);
    }

    @Override
    public Throwable initCause(Throwable cause) {
        return this;
    }

    /**
     * If the given exception is already a runtime exception, it is rethrown,
     * otherwise it will be thrown wrapped by an instance of this class.
     */
    public static void throwRuntime(Exception x) {
        if (x instanceof RuntimeException) {
            throw (RuntimeException) x;
        }
        throw new DSException(x);
    }

    public String toString() {
        return inner.toString();
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
    // Constants - in alphabetical order by field name.
    /////////////////////////////////////////////////////////////////

    /////////////////////////////////////////////////////////////////
    // Facets - in alphabetical order by field name.
    /////////////////////////////////////////////////////////////////

    private Exception inner;

    /////////////////////////////////////////////////////////////////
    // Initialization
    /////////////////////////////////////////////////////////////////

}
