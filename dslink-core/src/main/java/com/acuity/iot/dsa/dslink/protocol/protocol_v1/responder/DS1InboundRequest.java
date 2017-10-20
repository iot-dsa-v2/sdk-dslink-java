package com.acuity.iot.dsa.dslink.protocol.protocol_v1.responder;

import com.acuity.iot.dsa.dslink.protocol.protocol_v1.DS1Session;
import org.iot.dsa.dslink.DSIResponder;
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
    // Constants
    ///////////////////////////////////////////////////////////////////////////

    ///////////////////////////////////////////////////////////////////////////
    // Fields
    ///////////////////////////////////////////////////////////////////////////

    private String path;
    private DS1Session protocol;
    private DSMap request;
    private Integer requestId;
    private DSIResponder responder;
    private DS1Responder session;

    ///////////////////////////////////////////////////////////////////////////
    // Constructors
    ///////////////////////////////////////////////////////////////////////////

    DS1InboundRequest() {
    }

    ///////////////////////////////////////////////////////////////////////////
    // Methods in alphabetical order
    ///////////////////////////////////////////////////////////////////////////

    public String getPath() {
        return path;
    }

    public DS1Session getProtocol() {
        return protocol;
    }

    public DSMap getRequest() {
        return request;
    }

    public DSIResponder getResponder() {
        return responder;
    }

    public Integer getRequestId() {
        return requestId;
    }

    public DS1Responder getSession() {
        return session;
    }

    public DS1InboundRequest setPath(String path) {
        this.path = path;
        return this;
    }

    public DS1InboundRequest setProtocol(DS1Session protocol) {
        this.protocol = protocol;
        return this;
    }

    public DS1InboundRequest setRequest(DSMap request) {
        this.request = request;
        return this;
    }

    public DS1InboundRequest setResponder(DSIResponder responder) {
        this.responder = responder;
        return this;
    }

    public DS1InboundRequest setRequestId(Integer requestId) {
        this.requestId = requestId;
        return this;
    }

    public DS1InboundRequest setSession(DS1Responder session) {
        this.session = session;
        return this;
    }

    ///////////////////////////////////////////////////////////////////////////
    // Inner Classes
    ///////////////////////////////////////////////////////////////////////////

    ///////////////////////////////////////////////////////////////////////////
    // Initialization
    ///////////////////////////////////////////////////////////////////////////

}
