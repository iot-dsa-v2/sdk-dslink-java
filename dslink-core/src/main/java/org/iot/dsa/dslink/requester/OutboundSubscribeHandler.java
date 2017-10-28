package org.iot.dsa.dslink.requester;

import org.iot.dsa.node.DSElement;
import org.iot.dsa.node.DSStatus;
import org.iot.dsa.time.DSDateTime;

/**
 * Callback mechanism passed to the subscribe method in the requester.
 *
 * @author Daniel Shapiro, Aaron Hansen
 */
public interface OutboundSubscribeHandler extends OutboundRequestHandler {

    /**
     * Called by the requester before returning from the subscribe method.
     *
     * @param path Who is being subscribed.
     * @param qos  Quality of service, 0-3.
     * @param stream Mechanism to close the request stream.
     */
    public void onInit(String path, int qos, OutboundStream stream);

    /**
     * Subscription update mechanism.
     *
     * @param dateTime Timestamp of the value.
     * @param value    The update value.
     * @param status   The status of the value, never null.
     */
    public void onUpdate(DSDateTime dateTime, DSElement value, DSStatus status);

}
