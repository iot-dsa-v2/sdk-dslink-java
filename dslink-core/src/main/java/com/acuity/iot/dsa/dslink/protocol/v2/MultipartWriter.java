package com.acuity.iot.dsa.dslink.protocol.v2;

import com.acuity.iot.dsa.dslink.io.DSByteBuffer;
import java.util.Iterator;
import java.util.Map;

/**
 * Used to write multipart outbound messages when the body of a single message is larger than
 * the max possible body size.
 *
 * @author Aaron Hansen
 */
public class MultipartWriter implements MessageConstants {

    // Fields
    // ------

    private DSByteBuffer body;
    private Map<Integer, Object> headers;
    private int method;
    private int page = 0;
    private int requestId = -1;
    private Byte status;

    // Constructors
    // ------------

    MultipartWriter(int requestId,
                    int method,
                    Map<Integer, Object> headers,
                    DSByteBuffer body) {
        this.requestId = requestId;
        this.method = method;
        this.headers = headers;
        this.body = body;
        status = (Byte) headers.get(HDR_STATUS);
        if (status != null) {
            headers.put(HDR_STATUS, STS_OK);
        }
        page = -((body.length() / MAX_BODY) + 1);
    }

    // Methods
    // -------

    private void writeHeaders(DS2MessageWriter writer) {
        Object val;
        Map.Entry<Integer, Object> me;
        Iterator<Map.Entry<Integer, Object>> it = headers.entrySet().iterator();
        while (it.hasNext()) {
            me = it.next();
            val = me.getValue();
            if (val == NO_HEADER_VAL) {
                writer.addHeader(me.getKey());
            } else if (val instanceof Byte) {
                writer.addByteHeader(me.getKey(), (Byte) val);
            } else if (val instanceof Integer) {
                writer.addIntHeader(me.getKey(), (Integer) val);
            } else if (val instanceof String) {
                writer.addStringHeader(me.getKey(), (String) val);
            }
        }
    }

    /**
     * Call for each part, will return true when there are more part remain.
     */
    public boolean update(DS2MessageWriter writer, int ackId) {
        writer.init(requestId, ackId);
        writer.setMethod(method);
        headers.put(HDR_PAGE_ID, Integer.valueOf(page));
        int len = body.length();
        if (len < MessageConstants.MAX_BODY) {
            if (status != null) {
                headers.put(HDR_STATUS, status);
            }
        }
        writeHeaders(writer);
        len = Math.min(MAX_BODY, len);
        body.sendTo(writer.getBody(), len);
        if (page < 0) {
            page = 1;
        } else {
            page++;
        }
        return body.length() > 0;
    }

}
