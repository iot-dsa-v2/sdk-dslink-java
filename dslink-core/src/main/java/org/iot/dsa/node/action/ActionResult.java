package org.iot.dsa.node.action;

/**
 * Super interface for possible results of an action.
 *
 * @author Aaron Hansen
 * @see ActionTable
 * @see ActionValues
 */
public interface ActionResult {

    /**
     * The action that was invoked.  This is needed for custom DSResponder implementations where
     * the action is unknown.
     */
    public ActionSpec getAction();

    /**
     * Always called, whether or not the result is a stream, and no matter who closes it.
     */
    public void onClose();

}
