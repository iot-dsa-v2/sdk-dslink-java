package org.iot.dsa.rollup;

import java.util.Arrays;
import org.iot.dsa.node.DSDouble;
import org.iot.dsa.node.DSElement;
import org.iot.dsa.node.DSStatus;

/**
 * The median (middle) double value.
 *
 * @author Aaron Hansen
 */
final class MedianRollupFunction extends AbstractRollupFunction {

    /////////////////////////////////////////////////////////////////
    // Instance Fields
    /////////////////////////////////////////////////////////////////

    private double[] vals = new double[100];

    /////////////////////////////////////////////////////////////////
    // Constructors
    /////////////////////////////////////////////////////////////////

    public MedianRollupFunction() {
        super(DSRollup.MEDIAN);
        reset();
    }

    /////////////////////////////////////////////////////////////////
    // Public Methods
    /////////////////////////////////////////////////////////////////

    @Override
    public DSElement getValue() {
        int len = getCount();
        if (len == 0) {
            return DSDouble.NULL;
        }
        double res = 0;
        if (len == 1) {
            res = vals[0];
        } else if (len > 1) {
            Arrays.sort(vals, 0, len);
            res = vals[(int) ((len + 0.5d) / 2d)];
        }
        return DSDouble.valueOf(res);
    }

    /////////////////////////////////////////////////////////////////
    // Protected Methods
    /////////////////////////////////////////////////////////////////

    protected void onReset() {
    }

    @Override
    protected boolean onUpdate(DSElement arg, int status) {
        int len = getCount();
        if (getCount() == 0) {
            vals[0] = arg.toDouble();
        } else {
            if (len == vals.length) {
                double[] ary = new double[len * 2];
                System.arraycopy(vals, 0, ary, 0, len);
                vals = ary;
            }
            vals[len] = arg.toDouble();
        }
        return true;
    }

}
