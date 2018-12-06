package org.iot.dsa.conn;

/**
 * DSIConnected instances receive this notification when the ancestral DSConnection changes state.
 * The DSIConnected instance is responsible for propagating state changes to their subtree if
 * is needed.
 *
 * @author Aaron Hansen
 */
public interface DSIConnected {

    /**
     * Called when the ancestral DSIConnected changes state.  The subtree of this instance will not
     * notified, that is the responsibility of the DSIConnected implementation..
     */
    public void onConnectionChange(DSConnection conn);

}
