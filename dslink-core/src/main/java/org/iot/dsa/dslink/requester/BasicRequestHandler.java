package org.iot.dsa.dslink.requester;

import org.iot.dsa.node.DSElement;

/**
 * Empty callback implementations.
 *
 * @author Daniel Shapiro, Aaron Hansen
 */
public class BasicRequestHandler implements OutboundRequestHandler {

    /**
     * An instance that can be use for some requests where the callbacks don't really matter.
     */
    public static final BasicRequestHandler DEFAULT = new BasicRequestHandler();

    /**
     * Does nothing by default.
     *
     * <p>
     *
     * {@inheritDoc}
     */
    @Override
    public void onClose() {
    }

    /**
     * Does nothing by default.
     *
     * <p>
     *
     * {@inheritDoc}
     */
    @Override
    public void onError(DSElement details) {
    }

}
