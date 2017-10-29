package com.acuity.iot.dsa.dslink.protocol.protocol_v1.requester;

import org.iot.dsa.dslink.requester.OutboundInvokeHandler;
import org.iot.dsa.dslink.requester.OutboundInvokeHandler.Mode;
import org.iot.dsa.io.DSIWriter;
import org.iot.dsa.node.DSList;
import org.iot.dsa.node.DSMap;

/**
 * Manages the lifecycle of an invoke request and is also the outbound stream passed to the
 * requester.
 *
 * @author Daniel Shapiro, Aaron Hansen
 */
class DS1OutboundInvokeStub extends DS1OutboundStub {

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
            DSList updates = response.getList("updates");
            DSMap meta = response.getMap("meta");
            if (meta != null) {
                String mode = meta.getString("mode");
                if (mode != null) {
                    if ("refresh".equals(mode)) {
                        handler.onMode(Mode.REFRESH);
                    } else if ("append".equals(mode)) {
                        handler.onMode(Mode.APPEND);
                    } else if ("stream".equals(mode)) {
                        handler.onMode(Mode.STREAM);
                    }
                }
            }
            DSList columns = response.getList("columns");
            if (columns != null) {
                handler.onColumns(columns);
            }
            if (meta != null) {
                String modify = meta.get("modify", "");
                if (modify != null) {
                    String[] parts = toString().split(" ");
                    if (modify.startsWith("replace")) {
                        parts = parts[1].split("-");
                        int start = Integer.parseInt(parts[0]);
                        int end = Integer.parseInt(parts[1]);
                        handler.onReplace(start, end, updates);
                        return;
                    } else if (modify.startsWith("insert")) {
                        int idx = Integer.parseInt(parts[1]);
                        handler.onInsert(idx, updates);
                        return;
                    }
                }
            }
            if (updates == null) {
                return;
            }
            for (int i = 0, len = updates.size(); i < len; i++) {
                handler.onUpdate(updates.get(i).toList());
            }
        } catch (Exception x) {
            getRequester().severe(getRequester().getPath(), x);
        }
    }

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
