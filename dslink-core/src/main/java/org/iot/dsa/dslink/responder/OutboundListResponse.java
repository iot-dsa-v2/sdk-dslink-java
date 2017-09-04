package org.iot.dsa.dslink.responder;

/**
 * The responder is responsible for returning this upon notification of a list request.  The link
 * will encode the target first, then then children.  If there are many children, the link may break
 * it into several messages.
 *
 * @author Aaron Hansen
 * @see org.iot.dsa.dslink.DSResponder#onList(InboundListRequest)
 */
public interface OutboundListResponse {

    /**
     * The object that represents the path of the request.
     */
    public ApiObject getTarget();

    /**
     * Will be called no matter how the stream is closed.
     */
    public void onClose();

}
