package org.iot.dsa.dslink;

import org.iot.dsa.dslink.requester.OutboundInvokeHandler;
import org.iot.dsa.dslink.requester.OutboundListHandler;
import org.iot.dsa.dslink.requester.OutboundRequestHandler;
import org.iot.dsa.dslink.requester.OutboundSubscribeHandler;
import org.iot.dsa.node.DSElement;
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
     */
    public OutboundInvokeHandler invoke(String path, DSMap params, OutboundInvokeHandler handler);

    /**
     * Submits a list request.
     *
     * @param handler Callback mechanism.
     * @return The handler parameter.
     */
    public OutboundListHandler list(String path, OutboundListHandler handler);

    /**
     * Submits a remove request.
     *
     * @param handler Callback mechanism.
     * @return The handler parameter.
     */
    public OutboundRequestHandler remove(String path, OutboundRequestHandler handler);

    /**
     * Submits a subscribe request.
     *
     * @param handler Callback mechanism.
     * @return The handler parameter.
     */
    public OutboundSubscribeHandler subscribe(String path, int qos,
                                              OutboundSubscribeHandler handler);

    /**
     * Submits a set request.
     *
     * @param handler Callback mechanism.
     * @return The handler parameter.
     */
    public OutboundRequestHandler set(String path, DSElement value, OutboundRequestHandler handler);

}
