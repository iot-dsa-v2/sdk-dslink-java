package org.iot.dsa.conn;

import org.iot.dsa.node.DSIStatus;
import org.iot.dsa.node.DSInfo;
import org.iot.dsa.node.DSStatus;
import org.iot.dsa.time.DSDateTime;
import org.iot.dsa.util.DSEnabledNode;
import org.iot.dsa.util.DSException;

/**
 * Basic representation of a connection, which is subclassed by DSConnection for longer lived
 * connections.  DSConnection should really be subclassed but this can if desired.
 * <p>
 * Subclasses must:<br>
 * <ul>
 * <li> Call canConnect() and only execute connection logic if it returns true.
 * <li> Call connOk() after a successful connection.
 * <li> Call connDown(String) after a connection failure.  This does not need to be called for
 * higher level errors such as malformed sql statements being submitted over a connection.  In
 * those kinds of scenarios, just call connOk.
 * <li> Override checkConfig() and throw an exception if misconfigured.
 * </ul>
 *
 * @author Aaron Hansen
 */
public abstract class DSBaseConnection extends DSEnabledNode implements DSIStatus {

    ///////////////////////////////////////////////////////////////////////////
    // Class Fields
    ///////////////////////////////////////////////////////////////////////////

    protected static final String LAST_OK = "Last Ok";
    protected static final String LAST_FAIL = "Last Fail";

    ///////////////////////////////////////////////////////////////////////////
    // Instance Fields
    ///////////////////////////////////////////////////////////////////////////

    protected DSInfo lastFail = getInfo(LAST_FAIL);
    protected DSInfo lastOk = getInfo(LAST_OK);
    protected long lastOkMillis;
    protected DSStatus myStatus = getDefaultStatus();

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
        myStatus = myStatus.add(DSStatus.DOWN);
        updateStatus(myStatus, reason);
    }


    /**
     * Updates the last ok timestamp, removes down and fault status, and clears the status text.
     */
    public void connOk() {
        trace(trace() ? getPath() + ": connOk" : null);
        long now = System.currentTimeMillis();
        if ((now - lastOkMillis) > 1000) { //prevents subscription to last ok time from going crazy
            put(lastOk, DSDateTime.valueOf(now));
        }
        lastOkMillis = now;
        myStatus = myStatus.remove(DSStatus.FAULT);
        myStatus = myStatus.remove(DSStatus.DOWN);
        updateStatus(myStatus, "");
    }

    /**
     * The last time connOk was called.
     */
    public long getLastOk() {
        return lastOkMillis;
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
        try {
            checkConfig();
            configOk();
        } catch (Throwable x) {
            error(error() ? getPath() : null, x);
            configFault(DSException.makeMessage(x));
        }
        return isOperational();
    }

    /**
     * Implementations must throw an exception if there are any configuration errors.
     */
    protected abstract void checkConfig();

    /**
     * Puts the connection into the fault state and optionally sets the message.
     *
     * @param msg Optional
     */
    private void configFault(String msg) {
        debug(debug() ? getPath() + ": configFault - " + msg : null);
        put(lastFail, DSDateTime.currentTime());
        myStatus = myStatus.remove(DSStatus.FAULT);
        updateStatus(myStatus, msg);
    }

    /**
     * Removes fault state.  Does not clear status text, that will be cleared by connOk.
     */
    private void configOk() {
        debug(debug() ? getPath() + ": configOk " : null);
        myStatus = myStatus.remove(DSStatus.FAULT);
        updateStatus(myStatus, null);
    }

    @Override
    protected void declareDefaults() {
        super.declareDefaults();
        declareDefault(LAST_OK, DSDateTime.NULL).setReadOnly(true);
        declareDefault(LAST_FAIL, DSDateTime.NULL).setReadOnly(true);
    }

    /**
     * Returns DSStatus.down
     */
    @Override
    protected DSStatus getDefaultStatus() {
        return DSStatus.down;
    }

    @Override
    protected String getLogName() {
        return getLogName("connection");
    }

    /**
     * Checks the status for the fault flag.
     */
    protected boolean isConfigOk() {
        return !getStatus().isFault();
    }

}
