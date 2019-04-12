package com.acuity.iot.dsa.dslink.protocol.message;

import org.iot.dsa.io.DSIReader;

/**
 * V2 needs more than just a DSIReader, so this provides the necessary abstraction.
 *
 * @author Aaron Hansen
 */
public interface MessageReader {

    public DSIReader getReader();

}
