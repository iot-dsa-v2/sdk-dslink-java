package com.acuity.iot.dsa.dslink.protocol;

import org.iot.dsa.node.DSInfo;
import org.iot.dsa.node.DSString;
import org.iot.dsa.node.action.ActionInvocation;
import org.iot.dsa.node.action.ActionResult;
import org.iot.dsa.node.action.DSAction;

/**
 * Represents an upstream connection to a broker.
 *
 * @author Aaron Hansen
 */
public abstract class DSBrokerConnection extends DSTransportConnection {

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
                ((DSBrokerConnection) target.get()).disconnect();
                return null;
            }
        });
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
