package com.acuity.iot.dsa.dslink.protocol.message;

import com.acuity.iot.dsa.dslink.protocol.DSSession;
import org.iot.dsa.io.DSIWriter;
import org.iot.dsa.node.DSMap;

/**
 * Just some getters and setters common to many message.
 *
 * @author Aaron Hansen
 */
public class AbstractMessage implements OutboundMessage {

    /////////////////////////////////////////////////////////////////
    // Fields
    /////////////////////////////////////////////////////////////////

    private String method;
    private String path;
    private Integer rid;
    private String stream;

    /////////////////////////////////////////////////////////////////
    // Constructors
    /////////////////////////////////////////////////////////////////

    public AbstractMessage() {
    }

    public AbstractMessage(Integer requestId) {
        this.rid = requestId;
    }

    /////////////////////////////////////////////////////////////////
    // Public Methods
    /////////////////////////////////////////////////////////////////

    /**
     * Returns true.
     */
    @Override
    public boolean canWrite(DSSession session) {
        return true;
    }

    public String getMethod() {
        return method;
    }

    public String getPath() {
        return path;
    }

    public Integer getRequestId() {
        return rid;
    }

    public String getStream() {
        return stream;
    }

    /**
     * Looks for the path, method and request ID.
     */
    public AbstractMessage parseRequest(DSMap map) {
        setPath(map.get("path", null));
        setMethod(map.get("method", null));
        setRequestId(map.get("rid", -1));
        return this;
    }

    /**
     * Optional, null by default.
     */
    public AbstractMessage setMethod(String arg) {
        method = arg;
        return this;
    }

    /**
     * Optional, null by default.
     */
    public AbstractMessage setPath(String arg) {
        path = arg;
        return this;
    }

    /**
     * Optional, null by default.
     */
    public OutboundMessage setRequestId(Integer rid) {
        this.rid = rid;
        return this;
    }

    /**
     * Optional, null by default.
     */
    public AbstractMessage setStream(String arg) {
        stream = arg;
        return this;
    }

    /**
     * Subclasses should call this super implementation first. This starts the response map and will
     * write the key value pairs that have been configured on this object, but does not close the
     * response map.
     */
    @Override
    public void write(DSSession session, MessageWriter out) {
        DSIWriter writer = out.getWriter();
        writer.beginMap();
        if (rid >= 0) {
            writer.key("rid").value(rid);
        }
        if (method != null) {
            writer.key("method").value(method);
        }
        if (path != null) {
            writer.key("path").value(path);
        }
        if (stream != null) {
            writer.key("stream").value(stream);
        }
    }

}
