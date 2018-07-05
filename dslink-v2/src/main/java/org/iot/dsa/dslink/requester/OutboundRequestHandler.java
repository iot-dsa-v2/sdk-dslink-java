package org.iot.dsa.dslink.requester;

/**
 * Callbacks common to all outbound requests.
 *
 * @author Daniel Shapiro, Aaron Hansen
 */
public interface OutboundRequestHandler {

    /**
     * Callback for when the request stream is closed, no matter how or by who.  Will be called if
     * there is an error as well.
     */
    public void onClose();

    /**
     * Callback for when an error is received.  onClose will also be called after this.
     * Does nothing by default.
     */
    public void onError(ErrorType type, String msg);

}
