package org.iot.dsa.rollup;

import org.iot.dsa.node.DSDouble;
import org.iot.dsa.node.DSElement;
import org.iot.dsa.node.DSStatus;

/**
 * The delta between the min and max double values.
 *
 * @author Aaron Hansen
 */
final class RangeRollupFunction extends AbstractRollupFunction {

    /////////////////////////////////////////////////////////////////
    // Instance Fields
    /////////////////////////////////////////////////////////////////

    private double max;
    private double min;

    /////////////////////////////////////////////////////////////////
    // Constructors
    /////////////////////////////////////////////////////////////////

    public RangeRollupFunction() {
        super(DSRollup.RANGE);
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
        return DSDouble.valueOf(max - min);
    }

    /////////////////////////////////////////////////////////////////
    // Protected Methods
    /////////////////////////////////////////////////////////////////

    @Override
    protected void onReset() {
        min = 0;
        max = 0;
    }

    @Override
    protected boolean onUpdate(DSElement arg, int status) {
        double val = arg.toDouble();
        if (getCount() == 0) {
            min = val;
            max = val;
        } else {
            if (val > max) {
                max = val;
            } else if (val < min) {
                min = val;
            }
        }
        return true;
    }

}
