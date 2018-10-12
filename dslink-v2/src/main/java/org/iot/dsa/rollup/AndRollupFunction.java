package org.iot.dsa.rollup;

import org.iot.dsa.node.DSBool;
import org.iot.dsa.node.DSElement;
import org.iot.dsa.node.DSStatus;

/**
 * Logically ANDs values.
 *
 * @author Aaron Hansen
 */
final class AndRollupFunction extends AbstractRollupFunction {

    /////////////////////////////////////////////////////////////////
    // Instance Fields
    /////////////////////////////////////////////////////////////////

    private boolean val = false;

    /////////////////////////////////////////////////////////////////
    // Constructors
    /////////////////////////////////////////////////////////////////

    public AndRollupFunction() {
        super(DSRollup.AND);
        reset();
    }

    /////////////////////////////////////////////////////////////////
    // Public Methods
    /////////////////////////////////////////////////////////////////

    @Override
    public DSElement getValue() {
        if (getCount() == 0) {
            return DSBool.NULL;
        }
        return DSBool.valueOf(val);
    }

    /////////////////////////////////////////////////////////////////
    // Protected Methods
    /////////////////////////////////////////////////////////////////

    @Override
    protected void onReset() {
        val = false;
    }

    @Override
    protected boolean onUpdate(DSElement arg, DSStatus status) {
        if (getCount() == 0) {
            val = arg.toBoolean();
        } else {
            val &= arg.toBoolean();
        }
        return true;
    }

}
