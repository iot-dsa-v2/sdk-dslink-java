package org.iot.dsa.rollup;

import org.iot.dsa.node.DSDouble;
import org.iot.dsa.node.DSElement;
import org.iot.dsa.node.DSStatus;

/**
 * The min double value.
 *
 * @author Aaron Hansen
 */
final class MinRollupFunction extends AbstractRollupFunction {

    /////////////////////////////////////////////////////////////////
    // Instance Fields
    /////////////////////////////////////////////////////////////////

    private double val;

    /////////////////////////////////////////////////////////////////
    // Constructors
    /////////////////////////////////////////////////////////////////

    public MinRollupFunction() {
        super(DSRollup.MIN);
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
    }

    @Override
    protected boolean onUpdate(DSElement arg, DSStatus status) {
        double tmp = arg.toDouble();
        if (getCount() == 0) {
            val = tmp;
        } else if (tmp < val) {
            val = tmp;
        }
        return true;
    }

}
