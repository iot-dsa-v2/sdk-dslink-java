package com.acuity.iot.dsa.dslink.test;

import com.acuity.iot.dsa.dslink.transport.BufferedBinaryTransport;

/**
 * Routes requests and responses back to self.
 *
 * @author Aaron Hansen
 */
public class TestTransport extends BufferedBinaryTransport {

    @Override
    protected void doWrite(byte[] buf, int off, int len, boolean isLast) {
        receive(buf, off, len);
    }

}
