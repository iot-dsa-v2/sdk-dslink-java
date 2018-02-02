package com.acuity.iot.dsa.dslink.io;

import com.acuity.iot.dsa.dslink.transport.DSBinaryTransport;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import org.iot.dsa.util.DSException;

/**
 * A buffer for storing bytes being pushed from an input stream.  Useful when bytes are
 * coming in faster than can be processed.  This is not synchronized.
 *
 * @author Aaron Hansen
 */
public class DSByteBuffer {

    ///////////////////////////////////////////////////////////////////////////
    // Fields
    ///////////////////////////////////////////////////////////////////////////

    private byte[] buffer;
    private int length = 0;
    private int offset = 0;

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
     * Number of bytes available for reading.
     */
    public int available() {
        return length;
    }

    public void clear() {
        length = 0;
        offset = 0;
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

    /**
     * Number of bytes available for reading.
     */
    public int length() {
        return length;
    }

    /**
     * Overwrites bytes in the internal buffer, does not change the current length or position.
     */
    public void overwrite(int dest, byte b1, byte b2) {
        buffer[dest] = b1;
        buffer[++dest] = b2;
    }

    /**
     * Overwrites bytes in the internal buffer, does not change the current length or position.
     */
    public void overwrite(int dest, byte b1, byte b2, byte b3, byte b4) {
        buffer[dest] = b1;
        buffer[++dest] = b2;
        buffer[++dest] = b3;
        buffer[++dest] = b4;
    }

    /**
     * Overwrites the primitive in the internal buffer.  Does not change the buffer length or
     * position.
     *
     * @param dest      The offset in the internal buffer to write the bytes.
     * @param v         The value to encode.
     * @param bigEndian Whether to encode in big or little endian byte ordering.
     */
    public void overwriteInt(int dest, int v, boolean bigEndian) {
        if (bigEndian) {
            overwrite(dest, (byte) ((v >>> 24) & 0xFF),
                      (byte) ((v >>> 16) & 0xFF),
                      (byte) ((v >>> 8) & 0xFF),
                      (byte) ((v >>> 0) & 0xFF));
        } else {
            overwrite(dest, (byte) ((v >>> 0) & 0xFF),
                      (byte) ((v >>> 8) & 0xFF),
                      (byte) ((v >>> 16) & 0xFF),
                      (byte) ((v >>> 24) & 0xFF));
        }
    }

    /**
     * Overwrites the primitive in the internal buffer.  Does not change the buffer length or*
     * position.
     *
     * @param dest      The offset in the internal buffer to write the bytes.
     * @param v         The value to encode.
     * @param bigEndian Whether to encode in big or little endian byte ordering.
     */
    public void overwriteShort(int dest, short v, boolean bigEndian) {
        if (bigEndian) {
            overwrite(dest, (byte) ((v >>> 8) & 0xFF), (byte) ((v >>> 0) & 0xFF));
        } else {
            overwrite(dest, (byte) ((v >>> 0) & 0xFF), (byte) ((v >>> 8) & 0xFF));
        }
    }

    /**
     * Gets the bytes from the given buffer, which will be flipped, then cleared.
     */
    public void put(ByteBuffer buf) {
        int len = buf.position();
        int bufLen = buffer.length;
        if ((len + length + offset) >= bufLen) {
            if ((len + length) > bufLen) {  //the buffer is too small
                growBuffer(len + length);
            } else { //offset must be > 0, shift everything to index 0
                System.arraycopy(buffer, offset, buffer, 0, length);
                offset = 0;
            }
        }
        buf.flip();
        buf.get(buffer, length + offset, len);
        buf.clear();
        length += len;
    }

    /**
     * Add the byte to the buffer for reading.
     */
    public void put(byte b) {
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
     * Add the byte to the buffer for reading.
     */
    public void put(byte b1, byte b2) {
        int bufLen = buffer.length;
        int msgLen = 2;
        if ((msgLen + length + offset) >= bufLen) {
            if ((msgLen + length) > bufLen) {
                growBuffer(msgLen + length);
            } else { //offset must be > 0
                System.arraycopy(buffer, offset, buffer, 0, length);
                offset = 0;
            }
        }
        int idx = length + offset;
        buffer[idx] = b1;
        buffer[++idx] = b2;
        length += msgLen;
    }

    /**
     * Add the byte to the buffer for reading.
     */
    public void put(byte b1, byte b2, byte b3, byte b4) {
        int bufLen = buffer.length;
        int msgLen = 4;
        if ((msgLen + length + offset) >= bufLen) {
            if ((msgLen + length) > bufLen) {
                growBuffer(msgLen + length);
            } else { //offset must be > 0
                System.arraycopy(buffer, offset, buffer, 0, length);
                offset = 0;
            }
        }
        int idx = length + offset;
        buffer[idx] = b1;
        buffer[++idx] = b2;
        buffer[++idx] = b3;
        buffer[++idx] = b4;
        length += msgLen;
    }

    /**
     * Add the byte to the buffer for reading.
     */
    public void put(byte b1, byte b2, byte b3, byte b4, byte b5, byte b6, byte b7, byte b8) {
        int bufLen = buffer.length;
        int msgLen = 8;
        if ((msgLen + length + offset) >= bufLen) {
            if ((msgLen + length) > bufLen) {
                growBuffer(msgLen + length);
            } else { //offset must be > 0
                System.arraycopy(buffer, offset, buffer, 0, length);
                offset = 0;
            }
        }
        int idx = length + offset;
        buffer[idx] = b1;
        buffer[++idx] = b2;
        buffer[++idx] = b3;
        buffer[++idx] = b4;
        buffer[++idx] = b5;
        buffer[++idx] = b6;
        buffer[++idx] = b7;
        buffer[++idx] = b8;
        length += msgLen;
    }

    /**
     * Add bytes to the buffer for reading.
     *
     * @param msg The data source.
     */
    public void put(byte[] msg) {
        put(msg, 0, msg.length);
    }

    /**
     * Add bytes to the buffer for reading.
     *
     * @param msg The data source.
     * @param off The start offset in the buffer to put data.
     * @param len The maximum number of bytes to read.
     */
    public void put(byte[] msg, int off, int len) {
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
    }

    /**
     * Overwrite bytes in the internal buffer (which begins at index 0).
     *
     * @param dest The internal destination offset.
     * @param msg  The data source.
     * @param off  The start offset in the msg to put data.
     * @param len  The maximum number of bytes to put.
     */
    public void put(int dest, byte[] msg, int off, int len) {
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
    }

    /**
     * Encodes the primitive into buffer using big endian encoding.
     */
    public void putDouble(double v) {
        putDouble(v, true);
    }

    /**
     * Encodes the primitive into the buffer.
     *
     * @param v         The value to encode.
     * @param bigEndian Whether to encode in big or little endian byte ordering.
     */
    public void putDouble(double v, boolean bigEndian) {
        putLong(Double.doubleToRawLongBits(v), bigEndian);
    }

    /**
     * Encodes the primitive into buffer using big endian encoding.
     */
    public void putFloat(float v) {
        putFloat(v, true);
    }

    /**
     * Encodes the primitive into the buffer.
     *
     * @param v         The value to encode.
     * @param bigEndian Whether to encode in big or little endian byte ordering.
     */
    public void putFloat(float v, boolean bigEndian) {
        putInt(Float.floatToIntBits(v), bigEndian);
    }

    /**
     * Encodes the primitive into buffer using big endian encoding.
     */
    public void putInt(int v) {
        putInt(v, true);
    }

    /**
     * Encodes the primitive into buffer.
     *
     * @param v         The value to encode.
     * @param bigEndian Whether to encode in big or little endian byte ordering.
     */
    public void putInt(int v, boolean bigEndian) {
        if (bigEndian) {
            put((byte) ((v >>> 24) & 0xFF),
                (byte) ((v >>> 16) & 0xFF),
                (byte) ((v >>> 8) & 0xFF),
                (byte) ((v >>> 0) & 0xFF));
        } else {
            put((byte) ((v >>> 0) & 0xFF),
                (byte) ((v >>> 8) & 0xFF),
                (byte) ((v >>> 16) & 0xFF),
                (byte) ((v >>> 24) & 0xFF));
        }
    }

    /**
     * Encodes the primitive into the buffer using big endian encoding.
     */
    public void putLong(long v) {
        putLong(v, true);
    }

    /**
     * Encodes the primitive into the buffer.
     *
     * @param v         The value to encode.
     * @param bigEndian Whether to encode in big or little endian byte ordering.
     */
    public void putLong(long v, boolean bigEndian) {
        if (bigEndian) {
            put((byte) (v >>> 56),
                (byte) (v >>> 48),
                (byte) (v >>> 40),
                (byte) (v >>> 32),
                (byte) (v >>> 24),
                (byte) (v >>> 16),
                (byte) (v >>> 8),
                (byte) (v >>> 0));
        } else {
            put((byte) (v >>> 0),
                (byte) (v >>> 8),
                (byte) (v >>> 16),
                (byte) (v >>> 24),
                (byte) (v >>> 32),
                (byte) (v >>> 40),
                (byte) (v >>> 48),
                (byte) (v >>> 56));
        }
    }

    /**
     * Encodes the primitive into the buffer using big endian.
     */
    public void putShort(short v) {
        putShort(v, true);
    }

    /**
     * Encodes the primitive into the buffer.
     *
     * @param v         The value to encode.
     * @param bigEndian Whether to encode in big or little endian byte ordering.
     */
    public void putShort(short v, boolean bigEndian) {
        if (bigEndian) {
            put((byte) ((v >>> 8) & 0xFF), (byte) ((v >>> 0) & 0xFF));
        } else {
            put((byte) ((v >>> 0) & 0xFF), (byte) ((v >>> 8) & 0xFF));
        }
    }

    /**
     * Returns the next byte in the buffer, or -1 when nothing is available.
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
     * Push bytes from the internal buffer to the given buffer.
     *
     * @param buf The buffer into which data is read.
     * @param off The start offset in the buffer to put data.
     * @param len The maximum number of bytes to read.
     * @return The number of bytes read.
     */
    public int sendTo(byte[] buf, int off, int len) {
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
     * Push bytes from the internal buffer to the given buffer.
     *
     * @param buf The buffer into which data is read.
     * @return The number of bytes read.
     */
    public int sendTo(ByteBuffer buf) {
        if (length == 0) {
            return 0;
        }
        int len = Math.min(buf.remaining(), length);
        buf.put(buffer, offset, len);
        length -= len;
        if (length == 0) {
            offset = 0;
        } else {
            offset += len;
        }
        return len;
    }

    /**
     * Push bytes from the internal buffer to the transport.
     */
    public void sendTo(DSBinaryTransport transport, boolean isLast) {
        transport.write(buffer, offset, length, isLast);
        offset = 0;
        length = 0;
    }

    /**
     * Push bytes from the internal buffer to the stream.
     */
    public void sendTo(OutputStream out) {
        if (length == 0) {
            return;
        }
        try {
            out.write(buffer, offset, length);
            offset = 0;
            length = 0;
        } catch (Exception x) {
            DSException.throwRuntime(x);
        }
    }

    /**
     * Returns a new array.
     */
    public byte[] toByteArray() {
        byte[] ret = new byte[length];
        sendTo(ret, 0, length);
        return ret;
    }

}
