package com.acuity.iot.dsa.dslink.transport;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.URI;
import javax.net.ssl.SSLSocketFactory;
import org.iot.dsa.util.DSException;

/**
 * Plain old socket DSTransport.
 *
 * @author Aaron Hansen
 */
public class SocketTransport extends StreamBinaryTransport {

    private InputStream in;
    private OutputStream out;
    private Socket socket;

    /////////////////////////////////////////////////////////////////
    // Methods - In alphabetical order by method name.
    /////////////////////////////////////////////////////////////////

    @Override
    protected void doClose() {
        try {
            if (in != null) {
                in.close();
            }
        } catch (Exception x) {
            trace(getConnection().getConnectionId(), x);
        }
        try {
            if (out != null) {
                out.close();
            }
        } catch (Exception x) {
            trace(getConnection().getConnectionId(), x);
        }
        try {
            if (socket != null) {
                socket.close();
            }
        } catch (Exception x) {
            trace(getConnection().getConnectionId(), x);
        }
        in = null;
        out = null;
        socket = null;
    }

    @Override
    public DSTransport open() {
        if (isOpen()) {
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
            init(socket.getInputStream(), socket.getOutputStream());
            debug(debug() ? "SocketTransport open" : null);
        } catch (Exception x) {
            DSException.throwRuntime(x);
        }
        return this;
    }

}
