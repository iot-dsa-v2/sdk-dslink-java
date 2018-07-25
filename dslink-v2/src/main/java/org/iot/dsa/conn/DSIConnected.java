package org.iot.dsa.conn;

/**
 * Connected objects receive callbacks when their associate connection changes state.
 */
public interface DSIConnected {

    /**
     * Notification that the connection has changed state.
     */
    public void onChange(DSConnection conn);

}
