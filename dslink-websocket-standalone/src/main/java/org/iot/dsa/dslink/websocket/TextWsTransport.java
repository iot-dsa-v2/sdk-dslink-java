package org.iot.dsa.dslink.websocket;

import com.acuity.iot.dsa.dslink.DSTransport;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.net.URI;
import java.util.logging.Logger;
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
import org.iot.dsa.io.DSCharBuffer;
import org.iot.dsa.io.DSIoException;
import org.iot.dsa.io.DSIReader;
import org.iot.dsa.io.DSIWriter;
import org.iot.dsa.io.json.JsonAppender;
import org.iot.dsa.io.json.JsonReader;
import org.iot.dsa.logging.DSLogger;
import org.iot.dsa.logging.DSLogging;
import org.iot.dsa.util.DSException;

/**
 * Websocket client implementation of DSTransport based on Tyrus, the reference
 * implementation of JSR 356.
 *
 * @author Aaron Hansen
 */
@ClientEndpoint
public class TextWsTransport extends DSLogger implements DSTransport {

    ///////////////////////////////////////////////////////////////////////////
    // Constants
    ///////////////////////////////////////////////////////////////////////////

    private static final int BUF_SIZE = 8192;

    ///////////////////////////////////////////////////////////////////////////
    // Fields
    ///////////////////////////////////////////////////////////////////////////

    private DSCharBuffer buffer = new DSCharBuffer();
    private ClientManager client;
    private DSLinkConnection connection;
    private String connectionUri;
    private int endMessageThreshold = 32768;
    private Logger logger;
    private int messageSize;
    private boolean open = false;
    private DSIReader reader;
    private Session session;
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
        write("", true);
        messageSize = 0;
        return this;
    }

    @Override
    public DSLinkConnection getConnection() {
        return connection;
    }

    @Override
    public Logger getLogger() {
        if (logger == null) {
            logger = Logger.getLogger(connection.getLink().getLinkName() + ".transport");
        }
        return logger;
    }

    @Override
    public DSIReader getReader() {
        if (reader == null) {
            reader = new JsonReader(new MyReader());
        }
        return reader;
    }

    /**
     * @return Null if not open.
     */
    public Session getSession() {
        return session;
    }

    @Override
    public DSIWriter getWriter() {
        if (writer == null) {
            writer = new JsonAppender(new MyWriter()).setPrettyPrint(false);
        }
        return writer;
    }

    @Override
    public boolean isOpen() {
        return open;
    }

    @OnClose
    public void onClose(Session session, CloseReason reason) {
        if (open) {
            info(connectionUri + " remotely closed, reason = " + reason.toString());
            getConnection().close();
        }
    }

    @OnError
    public void onError(Session session, Throwable err) {
        severe(connectionUri, err);
        getConnection().close();
    }

    @OnMessage
    public void onMessage(Session session, String msgPart, boolean isLast) {
        try {
            finest(finest() ? "Recv: " + msgPart : null);
            buffer.put(msgPart);
        } catch (Exception x) {
            getConnection().close();
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
            client.connectToServer(this, new URI(connectionUri));
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
    private int read(char[] buf, int off, int len) {
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

    public boolean shouldEndMessage() {
        return messageSize > endMessageThreshold;
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
            throw new DSIoException("Closed " + connectionUri);
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
            severe(connectionUri, x);
            connection.close();
        }
    }

    /////////////////////////////////////////////////////////////////
    // Inner Classes
    /////////////////////////////////////////////////////////////////

    private class MyReader extends Reader {

        @Override
        public void close() {
            getConnection().close();
        }

        @Override
        public int read() throws IOException {
            return buffer.read();
        }

        @Override
        public int read(char[] buf) throws IOException {
            return buffer.read(buf, 0, buf.length);
        }

        @Override
        public int read(char[] buf, int off, int len) throws IOException {
            return buffer.read(buf, off, len);
        }

        @Override
        public boolean ready() {
            return buffer.available() > 0;
        }

    }

    private class MyWriter extends Writer {

        @Override
        public void close() {
            getConnection().close();
        }

        @Override
        public void flush() {
        }

        @Override
        public void write(int b) throws IOException {
            char ch = (char) b;
            TextWsTransport.this.write(ch + "", false);
        }

        @Override
        public void write(char[] buf) throws IOException {
            write(buf, 0, buf.length);
        }

        @Override
        public void write(char[] buf, int off, int len) throws IOException {
            TextWsTransport.this.write(new String(buf, off, len), false);
        }

        @Override
        public void write(String str) throws IOException {
            TextWsTransport.this.write(str, false);
        }

    }

    /////////////////////////////////////////////////////////////////
    // Initialization
    /////////////////////////////////////////////////////////////////

}//TyrusTransportFactory
