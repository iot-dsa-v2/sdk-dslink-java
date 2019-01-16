package com.acuity.iot.dsa.dslink.transport;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import org.iot.dsa.node.DSBytes;
import org.iot.dsa.util.DSException;

/**
 * For stream based binary transports.  Subclasses must call init(InputStream,OutputStream).
 *
 * @author Aaron Hansen
 */
public abstract class StreamBinaryTransport extends DSBinaryTransport {

    ///////////////////////////////////////////////////////////////////////////
    // Fields
    ///////////////////////////////////////////////////////////////////////////

    private static final int DEBUG_COLS = 30;

    private RuntimeException closeException;
    private InputStream innerIn;
    private OutputStream innerOut;
    private int messageSize;
    private boolean open = false;
    private InputStream outerIn = new MyInputStream();
    private StringBuilder traceIn;
    private int traceInSize = 0;
    private StringBuilder traceOut;
    private int traceOutSize = 0;

    /////////////////////////////////////////////////////////////////
    // Methods - In alphabetical order by method name.
    /////////////////////////////////////////////////////////////////

    @Override
    public void beginRecvMessage() {
        if (trace()) {
            if (traceIn == null) {
                traceIn = new StringBuilder();
            }
            traceInSize = 0;
            traceIn.append("Recv:\n");
        }
    }

    @Override
    public void beginSendMessage() {
        if (trace()) {
            if (traceOut == null) {
                traceOut = new StringBuilder();
            }
            traceOutSize = 0;
            traceOut.append("Send:\n");
        }
    }

    @Override
    public final DSTransport close() {
        synchronized (this) {
            if (!open) {
                return this;
            }
            open = false;
            notifyAll();
        }
        doClose();
        return this;
    }

    @Override
    public void endRecvMessage() {
        if (trace()) {
            if (traceIn != null) {
                if (traceIn.length() > 6) {
                    trace(traceIn.toString());
                }
                traceIn.setLength(0);
            }
        } else if (traceIn != null) {
            traceIn = null;
        }
    }

    @Override
    public void endSendMessage() {
        if (trace()) {
            if (traceOut != null) {
                if (traceOut.length() > 6) {
                    trace(traceOut.toString());
                }
                traceOut.setLength(0);
            }
        } else if (traceOut != null) {
            traceOut = null;
        }
    }

    @Override
    public InputStream getInput() {
        if (trace()) {
            return outerIn;
        }
        return innerIn;
    }

    /**
     * Must be called by the open() implementation.
     */
    public void init(InputStream in, OutputStream out) {
        this.innerIn = in;
        this.innerOut = out;
        open = true;
    }

    @Override
    public boolean isOpen() {
        return open;
    }

    @Override
    public int messageSize() {
        return messageSize;
    }

    /**
     * Handles logging and calls doWrite.
     */
    public void write(byte[] buf, int off, int len, boolean isLast) {
        if (trace()) {
            for (int i = off, j = off + len; i < j; i++) {
                if (traceOutSize > 0) {
                    traceOut.append(' ');
                }
                DSBytes.toHex(buf[i], traceOut);
                if (++traceOutSize == DEBUG_COLS) {
                    traceOutSize = 0;
                    traceOut.append('\n');
                }
            }
        }
        if (isLast) {
            messageSize = 0;
        } else {
            messageSize += len;
        }
        try {
            innerOut.write(buf, off, len);
        } catch (Exception x) {
            DSException.throwRuntime(x);
        }
    }

    protected final void close(Throwable reason) {
        closeException = DSException.makeRuntime(reason);
        close();
    }

    /**
     * Subclass to perform any cleanup, this does nothing.
     */
    protected void doClose() {
    }

    /**
     * Returns true if open, false if closed, throws and exception if closed with an exception.
     */
    protected boolean testOpen() {
        if (!open) {
            if (closeException != null) {
                throw closeException;
            }
            return false;
        }
        return true;
    }

    /////////////////////////////////////////////////////////////////
    // Inner Classes
    /////////////////////////////////////////////////////////////////

    private class MyInputStream extends InputStream {

        @Override
        public int available() throws IOException {
            return innerIn.available();
        }

        @Override
        public void close() {
            StreamBinaryTransport.this.close();
        }

        @Override
        public int read() throws IOException {
            if (!testOpen()) {
                return -1;
            }
            int ch = innerIn.read();
            if (ch == -1) {
                return ch;
            }
            if (trace()) {
                if (traceInSize > 0) {
                    traceIn.append(' ');
                }
                DSBytes.toHex((byte) ch, traceIn);
                if (++traceInSize == DEBUG_COLS) {
                    traceInSize = 0;
                    traceIn.append('\n');
                }
            }
            return ch;
        }

        @Override
        public int read(byte[] buf) throws IOException {
            if (!testOpen()) {
                return -1;
            }
            int ret = innerIn.read(buf);
            if (trace() && (ret > 0)) {
                for (int i = 0; i < ret; i++) {
                    if (traceInSize > 0) {
                        traceIn.append(' ');
                    }
                    DSBytes.toHex(buf[i], traceIn);
                    if (++traceInSize == DEBUG_COLS) {
                        traceInSize = 0;
                        traceIn.append('\n');
                    }
                }
            }
            return ret;
        }

        @Override
        public int read(byte[] buf, int off, int len) throws IOException {
            if (!testOpen()) {
                return -1;
            }
            int ret = innerIn.read(buf, off, len);
            if (trace() && (ret > 0)) {
                for (int i = 0; i < ret; i++) {
                    if (traceInSize > 0) {
                        traceIn.append(' ');
                    }
                    DSBytes.toHex(buf[i], traceIn);
                    if (++traceInSize == DEBUG_COLS) {
                        traceInSize = 0;
                        traceIn.append('\n');
                    }
                }
            }
            return ret;
        }
    }

}
