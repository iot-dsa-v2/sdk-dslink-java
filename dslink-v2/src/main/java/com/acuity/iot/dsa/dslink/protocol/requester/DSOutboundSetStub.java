package com.acuity.iot.dsa.dslink.protocol.requester;

import com.acuity.iot.dsa.dslink.protocol.DSSession;
import com.acuity.iot.dsa.dslink.protocol.message.MessageWriter;
import org.iot.dsa.dslink.requester.OutboundRequestHandler;
import org.iot.dsa.io.DSIWriter;
import org.iot.dsa.node.DSIValue;
import org.iot.dsa.node.DSMap;

/**
 * Manages the lifecycle of a set request and is also the outbound stream passed to the requester.
 *
 * @author Daniel Shapiro, Aaron Hansen
 */
public class DSOutboundSetStub extends DSOutboundStub {

    private OutboundRequestHandler request;
    private DSIValue value;

    protected DSOutboundSetStub(DSRequester requester,
                                Integer requestId,
                                String path,
                                DSIValue value,
                                OutboundRequestHandler request) {
        super(requester, requestId, path);
        this.value = value;
        this.request = request;
    }

    public OutboundRequestHandler getHandler() {
        return request;
    }

    /**
     * Does nothing, there is no response.
     */
    @Override
    public void handleResponse(DSMap response) {
    }

    /**
     * Writes the v1 request.
     */
    @Override
    public boolean write(DSSession session, MessageWriter writer) {
        DSIWriter out = writer.getWriter();
        out.beginMap();
        out.key("rid").value(getRequestId());
        out.key("method").value("set");
        out.key("permit").value("config");
        out.key("path").value(getPath());
        out.key("value").value(value.toElement());
        out.endMap();
        return true;
    }

    protected DSIValue getValue() {
        return value;
    }

}
