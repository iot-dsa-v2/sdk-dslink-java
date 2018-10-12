package org.iot.dsa.rollup;

import org.iot.dsa.node.DSDouble;
import org.iot.dsa.node.DSElement;
import org.iot.dsa.node.DSStatus;

/**
 * The max double value.
 *
 * @author Aaron Hansen
 */
final class MaxRollupFunction extends AbstractRollupFunction {

    /////////////////////////////////////////////////////////////////
    // Instance Fields
    /////////////////////////////////////////////////////////////////

    private double val;

    /////////////////////////////////////////////////////////////////
    // Constructors
    /////////////////////////////////////////////////////////////////

    public MaxRollupFunction() {
        super(DSRollup.MAX);
        reset();
    }

    /////////////////////////////////////////////////////////////////
    // Public Methods
    /////////////////////////////////////////////////////////////////

    @Override
    public DSElement getValue() {
        if (getCount() == 0) {
            return DSDouble.NULL;
        }
        return DSDouble.valueOf(val);
    }

    /////////////////////////////////////////////////////////////////
    // Protected Methods
    /////////////////////////////////////////////////////////////////

    @Override
    protected void onReset() {
        val = 0;
    }

    @Override
    protected boolean onUpdate(DSElement arg, DSStatus status) {
        double tmp = arg.toDouble();
        if (getCount() == 0) {
            val = tmp;
        } else if (tmp > val) {
            val = tmp;
        }
        return true;
    }

}
