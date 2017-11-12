package org.iot.dsa.io;

/**
 * A buffer for storing bytes being pushed from an input stream.  Useful when bytes are coming in
 * faster than can be processed.
 *
 * @author Aaron Hansen
 */
public class DSByteBuffer {

    ///////////////////////////////////////////////////////////////////////////
    // Constants
    ///////////////////////////////////////////////////////////////////////////

    ///////////////////////////////////////////////////////////////////////////
    // Fields
    ///////////////////////////////////////////////////////////////////////////

    private byte[] buffer;
    private int length = 0;
    private int offset = 0;
    private boolean open = false;
    private long timeout = 60000;

    /////////////////////////////////////////////////////////////////
    // Methods - Constructors
    /////////////////////////////////////////////////////////////////

    public DSByteBuffer() {
        this(8192);
    }

    public DSByteBuffer(int initialCapacity) {
        buffer = new byte[initialCapacity];
    }

    /////////////////////////////////////////////////////////////////
    // Methods - In alphabetical order by method name.
    /////////////////////////////////////////////////////////////////

    /**
     * The number of bytes available for reading.
     */
    public int available() {
        return length;
    }

    /**
     * Important for notifying waiting threads.
     */
    public synchronized DSByteBuffer close() {
        if (!open) {
            return this;
        }
        open = false;
        notify();
        return this;
    }

    /**
     * Number of millis the read methods will block before throwing an IOException.
     *
     * @return Zero or less is indefinite.
     */
    public long getTimeout() {
        return timeout;
    }

    /**
     * Increases the childCount of the buffer to at least the given.
     */
    private void growBuffer(int minSize) {
        int size = buffer.length;
        while (size < minSize) {
            size *= 2;
        }
        byte[] tmp = new byte[size];
        System.arraycopy(buffer, offset, tmp, 0, length);
        buffer = tmp;
        offset = 0;
    }

    public boolean isOpen() {
        return open;
    }

    public int length() {
        return length;
    }

    public synchronized DSByteBuffer open() {
        if (open) {
            return this;
        }
        open = true;
        notify();
        return this;
    }

    /**
     * Add the byte to the buffer for reading.
     */
    public synchronized void put(byte b) {
        if (!open) {
            throw new IllegalStateException("Buffer closed");
        }
        int bufLen = buffer.length;
        int msgLen = 1;
        if ((msgLen + length + offset) >= bufLen) {
            if ((msgLen + length) > bufLen) {
                growBuffer(msgLen + length);
            } else { //offset must be > 0
                System.arraycopy(buffer, offset, buffer, 0, length);
                offset = 0;
            }
        }
        buffer[length + offset] = b;
        length++;
        notify();
    }

    /**
     * Add bytes to the buffer for reading.
     *
     * @param msg The data source.
     */
    public synchronized void put(byte[] msg) {
        put(msg, 0, msg.length);
    }

    /**
     * Add bytes to the buffer for reading.
     *
     * @param msg The data source.
     * @param off The start offset in the buffer to put data.
     * @param len The maximum number of bytes to read.
     */
    public synchronized void put(byte[] msg, int off, int len) {
        if (!open) {
            throw new IllegalStateException("Buffer closed");
        }
        int bufLen = buffer.length;
        if ((len + length + offset) >= bufLen) {
            if ((len + length) > bufLen) {  //the buffer is too small
                growBuffer(len + length);
            } else { //offset must be > 0, shift everything to index 0
                System.arraycopy(buffer, offset, buffer, 0, length);
                offset = 0;
            }
        }
        System.arraycopy(msg, off, buffer, length + offset, len);
        length += len;
        notify();
    }

    /**
     * Overwrite bytes in the internal buffer (which begins at index 0).
     *
     * @param dest The internal destination offset.
     * @param msg  The data source.
     * @param off  The start offset in the msg to put data.
     * @param len  The maximum number of bytes to read.
     */
    public synchronized void put(int dest, byte[] msg, int off, int len) {
        if (!open) {
            throw new IllegalStateException("Buffer closed");
        }
        if (offset > 0) {
            System.arraycopy(buffer, offset, buffer, 0, length);
            offset = 0;
        }
        int newLen = dest + len;
        if (newLen >= buffer.length) {
            growBuffer(newLen);
        }
        System.arraycopy(msg, off, buffer, dest, len);
        if (newLen > length) {
            length = newLen;
        }
        if ((dest + len) > length) {
            length += len;
        }
        notify();
    }

    /**
     * Returns the nextRun incoming byte, or -1 when end of stream has been reached.
     *
     * @throws DSIoException if there are any issues.
     */
    public synchronized int read() {
        while (open && (length == 0)) {
            try {
                if (timeout > 0) {
                    wait(timeout);
                } else {
                    wait();
                }
            } catch (InterruptedException ignore) {
            }
        }
        notify();
        if (!open) {
            return -1;
        } else if (length == 0) {
            throw new DSIoException("Read timeout");
        }
        int ret = buffer[offset];
        offset++;
        length--;
        return ret;
    }

    /**
     * Reads incoming bytes into the given buffer.
     *
     * @param buf The buffer into which data is read.
     * @param off The start offset in the buffer to put data.
     * @param len The maximum number of bytes to read.
     * @return The number of bytes read or -1 for end of stream.
     * @throws DSIoException if there are any issues.
     */
    public synchronized int read(byte[] buf, int off, int len) {
        while (open && (length == 0)) {
            try {
                if (timeout > 0) {
                    wait(timeout);
                } else {
                    wait();
                }
            } catch (InterruptedException ignore) {
            }
        }
        notify();
        if (!open) {
            return -1;
        } else if (length == 0) {
            throw new DSIoException("Read timeout");
        }
        len = Math.min(len, length);
        System.arraycopy(buffer, offset, buf, off, len);
        length -= len;
        if (length == 0) {
            offset = 0;
        } else {
            offset += len;
        }
        return len;
    }

    /**
     * Number of millis the read methods will block before throwing an DSIoException.
     *
     * @param timeout Zero or less for indefinite.
     * @return This
     */
    public DSByteBuffer setTimeout(long timeout) {
        this.timeout = timeout;
        return this;
    }

    /**
     * Returns a new array.
     */
    public byte[] toByteArray() {
        byte[] ret = new byte[length];
        read(ret, 0, length);
        return ret;
    }

}
