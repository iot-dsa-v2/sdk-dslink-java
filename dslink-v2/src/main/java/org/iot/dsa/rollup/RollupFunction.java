package org.iot.dsa.rollup;

import org.iot.dsa.node.DSElement;
import org.iot.dsa.node.DSStatus;

/**
 * Rolls up values according to a specific DSRollup.  A function
 * will ignore values with invalid status, unless all of the values being
 * combined have invalid status.
 *
 * @author Aaron Hansen
 */
public interface RollupFunction {

    /**
     * The number of values combined.
     */
    public int getCount();

    /**
     * Defines how the RollupFunction aggregates values.
     */
    public DSRollup getRollup();

    /**
     * Status bits.
     *
     * @see DSStatus#getBits()
     */
    public int getStatus();

    /**
     * The current state of the rollup.
     */
    public DSElement getValue();

    /**
     * Clears the state of the RollupFunction; the count will be zero and
     * the current value will be null (getAnalyticValue().isNull()==true).
     */
    public RollupFunction reset();

    /**
     * Updates the combination with the given value.  Returns true
     * if the state of the rollup incorporates the given value.  For
     * example, for a "first" combination, the first element will return
     * true, but all others will return false.
     */
    public boolean update(DSElement value, int status);

}
