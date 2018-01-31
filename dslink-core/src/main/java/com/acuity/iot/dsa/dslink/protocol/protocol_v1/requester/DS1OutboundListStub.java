package com.acuity.iot.dsa.dslink.protocol.protocol_v1.requester;

import com.acuity.iot.dsa.dslink.protocol.message.MessageWriter;
import org.iot.dsa.dslink.DSRequestException;
import org.iot.dsa.dslink.requester.OutboundListHandler;
import org.iot.dsa.io.DSIWriter;
import org.iot.dsa.node.DSElement;
import org.iot.dsa.node.DSList;
import org.iot.dsa.node.DSMap;

/**
 * Manages the lifecycle of an list request and is also the outbound stream passed to the
 * requester.
 *
 * @author Daniel Shapiro, Aaron Hansen
 */
class DS1OutboundListStub extends DS1OutboundStub {

    ///////////////////////////////////////////////////////////////////////////
    // Fields
    ///////////////////////////////////////////////////////////////////////////

    private OutboundListHandler request;

    ///////////////////////////////////////////////////////////////////////////
    // Constructors
    ///////////////////////////////////////////////////////////////////////////

    public DS1OutboundListStub(DS1Requester requester,
                               Integer requestId,
                               String path,
                               OutboundListHandler request) {
        super(requester, requestId, path);
        this.request = request;
    }

    ///////////////////////////////////////////////////////////////////////////
    // Methods
    ///////////////////////////////////////////////////////////////////////////

    @Override
    public OutboundListHandler getHandler() {
        return request;
    }

    @Override
    protected void handleResponse(DSMap response) {
        try {
            DSList updates = response.getList("updates");
            if (updates != null) {
                String name;
                DSList list;
                DSMap map;
                for (DSElement elem : updates) {
                    if (elem.isList()) {
                        list = (DSList) elem;
                        name = list.getString(0);
                        request.onUpdate(name, list.get(1));
                    } else if (elem.isMap()) {
                        map = (DSMap) elem;
                        name = map.getString("name");
                        String change = map.getString("change");
                        if ("remove".equals(change)) {
                            request.onRemove(name);
                        }
                    } else {
                        throw new DSRequestException(
                                "Unexpected list update entry: " + elem.toString());
                    }
                }
            }
            if ("open".equals(response.getString("stream"))) {
                request.onInitialized();
            }
        } catch (Exception x) {
            getRequester().severe(getRequester().getPath(), x);
        }
    }

    @Override
    public void write(MessageWriter writer) {
        DSIWriter out = writer.getWriter();
        out.beginMap();
        out.key("rid").value(getRequestId());
        out.key("method").value("list");
        out.key("path").value(getPath());
        out.endMap();
    }

}
