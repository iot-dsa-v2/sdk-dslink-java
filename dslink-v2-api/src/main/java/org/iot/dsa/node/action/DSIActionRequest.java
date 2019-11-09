package org.iot.dsa.node.action;

import org.iot.dsa.dslink.ActionRequest;
import org.iot.dsa.node.DSIObject;
import org.iot.dsa.node.DSInfo;

/**
 * ActionRequest for DIActions.
 */
public interface DSIActionRequest extends ActionRequest {

    /**
     * The action being invoked.
     */
    default DSIAction getAction() {
        return getActionInfo().getAction();
    }

    /**
     * The info for the action being invoked.
     */
    DSInfo<DSIAction> getActionInfo();

    /**
     * The target of the action, such as a node or value.
     */
    default DSIObject getTarget() {
        return getTargetInfo().get();
    }

    /**
     * The target info of the action, such as a node or value.
     */
    DSInfo<?> getTargetInfo();

}
