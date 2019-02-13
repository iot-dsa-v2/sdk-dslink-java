package com.acuity.iot.dsa.dslink.protocol;

import com.acuity.iot.dsa.dslink.transport.DSTransport;
import org.iot.dsa.dslink.DSLinkConnection;
import org.iot.dsa.dslink.DSSysNode;
import org.iot.dsa.node.DSInfo;
import org.iot.dsa.node.DSNode;
import org.iot.dsa.node.DSPath;
import org.iot.dsa.node.DSString;
import org.iot.dsa.node.action.ActionInvocation;
import org.iot.dsa.node.action.ActionResult;
import org.iot.dsa.node.action.DSAction;

/**
 * Represents an upstream connection to a broker.
 *
 * @author Aaron Hansen
 */
public abstract class DSUpstreamConnection extends DSLinkConnection {

    ///////////////////////////////////////////////////////////////////////////
    // Class Fields
    ///////////////////////////////////////////////////////////////////////////

    protected static final String BROKER_URI = "Broker URI";
    protected static final String BROKER_PATH = "Path In Broker";
    protected static final String RECONNECT = "Reconnect";

    ///////////////////////////////////////////////////////////////////////////
    // Instance Fields
    ///////////////////////////////////////////////////////////////////////////

    private DSInfo brokerPath = getInfo(BROKER_PATH);

    ///////////////////////////////////////////////////////////////////////////
    // Public Methods
    ///////////////////////////////////////////////////////////////////////////

    /**
     * The path representing the link node in the broker.
     */
    public String getPathInBroker() {
        return brokerPath.getElement().toString();
    }

    /**
     * Concatenates the path in broker with the path of the node.
     */
    public String getPathInBroker(DSNode node) {
        StringBuilder buf = new StringBuilder();
        buf.append(getPathInBroker());
        return DSPath.append(buf, node.getPath()).toString();
    }

    public abstract DSTransport getTransport();

    ///////////////////////////////////////////////////////////////////////////
    // Protected Methods
    ///////////////////////////////////////////////////////////////////////////

    @Override
    protected void declareDefaults() {
        super.declareDefaults();
        declareDefault(BROKER_URI, DSString.NULL).setTransient(true).setReadOnly(true);
        declareDefault(BROKER_PATH, DSString.NULL).setTransient(true).setReadOnly(true);
        declareDefault(RECONNECT, new DSAction.Parameterless() {
            @Override
            public ActionResult invoke(DSInfo target, ActionInvocation invocation) {
                ((DSUpstreamConnection) target.get()).disconnect();
                return null;
            }
        });
    }

    @Override
    protected void doDisconnect() {
        try {
            if (getTransport() != null) {
                getTransport().close();
            }
        } catch (Exception x) {
            error(getPath(), x);
        }
    }

    /**
     * Creates and starts a thread for running the connection lifecycle.
     */
    @Override
    protected void onStable() {
        super.onStable();
        Thread t = new Thread(this, "Connection " + getName() + " Runner");
        t.setDaemon(true);
        t.start();
    }

    protected void setPathInBroker(String path) {
        put(brokerPath, DSString.valueOf(path));
    }

}
