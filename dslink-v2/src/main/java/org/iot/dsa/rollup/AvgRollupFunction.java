package org.iot.dsa.rollup;

import org.iot.dsa.node.DSDouble;
import org.iot.dsa.node.DSElement;
import org.iot.dsa.node.DSStatus;

/**
 * Computes the average, aka mean, value.
 *
 * @author Aaron Hansen
 */
final class AvgRollupFunction extends AbstractRollupFunction {

    /////////////////////////////////////////////////////////////////
    // Instance Fields
    /////////////////////////////////////////////////////////////////

    private double sum;

    /////////////////////////////////////////////////////////////////
    // Constructors
    /////////////////////////////////////////////////////////////////

    public AvgRollupFunction() {
        super(DSRollup.AVG);
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
        return DSDouble.valueOf(sum / getCount());
    }

    /////////////////////////////////////////////////////////////////
    // Protected Methods
    /////////////////////////////////////////////////////////////////

    @Override
    protected void onReset() {
        sum = 0;
    }

    @Override
    protected boolean onUpdate(DSElement arg, int status) {
        sum += arg.toDouble();
        return true;
    }

}
