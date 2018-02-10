package com.acuity.iot.dsa.dslink.protocol.protocol_v2;

import com.acuity.iot.dsa.dslink.io.DSByteBuffer;
import com.acuity.iot.dsa.dslink.transport.DSBinaryTransport;
import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharsetEncoder;
import org.iot.dsa.node.DSString;

/**
 * Used to write a DSA 2.n message (header and body).  Call init(int,int) to start a new message,
 * can be reused for multiple messages. Not thread safe, the intent is messages will be constructed
 * and written serially.
 *
 * @author Aaron Hansen
 */
public class DS2MessageWriter implements MessageConstants {

    // Fields
    // ------

    private DSByteBuffer body;
    private CharBuffer charBuffer;
    private DSByteBuffer header;
    private byte method;
    private ByteBuffer strBuffer;
    private CharsetEncoder utf8encoder;

    // Constructors
    // ------------

    public DS2MessageWriter() {
        header = new DSByteBuffer();
        body = new DSByteBuffer();
        utf8encoder = DSString.UTF8.newEncoder();
        init(-1, -1);
    }

    // Methods
    // -------

    /**
     * Encodes the key into the header buffer.
     */
    public DS2MessageWriter addHeader(byte key) {
        header.put(key);
        return this;
    }

    /**
     * Encodes the key value pair into the header buffer.
     */
    public DS2MessageWriter addHeader(byte key, byte value) {
        header.put(key);
        header.put(value);
        return this;
    }

    /**
     * Encodes the key value pair into the header buffer.
     */
    public DS2MessageWriter addHeader(byte key, int value) {
        header.put(key);
        header.putInt(value, false);
        return this;
    }

    /**
     * Encodes the key value pair into the header buffer.
     */
    public DS2MessageWriter addHeader(byte key, String value) {
        header.put(key);
        writeString(value, header);
        return this;
    }

    private void encodeHeaderLengths() {
        int hlen = header.length();
        int blen = body.length();
        header.replaceInt(0, hlen + blen, false);
        header.replaceShort(4, (short) hlen, false);
        header.replace(6, method);
    }

    public int getBodyLength() {
        return body.length();
    }

    public int getHeaderLength() {
        return header.length();
    }

    /**
     * Attempts to reuse a charbuffer, but will allocate a new one if the size demands
     * it.
     */
    private CharBuffer getCharBuffer(CharSequence arg) {
        int len = arg.length();
        if (charBuffer == null) {
            int tmp = 1024;
            while (tmp < len) {
                tmp += 1024;
            }
            charBuffer = CharBuffer.allocate(tmp);
        } else if (charBuffer.length() < len) {
            int tmp = charBuffer.length();
            while (tmp < len) {
                tmp += 1024;
            }
            charBuffer = CharBuffer.allocate(tmp);
        } else {
            charBuffer.clear();
        }
        charBuffer.append(arg);
        charBuffer.flip();
        return charBuffer;
    }

    /**
     * Called by writeString(), returns a bytebuffer for the given capacity ready for writing
     * (putting).  Attempts to reuse the same buffer as much as possible.
     */
    private ByteBuffer getStringBuffer(int len) {
        if (strBuffer == null) {
            int tmp = 1024;
            while (tmp < len) {
                tmp += 1024;
            }
            strBuffer = ByteBuffer.allocate(tmp);
        } else if (strBuffer.capacity() < len) {
            int tmp = strBuffer.capacity();
            while (tmp < len) {
                tmp += 1024;
            }
            strBuffer = ByteBuffer.allocate(tmp);
        } else {
            strBuffer.clear();
        }
        return strBuffer;
    }

    public DSByteBuffer getBody() {
        return body;
    }

    /**
     * Initialize a new message.
     *
     * @param requestId The request ID or -1 to omit.
     * @param ackId     -1 to omit, but can only be -1 when the requestId is also -1.
     */
    public DS2MessageWriter init(int requestId, int ackId) {
        body.clear();
        header.clear();
        header.skip(7);
        if (requestId >= 0) {
            header.putInt(requestId);
            if (ackId >= 0) {
                header.putInt(requestId);
            }
        }
        return this;
    }

    public DS2MessageWriter setMethod(byte method) {
        this.method = method;
        return this;
    }

    /**
     * This is for testing, it encodes the full message.
     */
    public byte[] toByteArray() {
        ByteArrayOutputStream out = new ByteArrayOutputStream(header.length() + body.length());
        encodeHeaderLengths();
        header.sendTo(out);
        body.sendTo(out);
        return out.toByteArray();
    }

    /**
     * Writes the message to the transport.
     */
    public DS2MessageWriter write(DSBinaryTransport out) {
        encodeHeaderLengths();
        header.sendTo(out, false);
        body.sendTo(out, true);
        return this;
    }

    /**
     * DSA 2.n encodes a string into the the given buffer.
     */
    public void writeString(CharSequence str, DSByteBuffer buf) {
        CharBuffer chars = getCharBuffer(str);
        ByteBuffer strBuffer = getStringBuffer(
                chars.position() * (int) utf8encoder.maxBytesPerChar());
        utf8encoder.encode(chars, strBuffer, false);
        buf.putShort((short) strBuffer.position(), false);
        buf.put(strBuffer);
    }

    /**
     * Writes a little endian integer to the buffer.
     public void writeIntLE(int v, ByteBuffer buf) {
     buf.put((byte) ((v >>> 0) & 0xFF));
     buf.put((byte) ((v >>> 8) & 0xFF));
     buf.put((byte) ((v >>> 16) & 0xFF));
     buf.put((byte) ((v >>> 32) & 0xFF));
     }
     */

    /**
     * Writes a little endian integer to the buffer.
     public void writeIntLE(int v, ByteBuffer buf, int idx) {
     buf.put(idx, (byte) ((v >>> 0) & 0xFF));
     buf.put(++idx, (byte) ((v >>> 8) & 0xFF));
     buf.put(++idx, (byte) ((v >>> 16) & 0xFF));
     buf.put(++idx, (byte) ((v >>> 32) & 0xFF));
     }
     */

    /**
     * Writes a little endian short to the buffer.
     public void writeShortLE(short v, ByteBuffer buf) {
     buf.put((byte) ((v >>> 0) & 0xFF));
     buf.put((byte) ((v >>> 8) & 0xFF));
     }
     */

    /**
     * Writes a little endian short to the buffer.
     public void writeShortLE(short v, ByteBuffer buf, int idx) {
     buf.put(idx, (byte) ((v >>> 0) & 0xFF));
     buf.put(++idx, (byte) ((v >>> 8) & 0xFF));
     }
     */

    /**
     * Writes an unsigned 16 bit little endian integer to the buffer.
     public void writeU16LE(int v, ByteBuffer buf) {
     buf.put((byte) ((v >>> 0) & 0xFF));
     buf.put((byte) ((v >>> 8) & 0xFF));
     }
     */

    /**
     * Writes an unsigned 16 bit little endian integer to the buffer.
     public void writeU16LE(int v, ByteBuffer buf, int idx) {
     buf.put(idx, (byte) ((v >>> 0) & 0xFF));
     buf.put(++idx, (byte) ((v >>> 8) & 0xFF));
     }
     */

    /**
     * Writes an unsigned 32 bit little endian integer to the buffer.
     public void writeU32LE(long v, ByteBuffer buf) {
     buf.put((byte) ((v >>> 0) & 0xFF));
     buf.put((byte) ((v >>> 8) & 0xFF));
     buf.put((byte) ((v >>> 16) & 0xFF));
     buf.put((byte) ((v >>> 32) & 0xFF));
     }
     */

    /**
     * Writes an unsigned 32 bit little endian integer to the buffer.
     public void writeU32LE(long v, ByteBuffer buf, int idx) {
     buf.put(idx, (byte) ((v >>> 0) & 0xFF));
     buf.put(++idx, (byte) ((v >>> 8) & 0xFF));
     buf.put(++idx, (byte) ((v >>> 16) & 0xFF));
     buf.put(++idx, (byte) ((v >>> 32) & 0xFF));
     }
     */

}
