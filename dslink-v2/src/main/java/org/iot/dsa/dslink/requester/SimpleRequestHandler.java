package org.iot.dsa.dslink.requester;

import org.iot.dsa.node.DSIValue;

/**
 * Empty callback implementations.
 *
 * @author Daniel Shapiro, Aaron Hansen
 */
public class SimpleRequestHandler implements OutboundRequestHandler {

    /**
     * An instance that can be used for those requests where the callbacks don't really matter.
     */
    public static final SimpleRequestHandler DEFAULT = new SimpleRequestHandler();

    /**
     * Does nothing by default.
     * <p>
     * {@inheritDoc}
     */
    @Override
    public void onClose() {
    }

    /**
     * Does nothing by default.
     * <p>
     * {@inheritDoc}
     */
    @Override
    public void onError(ErrorType type, String msg) {
    }

    /**
     * Does nothing by default.
     * <p>
     * {@inheritDoc}
     */
    @Override
    public void onInit(String path, DSIValue params, OutboundStream stream) {
    }

}
