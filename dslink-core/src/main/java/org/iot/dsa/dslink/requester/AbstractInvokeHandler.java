package org.iot.dsa.dslink.requester;

import org.iot.dsa.node.DSMap;

/**
 * Convenience implementation of the callback passed to the invoke method in the requester.
 *
 * @author Daniel Shapiro, Aaron Hansen
 */
public abstract class AbstractInvokeHandler implements OutboundInvokeHandler {

    ///////////////////////////////////////////////////////////////////////////
    // Fields
    ///////////////////////////////////////////////////////////////////////////

    private DSMap params;
    private String path;
    private OutboundStream stream;

    ///////////////////////////////////////////////////////////////////////////
    // Methods
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Returns the value passed to onInit.
     */
    public DSMap getParams() {
        return params;
    }

    /**
     * Returns the value passed to onInit.
     */
    public String getPath() {
        return path;
    }

    /**
     * Returns the value passed to onInit.
     */
    public OutboundStream getStream() {
        return stream;
    }

    /**
     * Sets the fields so they can be access via the corresponding getters.
     * <p>
     * <p>
     * <p>
     * {@inheritDoc}
     */
    @Override
    public void onInit(String path, DSMap params, OutboundStream stream) {
        this.path = path;
        this.params = params;
        this.stream = stream;
    }

}
