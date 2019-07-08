package org.iot.dsa.conn;

/**
 * DSIConnectionDescendant instances receive this notification when the ancestral DSConnection changes state.
 * The DSIConnectionDescendant instance is responsible for propagating state changes to their subtree if
 * is needed.
 *
 * @author Aaron Hansen
 */
public interface DSIConnectionDescendant {

    /**
     * Called when the ancestral DSIConnectionDescendant changes state.  The subtree of this instance will not
     * notified, that is the responsibility of the DSIConnectionDescendant implementation..
     */
    public void onConnectionChange(DSConnection conn);

}
