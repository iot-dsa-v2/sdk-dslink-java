package com.acuity.iot.dsa.dslink.transport;

import java.io.IOException;
import java.net.Socket;
import java.net.URI;
import javax.net.ssl.SSLSocketFactory;
import org.iot.dsa.DSRuntime;
import org.iot.dsa.util.DSException;

/**
 * Plain old socket DSTransport.
 *
 * @author Aaron Hansen
 */
public class SocketTransport extends DSTransportStream {

    private Socket socket;

    @Override
    public void close() {
        super.close();
        try {
            if (socket != null) {
                socket.close();
                socket = null;
            }
        } catch (IOException x) {
            debug(x.getMessage(), x);
        }
    }

    @Override
    public void open() {
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
            socket.setSoTimeout(getReadTimeout());
            open(socket.getInputStream(), socket.getOutputStream());
            DSRuntime.run(new Reader());
            debug(debug() ? "SocketTransport open" : null);
        } catch (Exception x) {
            DSException.throwRuntime(x);
        }
    }

    private class Reader implements Runnable {

        @Override
        public void run() {
            while (isOpen()) {
                getConnection().getSession().recvMessage(false);
            }
        }
    }

}
