package org.iot.dsa.dslink;

import org.iot.dsa.dslink.responder.InboundInvokeRequest;
import org.iot.dsa.dslink.responder.InboundListRequest;
import org.iot.dsa.dslink.responder.InboundSetRequest;
import org.iot.dsa.dslink.responder.InboundSubscribeRequest;
import org.iot.dsa.dslink.responder.OutboundListResponse;
import org.iot.dsa.dslink.responder.SubscriptionCloseHandler;
import org.iot.dsa.node.action.ActionResult;

/**
 * Interface for nodes in the node tree to manually handle requests.  The first implementation
 * encountered in the path of a request will be given the responsibility for processing the
 * request.
 *
 * For error handling, simply throw an exception from any of these methods to have the error
 * reported to the requester and the stream closed.  There are a few predefined exceptions to be
 * aware of listed below.  Non DSRequestExceptions will be reported as server errors.
 *
 * @author Aaron Hansen
 * @see DSInvalidPathException
 * @see DSPermissionException
 * @see DSRequestException
 */
public interface DSResponder {

    /**
     * The implementation should quickly create an object for responding to the request, but do no
     * processing of it on the calling thread. Simply throw a descriptive exception to report an
     * error with the request.
     *
     * @param request The details of the request and the mechanism for providing updates.
     * @return The initial response and close notification mechanism, can be null if the if the
     * result type is void.
     */
    public ActionResult onInvoke(InboundInvokeRequest request);

    /**
     * The implementation should quickly create an object for responding to the request, but do no
     * processing of it on the calling thread. Simply throw a descriptive exception to report an
     * error with the request.
     *
     * @param request The details of the request and the mechanism for providing updates.
     * @return The initial response and close mechanism.
     */
    public OutboundListResponse onList(InboundListRequest request);

    /**
     * The implementation should quickly create an object for responding to the request, but do no
     * processing of it on the calling thread. Simply throw a descriptive exception to report an
     * error with the request.
     *
     * @param request The details of the request and the mechanism for sending updates.
     * @return Who to notify when the subscription is closed.
     */
    public SubscriptionCloseHandler onSubscribe(InboundSubscribeRequest request);


    /**
     * The implementation should quickly create an object for responding to the request, but do no
     * processing of it on the calling thread. Simply throw a descriptive exception to report an
     * error with the request.
     *
     * @param request The details of the request.
     */
    public void onSet(InboundSetRequest request);

}
