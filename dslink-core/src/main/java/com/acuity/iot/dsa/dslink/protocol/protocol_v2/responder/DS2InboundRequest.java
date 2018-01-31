package com.acuity.iot.dsa.dslink.protocol.protocol_v2.responder;

import com.acuity.iot.dsa.dslink.protocol.protocol_v2.DS2Session;
import org.iot.dsa.dslink.DSLink;
import org.iot.dsa.dslink.responder.InboundRequest;
import org.iot.dsa.logging.DSLogger;
import org.iot.dsa.node.DSMap;

/**
 * Getters and setters common to most requests.
 *
 * @author Aaron Hansen
 */
class DS2InboundRequest extends DSLogger implements InboundRequest {

    ///////////////////////////////////////////////////////////////////////////
    // Fields
    ///////////////////////////////////////////////////////////////////////////

    private DSLink link;
    private String path;
    private DSMap request;
    private Integer requestId;
    private DS2Responder responder;
    private DS2Session session;

    ///////////////////////////////////////////////////////////////////////////
    // Constructors
    ///////////////////////////////////////////////////////////////////////////

    DS2InboundRequest() {
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

    public DS2Responder getResponder() {
        return responder;
    }

    public Integer getRequestId() {
        return requestId;
    }

    public DS2Session getSession() {
        return session;
    }

    public DS2InboundRequest setLink(DSLink link) {
        this.link = link;
        return this;
    }

    public DS2InboundRequest setPath(String path) {
        this.path = path;
        return this;
    }

    public DS2InboundRequest setSession(DS2Session session) {
        this.session = session;
        return this;
    }

    public DS2InboundRequest setRequest(DSMap request) {
        this.request = request;
        return this;
    }

    public DS2InboundRequest setRequestId(Integer requestId) {
        this.requestId = requestId;
        return this;
    }

    public DS2InboundRequest setResponder(DS2Responder responder) {
        this.responder = responder;
        return this;
    }

}
