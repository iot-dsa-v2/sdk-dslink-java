package com.acuity.iot.dsa.dslink.transport;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.URI;
import javax.net.ssl.SSLSocketFactory;
import org.iot.dsa.node.DSBytes;
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

    private static final int DEBUG_COLS = 30;

    private int debugInSize;
    private int debugOutSize;
    private int messageSize;
    private OutputStream out;
    private boolean open = false;
    private Socket socket;

    /////////////////////////////////////////////////////////////////
    // Methods - In alphabetical order by method name.
    /////////////////////////////////////////////////////////////////

    @Override
    public DSTransport beginRecvMessage() {
        debugInSize = 0;
        return this;
    }

    @Override
    public DSTransport beginSendMessage() {
        debugOutSize = 0;
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
            debug(getConnection().getConnectionId(), x);
        }
        try {
            if (socket != null) {
                socket.close();
            }
        } catch (Exception x) {
            debug(getConnection().getConnectionId(), x);
        }
        out = null;
        socket = null;
        open = false;
        return this;
    }

    @Override
    public DSTransport endSendMessage() {
        return this;
    }

    @Override
    public InputStream getInput() {
        InputStream ret = null;
        try {
            ret = socket.getInputStream();
            if (getDebugIn() != null) {
                ret = new DebugInputStream(ret);
            }
        } catch (IOException x) {
            DSException.throwRuntime(x);
        }
        return ret;
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
            String url = getConnectionUrl();
            URI uri = new URI(url);
            String scheme = uri.getScheme();
            String host = uri.getHost();
            int port = uri.getPort();
            if ("dss".equals(scheme)) {
                if (port < 0) {
                    port = 4128;
                }
                socket = SSLSocketFactory.getDefault().createSocket(host, port);
            } else if ("ds".equals(scheme)) {
                if (port < 0) {
                    port = 4120;
                }
                socket = new Socket(host, port);
            } else {
                throw new IllegalArgumentException("Invalid broker URI: " + url);
            }
            socket.setSoTimeout((int) getReadTimeout());
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
            StringBuilder debug = getDebugOut();
            if (debug != null) {
                for (int i = off, j = off + len; i < j; i++) {
                    if (debugOutSize > 0) {
                        debug.append(' ');
                    }
                    DSBytes.toHex(buf[i], debug);
                    if (++debugOutSize == DEBUG_COLS) {
                        debugOutSize = 0;
                        debug.append('\n');
                    }
                }
            }
            messageSize += len;
            if (out == null) {
                out = socket.getOutputStream();
            }
            out.write(buf, 0, len);
        } catch (IOException x) {
            DSException.throwRuntime(x);
        }
    }

    // Inner Classes
    // -------------

    private class DebugInputStream extends InputStream {

        private InputStream inner;

        public DebugInputStream(InputStream inner) {
            this.inner = inner;
        }

        @Override
        public int read() throws IOException {
            StringBuilder debug = getDebugIn();
            int ch = inner.read();
            if (debug != null) {
                if (debugInSize > 0) {
                    debug.append(' ');
                }
                DSBytes.toHex((byte) ch, debug);
                if (++debugInSize == DEBUG_COLS) {
                    debugInSize = 0;
                    debug.append('\n');
                }
            }
            return ch;
        }

        @Override
        public int read(byte[] buf) throws IOException {
            int ret = inner.read(buf);
            StringBuilder debug = getDebugOut();
            if ((debug != null) && (ret > 0)) {
                for (int i = 0; i < ret; i++) {
                    if (debugInSize > 0) {
                        debug.append(' ');
                    }
                    DSBytes.toHex(buf[i], debug);
                    if (++debugInSize == DEBUG_COLS) {
                        debugInSize = 0;
                        debug.append('\n');
                    }
                }
            }
            return ret;
        }

        @Override
        public int read(byte[] buf, int off, int len) throws IOException {
            int ret = inner.read(buf, off, len);
            StringBuilder debug = getDebugOut();
            if ((debug != null) && (ret > 0)) {
                for (int i = off, j = off + ret; i < j; i++) {
                    if (debugInSize > 0) {
                        debug.append(' ');
                    }
                    DSBytes.toHex(buf[i], debug);
                    if (++debugInSize == DEBUG_COLS) {
                        debugInSize = 0;
                        debug.append('\n');
                    }
                }
            }
            return ret;
        }

    } //DebugInputStream

}
