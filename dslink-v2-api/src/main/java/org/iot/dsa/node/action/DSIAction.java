package org.iot.dsa.node.action;

import static org.iot.dsa.dslink.Action.ResultsType.VALUES;

import org.iot.dsa.dslink.Action;
import org.iot.dsa.dslink.ActionResults;
import org.iot.dsa.node.DSIObject;
import org.iot.dsa.node.DSIValue;
import org.iot.dsa.node.DSInfo;
import org.iot.dsa.node.DSList;
import org.iot.dsa.node.DSMap;
import org.iot.dsa.table.DSIResults;
import org.iot.dsa.table.DSIResultsCursor;

/**
 * Defines an invokable action in the node tree.
 *
 * @author Aaron Hansen
 */
public interface DSIAction extends Action, DSIObject {

    /**
     * This will be called instead of getColumnCount().  It enables more dynamic behavior.
     * However, the default implementation of this method simply calls getColumnCount().
     *
     * @see Action#getColumnCount()
     */
    public default int getColumnCount(DSInfo target) {
        return getColumnCount();
    }

    /**
     * This will be called instead of getColumnMetadata(int,DSMap).  It enables more dynamic behavior.
     * However, the default implementation of this method simply calls getColumnMetadata(int,DSMap).
     *
     * @see Action#getColumnMetadata(int, DSMap)
     */
    public default void getColumnMetadata(DSInfo target, int idx, DSMap bucket) {
        getColumnMetadata(idx, bucket);
    }

    /**
     * This will be called instead of getParameterCount().  It enables more dynamic behavior.
     * However, the default implementation of this method simply calls getParameterCount().
     *
     * @see Action#getParameterCount()
     */
    public default int getParameterCount(DSInfo target) {
        return getParameterCount();
    }

    /**
     * This will be called instead of getParameterMetadata(int,DSMap).  It enables more dynamic behavior.
     * However, the default implementation of this method simply calls getParameterMetadata(int,DSMap).
     *
     * @see Action#getParameterMetadata(int, DSMap)
     */
    public default void getParameterMetadata(DSInfo target, int idx, DSMap bucket) {
        getParameterMetadata(idx, bucket);
    }

    /**
     * Execute the action for the given request.  It is safe to use the calling thread
     * for long lived operations.  If the return type is void, perform the full operation on the
     * calling thread so that errors will be properly reported and return null.
     * <p>
     * To report an error, simply throw a runtime exception from this method, or call
     * ActionInvocation.close(Exception) when processing asynchronously.
     *
     * @param request Details about the incoming invoke as well as the mechanism to
     *                send async updates over an open stream.
     * @return Can be null if the result type is void.
     * @throws RuntimeException Throw a runtime exception to report an error and close the stream.
     */
    public ActionResults invoke(DSIActionRequest request);


    /**
     * Calls the corresponding static toResults.
     */
    public default ActionResults makeResults(DSIActionRequest req, DSIValue... value) {
        return toResults(req, value);
    }

    /**
     * Calls the corresponding static toResults.
     */
    public default ActionResults makeResults(DSIActionRequest req, DSIResults res) {
        return toResults(req, res);
    }

    /**
     * Calls the corresponding static toResults.
     */
    public default ActionResults makeResults(DSIActionRequest req,
                                             DSIResults res,
                                             boolean autoClose) {
        return toResults(req, res, autoClose);
    }

    /**
     * Called for each parameter as it is being sent to the requester in response to a list
     * request. The intent is to update the default value to represent the current state of the
     * target.  Does nothing by default.
     *
     * @param target    The info about the target of the action (its parent).
     * @param parameter Map representing a single parameter.
     */
    public default void prepareParameter(DSInfo target, DSMap parameter) {
    }

    /**
     * Makes a single row result. Uses the columns defined in the action.
     * The result type of the action must also be VALUES.
     * The request will be automatically closed.
     */
    public static ActionResults toResults(final DSIActionRequest req, final DSIValue... value) {
        return new ActionResults() {

            boolean next = true;

            @Override
            public int getColumnCount() {
                return req.getAction().getColumnCount();
            }

            @Override
            public void getColumnMetadata(int idx, DSMap bucket) {
                req.getAction().getColumnMetadata(idx, bucket);
            }

            @Override
            public void getResults(DSList bucket) {
                for (DSIValue v : value) {
                    bucket.add(v.toElement());
                }
            }

            @Override
            public ResultsType getResultsType() {
                return VALUES;
            }

            @Override
            public boolean next() {
                if (!next) {
                    req.close();
                    return false;
                }
                next = false;
                return true;
            }

            {
                if (req.getAction().getResultsType() != VALUES) {
                    throw new IllegalStateException("Action result type not VALUES");
                }
            }
        };
    }

    /**
     * Convenience for makeRequests(req,res,true) which auto closes tables and streams.
     */
    public static ActionResults toResults(final DSIActionRequest req,
                                          final DSIResults res) {
        return toResults(req, res, true);
    }

    /**
     * Makes an action result for the given parameters.  If the DIResult defines columns those will
     * be used, otherwise the columns defined by the action will be used.
     *
     * @param req       Be sure to call enqueueRequest or close if autoClose if false.
     * @param res       Note that this can be a DIResultsCursor.
     * @param autoClose Whether or not to close the request when the results has no more rows.  If
     *                  the results is not a cursor, the request will be automatically closed
     *                  no matter what.
     */
    public static ActionResults toResults(final DSIActionRequest req,
                                          final DSIResults res,
                                          final boolean autoClose) {
        return new ActionResults() {

            boolean next = true;

            @Override
            public int getColumnCount() {
                int count = res.getColumnCount();
                if (count > 0) {
                    return count;
                }
                return req.getAction().getColumnCount(req.getTargetInfo());
            }

            @Override
            public void getColumnMetadata(int idx, DSMap bucket) {
                if (res.getColumnCount() > 0) {
                    res.getColumnMetadata(idx, bucket);
                } else {
                    req.getAction().getColumnMetadata(req.getTargetInfo(), idx, bucket);
                }
            }

            @Override
            public void getResults(DSList bucket) {
                for (int i = 0, len = getColumnCount(); i < len; i++) {
                    bucket.add(res.getValue(i).toElement());
                }
            }

            @Override
            public ResultsType getResultsType() {
                return req.getAction().getResultsType();
            }

            @Override
            public boolean next() {
                if (res instanceof DSIResultsCursor) {
                    DSIResultsCursor cur = (DSIResultsCursor) res;
                    next = cur.next();
                    if (!next & autoClose) {
                        req.close();
                    }
                    return next;
                }
                if (!next) {
                    req.close();
                    return false;
                }
                next = false;
                return true;
            }
        };
    }


}
