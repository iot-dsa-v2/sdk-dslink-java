package com.acuity.iot.dsa.dslink.transport;

import com.acuity.iot.dsa.dslink.io.DSByteBuffer;
import com.acuity.iot.dsa.dslink.io.DSCharBuffer;
import com.acuity.iot.dsa.dslink.io.DSIoException;
import com.acuity.iot.dsa.dslink.sys.cert.SysCertService;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URI;
import java.nio.ByteBuffer;
import javax.websocket.CloseReason;
import javax.websocket.ContainerProvider;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.RemoteEndpoint;
import javax.websocket.Session;
import javax.websocket.WebSocketContainer;
import org.iot.dsa.util.DSException;

/**
 * Abstract websocket transport.
 *
 * @author Aaron Hansen
 */
public abstract class DSTransportWs extends DSTransport {

    ///////////////////////////////////////////////////////////////////////////
    // Class Fields
    ///////////////////////////////////////////////////////////////////////////

    ///////////////////////////////////////////////////////////////////////////
    // Instance Fields
    ///////////////////////////////////////////////////////////////////////////

    private RemoteEndpoint.Basic basicRemote;
    private DSByteBuffer binReadBuffer;
    private ByteBuffer binWriteBuffer;
    private Session session;
    private DSCharBuffer textReadBuffer;

    ///////////////////////////////////////////////////////////////////////////
    // Public Methods
    ///////////////////////////////////////////////////////////////////////////

    @Override
    public void close() {
        super.close();
        try {
            if (session != null) {
                session.close();
                session = null;
            }
        } catch (IOException x) {
            debug("", x);
        }
    }

    @OnClose
    public void onClose(CloseReason reason) {
        if (isOpen()) {
            info("Remotely closed: " + reason.toString());
            close();
        }
    }

    @OnError
    public void onError(Throwable err) {
        if (isOpen()) {
            close(err);
        }
    }

    @OnMessage
    public void onMessage(byte[] buf, boolean isLast) {
        if (!isOpen()) {
            return;
        }
        synchronized (this) {
            getBinReadBuffer().put(buf, 0, buf.length);
            notifyAll();
        }
    }


    @OnMessage
    public void onMessage(String msgPart, boolean isLast) {
        if (!isOpen()) {
            return;
        }
        synchronized (this) {
            getTextReadBuffer().put(msgPart);
            notifyAll();
        }
    }

    @OnOpen
    public void onOpen(Session session) {
        this.session = session;
        this.basicRemote = session.getBasicRemote();
        setOpen();
    }

    ///////////////////////////////////////////////////////////////////////////
    // Protected Methods
    ///////////////////////////////////////////////////////////////////////////

    @Override
    protected int available() {
        if (isText()) {
            return getTextReadBuffer().available();
        }
        return getBinReadBuffer().available();
    }

    @Override
    protected int doRead(byte[] buf, int off, int len) {
        DSByteBuffer readBuf = getBinReadBuffer();
        synchronized (this) {
            if (testOpen() && readBuf.available() == 0) {
                try {
                    wait(getReadTimeout());
                } catch (Exception x) {
                    debug("", x);
                }
            }
            if (readBuf.available() > 0) {
                return readBuf.sendTo(buf, off, len);
            }
            if (!testOpen()) {
                return -1;
            }
            throw new DSIoException("Read timeout");
        }
    }

    @Override
    protected int doRead(char[] buf, int off, int len) {
        DSCharBuffer readBuf = getTextReadBuffer();
        synchronized (this) {
            if (testOpen() && readBuf.available() == 0) {
                try {
                    wait(getReadTimeout());
                } catch (Exception x) {
                    debug("", x);
                }
            }
            if (readBuf.available() > 0) {
                return readBuf.read(buf, off, len);
            }
            if (!testOpen()) {
                return -1;
            }
            throw new DSIoException("Read timeout");
        }
    }

    @Override
    protected void doWrite(byte[] buf, int off, int len, boolean isLast) {
        ByteBuffer byteBuffer = getBinWriteBuffer(len);
        if (len > 0) {
            byteBuffer.put(buf, off, len);
        }
        try {
            if (isLast) {
                byteBuffer.flip();
                basicRemote.sendBinary(byteBuffer, isLast);
                byteBuffer.clear();
            }
        } catch (IOException x) {
            throw new UncheckedIOException(x);
        }
    }

    @Override
    protected void doWrite(String msgPart, boolean isLast) {
        try {
            basicRemote.sendText(msgPart, isLast);
        } catch (IOException x) {
            throw new UncheckedIOException(x);
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // Private Methods
    ///////////////////////////////////////////////////////////////////////////

    private DSByteBuffer getBinReadBuffer() {
        if (binReadBuffer == null) {
            binReadBuffer = new DSByteBuffer();
        }
        return binReadBuffer;
    }

    /**
     * Attempts to reuse the existing byte buffer, but will allocate another if we need to grow.
     */
    private ByteBuffer getBinWriteBuffer(int len) {
        if (binWriteBuffer == null) {
            int tmp = 16 * 1024;
            while (tmp < len) {
                tmp += 1024;
            }
            binWriteBuffer = ByteBuffer.allocate(tmp);
        } else if (binWriteBuffer.capacity() < len) {
            int tmp = binWriteBuffer.capacity();
            while (tmp < len) {
                tmp += 1024;
            }
            binWriteBuffer = ByteBuffer.allocate(tmp);
        } else {
            binWriteBuffer.clear();
        }
        return binWriteBuffer;
    }

    private DSCharBuffer getTextReadBuffer() {
        if (textReadBuffer == null) {
            textReadBuffer = new DSCharBuffer();
        }
        return textReadBuffer;
    }

}
