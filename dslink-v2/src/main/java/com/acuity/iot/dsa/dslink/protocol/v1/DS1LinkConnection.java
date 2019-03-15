package com.acuity.iot.dsa.dslink.protocol.v1;

import com.acuity.iot.dsa.dslink.protocol.DSBrokerConnection;
import com.acuity.iot.dsa.dslink.transport.DSTransport;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import org.iot.dsa.dslink.DSLink;
import org.iot.dsa.dslink.DSLinkOptions;
import org.iot.dsa.io.json.Json;
import org.iot.dsa.node.DSInfo;
import org.iot.dsa.node.DSList;
import org.iot.dsa.node.DSMap;
import org.iot.dsa.node.DSString;
import org.iot.dsa.util.DSException;

/**
 * The DSA V1 connection implementation. Performs connection initialization with the broker, then
 * creates a transport based on the broker response.
 *
 * @author Aaron Hansen
 */
public class DS1LinkConnection extends DSBrokerConnection {

    ///////////////////////////////////////////////////////////////////////////
    // Constants
    ///////////////////////////////////////////////////////////////////////////

    static final String DSA_VERSION = "1.1.2";
    static final String WS_URI = "Broker WS URI";

    ///////////////////////////////////////////////////////////////////////////
    // Fields
    ///////////////////////////////////////////////////////////////////////////

    private DSInfo wsUri = getInfo(WS_URI);

    ///////////////////////////////////////////////////////////////////////////
    // Constructors
    ///////////////////////////////////////////////////////////////////////////

    public DS1LinkConnection() {
        getSession();
    }

    ///////////////////////////////////////////////////////////////////////////
    // Protected Methods
    ///////////////////////////////////////////////////////////////////////////

    @Override
    protected void checkConfig() {
    }

    @Override
    protected void declareDefaults() {
        super.declareDefaults();
        declareDefault(WS_URI, DSString.NULL).setReadOnly(true).setTransient(true);
        getInfo(ENABLED).setPrivate(true);
    }

    protected String getWsUri() {
        return wsUri.get().toString();
    }

    protected void initializeConnection() {
        try {
            if (DSString.isNotEmpty(getBrokerSalt()) && DSString.isNotEmpty(getBrokerKey())) {
                return;
            }
            String uri = makeHandshakeUri();
            setBrokerUri(uri);
            debug(debug() ? "Broker URI " + uri : null);
            DSMap response = Json.read(connect(new URL(uri), 0), true).toMap();
            String s = response.getString("dsId");
            if (s == null) {
                s = response.getString("id");
            }
            if (s != null) {
                setBrokerId(s);
            }
            setBrokerFormat(response.getString("format"));
            setBrokerKey(response.getString("tempKey"));
            setBrokerSalt(response.getString("salt"));
            setBrokerVersion(response.getString("version"));
            setWsUri(response.getString("wsUri"));
            setPathInBroker(response.getString("path"));
            debug(debug() ? response : null);
        } catch (Exception x) {
            DSException.throwRuntime(x);
        }
    }

    @Override
    protected DS1Session makeSession() {
        return new DS1Session();
    }

    @Override
    protected DSTransport makeTransport() {
        DSTransport.Factory factory = null;
        DSTransport transport = null;
        try {
            String type = getLink().getOptions().getConfig(
                    DSLinkOptions.CFG_WS_TRANSPORT_FACTORY,
                    "org.iot.dsa.dslink.websocket.StandaloneTransportFactory");
            factory = (DSTransport.Factory) Class.forName(type).newInstance();
            transport = factory.makeTransport(this);
            transport.setText("json".equals(getBrokerFormat()));
        } catch (Exception x) {
            DSException.throwRuntime(x);
        }
        String uri = makeWsUri();
        debug(debug() ? "Connection URL = " + uri : null);
        transport.setConnectionUrl(uri);
        debug(debug() ? "Transport type: " + transport.getClass().getName() : null);
        return transport;
    }

    protected void setWsUri(String arg) {
        put(wsUri, DSString.valueOf(arg));
    }

    ///////////////////////////////////////////////////////////////////////////
    // Package Methods
    ///////////////////////////////////////////////////////////////////////////

    ///////////////////////////////////////////////////////////////////////////
    // Private Methods
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Opens an http/s connection and handles redirects that switch protocols.
     */
    private InputStream connect(URL url, int redirects) throws Exception {
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
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
        debug(debug() ? "Connection initialization response code " + rc : null);
        if ((rc >= 300) && (rc <= 307) && (rc != 306) &&
                (rc != HttpURLConnection.HTTP_NOT_MODIFIED)) {
            conn.disconnect();
            if (++redirects > 5) {
                throw new IllegalStateException("Too many redirects");
            }
            String location = conn.getHeaderField("Location");
            if (location == null) {
                throw new IllegalStateException("Redirect missing location header");
            }
            url = new URL(url, location);
            debug(debug() ? "Following redirect to " + url : null);
            return connect(url, redirects);
        }
        if ((rc < 200) || (rc >= 300)) {
            conn.disconnect();
            throwConnectionException(conn, rc);
        }
        return conn.getInputStream();
    }

    /**
     * Adds dsId and maybe authToken parameters to the query string.
     *
     * @see DSLinkOptions#getBrokerUri()
     * @see DSLinkOptions#getToken()
     */
    private String makeHandshakeUri() {
        StringBuilder builder = new StringBuilder();
        DSLink link = getLink();
        String uri = link.getOptions().getBrokerUri();
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
        String token = link.getOptions().getToken();
        if ((token != null) && !token.isEmpty()) {
            builder.append("&token=").append(token);
        }
        return builder.toString();
    }

    /**
     * Adds auth, dsid, format and toekn parameters to the websocket query string.
     */
    private String makeWsUri() {
        StringBuilder buf = new StringBuilder();
        DSLink link = getLink();
        try {
            String uri = link.getOptions().getBrokerUri();
            URL url = new URL(uri);
            if (uri.toLowerCase().startsWith("https:")) {
                buf.append("wss://");
            } else {
                buf.append("ws://");
            }
            buf.append(url.getHost());
            if (url.getPort() >= 0) {
                buf.append(':').append(url.getPort());
            }
            String wsPath = getWsUri();
            if (wsPath.charAt(0) != '/') {
                buf.append('/');
            }
            buf.append(wsPath);
            String saltStr = getBrokerSalt();
            String keyStr = getBrokerKey();
            boolean queryStarted = false;
            if (DSString.isNotEmpty(saltStr) && DSString.isNotEmpty(keyStr)) {
                setBrokerSalt("");
                buf.append("?auth=");
                queryStarted = true;
                buf.append(link.getOptions().getKeys().generateAuth(saltStr, keyStr));
            }
            String token = link.getOptions().getToken();
            if (token != null) {
                if (queryStarted) {
                    buf.append("&token=").append(token);
                } else {
                    buf.append("?token=").append(token);
                    queryStarted = true;
                }
            }
            if (queryStarted) {
                buf.append("&dsId=").append(link.getDsId());
            } else {
                buf.append("?dsId=").append(link.getDsId());
            }
            buf.append("&format=").append(getBrokerFormat());
        } catch (Exception x) {
            DSException.throwRuntime(x);
        }
        return buf.toString();
    }

    /**
     * Throws an exception with a useful error message.
     */
    private void throwConnectionException(HttpURLConnection conn, int rc) throws Exception {
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
            builder.append(": ").append(new String(out.toByteArray(), "UTF-8"));
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

    /**
     * Writes the json map representing the connection request.
     */
    private void writeConnectionRequest(HttpURLConnection conn) throws Exception {
        DSMap map = new DSMap();
        DSLink link = getLink();
        map.put("publicKey", link.getOptions().getKeys().urlEncodePublicKey());
        map.put("isRequester", link.getMain().isRequester());
        map.put("isResponder", link.getMain().isResponder());
        map.put("linkData", new DSMap());
        map.put("version", DSA_VERSION);
        DSList list = map.putList("formats");
        if (link.getOptions().getMsgpack()) {
            list.add("msgpack");
        }
        list.add("json");
        map.put("enableWebSocketCompression", false);
        debug(debug() ? map : null);
        Json.write(map, conn.getOutputStream(), true);
    }


}
