package com.acuity.iot.dsa.dslink.protocol.message;

import com.acuity.iot.dsa.dslink.protocol.DSSession;

/**
 * @author Aaron Hansen
 */
public interface OutboundMessage {

    /**
     * True if the message is ready to be written.
     */
    public boolean canWrite(DSSession session);

    /**
     * Write the full request or response message object.
     */
    public void write(DSSession session, MessageWriter writer);

}
