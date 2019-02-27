package org.iot.dsa.dslink.requester;

import org.iot.dsa.node.DSMap;

/**
 * Convenience implementation of the callback passed to the invoke method in the requester.
 *
 * @author Daniel Shapiro, Aaron Hansen
 */
public abstract class AbstractInvokeHandler
        extends AbstractRequestHandler
        implements OutboundInvokeHandler {

    ///////////////////////////////////////////////////////////////////////////
    // Methods
    ///////////////////////////////////////////////////////////////////////////

    @Override
    public DSMap getParams() {
        return (DSMap) super.getParams();
    }

}
