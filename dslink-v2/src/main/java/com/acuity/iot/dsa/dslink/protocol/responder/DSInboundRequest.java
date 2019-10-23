package com.acuity.iot.dsa.dslink.protocol.responder;

import com.acuity.iot.dsa.dslink.protocol.DSSession;
import java.util.logging.Logger;
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

    public final DSLink getLink() {
        if (link == null) {
            link = responder.getLink();
        }
        return link;
    }

    @Override
    public Logger getLogger() {
        return getResponder().getLogger();
    }

    @Override
    public final String getPath() {
        return path;
    }

    @Override
    public final Integer getRequestId() {
        return requestId;
    }

    public final DSResponder getResponder() {
        return responder;
    }

    public final DSSession getSession() {
        return session;
    }

    public final DSInboundRequest setLink(DSLink link) {
        this.link = link;
        return this;
    }

    public final DSInboundRequest setPath(String path) {
        this.path = path;
        return this;
    }

    public final DSInboundRequest setRequestId(Integer requestId) {
        this.requestId = requestId;
        return this;
    }

    public final DSInboundRequest setResponder(DSResponder responder) {
        this.responder = responder;
        return this;
    }

    public final DSInboundRequest setSession(DSSession session) {
        this.session = session;
        return this;
    }

}
