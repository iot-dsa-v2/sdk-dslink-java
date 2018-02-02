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
    // Fields
    ///////////////////////////////////////////////////////////////////////////

    private int messageSize;
    private OutputStream out;
    private boolean open = false;
    private Socket socket;

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
        if (open) {
            throw new IllegalStateException("Already open");
        }
        try {
            socket = new Socket(getConnectionUrl(), 443);
            open = true;
            fine(fine() ? "SocketTransport open" : null);
        } catch (Exception x) {
            DSException.throwRuntime(x);
        }
        return this;
    }

    /**
     * Write the bytes to the socket, isLast is ignored.
     */
    public void write(byte[] buf, int off, int len, boolean isLast) {
        try {
            messageSize += len;
            if (out == null) {
                out = socket.getOutputStream();
            }
            out.write(buf, 0, len);
        } catch (IOException x) {
            DSException.throwRuntime(x);
        }
    }

}
