package com.acuity.iot.dsa.dslink.protocol.message;

import com.acuity.iot.dsa.dslink.protocol.DSSession;

/**
 * @author Aaron Hansen
 */
public interface OutboundMessage {

    /**
     * True if the message is ready to be written. A can be queued to write something, but may
     * not be able to such as waiting for an ack.
     */
    public boolean canWrite(DSSession session);

    /**
     * Write the full message and return whether or not the message requires an ack.
     */
    public boolean write(DSSession session, MessageWriter writer);

}
