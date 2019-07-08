package org.iot.dsa.dslink.responder;

import org.iot.dsa.node.DSMap;
import org.iot.dsa.node.action.ActionInvocation;

/**
 * See ActionInvocation for the real meat.
 *
 * @author Aaron Hansen
 * @see ActionInvocation
 */
public interface InboundInvokeRequest extends InboundRequest, ActionInvocation {

    /**
     * The parameters supplied by the invoker, or null.
     */
    public DSMap getParameters();

}
