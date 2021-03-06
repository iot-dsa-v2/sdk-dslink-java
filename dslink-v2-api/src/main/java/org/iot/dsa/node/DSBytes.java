package org.iot.dsa.node;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import org.iot.dsa.io.DSBase64;
import org.iot.dsa.util.DSException;

/**
 * Byte array that gets encoded as a base64 string.
 *
 * @author Aaron Hansen
 */
public class DSBytes extends DSElement {

    // Constants
    // ---------

    private final static char[] HEX = "0123456789ABCDEF".toCharArray();
    public static final DSBytes NULL = new DSBytes(new byte[0]);
    private static final String PREFIX = "\u001Bbytes:";

    // Fields
    // ------

    private byte[] value;

    // Constructors
    // ------------

    private DSBytes(byte[] val) {
        value = val;
    }

    // Public Methods
    // --------------

    public static byte[] decode(String encoded) {
        if (encoded.startsWith(PREFIX)) {
            encoded = encoded.substring(PREFIX.length());
        }
        return DSBase64.decode(encoded);
    }

    public static String encode(byte[] bytes) {
        return PREFIX + DSBase64.encodeUrl(bytes);
    }

    /**
     * True if the argument is a DSINumber and the values are equal or they are both isNull.
     */
    @Override
    public boolean equals(Object arg) {
        if (arg == this) {
            return true;
        }
        if (arg instanceof DSBytes) {
            DSBytes other = (DSBytes) arg;
            return Arrays.equals(value, other.value);
        }
        return false;
    }

    /**
     * Converts a hex string into a byte array.
     */
    public static byte[] fromHex(CharSequence s) {
        int len = s.length();
        byte[] ret = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            ret[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i + 1), 16));
        }
        return ret;
    }

    /**
     * The raw bytes, do not modify.
     */
    public byte[] getBytes() {
        return value;
    }

    @Override
    public DSElementType getElementType() {
        return DSElementType.BYTES;
    }

    @Override
    public DSValueType getValueType() {
        return DSValueType.BINARY;
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

    @Override
    public boolean isBytes() {
        return true;
    }

    /**
     * True if the string starts with the DSA escape sequence for a base 64 encoded string.
     */
    public static boolean isBytes(String arg) {
        if (arg == null) {
            return false;
        }
        return arg.startsWith(PREFIX);
    }

    @Override
    public boolean isNull() {
        return this == NULL;
    }

    /**
     * The number of bytes in the array.
     */
    public int length() {
        if (value == null) {
            return 0;
        }
        return value.length;
    }

    /**
     * Reads the primitive from a byte array.
     *
     * @param buf       Must be at least off + 8 in length.
     * @param off       The offset into the buffer to start reading.
     * @param bigEndian Whether to decode in big or little endian byte ordering.
     */
    public static double readDouble(byte[] buf, int off, boolean bigEndian) {
        return Double.longBitsToDouble(readLong(buf, off, bigEndian));
    }

    /**
     * Reads the primitive from a stream.
     *
     * @param in        Must have at 8 bytes to read.
     * @param bigEndian Whether to decode in big or little endian byte ordering.
     */
    public static double readDouble(InputStream in, boolean bigEndian) {
        return Double.longBitsToDouble(readLong(in, bigEndian));
    }

    /**
     * Reads the primitive from a byte array.
     *
     * @param buf       Must be at least off + 4 in length.
     * @param off       The offset into the buffer to start reading.
     * @param bigEndian Whether to decode in big or little endian byte ordering.
     */
    public static float readFloat(byte[] buf, int off, boolean bigEndian) {
        return Float.intBitsToFloat(readInt(buf, off, bigEndian));
    }

    /**
     * Reads the primitive a stream
     *
     * @param in        Must have at 4 bytes to read.
     * @param bigEndian Whether to decode in big or little endian byte ordering.
     */
    public static float readFloat(InputStream in, boolean bigEndian) {
        return Float.intBitsToFloat(readInt(in, bigEndian));
    }

    /**
     * Reads the primitive from a byte array.
     *
     * @param buf       Must be at least off + 4 in length.
     * @param off       The offset into the buffer to start reading.
     * @param bigEndian Whether to decode in big or little endian byte ordering.
     */
    public static int readInt(byte[] buf, int off, boolean bigEndian) {
        if (bigEndian) {
            return (((buf[off] & 0xFF) << 24) |
                    ((buf[++off] & 0xFF) << 16) |
                    ((buf[++off] & 0xFF) << 8) |
                    ((buf[++off] & 0xFF) << 0));
        }
        return (((buf[off] & 0xFF) << 0) |
                ((buf[++off] & 0xFF) << 8) |
                ((buf[++off] & 0xFF) << 16) |
                ((buf[++off] & 0xFF) << 24));
    }

    /**
     * Reads the primitive from a stream.
     *
     * @param in        Must have at least 4 bytes to read.
     * @param bigEndian Whether to decode in big or little endian byte ordering.
     */
    public static int readInt(InputStream in, boolean bigEndian) {
        int ret = 0;
        try {
            if (bigEndian) {
                return (((in.read() & 0xFF) << 24) |
                        ((in.read() & 0xFF) << 16) |
                        ((in.read() & 0xFF) << 8) |
                        ((in.read() & 0xFF) << 0));
            }
            ret = (((in.read() & 0xFF) << 0) |
                    ((in.read() & 0xFF) << 8) |
                    ((in.read() & 0xFF) << 16) |
                    ((in.read() & 0xFF) << 24));
        } catch (IOException x) {
            DSException.throwRuntime(x);
        }
        return ret;
    }

    /**
     * Reads the primitive from a byte array.
     *
     * @param buf       Must be at least off + 8 in length.
     * @param off       The offset into the buffer to start reading.
     * @param bigEndian Whether to decode in big or little endian byte ordering.
     */
    public static long readLong(byte[] buf, int off, boolean bigEndian) {
        if (bigEndian) {
            return (((long) (buf[off] & 0xFF) << 56) |
                    ((long) (buf[++off] & 0xFF) << 48) |
                    ((long) (buf[++off] & 0xFF) << 40) |
                    ((long) (buf[++off] & 0xFF) << 32) |
                    ((long) (buf[++off] & 0xFF) << 24) |
                    ((buf[++off] & 0xFF) << 16) |
                    ((buf[++off] & 0xFF) << 8) |
                    ((buf[++off] & 0xFF) << 0));
        }
        return (((buf[off] & 0xFF) << 0) |
                ((buf[++off] & 0xFF) << 8) |
                ((buf[++off] & 0xFF) << 16) |
                ((long) (buf[++off] & 0xFF) << 24) |
                ((long) (buf[++off] & 0xFF) << 32) |
                ((long) (buf[++off] & 0xFF) << 40) |
                ((long) (buf[++off] & 0xFF) << 48) |
                ((long) (buf[++off] & 0xFF) << 56));
    }

    /**
     * Reads the primitive from a stream.
     *
     * @param in        Must have 8 bytes to read.
     * @param bigEndian Whether to decode in big or little endian byte ordering.
     */
    public static long readLong(InputStream in, boolean bigEndian) {
        long ret = 0;
        try {
            if (bigEndian) {
                return (((long) (in.read() & 0xFF) << 56) |
                        ((long) (in.read() & 0xFF) << 48) |
                        ((long) (in.read() & 0xFF) << 40) |
                        ((long) (in.read() & 0xFF) << 32) |
                        ((long) (in.read() & 0xFF) << 24) |
                        ((in.read() & 0xFF) << 16) |
                        ((in.read() & 0xFF) << 8) |
                        ((in.read() & 0xFF) << 0));
            }
            ret = (((in.read() & 0xFF) << 0) |
                    ((in.read() & 0xFF) << 8) |
                    ((in.read() & 0xFF) << 16) |
                    ((long) (in.read() & 0xFF) << 24) |
                    ((long) (in.read() & 0xFF) << 32) |
                    ((long) (in.read() & 0xFF) << 40) |
                    ((long) (in.read() & 0xFF) << 48) |
                    ((long) (in.read() & 0xFF) << 56));
        } catch (IOException x) {
            DSException.throwRuntime(x);
        }
        return ret;
    }

    /**
     * Reads the primitive from a byte array.
     *
     * @param buf       Must be at least off + 2 in length.
     * @param off       The offset into the buffer to start reading.
     * @param bigEndian Whether to decode in big or little endian byte ordering.
     */
    public static short readShort(byte[] buf, int off, boolean bigEndian) {
        if (bigEndian) {
            return (short) (((buf[off] & 0xFF) << 8) | ((buf[++off] & 0xFF) << 0));
        }
        return (short) (((buf[off] & 0xFF) << 0) | ((buf[++off] & 0xFF) << 8));
    }

    /**
     * Reads the primitive from a stream.
     *
     * @param in        Must have 2 bytes to read.
     * @param bigEndian Whether to decode in big or little endian byte ordering.
     */
    public static short readShort(InputStream in, boolean bigEndian) {
        short ret = 0;
        try {
            if (bigEndian) {
                return (short) (((in.read() & 0xFF) << 8) | ((in.read() & 0xFF) << 0));
            }
            ret = (short) (((in.read() & 0xFF) << 0) | ((in.read() & 0xFF) << 8));
        } catch (IOException x) {
            DSException.throwRuntime(x);
        }
        return ret;
    }

    /**
     * Reads the primitive from a byte array.
     *
     * @param buf       Must be at least off + 2 in length.
     * @param off       The offset into the buffer to start reading.
     * @param bigEndian Whether to decode in big or little endian byte ordering.
     */
    public static int readU16(byte[] buf, int off, boolean bigEndian) {
        if (bigEndian) {
            return (((buf[off] & 0xFF) << 8) |
                    ((buf[++off] & 0xFF) << 0));
        }
        return (((buf[off] & 0xFF) << 0) |
                ((buf[++off] & 0xFF) << 8));
    }

    /**
     * Reads the primitive from a stream.
     *
     * @param in        Must have at least 2 bytes to read.
     * @param bigEndian Whether to decode in big or little endian byte ordering.
     */
    public static int readU16(InputStream in, boolean bigEndian) {
        int ret = 0;
        try {
            if (bigEndian) {
                return (((in.read() & 0xFF) << 8) |
                        ((in.read() & 0xFF) << 0));
            }
            ret = (((in.read() & 0xFF) << 0) |
                    ((in.read() & 0xFF) << 8));
        } catch (IOException x) {
            DSException.throwRuntime(x);
        }
        return ret;
    }

    /**
     * Reads the primitive from a byte array.
     *
     * @param buf       Must be at least off + 4 in length.
     * @param off       The offset into the buffer to start reading.
     * @param bigEndian Whether to decode in big or little endian byte ordering.
     */
    public static long readU32(byte[] buf, int off, boolean bigEndian) {
        if (bigEndian) {
            return (((long) (buf[++off] & 0xFF) << 24) |
                    ((buf[++off] & 0xFF) << 16) |
                    ((buf[++off] & 0xFF) << 8) |
                    ((buf[++off] & 0xFF) << 0));
        }
        return (((buf[off] & 0xFF) << 0) |
                ((buf[++off] & 0xFF) << 8) |
                ((buf[++off] & 0xFF) << 16) |
                ((long) (buf[++off] & 0xFF) << 24));
    }

    /**
     * Reads the primitive from a stream.
     *
     * @param in        Must have 8 bytes to read.
     * @param bigEndian Whether to decode in big or little endian byte ordering.
     */
    public static long readU32(InputStream in, boolean bigEndian) {
        long ret = 0;
        try {
            if (bigEndian) {
                return (((long) (in.read() & 0xFF) << 24) |
                        ((in.read() & 0xFF) << 16) |
                        ((in.read() & 0xFF) << 8) |
                        ((in.read() & 0xFF) << 0));
            }
            ret = (((in.read() & 0xFF) << 0) |
                    ((in.read() & 0xFF) << 8) |
                    ((in.read() & 0xFF) << 16) |
                    ((long) (in.read() & 0xFF) << 24));
        } catch (IOException x) {
            DSException.throwRuntime(x);
        }
        return ret;
    }

    @Override
    public byte[] toBytes() {
        return value;
    }

    /**
     * Converts the bytes into a hex string.
     *
     * @param val What to convert.
     * @param buf Where to put the results, can be null.
     * @return The buf parameter, or a new StringBuilder if the param was null.
     */
    public static StringBuilder toHex(byte val, StringBuilder buf) {
        if (buf == null) {
            buf = new StringBuilder();
        }
        buf.append(HEX[(val >>> 4) & 0x0F]);
        buf.append(HEX[val & 0x0F]);
        return buf;
    }

    /**
     * Converts the bytes into a hex string.
     *
     * @param bytes What to convert.
     * @param buf   Where to put the results, can be null.
     * @return The buf parameter, or a new StringBuilder if the param was null.
     */
    public static StringBuilder toHex(byte[] bytes, StringBuilder buf) {
        if (buf == null) {
            buf = new StringBuilder();
        }
        for (int i = 0, len = bytes.length; i < len; i++) {
            int val = bytes[i] & 0xFF;
            buf.append(HEX[val >>> 4] & 0x0F);
            buf.append(HEX[val & 0x0F]);
        }
        return buf;
    }

    @Override
    public String toString() {
        if (isNull()) {
            return "null";
        }
        return encode(value);
    }

    public static DSBytes valueOf(byte[] arg) {
        if (arg == null) {
            return NULL;
        }
        return new DSBytes(arg);
    }

    @Override
    public DSBytes valueOf(DSElement arg) {
        if ((arg == null) || arg.isNull()) {
            return NULL;
        }
        switch (arg.getElementType()) {
            case BOOLEAN:
                return valueOf(new byte[]{(byte) (arg.toBoolean() ? 1 : 0)});
            case BYTES:
                return (DSBytes) arg;
            case DOUBLE: {
                byte[] b = new byte[8];
                writeDouble(arg.toDouble(), b, 0, true);
                return valueOf(b);
            }
            case LONG: {
                byte[] b = new byte[8];
                writeLong(arg.toLong(), b, 0, true);
                return valueOf(b);
            }
            case STRING:
                return valueOf(arg.toString());
        }
        throw new IllegalArgumentException("Cannot decoding boolean: " + arg);
    }

    /**
     * Decodes a base64 encoded byte array.  If that throws an exception, returns the bytes
     * representing String.getBytes(UTF).
     */
    public static DSBytes valueOf(String arg) {
        if (arg == null) {
            return NULL;
        } else if (arg.length() == 0) {
            return NULL;
        } else if (arg.equalsIgnoreCase("null")) {
            return NULL;
        }
        if (arg.startsWith(PREFIX)) {
            arg = arg.substring(PREFIX.length());
        }
        try {
            return new DSBytes(DSBase64.decode(arg));
        } catch (Exception x) {
            return valueOf(arg.getBytes(DSString.UTF8));
        }
    }

    /**
     * Encodes the primitive into a byte array.
     *
     * @param v         The value to encode.
     * @param buf       Where to encode the value.  Must be at least off + 8 in length.
     * @param off       The offset into the buffer to start encoding.
     * @param bigEndian Whether to encode in big or little endian byte ordering.
     */
    public static void writeDouble(double v, byte[] buf, int off, boolean bigEndian) {
        writeLong(Double.doubleToRawLongBits(v), buf, off, bigEndian);
    }

    /**
     * Encodes the primitive into a stream.
     *
     * @param v         The value to encode.
     * @param out       Where to encode the value.
     * @param bigEndian Whether to encode in big or little endian byte ordering.
     */
    public static void writeDouble(double v, OutputStream out, boolean bigEndian) {
        writeLong(Double.doubleToRawLongBits(v), out, bigEndian);
    }

    /**
     * Encodes the primitive into a byte array.
     *
     * @param v         The value to encode.
     * @param buf       Where to encode the value.  Must be at least off + 4 in length.
     * @param off       The offset into the buffer to start encoding.
     * @param bigEndian Whether to encode in big or little endian byte ordering.
     */
    public static void writeFloat(float v, byte[] buf, int off, boolean bigEndian) {
        writeInt(Float.floatToIntBits(v), buf, off, bigEndian);
    }

    /**
     * Encodes the primitive into a stream
     *
     * @param v         The value to encode.
     * @param out       Where to encode the value.
     * @param bigEndian Whether to encode in big or little endian byte ordering.
     */
    public static void writeFloat(float v, OutputStream out, boolean bigEndian) {
        writeInt(Float.floatToIntBits(v), out, bigEndian);
    }

    /**
     * Encodes the primitive into a byte array.
     *
     * @param v         The value to encode.
     * @param buf       Where to encode the value.  Must be at least off + 4 in length.
     * @param off       The offset into the buffer to start encoding.
     * @param bigEndian Whether to encode in big or little endian byte ordering.
     */
    public static void writeInt(int v, byte[] buf, int off, boolean bigEndian) {
        if (bigEndian) {
            buf[off] = (byte) ((v >>> 24) & 0xFF);
            buf[++off] = (byte) ((v >>> 16) & 0xFF);
            buf[++off] = (byte) ((v >>> 8) & 0xFF);
            buf[++off] = (byte) ((v >>> 0) & 0xFF);
        } else {
            buf[off] = (byte) ((v >>> 0) & 0xFF);
            buf[++off] = (byte) ((v >>> 8) & 0xFF);
            buf[++off] = (byte) ((v >>> 16) & 0xFF);
            buf[++off] = (byte) ((v >>> 24) & 0xFF);
        }
    }

    /**
     * Encodes the primitive into a stream.
     *
     * @param v         The value to encode.
     * @param out       Where to encode the value.
     * @param bigEndian Whether to encode in big or little endian byte ordering.
     */
    public static void writeInt(int v, OutputStream out, boolean bigEndian) {
        try {
            if (bigEndian) {
                out.write((v >>> 24) & 0xFF);
                out.write((v >>> 16) & 0xFF);
                out.write((v >>> 8) & 0xFF);
                out.write((v >>> 0) & 0xFF);
            } else {
                out.write((v >>> 0) & 0xFF);
                out.write((v >>> 8) & 0xFF);
                out.write((v >>> 16) & 0xFF);
                out.write((v >>> 24) & 0xFF);
            }
        } catch (IOException x) {
            DSException.throwRuntime(x);
        }
    }

    /**
     * Encodes the primitive into a byte array.
     *
     * @param v         The value to encode.
     * @param buf       Where to encode the value. Must be at least off + 8 in length;
     * @param off       The offset into the buffer to start encoding.
     * @param bigEndian Whether to encode in big or little endian byte ordering.
     */
    public static void writeLong(long v, byte[] buf, int off, boolean bigEndian) {
        if (bigEndian) {
            buf[off] = (byte) (v >>> 56);
            buf[++off] = (byte) (v >>> 48);
            buf[++off] = (byte) (v >>> 40);
            buf[++off] = (byte) (v >>> 32);
            buf[++off] = (byte) (v >>> 24);
            buf[++off] = (byte) (v >>> 16);
            buf[++off] = (byte) (v >>> 8);
            buf[++off] = (byte) (v >>> 0);
        } else {
            buf[off] = (byte) (v >>> 0);
            buf[++off] = (byte) (v >>> 8);
            buf[++off] = (byte) (v >>> 16);
            buf[++off] = (byte) (v >>> 24);
            buf[++off] = (byte) (v >>> 32);
            buf[++off] = (byte) (v >>> 40);
            buf[++off] = (byte) (v >>> 48);
            buf[++off] = (byte) (v >>> 56);
        }
    }

    /**
     * Encodes the primitive into a stream.
     *
     * @param v         The value to encode.
     * @param out       Where to encode the value.
     * @param bigEndian Whether to encode in big or little endian byte ordering.
     */
    public static void writeLong(long v, OutputStream out, boolean bigEndian) {
        try {
            if (bigEndian) {
                out.write((byte) (v >>> 56));
                out.write((byte) (v >>> 48));
                out.write((byte) (v >>> 40));
                out.write((byte) (v >>> 32));
                out.write((byte) (v >>> 24));
                out.write((byte) (v >>> 16));
                out.write((byte) (v >>> 8));
                out.write((byte) (v >>> 0));
            } else {
                out.write((byte) (v >>> 0));
                out.write((byte) (v >>> 8));
                out.write((byte) (v >>> 16));
                out.write((byte) (v >>> 24));
                out.write((byte) (v >>> 32));
                out.write((byte) (v >>> 40));
                out.write((byte) (v >>> 48));
                out.write((byte) (v >>> 56));
            }
        } catch (IOException x) {
            DSException.throwRuntime(x);
        }
    }

    /**
     * Encodes the primitive into a byte array.
     *
     * @param v         The value to encode.
     * @param buf       Where to encode the value.  Must be a least off + 2 in length.
     * @param off       The offset into the buffer to start encoding.
     * @param bigEndian Whether to encode in big or little endian byte ordering.
     */
    public static void writeShort(short v, byte[] buf, int off, boolean bigEndian) {
        if (bigEndian) {
            buf[off] = (byte) ((v >>> 8) & 0xFF);
            buf[++off] = (byte) ((v >>> 0) & 0xFF);
        } else {
            buf[off] = (byte) ((v >>> 0) & 0xFF);
            buf[++off] = (byte) ((v >>> 8) & 0xFF);
        }
    }

    /**
     * Encodes the primitive into a stream.
     *
     * @param v         The value to encode.
     * @param out       Where to encode the value.
     * @param bigEndian Whether to encode in big or little endian byte ordering.
     */
    public static void writeShort(short v, OutputStream out, boolean bigEndian) {
        try {
            if (bigEndian) {
                out.write((v >>> 8) & 0xFF);
                out.write((v >>> 0) & 0xFF);
            } else {
                out.write((v >>> 0) & 0xFF);
                out.write((v >>> 8) & 0xFF);
            }
        } catch (IOException x) {
            DSException.throwRuntime(x);
        }
    }

    // Initialization
    // --------------

    static {
        DSRegistry.registerDecoder(DSBytes.class, NULL);
    }

}
