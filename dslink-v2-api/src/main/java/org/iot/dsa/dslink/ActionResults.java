package org.iot.dsa.dslink;

import org.iot.dsa.dslink.Action.ResultsType;
import org.iot.dsa.node.DSList;
import org.iot.dsa.node.DSMap;

/**
 * The results of an action request.  This is implemented as a cursor for all result types.
 * The implementation can return false from next() and resume later.  After returning
 * false from next(), the implementation must call ActionRequest.close() or
 * ActionRequest.enqueueResults().
 * <p>
 * If the result type is VALUES, next() will only be called once.  The implementation does not
 * need to call any other methods on ActionRequest.
 *
 * @author Aaron Hansen
 */
public interface ActionResults {

    /**
     * Unless the result type is void, this should return a value greater than zero.
     */
    public int getColumnCount();

    /**
     * Only needed if the column count is greater than 0.  Throws an IllegalStateException by
     * default.
     */
    public void getColumnMetadata(int idx, DSMap bucket);

    /**
     * The implementation should add the values of the current result set to the given bucket.  The
     * bucket may be reused across calls, the implementation should not cache references to it.
     */
    public void getResults(DSList bucket);

    /**
     * Determines how the results should be used.
     */
    public ResultsType getResultsType();

    /**
     * Initially, this cursor must be positioned before the first set of results. Return true to
     * advance the next set of results.  The implementation is responsible for calling
     * DIActionRequest.enqueueResults() or DIActionRequest.close().  This will only
     * be called once for a result type of VALUES.
     */
    public boolean next();

    /**
     * Always called, does nothing by default.
     */
    public default void onClose() {
    }

}
