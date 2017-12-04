package com.acuity.iot.dsa.dslink.test;

import com.acuity.iot.dsa.dslink.transport.DSBinaryTransport;
import com.acuity.iot.dsa.dslink.transport.DSTransport;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import org.iot.dsa.dslink.DSLinkConnection;
import org.iot.dsa.io.DSByteBuffer;
import org.iot.dsa.io.DSIoException;

/**
 * Routes requests and responses back to self.
 *
 * @author Aaron Hansen
 */
public class TestTransport extends DSBinaryTransport {

    ///////////////////////////////////////////////////////////////////////////
    // Fields
    ///////////////////////////////////////////////////////////////////////////

    private DSByteBuffer buffer = new DSByteBuffer();
    private DSLinkConnection connection;
    private String connectionUri;
    private int messageSize;
    private boolean open = false;

    /////////////////////////////////////////////////////////////////
    // Methods - In alphabetical order by method name.
    /////////////////////////////////////////////////////////////////

    @Override
    public DSTransport beginMessage() {
        synchronized (buffer) {
            while (buffer.available() > 0) {
                try {
                    buffer.wait(1000);
                } catch (Exception ignore) {
                }
            }
        }
        return this;
    }

    @Override
    public DSTransport close() {
        if (!open) {
            return this;
        }
        open = false;
        return this;
    }

    @Override
    public DSTransport endMessage() {
        messageSize = 0;
        return this;
    }

    @Override
    public DSLinkConnection getConnection() {
        return connection;
    }

    @Override
    public InputStream getInput() {
        return new MyInputStream();
    }

    @Override
    public boolean isOpen() {
        return open;
    }

    @Override
    public int messageSize() {
        return buffer.length();
    }

    @Override
    public DSTransport open() {
        if (open) {
            return this;
        }
        open = true;
        buffer.open();
        return this;
    }

    /**
     * Returns the nextRun incoming byte, or -1 when end of stream has been reached.
     *
     * @throws DSIoException if there are any issues.
     */
    private int read() {
        return buffer.read();
    }

    /**
     * Reads incoming bytes into the given buffer.
     *
     * @param buf The buffer into which data is read.
     * @param off The start offset in the buffer to put data.
     * @param len The maximum number of bytes to read.
     * @return The number of bytes read or -1 for end of stream.
     * @throws DSIoException if there are any issues.
     */
    private int read(byte[] buf, int off, int len) {
        return buffer.read(buf, off, len);
    }

    @Override
    public DSTransport setConnection(DSLinkConnection connection) {
        this.connection = connection;
        return this;
    }

    @Override
    public DSTransport setConnectionUrl(String uri) {
        this.connectionUri = uri;
        return this;
    }

    @Override
    public DSTransport setReadTimeout(long millis) {
        buffer.setTimeout(millis);
        return this;
    }

    @Override
    public void write(ByteBuffer buf, boolean isLast) {
        buffer.put(buf);
    }

    /////////////////////////////////////////////////////////////////
    // Inner Classes
    /////////////////////////////////////////////////////////////////

    private class MyInputStream extends InputStream {

        @Override
        public int available() {
            return buffer.available();
        }

        @Override
        public void close() {
            TestTransport.this.close();
        }

        @Override
        public int read() throws IOException {
            if (!open) {
                if (buffer.available() == 0) {
                    throw new IOException("Closed " + connectionUri);
                }
            }
            return TestTransport.this.read();
        }

        @Override
        public int read(byte[] buf) throws IOException {
            if (!open) {
                if (buffer.available() == 0) {
                    throw new IOException("Closed " + connectionUri);
                }
            }
            return TestTransport.this.read(buf, 0, buf.length);
        }

        @Override
        public int read(byte[] buf, int off, int len) throws IOException {
            if (!open) {
                if (buffer.available() == 0) {
                    throw new IOException("Closed " + connectionUri);
                }
            }
            return TestTransport.this.read(buf, off, len);
        }
    }

    /*
    private class MyOutputStream extends OutputStream {

        @Override
        public void close() {
            TestTransport.this.close();
        }

        @Override
        public void write(int b) throws IOException {
            TestTransport.this.write(b);
        }

        @Override
        public void write(byte[] buf) throws IOException {
            TestTransport.this.write(buf, 0, buf.length, false);
        }

        @Override
        public void write(byte[] buf, int off, int len) throws IOException {
            TestTransport.this.write(buf, off, len, false);
        }

    }
    */

}
