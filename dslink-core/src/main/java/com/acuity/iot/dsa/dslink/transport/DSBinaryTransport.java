package com.acuity.iot.dsa.dslink.transport;

import java.io.InputStream;
import java.nio.ByteBuffer;

/**
 * Transport that reads and writes binary data.
 *
 * @author Aaron Hansen
 */
public abstract class DSBinaryTransport extends DSTransport {

    public abstract InputStream getInput();

    public abstract void write(ByteBuffer buf, boolean isLast);

}
