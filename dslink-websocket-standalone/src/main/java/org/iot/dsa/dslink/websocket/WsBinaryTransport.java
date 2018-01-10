package org.iot.dsa.dslink.websocket;

import com.acuity.iot.dsa.dslink.transport.DSBinaryTransport;
import com.acuity.iot.dsa.dslink.transport.DSTransport;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.ByteBuffer;
import javax.websocket.*;
import org.glassfish.tyrus.client.ClientManager;
import org.iot.dsa.io.DSByteBuffer;
import org.iot.dsa.io.DSIoException;
import org.iot.dsa.util.DSException;

/**
 * Websocket client implementation of DSBinaryTransport based on Tyrus, the reference implementation
 * of JSR 356.
 *
 * @author Aaron Hansen
 */
@ClientEndpoint
public class WsBinaryTransport extends DSBinaryTransport {

    ///////////////////////////////////////////////////////////////////////////
    // Constants
    ///////////////////////////////////////////////////////////////////////////

    ///////////////////////////////////////////////////////////////////////////
    // Fields
    ///////////////////////////////////////////////////////////////////////////

    private DSByteBuffer buffer = new DSByteBuffer();
    private ClientManager client;
    private InputStream input = new MyInputStream();
    private int messageSize;
    private boolean open = false;
    private Session session;

    /////////////////////////////////////////////////////////////////
    // Methods - Constructors
    /////////////////////////////////////////////////////////////////

    /////////////////////////////////////////////////////////////////
    // Methods - In alphabetical order by method name.
    /////////////////////////////////////////////////////////////////

    @Override
    public DSTransport beginMessage() {
        return this;
    }

    @Override
    public DSTransport close() {
        if (!open) {
            return this;
        }
        open = false;
        try {
            if (session != null) {
                session.close();
            }
        } catch (Exception x) {
            finer(getConnection().getConnectionId(), x);
        }
        session = null;
        buffer.close();
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

    public int messageSize() {
        return messageSize;
    }

    @OnClose
    public void onClose(Session session, CloseReason reason) {
        if (open) {
            info(getConnectionUrl() + " remotely closed, reason = " + reason.toString());
            close();
        }
    }

    @OnError
    public void onError(Session session, Throwable err) {
        if (open) {
            open = false;
            severe(getConnectionUrl(), err);
            buffer.close(DSException.makeRuntime(err));
            if (err instanceof RuntimeException) {
                buffer.close((RuntimeException) err);
            } else {
                buffer.close(new DSException(err));
            }
        }
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
     * Flips the buffer, writes it, then clears it.
     */
    public void write(ByteBuffer buf, boolean isLast) {
        if (!open) {
            throw new DSIoException("Closed " + getConnectionUrl());
        }
        messageSize += buf.position();
        try {
            buf.flip();
            RemoteEndpoint.Basic basic = session.getBasicRemote();
            basic.sendBinary(buf, isLast);
            buf.clear();
            if (isLast) {
                messageSize = 0;
            }
        } catch (IOException x) {
            if (open) {
                close();
                DSException.throwRuntime(x);
            }
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
            WsBinaryTransport.this.close();
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

}
