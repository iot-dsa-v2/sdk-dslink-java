package com.acuity.iot.dsa.dslink.protocol.requester;

import com.acuity.iot.dsa.dslink.protocol.DSSession;
import com.acuity.iot.dsa.dslink.protocol.message.MessageWriter;
import org.iot.dsa.dslink.DSRequestException;
import org.iot.dsa.dslink.requester.OutboundListHandler;
import org.iot.dsa.io.DSIWriter;
import org.iot.dsa.node.DSElement;
import org.iot.dsa.node.DSList;
import org.iot.dsa.node.DSMap;
import org.iot.dsa.node.DSPath;

/**
 * Manages the lifecycle of an list request and is also the outbound stream passed to the
 * requester.
 *
 * @author Daniel Shapiro, Aaron Hansen
 */
public class DSOutboundListStub extends DSOutboundStub {

    private OutboundListHandler request;

    protected DSOutboundListStub(DSRequester requester,
                                 Integer requestId,
                                 String path,
                                 OutboundListHandler request) {
        super(requester, requestId, path);
        this.request = request;
    }

    @Override
    public OutboundListHandler getHandler() {
        return request;
    }

    /**
     * Reads the v1 response.
     */
    @Override
    public void handleResponse(DSMap response) {
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
                        request.onUpdate(DSPath.decodeName(name), list.get(1));
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
            getRequester().error(getRequester().getPath(), x);
        }
    }

    /**
     * Writes the v1 request.
     */
    @Override
    public boolean write(DSSession session, MessageWriter writer) {
        DSIWriter out = writer.getWriter();
        out.beginMap();
        out.key("rid").value(getRequestId());
        out.key("method").value("list");
        out.key("path").value(getPath());
        out.endMap();
        return true;
    }

}
