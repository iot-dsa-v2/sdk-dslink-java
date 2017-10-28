package org.iot.dsa.dslink.requester;

import org.iot.dsa.node.DSElement;
import org.iot.dsa.node.DSStatus;
import org.iot.dsa.time.DSDateTime;

/**
 * Parameter to the subscribe method on DSIRequester.  Provides callbacks for various state
 * changes.
 *
 * @author Daniel Shapiro, Aaron Hansen
 */
public interface OutboundSubscribeHandler extends OutboundRequestHandler {

    /**
     * Called by the requester before returning from the subscribe method.
     *
     * @param path   Who is being subscribed.
     * @param qos    Quality of service: 0-3
     * @param stub Mechanism to close the request stream.
     */
    public void onInit(String path, int qos, OutboundRequestStub stub);

    /**
     * Subscription update mechanism.
     *
     * @param dateTime Timestamp of the value.
     * @param value    The update value.
     * @param status   The status of the value.
     */
    public void onUpdate(DSDateTime dateTime, DSElement value, DSStatus status);

}
