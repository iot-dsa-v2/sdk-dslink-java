package org.iot.dsa.rollup;

import org.iot.dsa.node.DSElement;
import org.iot.dsa.node.DSLong;

/**
 * The number of values in the rollup.
 *
 * @author Aaron Hansen
 */
final class CountRollupFunction extends AbstractRollupFunction {

    /////////////////////////////////////////////////////////////////
    // Constructors
    /////////////////////////////////////////////////////////////////

    public CountRollupFunction() {
        super(DSRollup.COUNT);
        reset();
    }

    /////////////////////////////////////////////////////////////////
    // Public Methods
    /////////////////////////////////////////////////////////////////

    @Override
    public DSElement getValue() {
        return DSLong.valueOf(getCount());
    }

    /////////////////////////////////////////////////////////////////
    // Protected Methods
    /////////////////////////////////////////////////////////////////

    @Override
    protected void onReset() {
    }

    @Override
    protected boolean onUpdate(DSElement arg, int status) {
        return true;
    }

}
