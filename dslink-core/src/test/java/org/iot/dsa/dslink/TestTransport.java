package org.iot.dsa.dslink;

import com.acuity.iot.dsa.dslink.DSTransport;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.logging.Level;
import org.iot.dsa.io.DSByteBuffer;
import org.iot.dsa.io.DSIoException;
import org.iot.dsa.io.DSReader;
import org.iot.dsa.io.DSWriter;
import org.iot.dsa.io.json.JsonReader;
import org.iot.dsa.io.json.JsonWriter;

/**
 * Routes requests and responses back to self.
 *
 * @author Aaron Hansen
 */
public class TestTransport implements DSTransport {

    ///////////////////////////////////////////////////////////////////////////
    // Constants
    ///////////////////////////////////////////////////////////////////////////

    ///////////////////////////////////////////////////////////////////////////
    // Fields
    ///////////////////////////////////////////////////////////////////////////

    private DSByteBuffer buffer = new DSByteBuffer();
    private DSLinkConnection connection;
    private String connectionUri;
    private int endMessageThreshold = 32768;
    private int messageSize;
    private boolean open = false;
    private DSReader reader;
    private DSWriter writer;

    /////////////////////////////////////////////////////////////////
    // Methods - Constructors
    /////////////////////////////////////////////////////////////////

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
        buffer.close();
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
    public DSReader getReader() {
        if (reader == null) {
            reader = new JsonReader(new MyInputStream(), "UTF-8");
        }
        return reader;
    }

    @Override
    public DSWriter getWriter() {
        if (writer == null) {
            writer = new JsonWriter(new MyOutputStream());
        }
        return writer;
    }

    @Override
    public boolean isOpen() {
        return open;
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
        synchronized (buffer) {
            buffer.notify();
            return buffer.read();
        }
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
        synchronized (buffer) {
            buffer.notify();
            return buffer.read(buf, off, len);
        }
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

    public boolean shouldEndMessage() {
        return messageSize > endMessageThreshold;
    }

    public void write(int b) {
        try {
            if (!open) {
                throw new IOException("Closed " + connectionUri);
            }
            messageSize++;
            buffer.put((byte) b);
        } catch (IOException x) {
            connection.getLink().getLogger().log(Level.SEVERE, connectionUri, x);
            close();
        }
    }

    /**
     * Send the buffer and indicate whether it represents the end of the message.
     *
     * @param buf    The bytes to send.
     * @param len    The number of bytes to send from index 0.
     * @param isLast Whether or not this completes the message.
     * @throws DSIoException If there are any issue.
     */
    public void write(byte[] buf, int off, int len, boolean isLast) {
        if (!open) {
            throw new DSIoException("Closed " + connectionUri);
        }
        messageSize += len;
        buffer.put(buf, 0, len);
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
            return buffer.read();
        }

        @Override
        public int read(byte[] buf) throws IOException {
            return buffer.read(buf, 0, buf.length);
        }

        @Override
        public int read(byte[] buf, int off, int len) throws IOException {
            return buffer.read(buf, off, len);
        }
    }

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

    /////////////////////////////////////////////////////////////////
    // Initialization
    /////////////////////////////////////////////////////////////////

}//TyrusWsTransport
