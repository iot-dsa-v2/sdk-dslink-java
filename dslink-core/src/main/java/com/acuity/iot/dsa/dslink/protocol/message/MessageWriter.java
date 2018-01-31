package com.acuity.iot.dsa.dslink.protocol.message;

import org.iot.dsa.io.DSIWriter;

/**
 * Used to read a DSA 2.n message (header and body).  Call init(InputStream) to start a new message,
 * can be reused for multiple messages. Not thread safe, the intent is messages will be constructed
 * and read serially.
 *
 * @author Aaron Hansen
 */
public interface MessageWriter {

    public DSIWriter getWriter();

}
