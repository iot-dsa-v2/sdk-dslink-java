package com.acuity.iot.dsa.dslink.protocol.message;

import org.iot.dsa.io.DSWriter;
import org.iot.dsa.node.DSMap;

/**
 * Just some getters and setters common to many message.
 *
 * @author Aaron Hansen
 */
public class BaseMessage implements OutboundMessage {

    /////////////////////////////////////////////////////////////////
    // Fields
    /////////////////////////////////////////////////////////////////

    private String path;
    private String method;
    private Integer rid;
    private String stream;

    /////////////////////////////////////////////////////////////////
    // Constructors
    /////////////////////////////////////////////////////////////////

    public BaseMessage() {
    }

    public BaseMessage(Integer requestId) {
        this.rid = requestId;
    }

    /////////////////////////////////////////////////////////////////
    // Methods - In alphabetical order by method name.
    /////////////////////////////////////////////////////////////////

    public String getPath() {
        return path;
    }

    public String getMethod() {
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
    public BaseMessage parseRequest(DSMap map) {
        setPath(map.get("path", null));
        setMethod(map.get("method", null));
        setRequestId(map.get("rid", -1));
        return this;
    }

    /**
     * Optional, null by default.
     */
    public BaseMessage setPath(String arg) {
        path = arg;
        return this;
    }

    /**
     * Optional, null by default.
     */
    public BaseMessage setMethod(String arg) {
        method = arg;
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
    public BaseMessage setStream(String arg) {
        stream = arg;
        return this;
    }

    /**
     * Subclasses should call this super implementation first. This starts the response map and
     * will write the key value pairs that have been configured on this object, but does not
     * close the response map.
     */
    public void write(DSWriter writer) {
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

    /////////////////////////////////////////////////////////////////
    // Methods - Protected and in alphabetical order by method name.
    /////////////////////////////////////////////////////////////////

    /////////////////////////////////////////////////////////////////
    // Methods - Package and in alphabetical order by method name.
    /////////////////////////////////////////////////////////////////

    /////////////////////////////////////////////////////////////////
    // Methods - Private and in alphabetical order by method name.
    /////////////////////////////////////////////////////////////////

    /////////////////////////////////////////////////////////////////
    // Inner Classes - in alphabetical order by class name.
    /////////////////////////////////////////////////////////////////

    /////////////////////////////////////////////////////////////////
    // Facets - in alphabetical order by field name.
    /////////////////////////////////////////////////////////////////

    /////////////////////////////////////////////////////////////////
    // Initialization
    /////////////////////////////////////////////////////////////////

}
