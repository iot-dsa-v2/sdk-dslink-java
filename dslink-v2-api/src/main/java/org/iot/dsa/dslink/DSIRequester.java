package org.iot.dsa.dslink;

import org.iot.dsa.dslink.requester.AbstractInvokeHandler;
import org.iot.dsa.dslink.requester.AbstractListHandler;
import org.iot.dsa.dslink.requester.AbstractSubscribeHandler;
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
     * @see AbstractInvokeHandler
     */
    public OutboundInvokeHandler invoke(String path, DSMap params, OutboundInvokeHandler handler);

    /**
     * Submits a list request.  Child names do not need to be encoded unless they contain
     * a forward slash.  If encoding names, be sure to decode in all responder methods here,
     * including this one, see DSPath.
     *
     * @param handler Callback mechanism.
     * @return The handler parameter.
     * @see AbstractListHandler
     * @see org.iot.dsa.node.DSPath#encodeName(String)
     * @see org.iot.dsa.node.DSPath#decodeName(String)
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
     * @param qos     DSA quality of service, 0-2 supported.
     * @param handler Callback mechanism.
     * @return The handler parameter.
     * @see AbstractSubscribeHandler
     */
    public OutboundSubscribeHandler subscribe(String path, DSIValue qos,
                                              OutboundSubscribeHandler handler);

}

