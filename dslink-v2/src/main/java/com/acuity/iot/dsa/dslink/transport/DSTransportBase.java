package com.acuity.iot.dsa.dslink.transport;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import org.iot.dsa.dslink.DSLinkConnection;
import org.iot.dsa.node.DSBytes;
import org.iot.dsa.node.DSNode;
import org.iot.dsa.util.DSException;

/**
 * Binds an DSLinkConnection to a binary or text transport implementation.  Examples of transports would
 * be sockets, websockets and http.
 * <p>
 * Subclasses should call and override all protected methods.
 *
 * @author Aaron Hansen
 */
public abstract class DSTransportBase extends DSNode {

    ///////////////////////////////////////////////////////////////////////////
    // Class Fields
    ///////////////////////////////////////////////////////////////////////////

    private static final int DEFAULT_READ_TIMEOUT = 30000;
    private static final byte[] EMPTY_BYTES = new byte[0];
    private static final int HEX_COLS = 30;

    ///////////////////////////////////////////////////////////////////////////
    // Instance Fields
    ///////////////////////////////////////////////////////////////////////////

    private InputStream binaryInput;
    private OutputStream binaryOutput;
    private RuntimeException closeException;
    private DSLinkConnection connection;
    private boolean open;
    private int readTimeout = DEFAULT_READ_TIMEOUT;
    private boolean text = true;
    private Reader textInput;
    private Writer textOutput;
    private StringBuilder traceIn;
    private int traceInCols = 0;
    private StringBuilder traceOut;
    private int traceOutCols = 0;
    private int writeSize = 0;

    ///////////////////////////////////////////////////////////////////////////
    // Public Methods
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Called at the start of a new inbound message.
     */
    public void beginRecvMessage() {
    }

    /**
     * Called at the start of a new outbound message.
     */
    public void beginSendMessage() {
    }

    /**
     * Close the actual connection and clean up resources.  Calling when already closed will have no
     * effect.
     */
    public void close() {
        synchronized (this) {
            if (!open) {
                return;
            }
            open = false;
            notifyAll();
        }
    }

    /**
     * Called at the end of a message, this trace logs the entire message.
     */
    public void endRecvMessage() {
        if (trace() && (traceIn != null)) {
            trace(traceIn.toString());
            traceIn.setLength(0);
            traceIn.append("Recv:\n");
            traceInCols = 0;
        }
    }

    /**
     * Called at the end of a message, this ensures that message based transports know the message
     * is complete and this also trace logs the entire message.
     */
    public void endSendMessage() {
        if (writeSize == 0) {
            return;
        }
        try {
            if (isText()) {
                doWrite("", true);
            } else {
                doWrite(EMPTY_BYTES, 0, 0, true);
            }
        } catch (Exception x) {
            close(x);
        }
        writeSize = 0;
        if (trace() && (traceOut != null)) {
            trace(traceOut.toString());
            traceOut.setLength(0);
            traceOut.append("Send:\n");
            traceOutCols = 0;
        }
    }

    /**
     * For reading binary data, only use if isText() is false.
     */
    public InputStream getBinaryInput() {
        if (binaryInput == null) {
            binaryInput = new MyInputStream();
        }
        return binaryInput;
    }

    /**
     * For writing binary data, only use if isText() is false.
     */
    public OutputStream getBinaryOutput() {
        if (binaryOutput == null) {
            binaryOutput = new MyOutputStream();
        }
        return binaryOutput;
    }

    /**
     * If not explicitly set, searches for the ancestor.
     */
    public DSLinkConnection getConnection() {
        if (connection == null) {
            connection = (DSLinkConnection) getAncestor(DSLinkConnection.class);
        }
        return connection;
    }

    /**
     * Read timeout in milliseconds, default is 30000.
     */
    public int getReadTimeout() {
        return readTimeout;
    }

    /**
     * For reading text, only use if isText() is true.
     */
    public Reader getTextInput() {
        if (textInput == null) {
            textInput = new MyReader();
        }
        return textInput;
    }

    /**
     * For writing text, only use if isText() is true.
     */
    public Writer getTextOutput() {
        if (textOutput == null) {
            textOutput = new MyWriter();
        }
        return textOutput;
    }

    /**
     * Whether or not the transport is open for reading and writing.
     */
    public boolean isOpen() {
        return open;
    }

    /**
     * True if the transport is text based, and the text IO methods should be used.
     */
    public boolean isText() {
        return text;
    }

    /**
     * Blocking read operation, returns the number of bytes read.
     */
    public int read(byte[] buf, int off, int len) {
        try {
            int ret = doRead(buf, off, len);
            if (trace()) {
                if (traceIn == null) {
                    traceIn = new StringBuilder();
                    traceIn.append("Recv:\n");
                }
                for (int i = 0; i < len; i++) {
                    if (traceInCols > 0) {
                        traceIn.append(' ');
                    }
                    DSBytes.toHex(buf[i], traceIn);
                    if (++traceInCols == HEX_COLS) {
                        traceInCols = 0;
                        traceIn.append('\n');
                    }
                }
            }
            return ret;
        } catch (Exception x) {
            close(x);
        }
        return -1;
    }

    /**
     * Blocking read operation, returns the number of chars read.
     */
    public int read(char[] buf, int off, int len) {
        try {
            int ret = doRead(buf, off, len);
            if (trace() && (ret > 0)) {
                if (traceIn == null) {
                    traceIn = new StringBuilder();
                    traceIn.append("Recv:\n");
                }
                traceIn.append(buf, off, len);
            }
        } catch (Exception x) {
            close(x);
        }
        return -1;
    }

    /**
     * If not explicitly set, will search for the ancestor.
     */
    public DSTransportBase setConnection(DSLinkConnection connection) {
        this.connection = connection;
        return this;
    }

    /**
     * Read timeout in milliseconds, default is 30000.
     */
    public DSTransportBase setReadTimeout(int timeout) {
        this.readTimeout = timeout;
        return this;
    }

    /**
     * Whether or not the transport is text based, default is true.
     */
    public DSTransportBase setText(boolean isText) {
        this.text = isText;
        return this;
    }

    /**
     * Write binary data, only use if isText() is false.
     *
     * @param buf    The buffer containing the bytes to write.
     * @param off    The index in the buffer of the first byte to write.
     * @param len    The number of bytes to write.
     * @param isLast Indicator of end of frame (message) for frame oriented transports such as
     *               websockets.
     */
    public void write(byte[] buf, int off, int len, boolean isLast) {
        if (!testOpen()) {
            throw new IllegalStateException("Transport closed");
        }
        if (trace()) {
            if (traceOut == null) {
                traceOut = new StringBuilder();
                traceOut.append("Send:\n");
            }
            for (int i = 0; i < len; i++) {
                if (traceOutCols > 0) {
                    traceOut.append(' ');
                }
                DSBytes.toHex(buf[i], traceIn);
                if (++traceOutCols == HEX_COLS) {
                    traceOutCols = 0;
                    traceOut.append('\n');
                }
            }
        }
        writeSize += len;
        try {
            doWrite(buf, off, len, isLast);
        } catch (Exception x) {
            close(x);
        }
    }

    /**
     * Write text, only use if isText() returns true.
     *
     * @param msgPart The characters to send
     * @param isLast  Whether or not this completes the message.
     */
    public void write(String msgPart, boolean isLast) {
        if (!testOpen()) {
            throw new IllegalStateException("Transport closed");
        }
        if (trace()) {
            if (traceOut == null) {
                traceOut = new StringBuilder();
                traceOut.append("Send:\n");
            }
            traceOut.append(msgPart);
        }
        writeSize += msgPart.length();
        try {
            doWrite(msgPart, isLast);
        } catch (Exception x) {
            close(x);
        }
    }

    /**
     * Write text, only use if isText() returns true.
     *
     * @param buf The characters to send
     * @param off Starting offset in the buf.
     * @param len Number of chars to write.
     * @param isLast  Whether or not this completes the message.
     */
    public void write(char[] buf, int off, int len, boolean isLast) {
        if (!testOpen()) {
            throw new IllegalStateException("Transport closed");
        }
        if (trace()) {
            if (traceOut == null) {
                traceOut = new StringBuilder();
                traceOut.append("Send:\n");
            }
            traceOut.append(buf, off, len);
        }
        writeSize += len;
        try {
            doWrite(buf, off, len, isLast);
        } catch (Exception x) {
            close(x);
        }
    }

    /**
     * The size of the current outbound message (bytes for binary, chars for text).
     */
    public int writeMessageSize() {
        return writeSize;
    }

    ///////////////////////////////////////////////////////////////////////////
    // Protected Methods
    ///////////////////////////////////////////////////////////////////////////

    /**
     * The number of bytes or chans available for reading.
     */
    protected abstract int available();

    /**
     * Also calls close().  Any attempts to read or write will have the reason thrown.
     */
    protected void close(Throwable reason) {
        if (open) {
            error("", reason);
            closeException = DSException.makeRuntime(reason);
            close();
        }
    }

    /**
     * Perform the actual read.  Simply throw an exception if there is a problem.
     */
    protected abstract int doRead(byte[] buf, int off, int len);

    /**
     * Perform the actual read.  Simply throw an exception if there is a problem.
     */
    protected abstract int doRead(char[] buf, int off, int len);

    /**
     * Perform the actual write.  Simply throw an exception if there is a problem.
     */
    protected abstract void doWrite(byte[] buf, int off, int len, boolean isLast);

    /**
     * Perform the actual write.  Simply throw an exception if there is a problem.
     */
    protected abstract void doWrite(String msgPart, boolean isLast);

    /**
     * This constructs a string from the parameters and call doWrite(String,isLast).  Override
     * if you can be more efficient.
     */
    protected void doWrite(char[] buf, int off, int len, boolean isLast) {
        if (len == 0) {
            doWrite("", isLast);
        } else {
            doWrite(new String(buf, off, len), isLast);
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // Package Methods
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Subclasses must call this when the stream is opened.
     */
    void setOpen() {
        open = true;
    }

    /**
     * Returns true if open, false if closed, throws an exception if was closed with an exception.
     */
    boolean testOpen() {
        if (!isOpen()) {
            if (closeException != null) {
                throw closeException;
            }
            return false;
        }
        return true;
    }

    ///////////////////////////////////////////////////////////////////////////
    // Private Methods
    ///////////////////////////////////////////////////////////////////////////

    ///////////////////////////////////////////////////////////////////////////
    // Inner Classes
    ///////////////////////////////////////////////////////////////////////////

    private class MyInputStream extends InputStream {

        byte[] buf = new byte[1];

        @Override
        public int available() {
            return DSTransportBase.this.available();
        }

        @Override
        public void close() {
            DSTransportBase.this.close();
        }

        @Override
        public int read() {
            int ret = DSTransportBase.this.read(buf, 0, 1);
            if (ret != 1) {
                return ret;
            }
            return buf[0];
        }

        @Override
        public int read(byte[] buf) {
            return DSTransportBase.this.read(buf, 0, buf.length);
        }

        @Override
        public int read(byte[] buf, int off, int len) {
            return DSTransportBase.this.read(buf, off, len);
        }

    } //MyInputStream

    private class MyOutputStream extends OutputStream {

        byte[] buf = new byte[1];

        @Override
        public void close() {
            DSTransportBase.this.close();
        }

        @Override
        public void write(byte[] b) {
            DSTransportBase.this.write(b, 0, b.length, false);
        }

        @Override
        public void write(byte[] b, int off, int len) {
            DSTransportBase.this.write(b, off, len, false);
        }

        @Override
        public void write(int b) {
            buf[0] = (byte) b;
            DSTransportBase.this.write(buf, 0, 1, false);
        }

    } //MyOutputStream

    private class MyReader extends Reader {

        char[] buf = new char[1];

        @Override
        public void close() {
            DSTransportBase.this.close();
        }

        @Override
        public int read() {
            int ret = DSTransportBase.this.read(buf, 0, 1);
            if (ret != 1) {
                return ret;
            }
            return buf[0];
        }

        @Override
        public int read(char[] buf) {
            return DSTransportBase.this.read(buf, 0, buf.length);
        }

        @Override
        public int read(char[] buf, int off, int len) {
            return DSTransportBase.this.read(buf, off, len);
        }

        @Override
        public boolean ready() {
            return DSTransportBase.this.available() > 0;
        }

    }//MyReader

    private class MyWriter extends Writer {

        @Override
        public void close() {
            DSTransportBase.this.close();
        }

        @Override
        public void flush() {
        }

        @Override
        public void write(int b) {
            char ch = (char) b;
            DSTransportBase.this.write(ch + "", false);
        }

        @Override
        public void write(char[] buf) {
            write(buf, 0, buf.length);
        }

        @Override
        public void write(char[] buf, int off, int len) {
            DSTransportBase.this.write(buf, off, len, false);
        }

        @Override
        public void write(String str) {
            DSTransportBase.this.write(str, false);
        }

    }//MyWriter

}
