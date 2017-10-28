package org.iot.dsa.dslink.requester;

import org.iot.dsa.node.DSElement;

/**
 * Data and callbacks common to all outbound requests.
 *
 * @author Daniel Shapiro, Aaron Hansen
 */
public interface OutboundRequestHandler {

    /**
     * Callback for when the request stream is closed, no matter how or by who.  Will be called
     * if there is an error as well.
     */
    public void onClose();

    /**
     * Callback for when an error is received.  If the stream is also closed, onClose will also be
     * closed.  Does nothing by default.
     */
    public void onError(DSElement details);

}
