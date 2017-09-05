package com.acuity.iot.dsa.dslink;

import com.acuity.iot.dsa.dslink.protocol.protocol_v1.DS1Protocol;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.util.logging.Logger;
import org.iot.dsa.dslink.DSLink;
import org.iot.dsa.dslink.DSLinkConfig;
import org.iot.dsa.dslink.DSLinkConnection;
import org.iot.dsa.io.DSBase64;
import org.iot.dsa.io.json.JsonReader;
import org.iot.dsa.io.json.JsonWriter;
import org.iot.dsa.logging.DSLogging;
import org.iot.dsa.node.DSList;
import org.iot.dsa.node.DSMap;
import org.iot.dsa.time.DSTime;
import org.iot.dsa.util.DSException;

/**
 * The default connection implementation. Performs connection initialization with the broker, then
 * creates a transport and a protocol based on the broker response.
 *
 * @author Aaron Hansen
 */
public class DSConnection extends DSLinkConnection {

    ///////////////////////////////////////////////////////////////////////////
    // Constants
    ///////////////////////////////////////////////////////////////////////////

    private static final String DSA_VERSION = "1.1.2";
    //private static final String[] SUPPORTED_FORMATS = new String[]{"msgpack", "json"};
    private static final String[] SUPPORTED_FORMATS = new String[]{"json"};

    ///////////////////////////////////////////////////////////////////////////
    // Fields
    ///////////////////////////////////////////////////////////////////////////

    private String authToken;
    private String brokerUri;
    private String connectionId;
    private DSMap connectionInitResponse;
    private DSLink link;
    private Logger logger;
    private Object mutex = new Object();
    private DSProtocol protocol;
    private long reconnectRate = 1000;
    private DSTransport transport;

    ///////////////////////////////////////////////////////////////////////////
    // Constructors
    ///////////////////////////////////////////////////////////////////////////

    ///////////////////////////////////////////////////////////////////////////
    // Methods
    ///////////////////////////////////////////////////////////////////////////

    @Override
    public void close() {
        if (isOpen()) {
            DSTransport tpt = transport;
            if (tpt != null) {
                tpt.close();
            }
            DSProtocol ptl = protocol;
            if (ptl != null) {
                if (link.getRequester() != null) {
                    link.getRequester().onDisconnected(ptl.getRequesterSession());
                }
                ptl.close();
            }
        }
    }

    @Override
    public String getConnectionId() {
        if (connectionId == null) {
            StringBuilder builder = new StringBuilder();
            builder.append(link.getLinkName()).append("-");
            String uri = link.getConfig().getBrokerUri();
            if ((uri != null) && !uri.isEmpty()) {
                int idx = uri.indexOf("://") + 3;
                if ((idx > 0) && (uri.length() > idx)) {
                    int end = uri.indexOf("/", idx);
                    if (end > idx) {
                        builder.append(uri.substring(idx, end));
                    } else {
                        builder.append(uri.substring(idx));
                    }
                }
            } else {
                builder.append(Integer.toHexString(hashCode()));
            }
            connectionId = builder.toString();
            info(info() ? "Connection ID: " + connectionId : null);
        }
        return connectionId;
    }

    /**
     * Returns the brokers response to connection initialization.
     */
    public DSMap getConnectionInitResponse() {
        return connectionInitResponse;
    }

    @Override
    public DSLink getLink() {
        return link;
    }

    /**
     * The default logger for this connection, the logging name will be the connection ID.
     */
    @Override
    public Logger getLogger() {
        if (logger == null) {
            logger = DSLogging.getLogger(getLink().getLinkName() + "-connection");
        }
        return logger;
    }

    /**
     * Makes a DSA connection initialization request and returns the response map if successful.
     *
     * @return The json map representing the response from the server.
     * @throws Exception if there are any issues.
     */
    protected DSMap initializeConnection() throws Exception {
        String uri = makeBrokerUrl();
        config(config() ? "Broker URI " + uri : null);
        HttpURLConnection conn = (HttpURLConnection) new URL(uri).openConnection();
        conn.setDoInput(true);
        conn.setDoOutput(true);
        conn.setInstanceFollowRedirects(true);
        conn.setUseCaches(false);
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        //write the request map
        writeConnectionRequest(conn);
        //read response
        int rc = conn.getResponseCode();
        fine(fine() ? "Connection initialization response code " + rc : null);
        if ((rc < 200) || (rc >= 300)) {
            throwConnectionException(conn, rc);
        }
        JsonReader in = null;
        try {
            /*
            InputStream i = conn.getInputStream(); //TODO
            int ch = i.read();
            while (ch >= 0) {
                System.out.print((char)ch);
                ch = i.read();
            }
            if (i != null) {
                i.close();
                throw new IllegalStateException("Testing");
            }
            */
            in = new JsonReader(conn.getInputStream(), "UTF-8");
            DSMap res = in.getMap();
            finest(finest() ? res : null);
            in.close();
            return res;
        } finally {
            if (in != null) {
                in.close();
            }
        }
    }

    @Override
    public boolean isOpen() {
        DSTransport tpt = transport;
        if (tpt == null) {
            return false;
        }
        return tpt.isOpen();
    }

    /**
     * Adds dsId and maybe authToken parameters to the query string.
     *
     * @see DSLinkConfig#getBrokerUri()
     * @see DSLinkConfig#getToken()
     */
    protected String makeBrokerUrl() {
        StringBuilder builder = new StringBuilder();
        String uri = brokerUri;
        if ((uri == null) || uri.isEmpty() || !uri.contains("://")) {
            throw new IllegalArgumentException("Invalid broker uri: " + uri);
        }
        builder.append(uri);
        if (uri.indexOf('?') < 0) {
            builder.append('?');
        } else {
            builder.append('&');
        }
        builder.append("dsId=").append(getLink().getDsId());
        if ((authToken != null) && !authToken.isEmpty()) {
            builder.append("&token=").append(authToken);
        }
        return builder.toString();
    }

    /**
     * Looks at the connection initialization response to determine the protocol implementation.
     */
    protected DSProtocol makeProtocol() {
        String version = connectionInitResponse.get("version", "");
        if (!version.startsWith("1.1.2")) {
            throw new IllegalStateException("Unsupported version: " + version);
        }
        return new DS1Protocol();
    }

    /**
     * Looks at the connection initialization response to determine the type of transport then
     * instantiates the correct type fom the config.
     */
    protected DSTransport makeTransport() {
        DSTransport.Factory factory = null;
        DSTransport transport = null;
        try {
            String type = link.getConfig().getConfig(
                    DSLinkConfig.CFG_TRANSPORT_FACTORY,
                    "org.iot.dsa.dslink.websocket.StandaloneTransportFactory");
            factory = (DSTransport.Factory) Class.forName(type).newInstance();
            transport = factory.makeTransport(connectionInitResponse);
        } catch (Exception x) {
            DSException.throwRuntime(x);
        }
        String wsUri = connectionInitResponse.get("wsUri", null);
        if (wsUri == null) {
            throw new IllegalStateException("Only websocket transports are supported.");
        }
        String uri = makeWsUrl(wsUri);
        config(config() ? "Connection URL = " + uri : null);
        transport.setConnectionUrl(makeWsUrl(wsUri));
        transport.setConnection(this);
        transport.setReadTimeout(getLink().getConfig().getConfig(
                DSLinkConfig.CFG_READ_TIMEOUT, 60000));
        return transport;
    }

    /**
     * Adds auth and maybe authToken parameters to the websocket query string.
     *
     * @param wsPath Websocket base path returned from the broker during connection initialization.
     */
    protected String makeWsUrl(String wsPath) {
        StringBuilder buf = new StringBuilder();
        try {
            URL url = new URL(brokerUri);
            buf.append("ws://");
            buf.append(url.getHost());
            if (url.getPort() >= 0) {
                buf.append(':').append(url.getPort());
            }
            if (wsPath.charAt(0) != '/') {
                buf.append('/');
            }
            buf.append(wsPath).append("?auth=");
            String saltStr = connectionInitResponse.getString("salt");
            if (saltStr != null) {
                byte[] salt = saltStr.getBytes("UTF-8");
                String tempKey = connectionInitResponse.getString("tempKey");
                byte[] secret = getLink().getKeys().generateSharedSecret(tempKey);
                byte[] bytes = new byte[salt.length + secret.length];
                System.arraycopy(salt, 0, bytes, 0, salt.length);
                System.arraycopy(secret, 0, bytes, salt.length, secret.length);
                MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
                messageDigest.update(bytes);
                bytes = messageDigest.digest();
                buf.append(DSBase64.encodeUrl(bytes));
            } else {
                //The comment for the following in the original sdk was
                //"Fake auth parameter".  Maybe for testing?
                buf.append("_");
            }
            if (authToken != null) {
                buf.append("&token=").append(authToken);
            }
            buf.append("&dsId=").append(getLink().getDsId());
            buf.append("&format=").append("json");
        } catch (Exception x) {
            DSException.throwRuntime(x);
        }
        return buf.toString();
    }

    @Override
    public void setLink(DSLink link) {
        this.authToken = link.getConfig().getToken();
        this.brokerUri = link.getConfig().getBrokerUri();
        this.link = link;
    }

    /**
     * Spawns a thread to manage opening the connection and subsequent reconnections.
     */
    @Override
    public void onStable() {
        if (isOpen()) {
            throw new IllegalStateException("Connection already open");
        }
        new ConnectionRunThread(new ConnectionRunner()).start();
    }

    /**
     * Terminates the connection runner.
     */
    @Override
    public void onStopped() {
        close();
    }

    /**
     * Throws an exception with a useful error message.
     */
    private void throwConnectionException(HttpURLConnection conn, int rc)
            throws Exception {
        StringBuilder builder = new StringBuilder();
        ByteArrayOutputStream out = null;
        InputStream in = null;
        try {
            builder.append(conn.getResponseMessage());
            out = new ByteArrayOutputStream();
            byte[] buf = new byte[512];
            in = conn.getErrorStream();
            int len = in.read(buf);
            while (len > 0) {
                out.write(buf, 0, len);
                len = in.read(buf);
            }
            builder.append(": ")
                   .append(new String(out.toByteArray(), "UTF-8"));
        } catch (Exception ignore) {
        }
        try {
            if (in != null) {
                in.close();
            }
            if (out != null) {
                out.close();
            }
        } catch (Exception ignore) {
        }
        if (builder.length() > 0) {
            throw new IOException(builder.toString());
        }
        throw new IOException("HTTP " + rc);
    }

    public void requesterAllowed() {
        protocol.requesterAllowed();
    }

    public void updateSalt(String salt) {
        DSMap map = connectionInitResponse;
        if (map != null) {
            map.put("salt", salt);
        }
    }

    /**
     * Writes the json map representing the connection request.
     */
    protected void writeConnectionRequest(HttpURLConnection conn) throws Exception {
        JsonWriter out = null;
        try {
            out = new JsonWriter(conn.getOutputStream());
            DSMap map = new DSMap();
            map.put("publicKey", DSBase64.encodeUrl(link.getKeys().encodePublic()));
            map.put("isRequester", link.getConfig().isRequester());
            map.put("isResponder", link.getResponder() != null);
            map.put("linkData", new DSMap());
            map.put("version", DSA_VERSION);
            DSList list = map.putList("formats");
            for (String format : SUPPORTED_FORMATS) {
                list.add(format);
            }
            map.put("enableWebSocketCompression", false);
            finest(finest() ? map.toString() : null);
            out.value(map);
        } finally {
            if (out != null) {
                out.close();
            }
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // Inner Classes
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Daemon thread that manages connection and reconnection.
     */
    private class ConnectionRunner implements Runnable {

        /**
         * Runs until stop is called.
         */
        public void run() {
            while (isRunning()) {
                try {
                    synchronized (mutex) {
                        try {
                            mutex.wait(reconnectRate);
                        } catch (Exception x) {
                            warn(warn() ? getConnectionId() : null, x);
                        }
                    }
                    if (connectionInitResponse == null) {
                        connectionInitResponse = initializeConnection();
                    }
                    transport = makeTransport();
                    config(config() ? "Transport type: " + transport.getClass().getName() : null);
                    transport.open();
                    protocol = makeProtocol()
                            .setConnection(DSConnection.this)
                            .setTransport(transport);
                    config(config() ? "Protocol type: " + protocol.getClass().getName() : null);
                    protocol.run();
                    reconnectRate = 1000;
                } catch (Throwable x) {
                    reconnectRate = Math.min(reconnectRate * 2, DSTime.MILLIS_MINUTE);
                    severe(getConnectionId(), x);
                }
                try {
                    close();
                } catch (Exception x) {
                    fine(fine() ? getConnectionId() : null, x);
                }
                protocol = null;
                transport = null;
            }
        }
    }

    /**
     * Daemon thread that manages connection and reconnection.
     */
    private class ConnectionRunThread extends Thread {

        public ConnectionRunThread(ConnectionRunner runner) {
            super(runner);
            setName(getConnectionId() + " Runner");
            setDaemon(true);
        }
    }

}
