package com.acuity.iot.dsa.dslink.io;

/**
 * A buffer for storing chars being pushed from a reader.  Useful when chars are coming
 * in faster than can be processed.
 *
 * @author Aaron Hansen
 */
public class DSCharBuffer {

    ///////////////////////////////////////////////////////////////////////////
    // Fields
    ///////////////////////////////////////////////////////////////////////////

    private char[] buffer;
    private RuntimeException closeException;
    private int length = 0;
    private int offset = 0;
    private boolean open = false;
    private long timeout = 60000;

    /////////////////////////////////////////////////////////////////
    // Methods - Constructors
    /////////////////////////////////////////////////////////////////

    public DSCharBuffer() {
        this(8192);
    }

    public DSCharBuffer(int initialCapacity) {
        buffer = new char[initialCapacity];
    }

    /////////////////////////////////////////////////////////////////
    // Methods - In alphabetical order by method name.
    /////////////////////////////////////////////////////////////////

    /**
     * The number of chars available for reading.
     */
    public int available() {
        return length;
    }

    public synchronized void clear() {
        length = 0;
        offset = 0;
        notifyAll();
    }

    /**
     * Important for notifying waiting threads.
     */
    public synchronized void close() {
        if (!open) {
            return;
        }
        open = false;
        notifyAll();
    }

    /**
     * Important for notifying waiting threads.
     */
    public synchronized void close(RuntimeException toThrow) {
        if (!open) {
            return;
        }
        closeException = toThrow;
        open = false;
        notifyAll();
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
        char[] tmp = new char[size];
        System.arraycopy(buffer, offset, tmp, 0, length);
        buffer = tmp;
        offset = 0;
    }

    public boolean isOpen() {
        return open;
    }

    public synchronized void open() {
        if (open) {
            return;
        }
        length = 0;
        offset = 0;
        open = true;
        closeException = null;
        notifyAll();
    }

    /**
     * Add the char to the buffer for reading.
     */
    public synchronized void put(char b) {
        if (!open) {
            throw new DSIoException("Closed");
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
        notifyAll();
    }

    /**
     * Add chars to the buffer for reading.
     *
     * @param msg The data source.
     */
    public synchronized void put(char[] msg) {
        put(msg, 0, msg.length);
    }

    /**
     * Add chars to the buffer for reading.
     *
     * @param msg The data source.
     * @param off The start offset in the buffer to put data.
     * @param len The maximum number of chars to read.
     */
    public synchronized void put(char[] msg, int off, int len) {
        if (!open) {
            throw new DSIoException("Closed");
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
        System.arraycopy(msg, off, buffer, offset, len);
        length += len;
        notifyAll();
    }

    /**
     * Add chars to the buffer for reading.
     */
    public synchronized void put(String msg) {
        if (!open) {
            throw new DSIoException("Closed");
        }
        int len = msg.length();
        int bufLen = buffer.length;
        if ((len + length + offset) >= bufLen) {
            if ((len + length) > bufLen) {  //the buffer is too small
                growBuffer(len + length);
            } else { //offset must be > 0, shift everything to index 0
                System.arraycopy(buffer, offset, buffer, 0, length);
                offset = 0;
            }
        }
        msg.getChars(0, len, buffer, offset + length);
        length += len;
        notifyAll();
    }

    /**
     * Returns the next incoming char, or -1 when end of stream has been reached.
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
        notifyAll();
        if (!open) {
            if (length == 0) {
                if (closeException != null) {
                    throw closeException;
                }
                return -1;
            }
        } else if (length == 0) {
            throw new DSIoException("Read timeout");
        }
        int ret = buffer[offset];
        offset++;
        length--;
        return ret;
    }

    /**
     * Reads incoming chars into the given buffer.
     *
     * @param buf The buffer into which data is read.
     * @param off The start offset in the buffer to put data.
     * @param len The maximum number of chars to read.
     * @return The number of chars read or -1 for end of stream.
     * @throws DSIoException if there are any issues.
     */
    public synchronized int read(char[] buf, int off, int len) {
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
        notifyAll();
        if (!open) {
            if (length == 0) {
                if (closeException != null) {
                    throw closeException;
                }
                return -1;
            }
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
    public DSCharBuffer setTimeout(long timeout) {
        this.timeout = timeout;
        return this;
    }

}
