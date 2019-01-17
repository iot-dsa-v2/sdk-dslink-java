package org.iot.dsa.node;

import org.iot.dsa.node.action.DSAction;

/**
 * Only for use on DSIValues.  Replaces protocol write/set with a virtual action and the value
 * will be marked readonly in the protocol.  If the info of the value is marked readonly,
 * this action will not be available.
 *
 * @author Aaron Hansen
 */
public interface DSISetAction extends DSIValue {

    ///////////////////////////////////////////////////////////////////////////
    // Class Fields
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Default action name: Set
     */
    public static final String SET = "Set";

    ///////////////////////////////////////////////////////////////////////////
    // Public Methods
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Implementations should return the action for setting the value.
     */
    public DSAction getSetAction();

    /**
     * The name for the set action, "set" by default.
     */
    public default String getSetActionName() {
        return "Set";
    }

}
