package org.iot.dsa.dslink.websocket;

import com.acuity.iot.dsa.dslink.DSTransport;
import java.io.IOException;
import java.io.InputStream;
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
import org.iot.dsa.io.DSIReader;
import org.iot.dsa.io.DSIWriter;
import org.iot.dsa.io.DSIoException;
import org.iot.dsa.io.msgpack.MsgpackReader;
import org.iot.dsa.io.msgpack.MsgpackWriter;
import org.iot.dsa.util.DSException;

/**
 * Websocket client implementation of DSTransport based on Tyrus, the reference implementation of
 * JSR 356.
 *
 * @author Aaron Hansen
 */
@ClientEndpoint
public class MsgpackWsTranport extends DSTransport {

    ///////////////////////////////////////////////////////////////////////////
    // Constants
    ///////////////////////////////////////////////////////////////////////////

    ///////////////////////////////////////////////////////////////////////////
    // Fields
    ///////////////////////////////////////////////////////////////////////////

    private DSByteBuffer buffer = new DSByteBuffer();
    private ClientManager client;
    private DSLinkConnection connection;
    private int endMessageThreshold = 32768;
    private int messageSize;
    private boolean open = false;
    private MsgpackReader reader = new MsgpackReader(new MyInputStream());
    private Session session;
    private ByteBuffer writeBuffer = ByteBuffer.allocate(1024 * 64);
    private MsgpackWriter writer = new MsgpackWriter(writeBuffer);

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
        try {
            RemoteEndpoint.Basic basic = session.getBasicRemote();
            writeBuffer.flip();
            basic.sendBinary(writeBuffer, true);
            writeBuffer.clear();
            messageSize = 0;
        } catch (IOException x) {
            DSException.throwRuntime(x);
        }
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
            getConnection().onClose();
        }
    }

    @OnError
    public void onError(Session session, Throwable err) {
        severe(getConnectionUrl(), err);
        getConnection().onClose();
    }

    @OnMessage
    public void onMessage(Session session, byte[] msgPart, boolean isLast) {
        //isLast is ignored because we treat the incoming bytes as a pure stream.
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
            getConnection().onClose();
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

    /////////////////////////////////////////////////////////////////
    // Initialization
    /////////////////////////////////////////////////////////////////

}
