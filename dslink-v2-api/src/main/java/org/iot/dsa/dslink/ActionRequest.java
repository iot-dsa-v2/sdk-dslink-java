package org.iot.dsa.dslink;

import org.iot.dsa.node.DSMap;
import org.iot.dsa.security.DSPermission;

/**
 * Encapsulates the details of an action invocation and provides the mechanism for updating an open
 * stream.
 *
 * @author Aaron Hansen
 */
public interface ActionRequest {

    /**
     * For use with streams and open tables, will have no effect if the stream is already closed.
     */
    public void close();

    /**
     * Close and send an error. For use with streams and open tables, will have no effect if the
     * stream is already closed.
     */
    public void close(Exception reason);

    /**
     * This needs to be called when there are more results after ActionsResults.next()
     * returns false.
     */
    public void enqueueResults();

    /**
     * The parameters supplied by the invoker, or null.
     */
    public DSMap getParameters();

    /**
     * The permission level of the invoker.
     */
    public DSPermission getPermission();

    /**
     * Whether or not response is still open.
     */
    public boolean isOpen();

}
