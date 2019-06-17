package org.iot.dsa.conn;

import org.iot.dsa.DSRuntime;
import org.iot.dsa.node.DSInfo;
import org.iot.dsa.node.DSNode;
import org.iot.dsa.node.DSString;
import org.iot.dsa.node.event.DSEvent;
import org.iot.dsa.time.DSDateTime;
import org.iot.dsa.util.DSException;

/**
 * Represents connection with a lifecycle (connecting, connected, disconnecting, disconnected).
 * These connections do not have to have a long lived pipes such as a sockets, they could
 * represent http sessions or other such constructs.
 * <p>
 * Subclasses must:<br>
 * <ul>
 * <li> Implement doConnect, doDisconnect and doPing.
 * <li> Call connOk() after a successful communication.
 * <li> Call connDown(String) after a connection failure.  This does not need to be called for
 * higher level errors such as malformed sql statements being submitted over a connection.  In
 * those kinds of scenarios, just call connOk and let the ping loop determine if the connection
 * has been lost.
 * <li> Override checkConfig() and throw an exception if misconfigured.
 * </ul>
 * onStable() calls startUpdateTimer which calls updateState on a loop to manage connecting,
 * disconnecting and pinging.
 *
 * @author Aaron Hansen
 */
public abstract class DSConnection extends DSBaseConnection {

    ///////////////////////////////////////////////////////////////////////////
    // Class Fields
    ///////////////////////////////////////////////////////////////////////////

    protected static final String STATE = "State";
    protected static final String STATE_TIME = "State Time";

    /**
     * Event ID.
     */
    public static final String CONNECTED = "CONNECTED";

    /**
     * Singleton instance, fired whenever a connection transitions to connected.  There will
     * not be a child info or data accompanying the event.
     */
    public static final DSEvent CONNECTED_EVENT = new DSEvent(CONNECTED);

    /**
     * Event ID.
     */
    public static final String DISCONNECTED = "DISCONNECTED";

    /**
     * Singleton instance, fired whenever a connection transitions to disconnected.  There will
     * not be a child info or data accompanying the event.
     */
    public static final DSEvent DISCONNECTED_EVENT = new DSEvent(DISCONNECTED);

    ///////////////////////////////////////////////////////////////////////////
    // Instance Fields
    ///////////////////////////////////////////////////////////////////////////

    private long lastPing;
    private long retryConnectMs = 0;
    protected DSInfo state = getInfo(STATE);
    protected DSInfo stateTime = getInfo(STATE_TIME);
    private DSRuntime.Timer updateTimer;
    private boolean updating = false;

    ///////////////////////////////////////////////////////////////////////////
    // Public Methods
    ///////////////////////////////////////////////////////////////////////////

    /**
     * If the connection state is connected, this will call disconnect.  Otherwise, if not
     * already disconnected this transitions the connection to disconnected.
     *
     * @param reason Optional
     */
    @Override
    public void connDown(String reason) {
        debug(debug() ? "connDown: " + reason : null);
        if (getConnectionState().isConnected()) {
            put(lastFail, DSDateTime.now());
            if (reason != null) {
                if (!getStatusText().equals(reason)) {
                    put(statusText, DSString.valueOf(reason));
                }
            }
            disconnect();
        } else if (!getConnectionState().isDisconnected()) {
            put(state, DSConnectionState.DISCONNECTED);
            put(stateTime, DSDateTime.now());
            notifyConnectedDescendants(this, this);
            super.connDown(reason);
            try {
                onDisconnected();
            } catch (Exception x) {
                error(error() ? getPath() : null, x);
            }
            fire(DISCONNECTED_EVENT, null, null);
        }
    }


    /**
     * Update the last ok timestamp, will remove the down status if present and notifies the
     * subtree if the connection actually transitions to connected.
     */
    @Override
    public void connOk() {
        if (getConnectionState().isDisengaged()) {
            debug(debug() ? "Not connecting or connected, ignoring connOk()" : null);
            return;
        }
        long now = System.currentTimeMillis();
        if ((now - lastOkMillis) > 1000) { //prevents subscriptions to last ok from going crazy
            put(lastOk, DSDateTime.valueOf(now));
        }
        lastOkMillis = now;
        if (!isConnected()) {
            lastPing = now;
            put(state, DSConnectionState.CONNECTED);
            put(stateTime, DSDateTime.valueOf(now));
            notifyConnectedDescendants(this, this);
            super.connOk();
            try {
                onConnected();
            } catch (Exception x) {
                error(error() ? getPath() : null, x);
            }
            fire(CONNECTED_EVENT, null, null);
            synchronized (stateTime) {
                stateTime.notifyAll();
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
        put(stateTime, DSDateTime.now());
        notifyConnectedDescendants(this, this);
        if (isEnabled() && canConnect()) {
            try {
                doConnect();
            } catch (Throwable e) {
                error(error() ? getPath() : null, e);
                connDown(DSException.makeMessage(e));
            }
        } else {
            put(state, DSConnectionState.DISCONNECTED);
            put(stateTime, DSDateTime.now());
            notifyConnectedDescendants(this, this);
        }
    }

    /**
     * Transitions the connection to disconnecting then disconnected and makes all the
     * respective callbacks.
     */
    public void disconnect() {
        debug(debug() ? "Disconnect" : null);
        put(state, DSConnectionState.DISCONNECTING);
        put(stateTime, DSDateTime.now());
        notifyConnectedDescendants(this, this);
        try {
            doDisconnect();
        } catch (Throwable x) {
            error(getPath(), x);
        }
        put(state, DSConnectionState.DISCONNECTED);
        put(stateTime, DSDateTime.now());
        notifyConnectedDescendants(this, this);
        down = true;
        updateStatus(null);
        try {
            onDisconnected();
        } catch (Exception x) {
            error(error() ? getPath() : null, x);
        }
        fire(DISCONNECTED_EVENT, null, null);
    }

    public DSConnectionState getConnectionState() {
        return (DSConnectionState) state.get();
    }

    public boolean isConnected() {
        return getConnectionState().isConnected();
    }

    /**
     * Blocks the calling thread until the connection is connected or a timeout occurs.  Will throw
     * a IllegalStateException on timeout.
     *
     * @param timeout How long to wait in ms.  0 or less is an indefinite timeout.
     * @throws IllegalStateException If a timeout occurs.
     */
    public void waitForConnection(int timeout) {
        synchronized (stateTime) {
            if (isConnected()) {
                return;
            }
            try {
                if (timeout > 0) {
                    long end = System.currentTimeMillis() + timeout;
                    while (!isConnected()) {
                        stateTime.wait(timeout);
                        if (System.currentTimeMillis() > end) {
                            break;
                        }
                    }
                } else {
                    while (!isConnected()) {
                        stateTime.wait();
                    }
                }
            } catch (Exception x) {
                debug("", timeout);
            }
            if (!isConnected()) {
                throw new IllegalStateException("Timed out");
            }
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // Protected Methods
    ///////////////////////////////////////////////////////////////////////////
    @Override
    protected void declareDefaults() {
        super.declareDefaults();
        declareDefault(STATE, DSConnectionState.DISCONNECTED).setReadOnly(true).setTransient(true);
        declareDefault(STATE_TIME, DSDateTime.now()).setReadOnly(true).setTransient(true);
    }

    /**
     * Subclasses must establish the connection. You must call connOk or connDown, but that can
     * be async after this method has returned. You can throw an exception from this method instead
     * of calling connDown. This will only be called if configuration is ok.
     */
    protected abstract void doConnect();

    /**
     * Close and clean up resources, when this returns, the connection will be put into
     * the disconnected state, onDisconnected will be called, and descendants will be notified.
     */
    protected abstract void doDisconnect();

    /**
     * Override point, called by the run method.  Implementations should verify the connection
     * is still valid and call connOk or connDown, but those can be async and after this method
     * returns.  Throwing an exception will be treated as a connDown.  By default, this does
     * nothing.
     */
    protected void doPing() {
    }

    /**
     * Ping interval in milliseconds (default is 10000).  If the time since the last
     * ping exceeds this, the doPing will be called.
     *
     * @return 10000
     */
    protected long getPingInterval() {
        return 10000;
    }

    protected long getTimeInState() {
        DSDateTime time = (DSDateTime) stateTime.get();
        return System.currentTimeMillis() - time.timeInMillis();
    }

    /**
     * How often to call updateState in millis.
     *
     * @return 5000
     */
    protected long getUpdateInterval() {
        return 5000;
    }

    /**
     * Called by connOk() when transitioning to connected.  By default, this does nothing.
     */
    protected void onConnected() {
    }

    @Override
    protected synchronized void onDisabled() {
        updateState();
    }

    /**
     * Called by connDown() when transitioning to disconnected.  By default, this does nothing.
     */
    protected void onDisconnected() {
    }

    @Override
    protected synchronized void onEnabled() {
        updateState();
    }

    /**
     * Calls startUpdateTimer()
     */
    protected void onStable() {
        super.onStable();
        startUpdateTimer();
    }

    /**
     * Starts a timer to update state.
     */
    protected void startUpdateTimer() {
        if (updateTimer == null) {
            updateTimer = DSRuntime.run(() -> updateState(), System.currentTimeMillis(), getUpdateInterval());
        }
    }

    /**
     * Stops the update timer if there is one.
     */
    protected void stopUpdateTimer() {
        if (updateTimer != null) {
            updateTimer.cancel();
            updateTimer = null;
        }
    }

    /**
     * Manages connecting, disconnecting and pinging.  onStable starts a timer to call this method.
     */
    protected void updateState() {
        synchronized (this) {
            if (updating) {
                return;
            }
            updating = true;
        }
        try {
            if (isConnected()) {
                retryConnectMs = getUpdateInterval();
                if (!isEnabled()) {
                    disconnect();
                } else {
                    try {
                        long now = System.currentTimeMillis();
                        long duration = now - lastPing;
                        if (duration >= getPingInterval()) {
                            lastPing = now;
                            try {
                                doPing();
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
                    if (getTimeInState() >= retryConnectMs) {
                        retryConnectMs = Math.min(60000, retryConnectMs + getUpdateInterval());
                        connect();
                    }
                }
            }
        } finally {
            updating = false;
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // Package / Private Methods
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Calls DSIConnectionDescendant.onChange on implementations in the subtree.  Stops at instances of
     * DSIConnectionDescendant and DSConnection.
     */
    private void notifyConnectedDescendants(DSNode node, DSConnection conn) {
        DSInfo info = node.getFirstInfo();
        while (info != null) {
            if (info.is(DSIConnectionDescendant.class)) {
                try {
                    ((DSIConnectionDescendant) info.get()).onConnectionChange(conn);
                } catch (Throwable t) {
                    error(error() ? info.getPath(null) : null, t);
                }
            } else if (info.isNode() && !info.is(DSConnection.class)) {
                notifyConnectedDescendants(info.getNode(), conn);
            }
            info = info.next();
        }
    }

}
