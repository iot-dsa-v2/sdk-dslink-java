package com.acuity.iot.dsa.dslink.protocol.protocol_v1.responder;

import com.acuity.iot.dsa.dslink.protocol.protocol_v1.DS1Session;
import org.iot.dsa.dslink.DSLink;
import org.iot.dsa.dslink.responder.InboundRequest;
import org.iot.dsa.logging.DSLogger;
import org.iot.dsa.node.DSMap;

/**
 * Getters and setters common to most requests.
 *
 * @author Aaron Hansen
 */
class DS1InboundRequest extends DSLogger implements InboundRequest {

    ///////////////////////////////////////////////////////////////////////////
    // Fields
    ///////////////////////////////////////////////////////////////////////////

    private DSLink link;
    private String path;
    private DSMap request;
    private Integer requestId;
    private DS1Responder responder;
    private DS1Session session;

    ///////////////////////////////////////////////////////////////////////////
    // Constructors
    ///////////////////////////////////////////////////////////////////////////

    DS1InboundRequest() {
    }

    ///////////////////////////////////////////////////////////////////////////
    // Methods in alphabetical order
    ///////////////////////////////////////////////////////////////////////////

    public DSLink getLink() {
        return link;
    }

    public String getPath() {
        return path;
    }

    public DSMap getRequest() {
        return request;
    }

    public DS1Responder getResponder() {
        return responder;
    }

    public Integer getRequestId() {
        return requestId;
    }

    public DS1Session getSession() {
        return session;
    }

    public DS1InboundRequest setLink(DSLink link) {
        this.link = link;
        return this;
    }

    public DS1InboundRequest setPath(String path) {
        this.path = path;
        return this;
    }

    public DS1InboundRequest setSession(DS1Session session) {
        this.session = session;
        return this;
    }

    public DS1InboundRequest setRequest(DSMap request) {
        this.request = request;
        return this;
    }

    public DS1InboundRequest setRequestId(Integer requestId) {
        this.requestId = requestId;
        return this;
    }

    public DS1InboundRequest setResponder(DS1Responder responder) {
        this.responder = responder;
        return this;
    }

}
