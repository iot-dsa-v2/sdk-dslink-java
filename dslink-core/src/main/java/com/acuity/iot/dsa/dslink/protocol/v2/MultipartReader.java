package com.acuity.iot.dsa.dslink.protocol.v2;

import com.acuity.iot.dsa.dslink.io.DSByteBuffer;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Used to combine multiple inbound messages into one when the body of the entire message exceeds
 * the max possible size.
 *
 * @author Aaron Hansen
 */
public class MultipartReader implements MessageConstants {

    // Fields
    // ------

    private DSByteBuffer body = new DSByteBuffer();
    private int currentPage = 0;
    private Map<Integer, Object> headers = new HashMap<Integer, Object>();
    private int lastPage;
    private int method;
    private int requestId = -1;
    private Byte status;

    // Constructors
    // ------------

    MultipartReader(DS2MessageReader reader) {
        this.requestId = reader.getRequestId();
        this.method = reader.getMethod();
        this.headers.putAll(reader.getHeaders());
        int page = (Integer) headers.get(HDR_PAGE_ID);
        if (page >= 0) {
            throw new IllegalArgumentException("Invalid page id: " + page);
        }
        page = -page;
        lastPage = page - 1;
    }

    // Methods
    // -------

    /**
     * Call this once update returns false.
     */
    public DS2MessageReader makeReader() {
        DS2MessageReader reader = new DS2MessageReader();
        reader.init(requestId, method, body, headers);
        return reader;
    }

    private MultipartReader putBody(InputStream in, int len) {
        body.put(in, len);
        return this;
    }

    private MultipartReader putHeaders(Map<Integer, Object> headers) {
        this.headers.putAll(headers);
        status = (Byte) this.headers.get(HDR_STATUS);
        return this;
    }

    /**
     * Call for each part, will return true when there are more parts expected.
     */
    public boolean update(DS2MessageReader reader) {
        int page = (Integer) headers.get(HDR_PAGE_ID);
        if (page < currentPage) {
            throw new IllegalStateException("Out of order page");
        }
        currentPage = page;
        putHeaders(reader.getHeaders());
        putBody(reader.getBody(), reader.getBodyLength());
        return currentPage < lastPage;
    }

}
