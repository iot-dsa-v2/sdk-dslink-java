package org.iot.dsa.driver;

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
 * having a thread call this method, or to manage lifecycle another way.
 * <p>
 *
 * <b>Pinging</b>
 * ping() is only called by the run method.  It is only called if the time since the last OK or
 * last ping (whichever is later) exceeds the ping interval.  Implementations should call connOk
 * whenever there are successful communications to avoid unnecessarily pings.
 * <p>
 *
 * <b>DSIConnected</b>
 * By default, DSConnection will notify subtree instances of DSIConnected when the connection
 * transitions to connected and disconnected.  Subclasses could choose to notify at other times.
 *
 * @author Aaron Hansen
 */
public abstract class DSConnection extends DSNode implements DSIStatus {

    ///////////////////////////////////////////////////////////////////////////
    // Class Fields
    ///////////////////////////////////////////////////////////////////////////

    static final String ENABLED = "Enabled";
    static final String FAILURE = "Failure";
    static final String LAST_OK = "Last OK";
    static final String LAST_FAIL = "Last Fail";
    static final String STATE = "State";
    static final String STATUS = "Status";

    ///////////////////////////////////////////////////////////////////////////
    // Instance Fields
    ///////////////////////////////////////////////////////////////////////////

    private DSInfo enabled = getInfo(ENABLED);
    private DSInfo failure = getInfo(FAILURE);
    private DSInfo lastFail = getInfo(LAST_FAIL);
    private DSInfo lastOk = getInfo(LAST_OK);
    private DSInfo state = getInfo(STATE);
    private DSInfo status = getInfo(STATUS);

    ///////////////////////////////////////////////////////////////////////////
    // Constructors
    ///////////////////////////////////////////////////////////////////////////

    ///////////////////////////////////////////////////////////////////////////
    // Public Methods
    ///////////////////////////////////////////////////////////////////////////

    public DSConnectionState getConnectionState() {
        return (DSConnectionState) state.getObject();
    }

    /**
     * The last time connOk was called.
     */
    public DSDateTime getLastOk() {
        return (DSDateTime) lastOk.getObject();
    }

    public DSStatus getStatus() {
        return (DSStatus) status.getObject();
    }

    public boolean isConnected() {
        return getConnectionState().isConnected();
    }

    public boolean isEnabled() {
        return enabled.getElement().toBoolean();
    }

    /**
     * Call this to automatically manage the connection lifecycle, it will not return
     * until the node is stopped.
     */
    public synchronized void run() {
        long retryMs = 1000;
        long lastPing = 0;
        while (isRunning()) {
            if (isConnected()) {
                retryMs = 1000;
                if (!isEnabled()) {
                    disconnect();
                } else {
                    try {
                        long ivl = getPingInterval();
                        long last = Math.max(getLastOk().timeInMillis(), lastPing);
                        long now = System.currentTimeMillis();
                        long duration = now - last;
                        if (duration >= ivl) {
                            lastPing = now;
                            try {
                                ping();
                            } catch (Throwable t) {
                                error(error() ? getPath() : null, t);
                                connDown(DSException.makeMessage(t));
                            }
                        } else {
                            wait(ivl - duration);
                        }
                    } catch (Exception x) {
                        debug(debug() ? getPath() : null, x);
                    }
                }
            } else {
                if (isEnabled() && !getConnectionState().isEngaged()) {
                    connect();
                } else {
                    try {
                        wait(retryMs);
                    } catch (Exception x) {
                        debug(debug() ? getPath() : null, x);
                    }
                    retryMs = Math.max(60000, retryMs + 5000);
                }
            }
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // Protected Methods
    ///////////////////////////////////////////////////////////////////////////

    /**
     * You should call configOk, configFault, or throw an exception.
     */
    protected abstract void checkConfig();

    /**
     * Puts the connection into the fault state and optionally sets the error message.
     *
     * @param msg Optional
     */
    protected void configFault(String msg) {
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
        if (getStatus().isFault()) {
            put(status, getStatus().remove(DSStatus.FAULT));
        }
    }

    /**
     * Puts the connection into the down state, optionally sets the reason and notifies
     * the subtree if the connection actually transitions to down.
     *
     * @param reason Optional
     */
    protected void connDown(String reason) {
        put(lastFail, DSDateTime.currentTime());
        if (reason != null) {
            if (!failure.getElement().toString().equals(reason)) {
                put(failure, DSString.valueOf(reason));
            }
        }
        boolean notify = false;
        if (!getStatus().isDown()) {
            notify = true;
            put(status, getStatus().add(DSStatus.DOWN));
        }
        if (!getConnectionState().isDisconnected()) {
            notify = true;
            put(state, DSConnectionState.DISCONNECTED);
        }
        if (notify) {
            try {
                onDisconnected();
            } catch (Exception x) {
                error(error() ? getPath() : null, x);
            }
        }
    }

    /**
     * Update the last ok timestamp, will remove the down status if present and notifies the
     * subtree if the connection actually transitions to ok.
     */
    protected void connOk() {
        put(lastOk, DSDateTime.currentTime());
        boolean notify = false;
        if (getStatus().isDown()) {
            notify = true;
            put(status, getStatus().remove(DSStatus.DOWN));
        }
        if (!getConnectionState().isConnected()) {
            notify = true;
            put(state, DSConnectionState.CONNECTED);
        }
        if (notify) {
            try {
                onConnected();
            } catch (Exception x) {
                error(error() ? getPath() : null, x);
            }
        }
    }

    /**
     * Will attempt to connect only if the current state is disconnected.
     */
    protected void connect() {
        if (!getConnectionState().isDisconnected()) {
            return;
        }
        put(state, DSConnectionState.CONNECTING);
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
            error(error() ? getPath() : null, x);
            configFault(DSException.makeMessage(x));
        }
    }

    @Override
    protected void declareDefaults() {
        declareDefault(ENABLED, DSBool.TRUE);
        declareDefault(STATUS, DSStatus.down).setReadOnly(true).setTransient(true);
        declareDefault(STATE, DSConnectionState.DISCONNECTED).setReadOnly(true).setTransient(true);
        declareDefault(FAILURE, DSString.EMPTY).setReadOnly(true).setTransient(true);
        declareDefault(LAST_OK, DSDateTime.NULL).setReadOnly(true);
        declareDefault(LAST_FAIL, DSDateTime.NULL).setReadOnly(true);
    }

    /**
     * Will attempt to disconnected only if the current state is connected.
     */
    protected void disconnect() {
        if (!getConnectionState().isConnected()) {
            return;
        }
        put(state, DSConnectionState.DISCONNECTING);
        try {
            onDisconnect();
        } catch (Throwable x) {
            warn(warn() ? getPath() : null, x);
        }
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
     * Calls DSIConnected.onChange on all implementations in the subtree.  Stops at
     * instances of DSConnection, but if they implement DSIConnected, they will receive
     * the callback.  By default, this is only called by onConnected and onDisconnected.
     */
    protected void notifyDescendents() {
        notifyDescendents(this);
    }

    protected void onChildChanged(DSInfo info) {
        if (info == enabled) {
            synchronized (this) {
                notify();
            }
        }
    }

    /**
     * You must call connOk or connDown, it can be async after this method has returned.
     * You can throw an exception from this method instead of calling connDown.
     * This will only be called if configuration is ok.
     */
    protected abstract void onConnect();

    /**
     * Override point, called by connOkDown().  By default, this notifies all DSIConnected
     * objects in the subtree.  Overrides should probably call super.onConnected unless they
     * have a very good reason not to.
     */
    protected void onConnected() {
        notifyDescendents(this);
    }

    /**
     * You must call connDown, it can be async after this method has returned.  You can throw
     * an exception from this method instead of calling connDown.
     */
    protected abstract void onDisconnect();

    /**
     * Override point, called by connDown().  By default, this notifies all DSIConnected objects
     * in the subtree of the state change.  Overrides should probably call super.onDisconnected
     * untless they have a very good reason not to.
     */
    protected void onDisconnected() {
        notifyDescendents(this);
    }

    /**
     * Calls onDisconnected().
     */
    @Override
    protected void onStable() {
        onDisconnected();
    }

    /**
     * Override point, called by the run method.  Implementations should call verify the connection
     * is still valid and call connOk or connDown, but those can be async and after this method
     * returns.  Throwing an exception will be treated as a connDown.  By default, this only calls
     * connOk().
     */
    protected void ping() {
        connOk();
    }

    ///////////////////////////////////////////////////////////////////////////
    // Package / Private Methods
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Calls DSIConnected.onChange on all implementations in the subtree.  Stops at
     * instances of DSConnection, but if they implement DSIConnected, they will receive
     * the callback.
     */
    private void notifyDescendents(DSNode node) {
        DSInfo info = getFirstInfo();
        while (info != null) {
            if (info.is(DSIConnected.class)) {
                try {
                    ((DSIConnected) info.getObject()).onChange(this);
                } catch (Throwable t) {
                    error(error() ? info.getPath(null) : null, t);
                }
            }
            if (info.isNode() && !info.is(DSConnection.class)) {
                notifyDescendents(info.getNode());
            }
            info = info.next();
        }
    }


}
