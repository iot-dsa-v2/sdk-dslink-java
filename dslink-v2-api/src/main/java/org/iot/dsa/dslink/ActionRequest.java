package org.iot.dsa.dslink;

import org.iot.dsa.node.DSElement;
import org.iot.dsa.node.DSMap;
import org.iot.dsa.security.DSPermission;

/**
 * Encapsulates the details of an action invocation and provides the mechanism for async
 * updates.
 *
 * @author Aaron Hansen
 */
public interface ActionRequest {

    /**
     * For use with streams and AsyncActionResults, will have no effect if already closed.
     */
    public void close();

    /**
     * Close and send an error. For use with streams and AsyncActionResults, will have no effect
     * if already closed.
     */
    public void close(Exception reason);

    /**
     * A convenience for getting a single parameter out of the parameters map.
     */
    public default DSElement getParameter(String key) {
        DSMap map = getParameters();
        if (map == null) {
            return null;
        }
        return map.get(key);
    }

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

    /**
     * This is for streams and AsyncActionResults.  Call this after returning false
     * ActionResults.next() and there are more results.  AsyncActionResults implementors also
     * need to call this to send the initial columns.
     *
     * @see AsyncActionResults
     */
    public void sendResults();

}
