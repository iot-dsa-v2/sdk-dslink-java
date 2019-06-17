package com.acuity.iot.dsa.dslink.transport;

import com.acuity.iot.dsa.dslink.io.DSByteBuffer;
import com.acuity.iot.dsa.dslink.io.DSCharBuffer;
import com.acuity.iot.dsa.dslink.io.DSIoException;

/**
 * Routes requests and responses back to self.
 *
 * @author Aaron Hansen
 */
public class PipedTransport extends DSTransport {

    private DSByteBuffer binary;
    private int messages = 0;
    private DSCharBuffer text;

    @Override
    public void endRecvMessage() {
        super.endRecvMessage();
        if (--messages > 0) {
            getConnection().getSession().recvMessage(true);
        }
    }

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
                int i = text.read(buf, off, len);
                return i;
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
        if (!msgPart.isEmpty()) {
            synchronized (this) {
                text.put(msgPart);
                notifyAll();
            }
        }
        if (isLast) {
            messages++;
        }
        getConnection().getSession().recvMessage(true);
    }

    @Override
    protected void doWrite(byte[] buf, int off, int len, boolean isLast) {
        if (binary == null) {
            binary = new DSByteBuffer();
        }
        if (len > 0) {
            synchronized (this) {
                binary.put(buf, off, len);
                notifyAll();
            }
        }
        if (isLast) {
            messages++;
        }
        getConnection().getSession().recvMessage(true);
    }


}
