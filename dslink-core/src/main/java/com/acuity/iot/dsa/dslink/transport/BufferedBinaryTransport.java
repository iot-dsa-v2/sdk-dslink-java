package com.acuity.iot.dsa.dslink.transport;

import com.acuity.iot.dsa.dslink.io.DSByteBuffer;
import com.acuity.iot.dsa.dslink.io.DSIoException;
import java.io.InputStream;
import org.iot.dsa.node.DSBytes;
import org.iot.dsa.util.DSException;

/**
 * For binary transports that do not have an InputStream (such as websockets).  Subclasses
 * should call receive() for incoming bytes.
 *
 * @author Aaron Hansen
 */
public abstract class BufferedBinaryTransport extends DSBinaryTransport {

    ///////////////////////////////////////////////////////////////////////////
    // Fields
    ///////////////////////////////////////////////////////////////////////////

    private static final int DEBUG_COLS = 30;

    private RuntimeException closeException;
    private InputStream input = new MyInputStream();
    private int messageSize;
    private boolean open = false;
    private DSByteBuffer readBuffer = new DSByteBuffer();
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
            traceIn.append("Recv:\n");
        }
    }

    @Override
    public void beginSendMessage() {
        if (trace()) {
            if (traceOut == null) {
                traceOut = new StringBuilder();
            }
            traceOut.append("Send:\n");
        }
    }

    protected void close(Throwable reason) {
        closeException = DSException.makeRuntime(reason);
        close();
    }

    @Override
    public DSTransport close() {
        synchronized (this) {
            if (!open) {
                return this;
            }
            open = false;
            notifyAll();
        }
        return this;
    }

    /**
     * Called by the write method.
     */
    protected abstract void doWrite(byte[] buf, int off, int len, boolean isLast);

    @Override
    public void endRecvMessage() {
        if (trace()) {
            if (traceIn != null) {
                if (traceIn.length() > 6) {
                    trace(traceIn.toString());
                }
                traceIn.setLength(0);
                traceInSize = 0;
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
                traceOutSize = 0;
            }
        } else if (traceOut != null) {
            traceOut = null;
        }
    }

    @Override
    public InputStream getInput() {
        return input;
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
     * Overrides must call super to set the open state.
     */
    public DSTransport open() {
        open = true;
        return this;
    }

    /**
     * Call this for all incoming bytes.
     */
    protected void receive(byte[] bytes, int off, int len) {
        if (!testOpen()) {
            throw new IllegalStateException("Transport closed");
        }
        synchronized (this) {
            readBuffer.put(bytes, 0, len);
            notifyAll();
        }
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

    /**
     * Handles logging and calls doWrite.
     */
    public final void write(byte[] buf, int off, int len, boolean isLast) {
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
        doWrite(buf, off, len, isLast);
    }

    /////////////////////////////////////////////////////////////////
    // Inner Classes
    /////////////////////////////////////////////////////////////////

    private class MyInputStream extends InputStream {

        @Override
        public int available() {
            return readBuffer.available();
        }

        @Override
        public void close() {
            BufferedBinaryTransport.this.close();
        }

        @Override
        public int read() {
            synchronized (BufferedBinaryTransport.this) {
                if (testOpen() && readBuffer.available() == 0) {
                    try {
                        BufferedBinaryTransport.this.wait(getReadTimeout());
                    } catch (Exception x) {
                    }
                }
                if (!testOpen()) {
                    return -1;
                }
                if (readBuffer.available() == 0) {
                    throw new DSIoException("Read timeout");
                }
                int ch = readBuffer.read();
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
        }

        @Override
        public int read(byte[] buf) {
            if (buf.length == 0) {
                if (!testOpen()) {
                    return -1;
                }
                return 0;
            }
            synchronized (BufferedBinaryTransport.this) {
                if (testOpen() && readBuffer.available() == 0) {
                    try {
                        BufferedBinaryTransport.this.wait(getReadTimeout());
                    } catch (Exception x) {
                    }
                }
                if (!testOpen()) {
                    return -1;
                }
                if (readBuffer.available() == 0) {
                    throw new DSIoException("Read timeout");
                }
                int ret = readBuffer.sendTo(buf, 0, buf.length);
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

        @Override
        public int read(byte[] buf, int off, int len) {
            if (len == 0) {
                if (!testOpen()) {
                    return -1;
                }
                return 0;
            }
            synchronized (BufferedBinaryTransport.this) {
                if (testOpen() && readBuffer.available() == 0) {
                    try {
                        BufferedBinaryTransport.this.wait(getReadTimeout());
                    } catch (Exception x) {
                    }
                }
                if (!testOpen()) {
                    return -1;
                }
                if (readBuffer.available() == 0) {
                    throw new DSIoException("Read timeout");
                }
                int ret = readBuffer.sendTo(buf, off, len);
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

}
