package com.acuity.iot.dsa.dslink;

import com.acuity.iot.dsa.dslink.protocol.message.OutboundMessage;
import org.iot.dsa.dslink.DSRequesterInterface;

/**
 * The session requesters will use to submit requests.
 *
 * @author Aaron Hansen
 */
public interface DSRequesterSession extends DSRequesterInterface {

    /**
     * Append the message to the outgoing queue.
     */
    public void sendRequest(OutboundMessage res);

}
