package org.iot.dsa.conn;

import org.iot.dsa.node.DSBool;
import org.iot.dsa.node.DSIStatus;
import org.iot.dsa.node.DSInfo;
import org.iot.dsa.node.DSNode;
import org.iot.dsa.node.DSStatus;
import org.iot.dsa.node.DSString;
import org.iot.dsa.time.DSDateTime;
import org.iot.dsa.util.DSException;

/**
 * Abstract representation of a connection.  Maintains state, provides callbacks to manage
 * a lifecycle and notifies children of state changes.
 * <p>
 *
 * <b>Running</b>
 * The run method manages calling connect, disconnect and ping().  The subclass is responsible
 * having a thread call this method, or to manage the lifecycle another way.
 * <p>
 *
 * <b>Pinging</b>
 * ping() is only called by the run method.  It is only called if the time since the last OK or
 * last ping (whichever is later) exceeds the ping interval.  Implementations should call connOk
 * whenever there are successful communications to avoid unnecessary pings.
 * <p>
 *
 * <b>DSIConnected</b>
 * By default, DSConnection will notify subtree instances of DSIConnected when the connection
 * transitions to connected and disconnected.  Subclasses could choose to notify at other times.
 *
 * Connection Sequence
 *
 * Disconnection Sequence
 *
 * @author Aaron Hansen
 */
public abstract class DSConnection extends DSNode implements DSIStatus, Runnable {

    ///////////////////////////////////////////////////////////////////////////
    // Class Fields
    ///////////////////////////////////////////////////////////////////////////

    static final String FAILURE = "Failure";
    static final String LAST_OK = "Last OK";
    static final String LAST_FAIL = "Last Fail";
    static final String STATE = "State";
    static final String STATE_TIME = "State Timestamp";
    static final String STATUS = "Status";

    ///////////////////////////////////////////////////////////////////////////
    // Instance Fields
    ///////////////////////////////////////////////////////////////////////////

    private DSInfo failure = getInfo(FAILURE);
    private DSInfo lastFail = getInfo(LAST_FAIL);
    private DSInfo lastOk = getInfo(LAST_OK);
    private long lastOkMillis;
    private DSInfo state = getInfo(STATE);
    private DSInfo stateTime = getInfo(STATE_TIME);
    private DSInfo status = getInfo(STATUS);

    ///////////////////////////////////////////////////////////////////////////
    // Public Methods
    ///////////////////////////////////////////////////////////////////////////

    /**
     * If the connection state is connecting or connected, this will call disconnect.  If
     * disconnect has already been called, this notifies the subtree if the connection actually
     * transitions to disconnected.
     *
     * @param reason Optional
     */
    public void connDown(String reason) {
        debug(debug() ? "connDown: " + reason : null);
        if (getConnectionState().isEngaged()) {
            put(lastFail, DSDateTime.currentTime());
            if (reason != null) {
                if (!failure.getElement().toString().equals(reason)) {
                    put(failure, DSString.valueOf(reason));
                }
            }
            disconnect();
        } else if (getConnectionState().isDisconnecting()) {
            put(status, getStatus().add(DSStatus.DOWN));
            put(state, DSConnectionState.DISCONNECTED);
            put(stateTime, DSDateTime.currentTime());
            notifyDescendants(this);
            try {
                onDisconnected();
            } catch (Exception x) {
                error(error() ? getPath() : null, x);
            }
        }
    }


    /**
     * Update the last ok timestamp, will remove the down status if present and notifies the
     * subtree if the connection actually transitions to connected.
     */
    public void connOk() {
        if (getConnectionState().isDisengaged()) {
            return;
        }
        long now = System.currentTimeMillis();
        if ((now - lastOkMillis) > 1000) { //prevents subscription to last ok time from going crazy
            put(lastOk, DSDateTime.valueOf(now));
        }
        lastOkMillis = now;
        if (!isConnected()) {
            put(status, getStatus().remove(DSStatus.DOWN));
            put(state, DSConnectionState.CONNECTED);
            put(stateTime, DSDateTime.valueOf(now));
            notifyDescendants(this);
            try {
                onConnected();
            } catch (Exception x) {
                error(error() ? getPath() : null, x);
            }
        }
    }

    /**
     * Verifies configuration and if operational, initiates the connection logic.
     */
    public void connect() {
        if (!getConnectionState().isDisconnected()) {
            debug(debug() ? "Not disconnected, ignoring connect()" : null);
            return;
        }
        debug(debug() ? "Connect" : null);
        put(state, DSConnectionState.CONNECTING);
        put(stateTime, DSDateTime.currentTime());
        notifyDescendants(this);
        try {
            checkConfig();
            try {
                if (isOperational()) {
                    onConnect();
                }
            } catch (Throwable e) {
                error(error() ? getPath() : null, e);
                connDown(DSException.makeMessage(e));
            }
        } catch (Throwable x) {
            put(state, DSConnectionState.DISCONNECTED);
            put(stateTime, DSDateTime.currentTime());
            error(error() ? getPath() : null, x);
            configFault(DSException.makeMessage(x));
        }
    }

    /**
     * Initiates disconnection logic.
     */
    public void disconnect() {
        if (!getConnectionState().isConnected()) {
            debug(debug() ? "Not connected, ignoring disconnect()" : null);
            return;
        }
        debug(debug() ? "Disconnect" : null);
        put(state, DSConnectionState.DISCONNECTING);
        put(stateTime, DSDateTime.currentTime());
        notifyDescendants(this);
        try {
            onDisconnect();
        } catch (Throwable x) {
            error(getPath(), x);
            connDown(DSException.makeMessage(x));
        }
    }

    public DSConnectionState getConnectionState() {
        return (DSConnectionState) state.getObject();
    }

    /**
     * The last time connOk was called.
     */
    public long getLastOk() {
        return lastOkMillis;
    }

    @Override
    public DSStatus getStatus() {
        return (DSStatus) status.getObject();
    }

    public boolean isConnected() {
        return getConnectionState().isConnected();
    }

    public boolean isEnabled() {
        return true;
    }

    /**
     * Call this to automatically manage the connection lifecycle, it will not return
     * until the node is stopped.
     */
    public void run() {
        long INTERVAL = 5000;
        long retryMs = 0;
        long lastPing = 0;
        while (isRunning()) {
            if (isConnected()) {
                retryMs = 5000;
                if (!isEnabled()) {
                    disconnect();
                } else {
                    try {
                        long ivl = getPingInterval();
                        long last = Math.max(getLastOk(), lastPing);
                        long now = System.currentTimeMillis();
                        long duration = now - last;
                        if (duration >= ivl) {
                            lastPing = now;
                            try {
                                ping();
                            } catch (Throwable t) {
                                error(getPath(), t);
                                connDown(DSException.makeMessage(t));
                            }
                        }
                    } catch (Exception x) {
                        debug(debug() ? getPath() : null, x);
                    }
                }
            } else {
                if (isEnabled() && getConnectionState().isDisconnected()) {
                    if (getTimeInState() >= retryMs) {
                        retryMs = Math.min(60000, retryMs + INTERVAL);
                        connect();
                    }
                }
            }
            synchronized (this) {
                try {
                    wait(INTERVAL);
                } catch (Exception x) {
                    debug(debug() ? getPath() : null, x);
                }
            }
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // Protected Methods
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Call configOk or configFault before returning, or throw an exception.
     */
    protected abstract void checkConfig();

    /**
     * Puts the connection into the fault state and optionally sets the error message.
     *
     * @param msg Optional
     */
    protected void configFault(String msg) {
        debug(debug() ? "Config Fault: " + msg : null);
        put(lastFail, DSDateTime.currentTime());
        if (msg != null) {
            if (!failure.getElement().toString().equals(msg)) {
                put(failure, DSString.valueOf(msg));
            }
        }
        if (!getStatus().isFault()) {
            put(status, getStatus().add(DSStatus.FAULT));
        }
    }

    /**
     * Removes fault state.
     */
    protected void configOk() {
        debug(debug() ? "Config OK" : null);
        if (getStatus().isFault()) {
            put(status, getStatus().remove(DSStatus.FAULT));
        }
    }

    @Override
    protected void declareDefaults() {
        declareDefault(STATUS, DSStatus.down).setReadOnly(true).setTransient(true);
        declareDefault(STATE, DSConnectionState.DISCONNECTED).setReadOnly(true).setTransient(true);
        declareDefault(STATE_TIME, DSDateTime.currentTime()).setReadOnly(true).setTransient(true);
        declareDefault(FAILURE, DSString.EMPTY).setReadOnly(true).setTransient(true);
        declareDefault(LAST_OK, DSDateTime.NULL).setReadOnly(true);
        declareDefault(LAST_FAIL, DSDateTime.NULL).setReadOnly(true);
    }

    @Override
    protected String getLogName() {
        return getLogName("connection");
    }

    /**
     * Ping interval in milliseconds (default is 60000).  If the time since the last call
     * to connOk exceeds this, the ping method will be called.  Implementations should
     * call connOk whenever there have been successful communications to minimize pinging.
     *
     * @return 60000
     */
    protected long getPingInterval() {
        return 60000;
    }

    protected long getTimeInState() {
        DSDateTime time = (DSDateTime) stateTime.getObject();
        return System.currentTimeMillis() - time.timeInMillis();
    }

    protected boolean isConfigOk() {
        return !getStatus().isFault();
    }

    /**
     * True if running, enabled and config is ok.
     */
    protected boolean isOperational() {
        return isRunning() && isConfigOk() && isEnabled();
    }

    /**
     * Subclasses must establish the connection. You must call connOk or connDown, but that can
     * be async after this method has returned. You can throw an exception from this method instead
     * of calling connDown. This will only be called if configuration is ok.
     */
    protected abstract void onConnect();

    /**
     * Called by connOk() when transitioning to connected.  By default, this does nothing.
     */
    protected void onConnected() {
    }

    /**
     * Close and clean up resources.
     */
    protected abstract void onDisconnect();

    /**
     * Called by connDown() when transitioning to disconnected.  By default, this does nothing.
     */
    protected void onDisconnected() {
    }

    /**
     * Override point, called by the run method.  Implementations should call verify the connection
     * is still valid and call connOk or connDown, but those can be async and after this method
     * returns.  Throwing an exception will be treated as a connDown.  By default, this does
     * nothing.
     */
    protected void ping() {
    }

    ///////////////////////////////////////////////////////////////////////////
    // Package / Private Methods
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Calls DSIConnected.onChange on all implementations in the subtree.  Does not notify
     * children of DSIConnected objects.  Stops at instances of DSConnection, but if they
     * implement DSIConnected, they will receive the callback.
     */
    private void notifyDescendants() {
        notifyDescendants(this);
    }

    /**
     * Calls DSIConnected.onChange on implementations in the subtree.  Stops at instances of
     * DSIConnected and DSConnection.
     */
    private void notifyDescendants(DSNode node) {
        DSInfo info = node.getFirstInfo();
        while (info != null) {
            if (info.is(DSIConnected.class)) {
                try {
                    ((DSIConnected) info.getObject()).onChange(this);
                } catch (Throwable t) {
                    error(error() ? info.getPath(null) : null, t);
                }
            } else if (info.isNode() && !info.is(DSConnection.class)) {
                notifyDescendants(info.getNode());
            }
            info = info.next();
        }
    }


}
