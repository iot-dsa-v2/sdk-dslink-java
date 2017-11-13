package org.iot.dsa.io.msgpack;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharsetDecoder;
import org.iot.dsa.io.AbstractReader;
import org.iot.dsa.io.DSIReader;
import org.iot.dsa.node.DSBytes;
import org.iot.dsa.node.DSString;
import org.iot.dsa.util.DSException;

/**
 * MsgPack implementation of DSReader.  The same instance can be re-used with the setInput method.
 * This class is not thread safe.
 *
 * @author Aaron Hansen
 * @see DSIReader
 */
public class MsgpackReader extends AbstractReader implements DSIReader, MsgpackConstants {

    // Fields
    // ---------

    private byte[] bytes;
    private ByteBuffer byteBuffer;
    private CharBuffer charBuffer;
    private CharsetDecoder decoder = DSString.UTF8.newDecoder();
    private InputStream in;
    private Frame frame;
    private boolean wasValue = true;

    // Constructors
    // ------------

    public MsgpackReader() {
    }

    public MsgpackReader(InputStream in) {
        setInput(in);
    }


    // Public Methods
    // --------------

    @Override
    public void close() {
        try {
            in.close();
        } catch (Exception x) {
            throw new RuntimeException(x);
        }
    }

    /**
     * Returns a byte buffer wrapping the given bytes and ready for reading (getting).  Attempts to
     * reuse the same buffer.
     */
    private ByteBuffer getByteBuffer(byte[] bytes, int off, int len) {
        if ((byteBuffer == null) || (byteBuffer.capacity() < len)) {
            int tmp = 1024;
            while (tmp < len) {
                tmp += 1024;
            }
            byteBuffer = ByteBuffer.allocate(tmp);
        } else {
            byteBuffer.clear();
        }
        byteBuffer.put(bytes, 0, len);
        byteBuffer.flip();
        return byteBuffer;
    }

    /**
     * Returns a char buffer with the given capacity, ready for writing (putting).  Attempts to
     * reuse the same char buffer.
     */
    private CharBuffer getCharBuffer(int size) {
        if ((charBuffer == null) || (charBuffer.length() < size)) {
            int tmp = 1024;
            while (tmp < size) {
                tmp += 1024;
            }
            charBuffer = CharBuffer.allocate(tmp);
        } else {
            charBuffer.clear();
        }
        return charBuffer;
    }

    public static final boolean isFixInt(byte b) {
        int v = b & 0xff;
        return v <= 0x7f || v >= 0xe0;
    }

    public static final boolean isFixStr(byte b) {
        return (b & (byte) 0xe0) == FIXSTR_PREFIX;
    }

    public static final boolean isFixedList(byte b) {
        return (b & (byte) 0xf0) == FIXLIST_PREFIX;
    }

    public static final boolean isFixedMap(byte b) {
        return (b & (byte) 0xf0) == FIXMAP_PREFIX;
    }

    @Override
    public Token next() {
        if (frame != null) {
            //check to see if we've read all the children of the parent list/map.
            if (frame.map) {
                //don't count keys, only values
                if (wasValue) {
                    if (!frame.next()) {
                        frame = frame.parent;
                        setEndMap();
                        return last();
                    }
                }
                wasValue = !wasValue;
            } else {
                if (!frame.next()) {
                    frame = frame.parent;
                    setEndList();
                    return last();
                }
            }
        }
        byte b = 0;
        try {
            b = (byte) in.read();
            switch (b) {
                case NULL:
                    return setNextValueNull();
                case FALSE:
                    return setNextValue(false);
                case TRUE:
                    return setNextValue(true);
                case BIN8:
                case BIN16:
                case BIN32:
                    return readBytes(b);
                case FLOAT32:
                case FLOAT64:
                case UINT8:
                case UINT16:
                case UINT32:
                case UINT64:
                case INT8:
                case INT16:
                case INT32:
                case INT64:
                    return readNumber(b);
                case STR8:
                case STR16:
                case STR32:
                    return readString(b);
                case LIST16:
                case LIST32:
                    return readList(b);
                case MAP16:
                case MAP32:
                    return readMap(b);
                default:
                    ;
            }
            if (isFixInt(b)) {
                return setNextValue(b);
            }
            if (isFixStr(b)) {
                return readString(b);
            }
            if (isFixedList(b)) {
                return readList(b);
            }
            if (isFixedMap(b)) {
                return readMap(b);
            }
        } catch (IOException x) {
            DSException.throwRuntime(x);
        }
        throw new IllegalStateException("Unknown type: " + b);
    }

    /**
     * Reads bytes into an array that is guaranteed to be at least the given size but will probably
     * be longer.
     */
    private byte[] readBytes(int size) throws IOException {
        if ((bytes == null) || (bytes.length < size)) {
            int tmp = 1024;
            while (tmp < size) {
                tmp += 1024;
            }
            bytes = new byte[tmp];
        }
        if (in.read(bytes, 0, size) != size) {
            throw new IOException("Unexpected end of input");
        }
        return bytes;
    }

    @Override
    public MsgpackReader reset() {
        super.reset();
        return this;
    }

    /**
     * Sets the input source, resets to ROOT, and returns this.
     */
    public MsgpackReader setInput(InputStream inputStream) {
        this.in = inputStream;
        return reset();
    }

    private Token readBytes(byte b) throws IOException {
        int size = 0;
        switch (b) {
            case BIN8:
                size = in.read() & 0xFF;
                break;
            case BIN16:
                size = DSBytes.readShort(in, true);
                break;
            case BIN32:
                size = DSBytes.readInt(in, true);
                break;
            default:
                throw new IllegalStateException("Unknown bytes: " + b);
        }
        byte[] bytes = new byte[size];
        in.read(bytes);
        return setNextValue(bytes);
    }

    private Token readList(byte b) throws IOException {
        int size = 0;
        if (isFixedList(b)) {
            size = b & 0x0f;
        } else {
            switch (b) {
                case LIST16:
                    size = DSBytes.readShort(in, true);
                    break;
                case LIST32:
                    size = DSBytes.readInt(in, true);
                    break;
                default:
                    throw new IllegalStateException("Unknown list type: " + b);
            }
        }
        frame = new Frame(size, false);
        return setBeginList();
    }

    private Token readMap(byte b) throws IOException {
        int size = 0;
        if (isFixedMap(b)) {
            size = b & 0x0f;
        } else {
            switch (b) {
                case MAP16:
                    size = DSBytes.readShort(in, true);
                    break;
                case MAP32:
                    size = DSBytes.readInt(in, true);
                    break;
                default:
                    throw new IllegalStateException("Unknown map type: " + b);
            }
        }
        frame = new Frame(size, true);
        return setBeginMap();
    }

    private Token readNumber(byte b) throws IOException {
        switch (b) {
            case FLOAT32:
                return setNextValue(DSBytes.readFloat(in, true));
            case FLOAT64:
                return setNextValue(DSBytes.readDouble(in, true));
            case INT8:
            case UINT8:
                return setNextValue((short) in.read() & 0xFF);
            case INT16:
            case UINT16:
                return setNextValue(DSBytes.readShort(in, true));
            case INT32:
            case UINT32:
                return setNextValue(DSBytes.readInt(in, true));
            default: //INT64
                return setNextValue(DSBytes.readLong(in, true));
        }
    }

    private Token readString(byte b) throws IOException {
        int size = 0;
        if (isFixStr(b)) {
            size = b & 0x1F;
        } else {
            switch (b) {
                case STR8:
                    size = in.read() & 0xFF;
                    break;
                case STR16:
                    size = DSBytes.readShort(in, true);
                    break;
                case STR32:
                    size = DSBytes.readInt(in, true);
                    break;
                default:
                    throw new IllegalStateException("Unknown string type: " + b);
            }
        }
        byte[] bytes = readBytes(size);
        ByteBuffer byteBuf = getByteBuffer(bytes, 0, size);
        CharBuffer charBuf = getCharBuffer(size);
        decoder.decode(byteBuf, charBuf, false);
        charBuf.flip();
        setNextValue(charBuf.toString());
        return last();
    }

    // Inner Classes
    // -------------

    private class Frame {

        boolean map = true;
        int size;
        Frame parent;

        Frame(int size, boolean map) {
            this.map = map;
            this.size = size;
            this.parent = frame;
        }

        public boolean next() {
            return --size >= 0;
        }

    }

}
