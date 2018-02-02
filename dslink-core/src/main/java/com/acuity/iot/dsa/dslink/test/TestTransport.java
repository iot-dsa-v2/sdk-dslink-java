package com.acuity.iot.dsa.dslink.test;

import com.acuity.iot.dsa.dslink.transport.PushBinaryTransport;

/**
 * Routes requests and responses back to self.
 *
 * @author Aaron Hansen
 */
public class TestTransport extends PushBinaryTransport {

    ///////////////////////////////////////////////////////////////////////////
    // Fields
    ///////////////////////////////////////////////////////////////////////////

    private int messageSize;

    /////////////////////////////////////////////////////////////////
    // Methods - In alphabetical order by method name.
    /////////////////////////////////////////////////////////////////

    @Override
    public int messageSize() {
        return messageSize;
    }

    @Override
    public void write(byte[] buf, int off, int len, boolean isLast) {
        messageSize += len;
        push(buf, off, len);
        if (isLast) {
            messageSize = 0;
        }
    }

}
