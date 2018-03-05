package com.acuity.iot.dsa.dslink.protocol.v1;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import org.iot.dsa.dslink.DSLink;
import org.iot.dsa.dslink.DSLinkConfig;
import org.iot.dsa.io.DSBase64;
import org.iot.dsa.io.json.JsonReader;
import org.iot.dsa.io.json.JsonWriter;
import org.iot.dsa.node.DSList;
import org.iot.dsa.node.DSMap;
import org.iot.dsa.node.DSNode;
import org.iot.dsa.util.DSException;

/**
 * The default connection implementation. Performs connection initialization with the broker, then
 * creates a transport and a protocol based on the broker response.
 *
 * @author Aaron Hansen
 */
public class DS1ConnectionInit extends DSNode {

    ///////////////////////////////////////////////////////////////////////////
    // Constants
    ///////////////////////////////////////////////////////////////////////////

    private static final String DSA_VERSION = "1.1.2";
    //private static final String[] SUPPORTED_FORMATS = new String[]{"msgpack", "json"};
    private static final String[] SUPPORTED_FORMATS = new String[]{"json"};

    private String BROKER_REQ = "Broker Request";
    private String BROKER_RES = "Broker Response";

    ///////////////////////////////////////////////////////////////////////////
    // Fields
    ///////////////////////////////////////////////////////////////////////////

    private String authToken;
    private String brokerUri;
    private DS1LinkConnection connection;
    private DSLink link;
    private DSMap response;

    ///////////////////////////////////////////////////////////////////////////
    // Constructors
    ///////////////////////////////////////////////////////////////////////////

    ///////////////////////////////////////////////////////////////////////////
    // Methods
    ///////////////////////////////////////////////////////////////////////////

    DSLink getLink() {
        return connection.getLink();
    }

    /**
     * Returns the brokers response to connection initialization.
     */
    DSMap getResponse() {
        return response;
    }

    /**
     * Makes a DSA connection initialization request and returns the response map if successful.
     *
     * @return The json map representing the response from the server.
     * @throws Exception if there are any issues.
     */
    void initializeConnection() throws Exception {
        String uri = makeBrokerUrl();
        fine(fine() ? "Broker URI " + uri : null);
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
            in = new JsonReader(conn.getInputStream(), "UTF-8");
            response = in.getMap();
            put(BROKER_RES, response).setReadOnly(true);
            trace(trace() ? response : null);
        } finally {
            if (in != null) {
                in.close();
            }
        }
    }

    /**
     * Adds dsId and maybe authToken parameters to the query string.
     *
     * @see DSLinkConfig#getBrokerUri()
     * @see DSLinkConfig#getToken()
     */
    String makeBrokerUrl() {
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
     * Adds auth and maybe authToken parameters to the websocket query string.
     *
     * @param wsPath Websocket base path returned from the broker during connection initialization.
     */
    String makeWsUrl(String wsPath) {
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
            String saltStr = response.getString("salt");
            if (saltStr != null) {
                byte[] salt = saltStr.getBytes("UTF-8");
                String tempKey = response.getString("tempKey");
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
            buf.append("&format=").append(response.getString("format"));
        } catch (Exception x) {
            DSException.throwRuntime(x);
        }
        return buf.toString();
    }

    @Override
    public void onStable() {
        this.connection = (DS1LinkConnection) getParent();
        this.link = connection.getLink();
        this.authToken = link.getConfig().getToken();
        this.brokerUri = link.getConfig().getBrokerUri();
    }

    /**
     * Throws an exception with a useful error message.
     */
    void throwConnectionException(HttpURLConnection conn, int rc) throws Exception {
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

    void updateSalt(String salt) {
        DSMap map = response;
        if (map != null) {
            map.put("salt", salt);
        }
    }

    /**
     * Writes the json map representing the connection request.
     */
    void writeConnectionRequest(HttpURLConnection conn) throws Exception {
        JsonWriter out = null;
        try {
            out = new JsonWriter(conn.getOutputStream());
            DSMap map = new DSMap();
            map.put("publicKey", DSBase64.encodeUrl(link.getKeys().encodePublic()));
            map.put("isRequester", link.getMain().isRequester());
            map.put("isResponder", link.getMain().isResponder());
            map.put("linkData", new DSMap());
            map.put("version", DSA_VERSION);
            DSList list = map.putList("formats");
            for (String format : SUPPORTED_FORMATS) {
                list.add(format);
            }
            map.put("enableWebSocketCompression", false);
            put(BROKER_REQ, map).setReadOnly(true);
            trace(trace() ? map.toString() : null);
            out.value(map);
        } finally {
            if (out != null) {
                out.close();
            }
        }
    }

}
