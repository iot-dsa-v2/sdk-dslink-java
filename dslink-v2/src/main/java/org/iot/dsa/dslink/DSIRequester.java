package org.iot.dsa.dslink;

import org.iot.dsa.dslink.requester.OutboundInvokeHandler;
import org.iot.dsa.dslink.requester.OutboundListHandler;
import org.iot.dsa.dslink.requester.OutboundRequestHandler;
import org.iot.dsa.dslink.requester.OutboundSubscribeHandler;
import org.iot.dsa.dslink.requester.SimpleRequestHandler;
import org.iot.dsa.node.DSIValue;
import org.iot.dsa.node.DSMap;

/**
 * Interface for submitting outbound requests.  Accessible via the connection object.
 *
 * @author Daniel Shapiro, Aaron Hansen
 */
public interface DSIRequester {

    /**
     * Submits an invoke request.
     *
     * @param handler Callback mechanism.
     * @return The handler parameter.
     * @see org.iot.dsa.dslink.requester.AbstractInvokeHandler
     */
    public OutboundInvokeHandler invoke(String path, DSMap params, OutboundInvokeHandler handler);

    /**
     * Submits a list request.
     *
     * @param handler Callback mechanism.
     * @return The handler parameter.
     * @see org.iot.dsa.dslink.requester.AbstractListHandler
     */
    public OutboundListHandler list(String path, OutboundListHandler handler);

    /**
     * Submits request to remove an attribute.
     *
     * @param handler Callback mechanism.
     * @return The handler parameter.
     * @see SimpleRequestHandler
     */
    public OutboundRequestHandler remove(String path, OutboundRequestHandler handler);

    /**
     * Submits a set request.
     *
     * @param handler Callback mechanism.
     * @return The handler parameter.
     * @see SimpleRequestHandler
     */
    public OutboundRequestHandler set(String path, DSIValue value, OutboundRequestHandler handler);

    /**
     * Submits a subscribe request.
     *
     * @param handler Callback mechanism.
     * @return The handler parameter.
     * @see org.iot.dsa.dslink.requester.AbstractSubscribeHandler
     */
    public OutboundSubscribeHandler subscribe(String path, int qos,
                                              OutboundSubscribeHandler handler);

}

