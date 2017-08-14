package com.acuity.iot.dsa.dslink;

import com.acuity.iot.dsa.dslink.protocol.message.OutboundMessage;
import org.iot.dsa.dslink.DSLinkSession;

/**
 * The session responders will use to submit responses.
 *
 * @author Aaron Hansen
 */
public interface DSResponderSession extends DSLinkSession {

    /**
     * Append the message to the outgoing queue.
     */
    public void sendResponse(OutboundMessage res);

}
