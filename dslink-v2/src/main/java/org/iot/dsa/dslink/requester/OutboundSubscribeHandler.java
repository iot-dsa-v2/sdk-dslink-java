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
     * Subscription update mechanism.
     *
     * @param dateTime Timestamp of the value.
     * @param value    The update value.
     * @param status   The status of the value, never null.
     */
    public void onUpdate(DSDateTime dateTime, DSElement value, DSStatus status);

}
