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
    int getCount();

    /**
     * Defines how the RollupFunction aggregates values.
     */
    DSRollup getRollup();

    /**
     * Status bits.
     *
     * @see DSStatus#getBits()
     */
    int getStatus();

    /**
     * The current state of the rollup.
     */
    DSElement getValue();

    /**
     * Clears the state of the RollupFunction; the count will be zero and
     * the current value will be null (getAnalyticValue().isNull()==true).
     */
    RollupFunction reset();

    /**
     * Updates the combination with the given value.  Returns true
     * if the state of the rollup incorporates the given value.  For
     * example, for a "first" combination, the first element will return
     * true, but all others will return false.
     */
    boolean update(DSElement value, int status);

}
