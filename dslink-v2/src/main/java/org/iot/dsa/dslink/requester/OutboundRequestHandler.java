package org.iot.dsa.dslink.requester;

import org.iot.dsa.node.DSIValue;

/**
 * Callbacks common to all outbound requests.
 *
 * @author Daniel Shapiro, Aaron Hansen
 */
public interface OutboundRequestHandler {

    /**
     * Parameters are specific to the request type and may be null.
     */
    public DSIValue getParams();

    /**
     * Path of the request.
     */
    public String getPath();

    /**
     * The mechanism to close the request stream.
     */
    public OutboundStream getStream();

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

    /**
     * Called by the requester before submitting the actual request to the responder.
     *
     * @param path   Path being of the request.
     * @param params Any additional parameters supplied to the operation.
     * @param stream Mechanism to close the request stream.
     */
    public void onInit(String path, DSIValue params, OutboundStream stream);

}
