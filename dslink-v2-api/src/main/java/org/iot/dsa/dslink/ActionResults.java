package org.iot.dsa.dslink;

import org.iot.dsa.dslink.Action.ResultsType;
import org.iot.dsa.node.DSList;
import org.iot.dsa.node.DSMap;

/**
 * The results of an action request.  This is implemented as a cursor for all result types.
 * For async results, implement AsyncActionResults instead.
 *
 * @author Aaron Hansen
 * @see AsyncActionResults
 */
public interface ActionResults {

    /**
     * Unless the result type is void, this should return a value greater than zero.
     */
    public int getColumnCount();

    /**
     * Only needed if the column count is greater than 0.
     */
    public void getColumnMetadata(int idx, DSMap bucket);

    /**
     * The implementation should add the values of the current row to the given bucket.  The
     * bucket can be reused across calls, the implementation should not cache references to it.
     */
    public void getResults(DSList bucket);

    /**
     * Determines how the results should be used.
     */
    public ResultsType getResultsType();

    /**
     * Initially, this cursor must be positioned before the first set of results. Return true to
     * advance the next set of results.  If the implementation is a stream or AsyncActionResults,
     * this can return false and later have more rows, the implementor is responsible for calling
     * ActionRequest.sendResults() or ActionRequest.close().  If not a stream or async, the request
     * will be closed once this returns false.  This will only be called once for a results
     * type of VALUES.
     */
    public boolean next();

    /**
     * Always called, does nothing by default.
     */
    public default void onClose() {
    }

}
