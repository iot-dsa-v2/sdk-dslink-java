package com.acuity.iot.dsa.dslink.transport;

import com.acuity.iot.dsa.dslink.io.DSByteBuffer;
import com.acuity.iot.dsa.dslink.io.DSIoException;
import java.io.IOException;
import java.io.InputStream;
import org.iot.dsa.util.DSException;

/**
 * For transports that push values for reading (such as the TestTransport and websockets).
 *
 * @author Aaron Hansen
 */
public abstract class PushBinaryTransport extends DSBinaryTransport {

    ///////////////////////////////////////////////////////////////////////////
    // Fields
    ///////////////////////////////////////////////////////////////////////////

    private RuntimeException closeException;
    private InputStream input = new MyInputStream();
    private boolean open = false;
    private DSByteBuffer readBuffer = new DSByteBuffer();

    /////////////////////////////////////////////////////////////////
    // Methods - In alphabetical order by method name.
    /////////////////////////////////////////////////////////////////

    @Override
    public DSTransport beginMessage() {
        return this;
    }

    protected void close(Throwable reason) {
        closeException = DSException.makeRuntime(reason);
        close();
    }

    @Override
    public DSTransport close() {
        synchronized (this) {
            open = false;
            notifyAll();
        }
        return this;
    }

    @Override
    public DSTransport endMessage() {
        return this;
    }

    @Override
    public InputStream getInput() {
        return input;
    }

    @Override
    public boolean isOpen() {
        return open;
    }

    /**
     * Call super to set the open state.
     */
    public DSTransport open() {
        open = true;
        return this;
    }

    /**
     * Call this to push bytes.
     */
    protected void push(byte[] bytes, int off, int len) {
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
            PushBinaryTransport.this.close();
        }

        @Override
        public int read() throws IOException {
            synchronized (PushBinaryTransport.this) {
                if (testOpen() && readBuffer.available() == 0) {
                    try {
                        PushBinaryTransport.this.wait(getReadTimeout());
                    } catch (Exception x) {
                    }
                }
                if (!testOpen()) {
                    return -1;
                }
                if (readBuffer.available() == 0) {
                    throw new DSIoException("Read timeout");
                }
                return readBuffer.read();
            }
        }

        @Override
        public int read(byte[] buf) throws IOException {
            if (buf.length == 0) {
                if (!testOpen()) {
                    return -1;
                }
                return 0;
            }
            synchronized (PushBinaryTransport.this) {
                if (testOpen() && readBuffer.available() == 0) {
                    try {
                        PushBinaryTransport.this.wait(getReadTimeout());
                    } catch (Exception x) {
                    }
                }
                if (!testOpen()) {
                    return -1;
                }
                if (readBuffer.available() == 0) {
                    throw new DSIoException("Read timeout");
                }
                return readBuffer.sendTo(buf, 0, buf.length);
            }
        }

        @Override
        public int read(byte[] buf, int off, int len) throws IOException {
            if (len == 0) {
                if (!testOpen()) {
                    return -1;
                }
                return 0;
            }
            synchronized (PushBinaryTransport.this) {
                if (testOpen() && readBuffer.available() == 0) {
                    try {
                        PushBinaryTransport.this.wait(getReadTimeout());
                    } catch (Exception x) {
                    }
                }
                if (!testOpen()) {
                    return -1;
                }
                if (readBuffer.available() == 0) {
                    throw new DSIoException("Read timeout");
                }
                return readBuffer.sendTo(buf, off, len);
            }
        }
    }

}
