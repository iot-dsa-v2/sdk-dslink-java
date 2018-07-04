package com.acuity.iot.dsa.dslink.protocol.v2;

import com.acuity.iot.dsa.dslink.io.DSByteBuffer;
import com.acuity.iot.dsa.dslink.io.msgpack.MsgpackReader;
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
 * @author Aaron Hansen
 */
public class DS2MessageReader extends DS2Message {

    // Fields
    // ------

    private int ackId;
    private int bodyLength;
    private CharBuffer charBuffer;
    private Map<Integer, Object> headers = new HashMap<Integer, Object>();
    private InputStream input;
    private int method;
    private MsgpackReader reader;
    private int requestId;
    private ByteBuffer strBuffer;
    private CharsetDecoder utf8decoder = DSString.UTF8.newDecoder();

    // Constructors
    // ------------

    public DS2MessageReader() {
    }

    // Methods
    // -------

    @Override
    protected void getDebug(StringBuilder buf) {
        buf.append("RECV ");
        debugMethod(getMethod(), buf);
        if (requestId > 0) {
            buf.append(", ").append("Rid ").append(requestId);
        }
        if (ackId > 0) {
            buf.append(", ").append("Ack ").append(ackId);
        }
        debugHeaders(headers, buf);
    }

    public int getAckId() {
        return ackId;
    }

    public InputStream getBody() {
        return input;
    }

    public MsgpackReader getBodyReader() {
        if (reader == null) {
            reader = new MsgpackReader(input);
        }
        return reader;
    }

    public int getBodyLength() {
        return bodyLength;
    }

    /**
     * Returns a char buffer with the given capacity, ready for writing (putting).  Attempts to
     * reuse the same char buffer.
     */
    private CharBuffer getCharBuffer(int size) {
        if ((charBuffer == null) || (charBuffer.capacity() < size)) {
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

    public Object getHeader(Integer key) {
        return headers.get(key);
    }

    public Object getHeader(Integer key, Object def) {
        Object ret = headers.get(key);
        if (ret == null) {
            ret = def;
        }
        return ret;
    }

    public Map<Integer, Object> getHeaders() {
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

    void init(int requestId,
              int method,
              DSByteBuffer body,
              Map<Integer, Object> headers) {
        this.requestId = requestId;
        this.method = method;
        input = body;
        bodyLength = body.length();
        this.headers.clear();
        this.headers.putAll(headers);
    }

    public void init(InputStream in) {
        try {
            input = in;
            getBodyReader().reset();
            headers.clear();
            int tlen = DSBytes.readInt(in, false);
            int hlen = DSBytes.readShort(in, false);
            bodyLength = tlen - hlen;
            method = in.read() & 0xFF;
            hlen -= 7;
            requestId = -1;
            ackId = -1;
            if (hlen >= 4) {
                requestId = DSBytes.readInt(in, false);
                hlen -= 4;
                if (hlen >= 4) {
                    ackId = DSBytes.readInt(in, false);
                    hlen -= 4;
                }
                if (hlen > 0) {
                    parseDynamicHeaders(in, hlen, headers);
                }
            }
            if (debug()) {
                printDebug();
            }
        } catch (IOException x) {
            DSException.throwRuntime(x);
        }
    }

    public boolean isAck() {
        return method == MSG_ACK;
    }

    public boolean isPing() {
        return method == MSG_PING;
    }

    public boolean isMultipart() {
        Integer page = (Integer) headers.get(HDR_PAGE_ID);
        return page != null;
    }

    public boolean isRequest() {
        switch (method) {
            case MSG_CLOSE:
            case MSG_INVOKE_REQ:
            case MSG_LIST_REQ:
            case MSG_OBSERVE_REQ:
            case MSG_SUBSCRIBE_REQ:
            case MSG_SET_REQ:
                return true;
        }
        return false;
    }

    public boolean isResponse() {
        return (method & 0x80) != 0;
    }

    void parseDynamicHeaders(InputStream in, int len, Map<Integer, Object> headers)
            throws IOException {
        int code;
        Object val;
        while (len > 0) {
            code = in.read() & 0xFF;
            len--;
            switch (code) {
                case HDR_STATUS:
                case HDR_ALIAS_COUNT:
                case HDR_QOS:
                case HDR_MAX_PERMISSION:
                    val = (byte) in.read();
                    len--;
                    break;
                case HDR_SEQ_ID:
                case HDR_PAGE_ID:
                case HDR_QUEUE_SIZE:
                case HDR_QUEUE_DURATION:
                    val = DSBytes.readInt(in, false);
                    len -= 4;
                    break;
                case HDR_AUDIT:
                case HDR_ERROR_DETAIL:
                case HDR_PUB_PATH:
                case HDR_ATTRIBUTE_FIELD:
                case HDR_PERMISSION_TOKEN:
                case HDR_TARGET_PATH:
                case HDR_SOURCE_PATH:
                    short slen = DSBytes.readShort(in, false);
                    len -= 2;
                    val = readString(in, slen);
                    len -= slen;
                    break;
                default:
                    val = NO_HEADER_VAL;
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
