package org.iot.dsa.dslink.requester;

import org.iot.dsa.node.DSElement;

/**
 * Data and callbacks common to all outbound requests.
 *
 * @author Daniel Shapiro, Aaron Hansen
 */
public class AbstractRequestHandler implements OutboundRequestHandler {

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
