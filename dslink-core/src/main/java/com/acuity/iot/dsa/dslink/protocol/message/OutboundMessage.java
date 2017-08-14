package com.acuity.iot.dsa.dslink.protocol.message;

import org.iot.dsa.io.DSWriter;

/**
 * @author Aaron Hansen
 */
public interface OutboundMessage {

    /**
     * Write the full request or response message object.
     */
    public void write(DSWriter writer);

}
