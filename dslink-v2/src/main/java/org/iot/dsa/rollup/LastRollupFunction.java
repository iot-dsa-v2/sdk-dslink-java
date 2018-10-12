package org.iot.dsa.rollup;

import org.iot.dsa.node.DSElement;
import org.iot.dsa.node.DSNull;
import org.iot.dsa.node.DSStatus;

/**
 * The last value in the rollup.
 *
 * @author Aaron Hansen
 */
final class LastRollupFunction extends AbstractRollupFunction {

    /////////////////////////////////////////////////////////////////
    // Instance Fields
    /////////////////////////////////////////////////////////////////

    private DSElement val;

    /////////////////////////////////////////////////////////////////
    // Constructors
    /////////////////////////////////////////////////////////////////

    public LastRollupFunction() {
        super(DSRollup.LAST);
        reset();
    }

    /////////////////////////////////////////////////////////////////
    // Public Methods
    /////////////////////////////////////////////////////////////////

    @Override
    public DSElement getValue() {
        return val;
    }

    /////////////////////////////////////////////////////////////////
    // Protected Methods
    /////////////////////////////////////////////////////////////////

    @Override
    protected void onReset() {
        val = DSNull.NULL;
    }

    @Override
    protected boolean onUpdate(DSElement arg, DSStatus status) {
        val = arg;
        return true;
    }

}
