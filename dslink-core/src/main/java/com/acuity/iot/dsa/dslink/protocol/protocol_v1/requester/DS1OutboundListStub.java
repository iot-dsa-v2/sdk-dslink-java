package com.acuity.iot.dsa.dslink.protocol.protocol_v1.requester;

import org.iot.dsa.dslink.requester.OutboundListRequest;
import org.iot.dsa.dslink.requester.OutboundListStub;
import org.iot.dsa.io.DSIWriter;
import org.iot.dsa.node.DSElement;
import org.iot.dsa.node.DSMap;

/**
 * @author Daniel Shapiro, Aaron Hansen
 */
public class DS1OutboundListStub extends DS1OutboundRequestStub implements OutboundListStub {

    ///////////////////////////////////////////////////////////////////////////
    // Fields
    ///////////////////////////////////////////////////////////////////////////

    private boolean open = true;
    private OutboundListRequest request;

    ///////////////////////////////////////////////////////////////////////////
    // Constructors
    ///////////////////////////////////////////////////////////////////////////

    public DS1OutboundListStub(OutboundListRequest request) {
        this.request = request;
    }

    ///////////////////////////////////////////////////////////////////////////
    // Methods
    ///////////////////////////////////////////////////////////////////////////

    @Override
    public OutboundListRequest getRequest() {
        return request;
    }

    @Override
    protected void handleResponse(DSMap response) {
        try {
            request.onResponse(response);
        } catch (Exception x) {
            getRequester().severe(getRequester().getPath(), x);
        }
    }

    /*
    private void processListResponse(int rid, DSMap map, OutboundListStub req) {
        String stream = map.getString("stream");
        if (stream != null) {
            req.setLatestStreamState(StreamState.valueOf(stream.toUpperCase()));
        }
        StreamState streamState = req.getLatestStreamState();
        if (streamState.isClosed()) {
            requests.remove(rid);
        }
        DSList updates = map.getList("updates");
        DS1InboundListResponse response = new DS1InboundListResponse();
        response.setStreamState(streamState);
        response.setUpdates(updates);
        boolean keepOpen = req.onResponse(response);
        if (!streamState.isClosed() && !keepOpen) {
            sendClose(rid);
        }
    }
    */

    @Override
    public void write(DSIWriter out) {
        out.beginMap();
        out.key("rid").value(getRequestId());
        out.key("method").value("list");
        out.key("path").value(request.getPath());
        out.endMap();
    }

}
