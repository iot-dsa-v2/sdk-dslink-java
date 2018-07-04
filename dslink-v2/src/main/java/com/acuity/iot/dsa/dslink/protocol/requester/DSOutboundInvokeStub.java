package com.acuity.iot.dsa.dslink.protocol.requester;

import com.acuity.iot.dsa.dslink.protocol.message.MessageWriter;
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
public class DSOutboundInvokeStub extends DSOutboundStub {

    ///////////////////////////////////////////////////////////////////////////
    // Fields
    ///////////////////////////////////////////////////////////////////////////

    private OutboundInvokeHandler handler;
    private DSMap params;

    ///////////////////////////////////////////////////////////////////////////
    // Constructors
    ///////////////////////////////////////////////////////////////////////////

    protected DSOutboundInvokeStub(DSRequester requester,
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

    protected DSMap getParams() {
        return params;
    }

    /**
     * Writes the v1 response by default.  V2 probably only needs to add some headers.
     */
    @Override
    public void handleResponse(DSMap response) {
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
                meta = meta.getMap("meta");
                if ((meta != null) && (meta.size() > 0)) {
                    handler.onTableMeta(meta);
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
            getRequester().error(getRequester().getPath(), x);
        }
    }

    /**
     * Writes the v1 version by default.
     */
    @Override
    public void write(MessageWriter writer) {
        DSIWriter out = writer.getWriter();
        out.beginMap();
        out.key("rid").value(getRequestId());
        out.key("method").value("invoke");
        out.key("path").value(getPath());
        if (params == null) {
            params = new DSMap();
        }
        out.key("params").value(params);
        out.endMap();
    }

}
