package org.iot.dsa.rollup;

import org.iot.dsa.node.DSDouble;
import org.iot.dsa.node.DSElement;
import org.iot.dsa.node.DSStatus;

/**
 * Adds double values.
 *
 * @author Aaron Hansen
 */
final class SumRollupFunction extends AbstractRollupFunction {

    /////////////////////////////////////////////////////////////////
    // Instance Fields
    /////////////////////////////////////////////////////////////////

    private double value = 0;

    /////////////////////////////////////////////////////////////////
    // Constructors
    /////////////////////////////////////////////////////////////////

    public SumRollupFunction() {
        super(DSRollup.SUM);
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
        return DSDouble.valueOf(value);
    }

    /////////////////////////////////////////////////////////////////
    // Protected Methods
    /////////////////////////////////////////////////////////////////

    @Override
    protected void onReset() {
        value = 0;
    }

    @Override
    protected boolean onUpdate(DSElement arg, DSStatus status) {
        if (getCount() == 0) {
            value = arg.toDouble();
        } else {
            value += arg.toDouble();
        }
        return true;
    }

}
