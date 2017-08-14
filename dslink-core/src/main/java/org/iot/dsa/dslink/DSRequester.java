package org.iot.dsa.dslink;

/**
 * Session object used by requesters.
 *
 * @author Aaron Hansen
 */
public interface DSRequester {

    /**
     * Called just after the connection is opened.  Can be called multiple times in between
     * calls to onStart and onStop.
     */
    public void onConnected(DSRequesterInterface session);

    /**
     * Called just after the connection is closed.  Can be called multiple times in between
     * calls to onStart and onStop.
     */
    public void onDisconnected(DSRequesterInterface session);

}
