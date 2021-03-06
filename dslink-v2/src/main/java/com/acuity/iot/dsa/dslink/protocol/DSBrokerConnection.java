package com.acuity.iot.dsa.dslink.protocol;

import org.iot.dsa.dslink.ActionResults;
import org.iot.dsa.dslink.DSLinkConnection;
import org.iot.dsa.node.DSInfo;
import org.iot.dsa.node.DSString;
import org.iot.dsa.node.action.DSAction;
import org.iot.dsa.node.action.DSIActionRequest;
import org.iot.dsa.util.DSException;

/**
 * Represents an upstream connection to a broker.
 *
 * @author Aaron Hansen
 */
public abstract class DSBrokerConnection extends DSLinkConnection {

    ///////////////////////////////////////////////////////////////////////////
    // Class Fields
    ///////////////////////////////////////////////////////////////////////////

    protected static final String BROKER_ID = "Broker ID";
    protected static final String BROKER_FORMAT = "Broker Format";
    protected static final String BROKER_PATH = "Path In Broker";
    protected static final String BROKER_PUB_KEY = "Broker Public Key";
    protected static final String BROKER_SALT = "Broker Salt";
    protected static final String BROKER_URI = "Broker URI";
    protected static final String BROKER_VERSION = "Broker Version";
    protected static final String RECONNECT = "Reconnect";

    ///////////////////////////////////////////////////////////////////////////
    // Instance Fields
    ///////////////////////////////////////////////////////////////////////////

    private DSInfo<?> brokerDsId = getInfo(BROKER_ID);
    private DSInfo<?> brokerFormat = getInfo(BROKER_FORMAT);
    private DSInfo<?> brokerPath = getInfo(BROKER_PATH);
    private DSInfo<?> brokerPubKey = getInfo(BROKER_PUB_KEY);
    private DSInfo<?> brokerSalt = getInfo(BROKER_SALT);
    private DSInfo<?> brokerUri = getInfo(BROKER_URI);
    private DSInfo<?> brokerVersion = getInfo(BROKER_VERSION);

    ///////////////////////////////////////////////////////////////////////////
    // Public Methods
    ///////////////////////////////////////////////////////////////////////////

    /**
     * The path representing the link node in the broker.
     */
    @Override
    public String getPathInBroker() {
        return brokerPath.getElement().toString();
    }

    protected void setPathInBroker(String path) {
        put(brokerPath, DSString.valueOf(path));
    }

    @Override
    public DSSession getSession() {
        return (DSSession) super.getSession();
    }

    ///////////////////////////////////////////////////////////////////////////
    // Protected Methods
    ///////////////////////////////////////////////////////////////////////////

    @Override
    protected void checkConfig() {
        String uri = getBrokerUri();
        if ((uri == null) || uri.isEmpty()) {
            throw new IllegalStateException("Missing broker URI");
        }
    }

    @Override
    protected void declareDefaults() {
        super.declareDefaults();
        declareDefault(BROKER_ID, DSString.NULL).setTransient(true).setReadOnly(true);
        declareDefault(BROKER_FORMAT, DSString.NULL).setTransient(true).setReadOnly(true);
        declareDefault(BROKER_PATH, DSString.NULL).setTransient(true).setReadOnly(true);
        declareDefault(BROKER_PUB_KEY, DSString.NULL).setTransient(true).setReadOnly(true);
        declareDefault(BROKER_SALT, DSString.NULL).setTransient(true).setReadOnly(true);
        declareDefault(BROKER_URI, DSString.NULL).setTransient(true).setReadOnly(true);
        declareDefault(BROKER_VERSION, DSString.NULL).setTransient(true).setReadOnly(true);
        declareDefault(RECONNECT, new DSAction() {
            @Override
            public ActionResults invoke(DSIActionRequest request) {
                ((DSBrokerConnection) request.getTarget()).disconnect();
                return null;
            }
        });
    }

    @Override
    protected void doConnect() {
        try {
            initializeConnection();
            getTransport().open();
            connOk();
        } catch (Exception x) {
            debug(null, debug() ? x : null);
            connDown(DSException.makeMessage(x));
        }
    }

    @Override
    protected void doPing() {
        getSession().sendMessage();
    }

    protected String getBrokerFormat() {
        return brokerFormat.get().toString();
    }

    protected void setBrokerFormat(String arg) {
        put(brokerFormat, DSString.valueOf(arg));
    }

    protected String getBrokerId() {
        return brokerDsId.get().toString();
    }

    protected void setBrokerId(String arg) {
        put(brokerDsId, DSString.valueOf(arg));
    }

    protected String getBrokerKey() {
        return brokerPubKey.get().toString();
    }

    protected void setBrokerKey(String arg) {
        put(brokerPubKey, DSString.valueOf(arg));
    }

    protected String getBrokerSalt() {
        return brokerSalt.get().toString();
    }

    /**
     * For the sessions to update
     */
    public void setBrokerSalt(String arg) {
        put(brokerSalt, DSString.valueOf(arg));
    }

    protected String getBrokerUri() {
        return brokerUri.get().toString();
    }

    protected void setBrokerUri(String arg) {
        put(brokerUri, DSString.valueOf(arg));
    }

    protected String getBrokerVersion() {
        return brokerVersion.get().toString();
    }

    protected void setBrokerVersion(String arg) {
        put(brokerVersion, DSString.valueOf(arg));
    }

    protected abstract void initializeConnection();

}
