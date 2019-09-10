package org.iot.dsa.dslink.responder;

import org.iot.dsa.node.DSMap;
import org.iot.dsa.node.action.DSIActionRequest;

/**
 * See ActionInvocation for the real meat.
 *
 * @author Aaron Hansen
 * @see DSIActionRequest
 */
public interface InboundInvokeRequest extends InboundRequest, DSIActionRequest {

    /**
     * The parameters supplied by the invoker, or null.
     */
    public DSMap getParameters();

}
