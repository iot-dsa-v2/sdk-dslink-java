package org.iot.dsa.dslink;

import org.iot.dsa.dslink.responder.InboundInvokeRequest;
import org.iot.dsa.dslink.responder.InboundListRequest;
import org.iot.dsa.dslink.responder.InboundSetRequest;
import org.iot.dsa.dslink.responder.InboundSubscribeRequest;
import org.iot.dsa.dslink.responder.OutboundListResponse;
import org.iot.dsa.dslink.responder.SubscriptionCloseHandler;
import org.iot.dsa.node.DSMap;
import org.iot.dsa.node.action.ActionResult;
import org.iot.dsa.security.DSPermission;

/**
 * Session object used by requesters.
 *
 * @author Aaron Hansen
 */
public interface DSIRequester {

    /**
     * The implementation should quickly create an object for responding to the request, but do no
     * processing of it on the calling thread. Simply throw a descriptive exception to report an
     * error with the request.
     *
     * @param request The details of the request and the mechanism for providing updates.
     * @return The initial response and close notification mechanism, can be null if the if the
     * result type is void.
     */
    public void invoke(String path,
                       DSMap parameters,
                       DSPermission permission,
                       InvokeResponseHandler handler);

    /**
     * The implementation should quickly create an object for responding to the request, but do no
     * processing of it on the calling thread. Simply throw a descriptive exception to report an
     * error with the request.
     *
     * @param request The details of the request and the mechanism for providing updates.
     * @return The initial response and close mechanism.
     */
    public void list(InboundListRequest request);

    /**
     * The implementation should quickly create an object for responding to the request, but do no
     * processing of it on the calling thread. Simply throw a descriptive exception to report an
     * error with the request.
     *
     * @param request The details of the request and the mechanism for sending updates.
     * @return Who to notify when the subscription is closed.
     */
    public void subscribe(InboundSubscribeRequest request);


    /**
     * The implementation should do no processing of it on the calling thread. Simply throw a
     * descriptive exception to report an error with the request.
     *
     * @param request The details of the request.
     */
    public void set(InboundSetRequest request);

}
