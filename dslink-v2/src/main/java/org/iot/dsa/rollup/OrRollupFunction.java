package org.iot.dsa.rollup;

import org.iot.dsa.node.DSBool;
import org.iot.dsa.node.DSElement;

/**
 * Logically ORs values.
 *
 * @author Aaron Hansen
 */
final class OrRollupFunction extends AbstractRollupFunction {

    /////////////////////////////////////////////////////////////////
    // Instance Fields
    /////////////////////////////////////////////////////////////////

    private boolean val;

    /////////////////////////////////////////////////////////////////
    // Constructors
    /////////////////////////////////////////////////////////////////

    public OrRollupFunction() {
        super(DSRollup.OR);
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
    }

    @Override
    protected boolean onUpdate(DSElement arg, int status) {
        if (getCount() == 0) {
            val = arg.toBoolean();
        } else {
            val |= arg.toBoolean();
        }
        return true;
    }

}
