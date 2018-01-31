package com.acuity.iot.dsa.dslink.protocol.message;

/**
 * @author Aaron Hansen
 */
public interface OutboundMessage {

    /**
     * Write the full request or response message object.
     */
    public void write(MessageWriter writer);

}
