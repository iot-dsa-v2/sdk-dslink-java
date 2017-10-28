package com.acuity.iot.dsa.dslink.protocol.protocol_v1.requester;

import org.iot.dsa.dslink.requester.OutboundInvokeHandler;
import org.iot.dsa.io.DSIWriter;
import org.iot.dsa.node.DSMap;

/**
 * Manages the lifecycle of an invoke request and is also the close stub passed to the requester.
 *
 * @author Daniel Shapiro, Aaron Hansen
 */
class DS1OutboundInvokeStub extends DS1OutboundStream {

    ///////////////////////////////////////////////////////////////////////////
    // Fields
    ///////////////////////////////////////////////////////////////////////////

    private OutboundInvokeHandler handler;
    private DSMap params;

    ///////////////////////////////////////////////////////////////////////////
    // Constructors
    ///////////////////////////////////////////////////////////////////////////

    public DS1OutboundInvokeStub(DS1Requester requester,
                                 Integer requestId,
                                 String path,
                                 DSMap params,
                                 OutboundInvokeHandler handler) {
        super(requester, requestId, path);
        this.params = params;
        this.handler = handler;
    }

    ///////////////////////////////////////////////////////////////////////////
    // Methods
    ///////////////////////////////////////////////////////////////////////////

    @Override
    public OutboundInvokeHandler getHandler() {
        return handler;
    }

    @Override
    protected void handleResponse(DSMap response) {
        try {
            //handler.onResponse(response);
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
        out.key("permit").value("config");
        out.key("path").value(getPath());
        if (params == null) {
            params = new DSMap();
        }
        out.key("params").value(params);
        out.endMap();
    }

}
