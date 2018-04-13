package org.iot.dsa.dslink.websocket;

import com.acuity.iot.dsa.dslink.transport.BufferedBinaryTransport;
import com.acuity.iot.dsa.dslink.transport.DSTransport;
import java.io.IOException;
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
import org.iot.dsa.util.DSException;

/**
 * Websocket client implementation of DSBinaryTransport based on Tyrus, the reference implementation
 * of JSR 356.
 *
 * @author Aaron Hansen
 */
@ClientEndpoint
public class WsBinaryTransport extends BufferedBinaryTransport {

    ///////////////////////////////////////////////////////////////////////////
    // Fields
    ///////////////////////////////////////////////////////////////////////////

    private ClientManager client;
    private Session session;
    private ByteBuffer writeBuffer;

    /////////////////////////////////////////////////////////////////
    // Methods - In alphabetical order by method name.
    /////////////////////////////////////////////////////////////////

    @Override
    protected void doWrite(byte[] buf, int off, int len, boolean isLast) {
        ByteBuffer byteBuffer = getByteBuffer(len);
        try {
            byteBuffer.put(buf, off, len);
            byteBuffer.flip();
            RemoteEndpoint.Basic basic = session.getBasicRemote();
            basic.sendBinary(byteBuffer, isLast);
            byteBuffer.clear();
        } catch (IOException x) {
            DSException.throwRuntime(x);
        }
    }

    /**
     * Called by write(), returns a bytebuffer for the given capacity ready for writing
     * (putting).  Attempts to reuse the same buffer as much as possible.
     */
    private ByteBuffer getByteBuffer(int len) {
        if (writeBuffer == null) {
            int tmp = 16 * 1024;
            while (tmp < len) {
                tmp += 1024;
            }
            writeBuffer = ByteBuffer.allocate(tmp);
        } else if (writeBuffer.capacity() < len) {
            int tmp = writeBuffer.capacity();
            while (tmp < len) {
                tmp += 1024;
            }
            writeBuffer = ByteBuffer.allocate(tmp);
        } else {
            writeBuffer.clear();
        }
        return writeBuffer;
    }

    @OnClose
    public void onClose(Session session, CloseReason reason) {
        info(getConnectionUrl() + " closed remotely, reason = " + reason.toString());
        close();
    }

    @OnError
    public void onError(Session session, Throwable err) {
        close(err);
    }

    @OnMessage
    public void onMessage(Session session, byte[] msgPart, boolean isLast) {
        receive(msgPart, 0, msgPart.length);
    }

    @OnOpen
    public void onOpen(Session session, EndpointConfig config) {
        this.session = session;
    }

    @Override
    public DSTransport open() {
        try {
            super.open();
            if (client == null) {
                client = ClientManager.createClient();
            }
            client.setDefaultMaxBinaryMessageBufferSize(64 * 1024);
            client.setDefaultMaxTextMessageBufferSize(64 * 1024);
            client.connectToServer(this, new URI(getConnectionUrl()));
            fine(fine() ? "Transport open" : null);
        } catch (Exception x) {
            DSException.throwRuntime(x);
        }
        return this;
    }


}
