package com.acuity.iot.dsa.dslink.protocol.protocol_v2;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharsetDecoder;
import java.util.HashMap;
import java.util.Map;
import org.iot.dsa.node.DSBytes;
import org.iot.dsa.node.DSString;
import org.iot.dsa.util.DSException;

/**
 * Used to read a DSA 2.n message (header and body).  Call init(InputStream) to start a new message,
 * can be reused for multiple messages. Not thread safe, the intent is messages will be constructed
 * and read serially.
 *
 * * @author Aaron Hansen
 * 1
 */
public class MessageReader implements MessageConstants {

    // Fields
    // ------

    private int ackId;
    private int bodyLength;
    private CharBuffer charBuffer;
    private Map<Byte, Object> headers = new HashMap<Byte, Object>();
    private InputStream input;
    private int method;
    private int requestId;
    private ByteBuffer strBuffer;
    private CharsetDecoder utf8decoder = DSString.UTF8.newDecoder();

    // Constructors
    // ------------

    public MessageReader() {
    }

    // Methods
    // -------

    public int getAckId() {
        return ackId;
    }

    public InputStream getBody() {
        return input;
    }

    public int getBodyLength() {
        return bodyLength;
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

    public Map<Byte, Object> getHeaders() {
        return headers;
    }

    public int getMethod() {
        return method;
    }

    public int getRequestId() {
        return requestId;
    }

    /**
     * Called by readString(), returns a bytebuffer for the given capacity ready for writing
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

    public MessageReader init(InputStream in) {
        try {
            input = in;
            headers.clear();
            int tlen = DSBytes.readInt(in, false);
            int hlen = DSBytes.readShort(in, false);
            bodyLength = tlen - hlen;
            method = in.read() & 0xFF;
            hlen -= 7;
            requestId = -1;
            ackId = -1;
            if (hlen > 4) {
                requestId = DSBytes.readInt(in, false);
                hlen -= 4;
                if (hlen > 4) {
                    ackId = DSBytes.readInt(in, false);
                    hlen -= 4;
                }
                parseDynamicHeaders(in, hlen);
            }
        } catch (IOException x) {
            DSException.throwRuntime(x);
        }
        return this;
    }

    public boolean isRequest() {
        switch (method) {
            case MSG_CLOSE :
            case MSG_INVOKE_REQ :
            case MSG_LIST_REQ :
            case MSG_OBSERVE_REQ :
                return true;
        }
        return false;
    }

    public boolean isResponse() {
        return (method & 0x80) != 0;
    }

    private void parseDynamicHeaders(InputStream in, int len) throws IOException {
        byte code;
        Object val;
        while (len > 0) {
            code = (byte) in.read();
            val = null;
            len--;
            switch (code) {
                case 0:
                case 8:
                case 12:
                case 32:
                    val = in.read();
                    len--;
                    break;
                case 1:
                case 2:
                case 14:
                case 15:
                    val = DSBytes.readInt(in, false);
                    len -= 4;
                    break;
                case 21:
                case 41:
                case 60:
                case 80:
                case 81:
                    short slen = DSBytes.readShort(in, false);
                    len -= 2;
                    len -= slen;
                    val = readString(in, slen);
                    break;
                default:
                    val = code;
            }
            headers.put(code, val);
        }
    }

    /**
     * Decodes a DSA 2.n string.
     */
    public String readString(InputStream in) {
        int len = DSBytes.readShort(in, false);
        if (len == 0) {
            return "";
        }
        return readString(in, len);
    }

    /**
     * Decodes a DSA 2.n string.
     */
    private String readString(InputStream in, int len) {
        String ret = null;
        try {
            ByteBuffer byteBuf = getStringBuffer(len);
            for (int i = len; --i >= 0; ) {
                byteBuf.put((byte) in.read());
            }
            byteBuf.flip();
            CharBuffer charBuf = getCharBuffer(len);
            utf8decoder.decode(byteBuf, charBuf, false);
            charBuf.flip();
            ret = charBuf.toString();
        } catch (IOException x) {
            DSException.throwRuntime(x);
        }
        return ret;
    }

}
