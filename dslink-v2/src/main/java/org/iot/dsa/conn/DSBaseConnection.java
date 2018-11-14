package org.iot.dsa.conn;

import org.iot.dsa.node.DSIStatus;
import org.iot.dsa.node.DSInfo;
import org.iot.dsa.node.DSNode;
import org.iot.dsa.node.DSStatus;
import org.iot.dsa.node.DSString;
import org.iot.dsa.time.DSDateTime;
import org.iot.dsa.util.DSException;

/**
 * Basic representation of a connection, which is subclassed by DSConnection to be stateful,
 * but can be subclassed directly for simpler / short lived connections.
 * <p>
 * Subclasses should call canConnect() and only execute connection logic only if it returns true.
 * If the subclass adds an enabled child, they should call canConnect() to update the status
 * accordingly.
 *
 * @author Aaron Hansen
 */
public abstract class DSBaseConnection extends DSNode implements DSIStatus {

    ///////////////////////////////////////////////////////////////////////////
    // Class Fields
    ///////////////////////////////////////////////////////////////////////////

    protected static final String LAST_OK = "Last Ok";
    protected static final String LAST_FAIL = "Last Fail";
    protected static final String STATUS = "Status";
    protected static final String STATUS_TEXT = "Status Text";

    ///////////////////////////////////////////////////////////////////////////
    // Instance Fields
    ///////////////////////////////////////////////////////////////////////////

    protected DSInfo lastFail = getInfo(LAST_FAIL);
    protected DSInfo lastOk = getInfo(LAST_OK);
    protected long lastOkMillis;
    protected DSInfo status = getInfo(STATUS);
    protected DSInfo statusText = getInfo(STATUS_TEXT);

    ///////////////////////////////////////////////////////////////////////////
    // Public Methods
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Sets the down status flag and updates the reason only if the reason doesn't match the
     * current value.
     *
     * @param reason Optional
     */
    public void connDown(String reason) {
        debug(debug() ? getPath() + ": connDown - " + reason : null);
        put(lastFail, DSDateTime.currentTime());
        if (!getStatus().isDown()) {
            put(status, getStatus().add(DSStatus.DOWN));
        }
        if (reason != null) {
            if (!statusText.getElement().toString().equals(reason)) {
                put(statusText, DSString.valueOf(reason));
            }
        }
    }


    /**
     * Updates the last ok timestamp, remove down and fault status, and clears the status text,
     * if needed.
     */
    public void connOk() {
        trace(trace() ? getPath() + ": connOk" : null);
        long now = System.currentTimeMillis();
        if ((now - lastOkMillis) > 1000) { //prevents subscription to last ok time from going crazy
            put(lastOk, DSDateTime.valueOf(now));
        }
        lastOkMillis = now;
        if (getStatus().isFault()) {
            put(status, getStatus().remove(DSStatus.FAULT));
        }
        if (getStatus().isDown()) {
            put(status, getStatus().remove(DSStatus.DOWN));
        }
        if (!getStatusText().isEmpty()) {
            put(statusText, DSString.EMPTY);
        }
    }

    /**
     * The last time connOk was called.
     */
    public long getLastOk() {
        return lastOkMillis;
    }

    @Override
    public DSStatus getStatus() {
        return (DSStatus) status.get();
    }

    public String getStatusText() {
        return statusText.getElement().toString();
    }

    /**
     * Checks the status for the fault flag.
     */
    protected boolean isConfigOk() {
        return !getStatus().isFault();
    }

    /**
     * Override point for connections that add an enabled child value.
     *
     * @return Default implementation returns true.
     */
    public boolean isEnabled() {
        return true;
    }

    /**
     * True if running, enabled and config is ok.
     */
    public boolean isOperational() {
        return isRunning() && isConfigOk() && isEnabled();
    }

    ///////////////////////////////////////////////////////////////////////////
    // Protected Methods
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Checks config, deals with any errors and returns whether or not this is operational.
     */
    protected boolean canConnect() {
        if (isEnabled()) {
            if (getStatus().isDisabled()) {
                put(status, getStatus().remove(DSStatus.disabled));
            }
        } else {
            if (!getStatus().isDisabled()) {
                put(status, DSStatus.disabled);
            }
            return false;
        }
        try {
            checkConfig();
            return isOperational();
        } catch (Throwable x) {
            error(error() ? getPath() : null, x);
            configFault(DSException.makeMessage(x));
        }
        return false;
    }

    /**
     * Validate configuration, then call configOk, configFault, or just throw an exception.
     */
    protected abstract void checkConfig();

    /**
     * Puts the connection into the fault state and optionally sets the message.
     *
     * @param msg Optional
     */
    protected void configFault(String msg) {
        debug(debug() ? getPath() + ": configFault - " + msg : null);
        put(lastFail, DSDateTime.currentTime());
        if (msg != null) {
            if (!getStatusText().equals(msg)) {
                put(statusText, DSString.valueOf(msg));
            }
        }
        if (!getStatus().isFault()) {
            put(status, getStatus().add(DSStatus.FAULT));
        }
    }

    /**
     * Removes fault state.  Does not clear status text, that will be cleared by connOk.
     */
    protected void configOk() {
        debug(debug() ? getPath() + ": configOk " : null);
        if (getStatus().isFault()) {
            put(status, getStatus().remove(DSStatus.FAULT));
        }
    }

    @Override
    protected void declareDefaults() {
        declareDefault(STATUS, DSStatus.down).setReadOnly(true).setTransient(true);
        declareDefault(STATUS_TEXT, DSString.EMPTY).setReadOnly(true).setTransient(true);
        declareDefault(LAST_OK, DSDateTime.NULL).setReadOnly(true);
        declareDefault(LAST_FAIL, DSDateTime.NULL).setReadOnly(true);
    }

    @Override
    protected String getLogName() {
        return getLogName("connection");
    }


}
