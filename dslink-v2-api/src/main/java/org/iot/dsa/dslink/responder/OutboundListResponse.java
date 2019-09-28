package org.iot.dsa.dslink.responder;

import org.iot.dsa.dslink.DSIResponder;

/**
 * The responder returns this from the list request so that it can be notified when the list
 * stream is closed by the requester.
 *
 * @author Aaron Hansen
 * @see DSIResponder#onList(InboundListRequest)
 */
public interface OutboundListResponse {

    /**
     * Will be called no matter how the stream is closed.
     */
    public void onClose();

}
