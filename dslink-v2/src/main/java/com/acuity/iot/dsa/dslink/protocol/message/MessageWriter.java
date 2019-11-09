package com.acuity.iot.dsa.dslink.protocol.message;

import org.iot.dsa.io.DSIWriter;

/**
 * V2 needs more than just a DSIWriter, so this provides the necessary abstraction.
 *
 * @author Aaron Hansen
 */
public interface MessageWriter {

    DSIWriter getWriter();

}
