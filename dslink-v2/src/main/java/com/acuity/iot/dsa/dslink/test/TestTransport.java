package com.acuity.iot.dsa.dslink.test;

import com.acuity.iot.dsa.dslink.io.DSByteBuffer;
import com.acuity.iot.dsa.dslink.io.DSCharBuffer;
import com.acuity.iot.dsa.dslink.io.DSIoException;
import com.acuity.iot.dsa.dslink.transport.DSTransport;

/**
 * Routes requests and responses back to self.
 *
 * @author Aaron Hansen
 */
public class TestTransport extends DSTransport {

    private DSByteBuffer binary;
    private DSCharBuffer text;

    @Override
    public void open() {
        setOpen();
    }

    @Override
    protected int available() {
        if (isText()) {
            if (text == null) {
                return 0;
            }
            return text.available();
        }
        if (binary == null) {
            return 0;
        }
        return binary.available();
    }

    @Override
    protected int doRead(byte[] buf, int off, int len) {
        synchronized (this) {
            if (testOpen() && available() == 0) {
                try {
                    wait(getReadTimeout());
                } catch (Exception x) {
                    debug("", x);
                }
            }
            if (available() > 0) {
                return binary.sendTo(buf, off, len);
            }
            if (!testOpen()) {
                return -1;
            }
            throw new DSIoException("Read timeout");
        }
    }

    @Override
    protected int doRead(char[] buf, int off, int len) {
        synchronized (this) {
            if (testOpen() && available() == 0) {
                try {
                    wait(getReadTimeout());
                } catch (Exception x) {
                    debug("", x);
                }
            }
            if (available() > 0) {
                return text.read(buf, off, len);
            }
            if (!testOpen()) {
                return -1;
            }
            throw new DSIoException("Read timeout");
        }
    }

    @Override
    protected void doWrite(String msgPart, boolean isLast) {
        if (text == null) {
            text = new DSCharBuffer();
        }
        synchronized (this) {
            text.put(msgPart);
            notify();
        }
    }

    @Override
    protected void doWrite(byte[] buf, int off, int len, boolean isLast) {
        if (binary == null) {
            binary = new DSByteBuffer();
        }
        synchronized (this) {
            binary.put(buf, off, len);
            notify();
        }
    }


}
