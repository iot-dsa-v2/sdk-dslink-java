package com.acuity.iot.dsa.dslink.transport;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import org.iot.dsa.util.DSException;

/**
 * Plain old socket DSTransport.
 *
 * @author Aaron Hansen
 */
public class SocketTransport extends DSBinaryTransport {

    ///////////////////////////////////////////////////////////////////////////
    // Constants
    ///////////////////////////////////////////////////////////////////////////

    ///////////////////////////////////////////////////////////////////////////
    // Fields
    ///////////////////////////////////////////////////////////////////////////

    private byte[] buffer = new byte[8192];
    private int messageSize;
    private OutputStream out;
    private boolean open = false;
    private Socket socket;

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
        try {
            if (out != null) {
                out.close();
            }
        } catch (Exception x) {
            finer(getConnection().getConnectionId(), x);
        }
        try {
            if (socket != null) {
                socket.close();
            }
        } catch (Exception x) {
            finer(getConnection().getConnectionId(), x);
        }
        out = null;
        socket = null;
        open = false;
        return this;
    }

    @Override
    public DSTransport endMessage() {
        return this;
    }

    @Override
    public InputStream getInput() {
        try {
            return socket.getInputStream();
        } catch (IOException x) {
            DSException.throwRuntime(x);
        }
        return null; //unreachable
    }

    @Override
    public boolean isOpen() {
        if ((socket == null) || socket.isClosed()) {
            return false;
        }
        return true;
    }

    public int messageSize() {
        return messageSize;
    }

    @Override
    public DSTransport open() {
        try {
            if (open) {
                return this;
            }
            socket = new Socket(getConnectionUrl(), 443);
            open = true;
            fine(fine() ? "SocketTransport open" : null);
        } catch (Exception x) {
            DSException.throwRuntime(x);
        }
        return this;
    }

    /**
     * Flips the buffer, writes it, then clears it.
     */
    public void write(ByteBuffer buf, boolean isLast) {
        messageSize += buf.position();
        try {
            if (out == null) {
                out = socket.getOutputStream();
            }
            buf.flip();
            int len = buf.remaining();
            while (len > 0) {
                len = Math.min(buf.remaining(), buffer.length);
                buf.get(buffer, 0, len);
                out.write(buffer, 0, len);
                len = buf.remaining();
            }
            buf.clear();
            if (isLast) {
                messageSize = 0;
            }
        } catch (IOException x) {
            DSException.throwRuntime(x);
        }
    }

}
