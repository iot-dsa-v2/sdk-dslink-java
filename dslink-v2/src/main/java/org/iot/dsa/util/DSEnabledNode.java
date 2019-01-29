package org.iot.dsa.util;

import org.iot.dsa.node.DSBool;
import org.iot.dsa.node.DSIStatus;
import org.iot.dsa.node.DSInfo;
import org.iot.dsa.node.DSStatus;

/**
 * A status node with the ability to disable.
 *
 * @author Aaron Hansen
 */
public abstract class DSEnabledNode extends DSStatusNode implements DSIStatus {

    ///////////////////////////////////////////////////////////////////////////
    // Class Fields
    ///////////////////////////////////////////////////////////////////////////

    protected static final String ENABLED = "Enabled";

    ///////////////////////////////////////////////////////////////////////////
    // Instance Fields
    ///////////////////////////////////////////////////////////////////////////

    private DSInfo enabled = getInfo(ENABLED);

    ///////////////////////////////////////////////////////////////////////////
    // Public Methods
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Uses the status property to account for any inherited state.
     *
     * @return Default implementation returns true.
     */
    public boolean isEnabled() {
        return enabled.getElement().toBoolean();
    }

    public void setEnabled(boolean enabled) {
        put(this.enabled, DSBool.valueOf(enabled));
    }

    ///////////////////////////////////////////////////////////////////////////
    // Protected Methods
    ///////////////////////////////////////////////////////////////////////////

    @Override
    protected void declareDefaults() {
        super.declareDefaults();
        declareDefault(ENABLED, DSBool.TRUE);
    }

    @Override
    protected int getThisStatus() {
        int ret = super.getThisStatus();
        if (isEnabled()) {
            ret = ret & ~DSStatus.DISABLED;
        } else {
            ret = ret | DSStatus.DISABLED;
        }
        return ret;
    }

    /**
     * Traps changes to the enabled property and updates the status accordingly.  Overrides
     * should call the super implemenation.
     */
    @Override
    protected void onChildChanged(DSInfo child) {
        if (child == enabled) {
            if (child.getElement().toBoolean()) {
                updateStatus(null);
            } else {
            }
        }
        super.onChildChanged(child);
    }


}
