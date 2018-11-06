package org.iot.dsa.node.action;

import org.iot.dsa.node.DSList;
import org.iot.dsa.node.DSMap;
import org.iot.dsa.security.DSPermission;

/**
 * Encapsulates the details of an action invocation and provides the mechanism for updating an open
 * stream.
 *
 * @author Aaron Hansen
 */
public interface ActionInvocation {

    /**
     * Only use with stream and open tables, instructs the requester to clear all existing rows.
     */
    public void clearAllRows();

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
     * The parameters supplied by the invoker, or null.
     */
    public DSMap getParameters();

    /**
     * The permission level of the invoker, should be verified against the permission level required
     * by the action.
     */
    public DSPermission getPermission();

    /**
     * Only use with open tables, insert the rows at the given index.
     */
    public void insert(int index, DSList... rows);

    /**
     * Whether or not response is still open.
     */
    public boolean isOpen();

    /**
     * Only use with stream and open tables.  Should not be called until the initial row iterator is
     * complete, otherwise the update buffer could grow very large.
     */
    public void send(DSList row);

    /**
     * Only use with open tables,  deletes len rows starting at the given index, then inserts the
     * given rows in their place.
     *
     * @param idx  Delete len rows starting at this index and replace with the given rows.
     * @param len  Does not have to match the number of rows.
     * @param rows Does not have to match the len.
     */
    public void replace(int idx, int len, DSList... rows);

}
