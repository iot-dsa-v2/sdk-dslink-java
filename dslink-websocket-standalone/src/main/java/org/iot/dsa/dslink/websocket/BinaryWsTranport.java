package org.iot.dsa.dslink.websocket;

import com.acuity.iot.dsa.dslink.DSTransport;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.nio.ByteBuffer;
import javax.websocket.ClientEndpoint;
import javax.websocket.CloseReason;
import javax.websocket.EndpointConfig;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.RemoteEndpoint;
import javax.websocket.Session;
import org.glassfish.tyrus.client.ClientManager;
import org.iot.dsa.dslink.DSLinkConnection;
import org.iot.dsa.io.DSByteBuffer;
import org.iot.dsa.io.DSIoException;
import org.iot.dsa.io.DSIReader;
import org.iot.dsa.io.DSIWriter;
import org.iot.dsa.logging.DSLogger;
import org.iot.dsa.util.DSException;

/**
 * Websocket client implementation of DSTransport based on Tyrus, the reference
 * implementation of JSR 356.
 *
 * @author Aaron Hansen
 */
@ClientEndpoint
public class BinaryWsTranport extends DSTransport {

    ///////////////////////////////////////////////////////////////////////////
    // Constants
    ///////////////////////////////////////////////////////////////////////////

    private static final int BUF_SIZE = 8192;
    private static final byte[] EMPTY = new byte[0];

    ///////////////////////////////////////////////////////////////////////////
    // Fields
    ///////////////////////////////////////////////////////////////////////////

    private DSByteBuffer buffer = new DSByteBuffer();
    private ClientManager client;
    private DSLinkConnection connection;
    private int endMessageThreshold = 32768;
    private int messageSize;
    private boolean open = false;
    private DSIReader reader;
    private Session session;
    private ByteBuffer writeBuffer = ByteBuffer.allocate(BUF_SIZE);
    private DSIWriter writer;

    /////////////////////////////////////////////////////////////////
    // Methods - Constructors
    /////////////////////////////////////////////////////////////////

    /////////////////////////////////////////////////////////////////
    // Methods - In alphabetical order by method name.
    /////////////////////////////////////////////////////////////////

    @Override
    public DSTransport beginMessage() {
        messageSize = 0;
        return this;
    }

    @Override
    public DSTransport close() {
        if (!open) {
            return this;
        }
        try {
            if (session != null) {
                session.close();
            }
            session = null;
        } catch (Exception x) {
            finer(connection.getConnectionId(), x);
        }
        open = false;
        buffer.close();
        return this;
    }

    @Override
    public DSTransport endMessage() {
        write(EMPTY, 0, 0, true);
        messageSize = 0;
        return this;
    }

    @Override
    public DSLinkConnection getConnection() {
        return connection;
    }

    @Override
    public DSIReader getReader() {
        if (reader == null) {
            reader = null; //TODO
        }
        return reader;
    }

    @Override
    public DSIWriter getWriter() {
        if (writer == null) {
            writer = null; //TODO
        }
        return writer;
    }

    /**
     * @return Null if not open.
     */
    public Session getSession() {
        return session;
    }

    @Override
    public boolean isOpen() {
        return open;
    }

    @OnClose
    public void onClose(Session session, CloseReason reason) {
        if (open) {
            info(getConnectionUrl() + " remotely closed, reason = " + reason.toString());
            getConnection().close();
        }
    }

    @OnError
    public void onError(Session session, Throwable err) {
        severe(getConnectionUrl(), err);
        getConnection().close();
    }

    @OnMessage
    public void onMessage(Session session, byte[] msgPart, boolean isLast) {
        //isLast is ignored because we treat the incoming bytes as a pure
        //stream.  Wish we could do the same with outbound...
        buffer.put(msgPart, 0, msgPart.length);
    }

    @OnOpen
    public void onOpen(Session session, EndpointConfig config) {
        open = true;
        this.session = session;
    }

    @Override
    public DSTransport open() {
        try {
            if (open) {
                return this;
            }
            if (client == null) {
                client = ClientManager.createClient();
            }
            client.connectToServer(this, new URI(getConnectionUrl()));
            buffer.open();
            open = true;
            fine(fine() ? "Transport open" : null);
        } catch (Exception x) {
            DSException.throwRuntime(x);
        }
        return this;
    }

    /**
     * Returns the next incoming byte, or -1 when end of stream has been reached.
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
    public DSTransport setReadTimeout(long millis) {
        super.setReadTimeout(millis);
        buffer.setTimeout(millis);
        return this;
    }

    public boolean shouldEndMessage() {
        return messageSize > endMessageThreshold;
    }

    public void write(int b) {
        try {
            if (!open) {
                throw new IOException("Closed " + getConnectionUrl());
            }
            messageSize++;
            RemoteEndpoint.Basic basic = session.getBasicRemote();
            writeBuffer.put((byte) b);
            basic.sendBinary(writeBuffer, false);
            writeBuffer.clear();
        } catch (IOException x) {
            severe(getConnectionUrl(), x);
            connection.close();
        }
    }

    /**
     * Send the buffer and indicate whether it represents the end of the message.
     *
     * @param buf    The bytes to send.
     * @param idx    The index of the first byte to write.
     * @param len    The number of bytes to send.
     * @param isLast Whether or not this completes the message.
     * @throws DSIoException If there are any issue.
     */
    public void write(byte[] buf, int idx, int len, boolean isLast) {
        if (!open) {
            throw new DSIoException("Closed " + getConnectionUrl());
        }
        try {
            RemoteEndpoint.Basic basic = session.getBasicRemote();
            messageSize += len;
            while (len > BUF_SIZE) {
                writeBuffer.put(buf, 0, BUF_SIZE);
                basic.sendBinary(writeBuffer, false);
                idx += BUF_SIZE;
                len -= BUF_SIZE;
                writeBuffer.clear();
            }
            if (len > 0) {
                writeBuffer.put(buf, idx, len);
                basic.sendBinary(writeBuffer, isLast);
                writeBuffer.clear();
            }
        } catch (IOException x) {
            severe(getConnectionUrl(), x);
            connection.close();
        }
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
            getConnection().close();
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
            getConnection().close();
        }

        @Override
        public void write(int b) throws IOException {
            BinaryWsTranport.this.write(b);
        }

        @Override
        public void write(byte[] buf) throws IOException {
            BinaryWsTranport.this.write(buf, 0, buf.length, false);
        }

        @Override
        public void write(byte[] buf, int off, int len) throws IOException {
            BinaryWsTranport.this.write(buf, off, len, false);
        }

    }

    /////////////////////////////////////////////////////////////////
    // Initialization
    /////////////////////////////////////////////////////////////////

}//TyrusTransportFactory
