package com.acuity.iot.dsa.dslink.protocol.responder;

import com.acuity.iot.dsa.dslink.DSSession;
import org.iot.dsa.dslink.DSLink;
import org.iot.dsa.dslink.responder.InboundRequest;
import org.iot.dsa.logging.DSLogger;

/**
 * Getters and setters common to most requests.
 *
 * @author Aaron Hansen
 */
public abstract class DSInboundRequest extends DSLogger implements InboundRequest {

    ///////////////////////////////////////////////////////////////////////////
    // Fields
    ///////////////////////////////////////////////////////////////////////////

    private DSLink link;
    private String path;
    private Integer requestId;
    private DSResponder responder;
    private DSSession session;

    ///////////////////////////////////////////////////////////////////////////
    // Methods in alphabetical order
    ///////////////////////////////////////////////////////////////////////////

    public DSLink getLink() {
        if (link == null) {
            link = responder.getLink();
        }
        return link;
    }

    @Override
    protected String getLogName() {
        return getClass().getSimpleName();
    }

    public String getPath() {
        return path;
    }

    public Integer getRequestId() {
        return requestId;
    }

    public DSResponder getResponder() {
        return responder;
    }

    public DSSession getSession() {
        return session;
    }

    public DSInboundRequest setLink(DSLink link) {
        this.link = link;
        return this;
    }

    public DSInboundRequest setPath(String path) {
        this.path = path;
        return this;
    }

    public DSInboundRequest setResponder(DSResponder responder) {
        this.responder = responder;
        return this;
    }

    public DSInboundRequest setSession(DSSession session) {
        this.session = session;
        return this;
    }

    public DSInboundRequest setRequestId(Integer requestId) {
        this.requestId = requestId;
        return this;
    }

}
