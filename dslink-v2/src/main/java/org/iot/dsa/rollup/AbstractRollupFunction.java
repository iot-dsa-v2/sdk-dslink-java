package org.iot.dsa.rollup;

import org.iot.dsa.node.DSElement;
import org.iot.dsa.node.DSStatus;

/**
 * Common base for the implementations of the RollupFunction interface.
 *
 * @author Aaron Hansen
 */
abstract class AbstractRollupFunction implements RollupFunction {

    /////////////////////////////////////////////////////////////////
    // Instance Fields
    /////////////////////////////////////////////////////////////////

    private int bits = DSStatus.UNKNOWN;
    private int count = 0;
    private boolean resetOnValid = false;
    private DSRollup rollup;

    /////////////////////////////////////////////////////////////////
    // Constructors
    /////////////////////////////////////////////////////////////////

    protected AbstractRollupFunction(DSRollup rollup) {
        this.rollup = rollup;
    }

    /////////////////////////////////////////////////////////////////
    // Methods - Public and in alphabetical order by method name.
    /////////////////////////////////////////////////////////////////

    @Override
    public int getCount() {
        return count;
    }

    @Override
    public DSRollup getRollup() {
        return rollup;
    }

    @Override
    public int getStatus() {
        return bits;
    }

    @Override
    public RollupFunction reset() {
        bits = DSStatus.UNKNOWN;
        count = 0;
        resetOnValid = false;
        onReset();
        return this;
    }

    @Override
    public boolean update(DSElement value, int status) {
        if (!isValid(value, status)) {
            if (count == 0) {
                resetOnValid = true;
            } else if (!resetOnValid) {
                return false;
            }
        } else if (resetOnValid) {
            resetOnValid = false;
            reset();
        }
        if (!onUpdate(value, status)) {
            return false;
        }
        if (count == 0) {
            bits = status;
        } else {
            bits = bits | status;
        }
        count++;
        return true;
    }

    /////////////////////////////////////////////////////////////////
    // Methods - Protected and in alphabetical order by method name.
    /////////////////////////////////////////////////////////////////

    /**
     * Returns true if the value is non-null and the status is good.
     */
    protected boolean isValid(DSElement value, int status) {
        if (value == null) {
            return false;
        }
        if (value.isNull()) {
            return false;
        }
        return DSStatus.isGood(status);
    }

    /**
     * Reset the state of the function.
     */
    protected abstract void onReset();

    /**
     * Return true if the call is represented in the results. Returning
     * true will update the count.
     */
    protected abstract boolean onUpdate(DSElement value, int status);

}
