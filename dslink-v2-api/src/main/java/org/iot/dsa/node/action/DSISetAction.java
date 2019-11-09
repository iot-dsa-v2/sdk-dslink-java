package org.iot.dsa.node.action;

import org.iot.dsa.node.DSIValue;

/**
 * If setting a value is more complex than one of the primitive editors, this can be used
 * to create a custom set action. Only for use on DSIValues.  Replaces protocol level write/set
 * with a virtual action and the value will be marked readonly in the protocol.  If the info of
 * the value is marked readonly, this action will not be available.
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
    String SET = "Set";

    ///////////////////////////////////////////////////////////////////////////
    // Public Methods
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Implementations should return the action for setting the value.
     */
    DSAction getSetAction();

    /**
     * The name for the set action, "set" by default.
     */
    default String getSetActionName() {
        return SET;
    }

}
