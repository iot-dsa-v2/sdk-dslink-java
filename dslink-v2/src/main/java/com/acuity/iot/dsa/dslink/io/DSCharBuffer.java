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
    private int length = 0;
    private int offset = 0;

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

    public void clear() {
        length = 0;
        offset = 0;
    }

    /**
     * Add the char to the buffer for reading.
     */
    public void put(char b) {
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
    }

    /**
     * Add chars to the buffer for reading.
     *
     * @param msg The data source.
     */
    public void put(char[] msg) {
        put(msg, 0, msg.length);
    }

    /**
     * Add chars to the buffer for reading.
     *
     * @param msg The data source.
     * @param off The start offset in the buffer to put data.
     * @param len The maximum number of chars to read.
     */
    public void put(char[] msg, int off, int len) {
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
    }

    /**
     * Add chars to the buffer for reading.
     */
    public void put(String msg) {
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
    }

    /**
     * Returns the next incoming char, or -1 when there is no data.
     */
    public int read() {
        if (length == 0) {
            return -1;
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
     * @return The number of chars read.
     * @throws DSIoException if there are any issues.
     */
    public int read(char[] buf, int off, int len) {
        if (length == 0) {
            return 0;
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
     * Increases the childCount of the buffer to at least the given.
     */
    private void growBuffer(int minSize) {
        int size = buffer.length;
        while (size < minSize) {
            if (size < 1000000) {
                size *= 2;
            } else {
                size += 1000000;
            }
        }
        char[] tmp = new char[size];
        System.arraycopy(buffer, offset, tmp, 0, length);
        buffer = tmp;
        offset = 0;
    }

}
