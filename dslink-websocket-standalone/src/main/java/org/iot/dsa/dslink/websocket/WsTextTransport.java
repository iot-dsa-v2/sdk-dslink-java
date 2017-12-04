package org.iot.dsa.dslink.websocket;

import com.acuity.iot.dsa.dslink.transport.DSTextTransport;
import com.acuity.iot.dsa.dslink.transport.DSTransport;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.net.URI;
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
import org.iot.dsa.io.DSCharBuffer;
import org.iot.dsa.io.DSIoException;
import org.iot.dsa.util.DSException;

/**
 * Websocket client implementation of DSTextTransport based on Tyrus, the reference implementation
 * of JSR 356.
 *
 * @author Aaron Hansen
 */
@ClientEndpoint
public class WsTextTransport extends DSTextTransport {

    ///////////////////////////////////////////////////////////////////////////
    // Fields
    ///////////////////////////////////////////////////////////////////////////

    private DSCharBuffer buffer = new DSCharBuffer();
    private ClientManager client;
    private int messageSize;
    private Reader myReader;
    private Writer myWriter;
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
            finer(getConnection().getConnectionId(), x);
        }
        open = false;
        buffer.close();
        return this;
    }

    @Override
    public DSTransport endMessage() {
        write("", true);
        messageSize = 0;
        return this;
    }

    public Reader getReader() {
        if (myReader == null) {
            myReader = new MyReader();
        }
        return myReader;
    }

    public Writer getWriter() {
        if (myWriter == null) {
            myWriter = new MyWriter();
        }
        return myWriter;
    }

    @Override
    public boolean isOpen() {
        return open;
    }

    @Override
    public int messageSize() {
        return messageSize;
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
    public void onMessage(Session session, String msgPart, boolean isLast) {
        try {
            finest(finest() ? "Recv: " + msgPart : null);
            buffer.put(msgPart);
        } catch (Exception x) {
            getConnection().onClose();
            DSException.throwRuntime(x);
        }
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
     * Send the buffer and indicate whether it represents the end of the message.
     *
     * @param text   The characters to send
     * @param isLast Whether or not this completes the message.
     * @throws DSIoException If there are any issue.
     */
    public void write(String text, boolean isLast) {
        if (!open) {
            throw new DSIoException("Closed " + getConnectionUrl());
        }
        try {
            RemoteEndpoint.Basic basic = session.getBasicRemote();
            int len = text.length();
            messageSize += len;
            if (len > 0) {
                finest(finest() ? "Send: " + text : null);
            }
            basic.sendText(text, isLast);
        } catch (IOException x) {
            severe(getConnectionUrl(), x);
            getConnection().onClose();
        }
    }

    /////////////////////////////////////////////////////////////////
    // Inner Classes
    /////////////////////////////////////////////////////////////////

    private class MyReader extends Reader {

        @Override
        public void close() {
            getConnection().onClose();
        }

        @Override
        public int read() throws IOException {
            if (!open) {
                if (buffer.available() == 0) {
                    return -1;
                }
            }
            return buffer.read();
        }

        @Override
        public int read(char[] buf) throws IOException {
            if (!open) {
                if (buffer.available() == 0) {
                    return -1;
                }
            }
            return buffer.read(buf, 0, buf.length);
        }

        @Override
        public int read(char[] buf, int off, int len) throws IOException {
            if (!open) {
                if (buffer.available() == 0) {
                    return -1;
                }
            }
            return buffer.read(buf, off, len);
        }

        @Override
        public boolean ready() {
            return buffer.available() > 0;
        }

    }//MyReader

    private class MyWriter extends Writer {

        @Override
        public void close() {
            getConnection().onClose();
        }

        @Override
        public void flush() {
        }

        @Override
        public void write(int b) throws IOException {
            char ch = (char) b;
            WsTextTransport.this.write(ch + "", false);
        }

        @Override
        public void write(char[] buf) throws IOException {
            write(buf, 0, buf.length);
        }

        @Override
        public void write(char[] buf, int off, int len) throws IOException {
            WsTextTransport.this.write(new String(buf, off, len), false);
        }

        @Override
        public void write(String str) throws IOException {
            WsTextTransport.this.write(str, false);
        }

    }//MyWriter

}
