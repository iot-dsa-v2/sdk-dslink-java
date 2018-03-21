package com.acuity.iot.dsa.dslink.transport;

import java.io.InputStream;

/**
 * Transport that reads and writes binary data.
 *
 * @author Aaron Hansen
 */
public abstract class DSBinaryTransport extends DSTransport {

    /**
     * For reading raw bytes from the underlying transport.  The DSK does not care about frame
     * oriented transports.
     */
    public abstract InputStream getInput();

    /**
     * Writes bytes to the underlying transport.
     *
     * @param buf    The buffer containing the bytes to write.
     * @param off    The index in the buffer of the first byte to write.
     * @param len    The number of bytes to write.
     * @param isLast Indicator of end of frame (message) for frame oriented transports such as
     *               websockets.
     */
    public abstract void write(byte[] buf, int off, int len, boolean isLast);

}
