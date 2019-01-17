package org.iot.dsa.rollup;

import org.iot.dsa.node.DSElement;
import org.iot.dsa.node.DSNull;

/**
 * First value in the rollup.
 *
 * @author Aaron Hansen
 */
final class FirstRollupFunction extends AbstractRollupFunction {

    /////////////////////////////////////////////////////////////////
    // Instance Fields
    /////////////////////////////////////////////////////////////////

    private DSElement val;

    /////////////////////////////////////////////////////////////////
    // Constructors
    /////////////////////////////////////////////////////////////////

    public FirstRollupFunction() {
        super(DSRollup.FIRST);
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
    protected boolean onUpdate(DSElement arg, int status) {
        if (getCount() == 0) {
            val = arg;
            return true;
        }
        return false;
    }

}
