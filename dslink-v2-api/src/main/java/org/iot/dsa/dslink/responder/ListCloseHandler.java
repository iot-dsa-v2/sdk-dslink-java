package org.iot.dsa.dslink.responder;

/**
 * The responder returns this from the list request so that it can be notified when the list
 * stream is closed.
 *
 * @author Aaron Hansen
 * @see org.iot.dsa.dslink.DSIResponder#onList(InboundListRequest)
 */
public interface ListCloseHandler {

    /**
     * Will be called no matter how the stream is closed.
     */
    void onClose();

}
