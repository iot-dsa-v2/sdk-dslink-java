package org.iot.dsa.io.msgpack;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharsetEncoder;
import org.iot.dsa.io.AbstractWriter;
import org.iot.dsa.node.DSString;
import org.iot.dsa.util.DSException;

/**
 * @author Aaron Hansen
 */
public abstract class MsgpackWriter extends AbstractWriter implements MsgpackConstants {

    // Fields
    // ------

    private ByteBuffer byteBuffer = ByteBuffer.allocate(1024 * 64);
    private CharBuffer charBuffer;
    private Frame frame;
    private CharsetEncoder encoder = DSString.UTF8.newEncoder();
    private ByteBuffer strBuffer;

    // Constructors
    // ------------

    // Methods
    // -------

    /**
     * Used by writeString(), returns the string wrapped in a charbuffer that is ready for reading
     * (getting).  Attempts to reuse the same buffer as much as possible.
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

    /**
     * Returns the number of bytes in the outgoing byte buffer.
     */
    public int length() {
        return byteBuffer.position();
    }

    @Override
    protected void writeSeparator() throws IOException {
    }

    @Override
    public void writeNewLineIndent() {
    }

    @Override
    protected void write(boolean arg) throws IOException {
        if (frame != null) {
            frame.increment();
        }
        byteBuffer.put(arg ? TRUE : FALSE);
    }

    @Override
    protected void write(byte[] arg) throws IOException {
        if (frame != null) {
            frame.increment();
        }
        if (arg == null) {
            writeNull();
            return;
        }
        int len = arg.length;
        if (len < (1 << 8)) {
            byteBuffer.put(BIN8);
            byteBuffer.put((byte) len);
        }
        else if (len < (1 << 16)) {
            byteBuffer.put(BIN16);
            byteBuffer.putShort((short) len);
        }
        else {
            byteBuffer.put(BIN32);
            byteBuffer.putInt(len);
        }
        byteBuffer.put(arg);
    }

    @Override
    protected void write(double arg) throws IOException {
        if (frame != null) {
            frame.increment();
        }
        byteBuffer.put(FLOAT64);
        byteBuffer.putDouble(arg);
    }

    @Override
    protected void write(long arg) throws IOException {
        if (frame != null) {
            frame.increment();
        }
        if (arg < -(1 << 5)) {
            if (arg < -(1 << 15)) {
                if (arg < -(1L << 31)) {
                    byteBuffer.put(INT64);
                    byteBuffer.putLong(arg);
                } else {
                    byteBuffer.put(INT32);
                    byteBuffer.putInt((int) arg);
                }
            } else {
                if (arg < -(1 << 7)) {
                    byteBuffer.put(INT16);
                    byteBuffer.putShort((short) arg);
                } else {
                    byteBuffer.put(INT8);
                    byteBuffer.put((byte) arg);
                }
            }
        } else if (arg < (1 << 7)) {
            byteBuffer.put((byte) arg);
        } else {
            if (arg < (1 << 16)) {
                if (arg < (1 << 8)) {
                    byteBuffer.put(UINT8);
                    byteBuffer.put((byte) arg);
                } else {
                    byteBuffer.put(UINT16);
                    byteBuffer.putShort((short) arg);
                }
            } else {
                if (arg < (1L << 32)) {
                    byteBuffer.put(UINT32);
                    byteBuffer.putInt((int) arg);
                } else {
                    byteBuffer.put(UINT64);
                    byteBuffer.putLong(arg);
                }
            }
        }
    }

    @Override
    protected void writeKey(CharSequence arg) throws IOException {
        writeString(arg);
    }

    @Override
    protected void writeKeyValueSeparator() throws IOException {
    }

    @Override
    protected void writeListEnd() throws IOException {
        frame.writeSize();
        frame = frame.parent;
        if (frame == null) {
            flush();
        }
    }

    @Override
    protected void writeListStart() throws IOException {
        writeListStart(-1);
    }

    /**
     * A negative size implies dynamic and will be written when the list is closed.
     */
    protected void writeListStart(int len) throws IOException {
        if (frame != null) {
            frame.increment();
        }
        this.frame = new Frame(true);
        if (len < 0) {
            byteBuffer.put(LIST16);
            frame.offset = byteBuffer.position();
            byteBuffer.putShort((short) 0);
        } else if (len < (1 << 4)) {
            byteBuffer.put((byte) (FIXLIST_PREFIX | len));
        } else if (len < (1 << 16)) {
            byteBuffer.put(LIST16);
            byteBuffer.putShort((short) len);
        } else {
            byteBuffer.put(LIST32);
            byteBuffer.putInt(len);
        }
    }

    @Override
    protected void writeMapEnd() throws IOException {
        frame.writeSize();
        frame = frame.parent;
        if (frame == null) {
            flush();
        }
    }

    @Override
    protected void writeMapStart() throws IOException {
        writeMapStart(-1);
    }

    /**
     * A negative size implies dynamic and will be written when the map is closed.
     */
    protected void writeMapStart(int len) throws IOException {
        if (frame != null) {
            frame.increment();
        }
        this.frame = new Frame(true);
        if (len < 0) {
            byteBuffer.put(MAP16);
            frame.offset = byteBuffer.position();
            byteBuffer.putShort((short) 0);
        } else if (len < (1 << 4)) {
            byteBuffer.put((byte) (FIXMAP_PREFIX | len));
        } else if (len < (1 << 16)) {
            byteBuffer.put(MAP16);
            byteBuffer.putShort((short) len);
        } else {
            byteBuffer.put(MAP32);
            byteBuffer.putInt(len);
        }
    }

    @Override
    protected void writeNull() throws IOException {
        if (frame != null) {
            frame.increment();
        }
        byteBuffer.put(NULL);
    }

    private void writeString(CharSequence arg) throws IOException {
        CharBuffer chars = getCharBuffer(arg);
        ByteBuffer strBuffer = getStringBuffer(chars.position() * (int) encoder.maxBytesPerChar());
        encoder.encode(chars, strBuffer, false);
        strBuffer.flip();
        int len = strBuffer.remaining();
        if (len < (1 << 5)) {
            byteBuffer.put((byte) (FIXSTR_PREFIX | len));
        } else if (len < (1 << 8)) {
            byteBuffer.put(STR8);
            byteBuffer.put((byte) len);
        } else if (len < (1 << 16)) {
            byteBuffer.put(STR16);
            byteBuffer.putShort((short) len);
        } else {
            byteBuffer.put(STR32);
            byteBuffer.putInt(len);
        }
        byteBuffer.put(strBuffer);
        encoder.reset();
    }

    /**
     * Writes the internal buffer to the parameter.  The internal buffer will be cleared.
     */
    protected void writeTo(ByteBuffer out) {
        byteBuffer.flip();
        out.put(byteBuffer);
        byteBuffer.clear();
    }

    /**
     * Writes the internal buffer to the parameter.  The internal buffer will be cleared.
     *
     * @throws DSException if there is an IOException.
     */
    protected void writeTo(OutputStream out) {
        try {
            byteBuffer.flip();
            //TODO - performance opt, write buffer to byte[], then byte[] to stream?
            while (byteBuffer.hasRemaining()) {
                out.write(byteBuffer.get());
            }
            byteBuffer.clear();
        } catch (IOException x) {
            DSException.throwRuntime(x);
        }
    }

    @Override
    protected void writeValue(CharSequence arg) throws IOException {
        if (frame != null) {
            frame.increment();
        }
        writeString(arg);
    }

    // Inner Classes
    // -------------

    class Frame {

        boolean map = true;
        int offset = -1;
        Frame parent;
        int size = 0;

        Frame(boolean map) {
            this.map = map;
            this.parent = frame;
        }

        void increment() {
            size++;
        }

        boolean isMap() {
            return map;
        }

        void writeSize() {
            if (offset >= 0) {
                byteBuffer.putShort(offset, (short) size);
            }
        }
    }

}
