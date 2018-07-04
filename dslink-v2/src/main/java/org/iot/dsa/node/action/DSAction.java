package org.iot.dsa.node.action;

import org.iot.dsa.node.DSInfo;
import org.iot.dsa.node.DSMap;

/**
 * The default action implementation routes invocations to DSNode.onInvoke.
 * <p>
 * {@inheritDoc}
 *
 * @see org.iot.dsa.node.DSNode#onInvoke(DSInfo, ActionInvocation)
 */
public class DSAction extends DSAbstractAction {

    /**
     * Use this when you have no-arg, no-return actions.  This instance cannot be
     * modified.
     */
    public static final DSAction DEFAULT = new DSAction(true);

    public DSAction() {
    }

    private DSAction(boolean immutable) {
        setImmutable(immutable);
    }

    /**
     * This implementation call onInvoke on the proper parent node.
     * <p>
     * {@inheritDoc}
     */
    @Override
    public ActionResult invoke(DSInfo info, ActionInvocation invocation) {
        return info.getParent().onInvoke(info, invocation);
    }

    /**
     * This implemenation does nothing to the parameter.
     * <p>
     * {@inheritDoc}
     */
    public void prepareParameter(DSInfo info, DSMap parameter) {
    }


}
