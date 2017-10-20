package com.acuity.iot.dsa.dslink.protocol.protocol_v1.requester;

import org.iot.dsa.dslink.requester.OutboundInvokeRequest;
import org.iot.dsa.dslink.requester.OutboundInvokeStub;
import org.iot.dsa.io.DSIWriter;
import org.iot.dsa.node.DSMap;
import org.iot.dsa.security.DSPermission;

/**
 * @author Daniel Shapiro, Aaron Hansen
 */
public class DS1OutboundInvokeStub extends DS1OutboundRequestStub implements OutboundInvokeStub {

    ///////////////////////////////////////////////////////////////////////////
    // Fields
    ///////////////////////////////////////////////////////////////////////////

    private boolean open = true;
    private OutboundInvokeRequest request;

    ///////////////////////////////////////////////////////////////////////////
    // Constructors
    ///////////////////////////////////////////////////////////////////////////

    public DS1OutboundInvokeStub(OutboundInvokeRequest request) {
        this.request = request;
    }

    ///////////////////////////////////////////////////////////////////////////
    // Methods
    ///////////////////////////////////////////////////////////////////////////

    @Override
    public OutboundInvokeRequest getRequest() {
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
    private void processInvokeResponse(int rid, DSMap map, OutboundInvokeRequest req) {
       String stream = map.getString("stream");
       if (stream != null) {
           req.setLatestStreamState(StreamState.valueOf(stream.toUpperCase()));
       }
       StreamState streamState = req.getLatestStreamState();
       if (streamState.isClosed()) {
           requests.remove(rid);
       }
       DSList columns = map.getList("columns");
       DSList updates = map.getList("updates");
       DSMap meta = map.getMap("meta");
       DS1InboundInvokeResponse response = new DS1InboundInvokeResponse();
       response.setStreamState(streamState);
       response.setMetadata(meta);
       if (columns != null) {
           for (int i = 0; i < columns.size(); i++) {
               response.addColumn(columns.getMap(i));
           }
       }
       if (updates != null) {
           for (int i = 0; i < updates.size(); i++) {
               DSList row = updates.getList(i);
               response.addRow(row);
           }
       }
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
        out.key("method").value("invoke");
        DSPermission permit = request.getPermission();
        if (permit != null) {
            out.key("permit").value(permit.toString());
        }
        out.key("path").value(request.getPath());
        DSMap params = request.getParameters();
        if (params == null) {
            params = new DSMap();
        }
        out.key("params").value(params);
        out.endMap();
    }

}
