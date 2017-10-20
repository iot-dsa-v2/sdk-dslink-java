package org.iot.dsa.dslink;

import org.iot.dsa.dslink.requester.OutboundInvokeRequest;
import org.iot.dsa.dslink.requester.OutboundInvokeStub;
import org.iot.dsa.dslink.requester.OutboundListRequest;
import org.iot.dsa.dslink.requester.OutboundListStub;
import org.iot.dsa.dslink.requester.OutboundRemoveRequest;
import org.iot.dsa.dslink.requester.OutboundSetRequest;
import org.iot.dsa.dslink.requester.OutboundSubscribeRequest;
import org.iot.dsa.dslink.requester.OutboundSubscribeStub;

/**
 * Interface for submitting outgoing requests.  Accessible via the connection object.
 *
 * @author Daniel Shapiro, Aaron Hansen
 */
public interface DSIRequester {

    /**
     * Submits an invoke request.
     *
     * @param request The details of the request and the mechanism for receiving updates.
     * @return The mechanism to close the stream.
     */
    public OutboundInvokeStub invoke(OutboundInvokeRequest request);

    /**
     * Submits a list request.
     *
     * @param request The details of the request and the mechanism for receiving updates.
     * @return The mechanism to close the stream.
     */
    public OutboundListStub list(OutboundListRequest request);

    /**
     * Submits a remove request.
     *
     * @param request The details of the request.  onError will be called for any problems, then
     *                onClose will be called to indicate the request is complete, whether or not
     *                there was an error.
     */
    public void remove(OutboundRemoveRequest request);

    /**
     * Submits a subscribe request.
     *
     * @param request The details of the request.  onError will be called for any problems, then
     *                onClose will be called to indicate the subscription is closed, whether or not
     *                there was an error.
     * @return The mechanism to close the subscription.
     */
    public OutboundSubscribeStub subscribe(OutboundSubscribeRequest request);


    /**
     * Submits a set request.
     *
     * @param request The details of the request.  onError will be called for any problems, then
     *                onClose will be called to indicate the request is complete, whether or not
     *                there was an error.
     */
    public void set(OutboundSetRequest request);

}
