package com.acuity.iot.dsa.dslink.protocol.requester;

import com.acuity.iot.dsa.dslink.protocol.DSSession;
import com.acuity.iot.dsa.dslink.protocol.message.MessageWriter;
import java.util.concurrent.ConcurrentLinkedQueue;
import org.iot.dsa.dslink.DSRequestException;
import org.iot.dsa.dslink.requester.ErrorType;
import org.iot.dsa.dslink.requester.OutboundListHandler;
import org.iot.dsa.dslink.requester.OutboundStream;
import org.iot.dsa.io.DSIWriter;
import org.iot.dsa.node.DSElement;
import org.iot.dsa.node.DSIValue;
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

    ///////////////////////////////////////////////////////////////////////////
    // Instance Fields
    ///////////////////////////////////////////////////////////////////////////

    private OutboundListHandler handler;
    private boolean initialized = false;
    private DSMap state = new DSMap();

    ///////////////////////////////////////////////////////////////////////////
    // Constructors
    ///////////////////////////////////////////////////////////////////////////

    protected DSOutboundListStub(DSRequester requester,
                                 Integer requestId,
                                 String path,
                                 OutboundListHandler handler) {
        super(requester, requestId, path);
        this.handler = handler;
        handler.onInit(path, null, this);
    }

    ///////////////////////////////////////////////////////////////////////////
    // Public Methods
    ///////////////////////////////////////////////////////////////////////////

    public synchronized void addHandler(OutboundListHandler handler) {
        HandlerAdapter adapter;
        if (this.handler instanceof HandlerAdapter) {
            adapter = (HandlerAdapter) this.handler;
        } else {
            adapter = new HandlerAdapter(this.handler);
            this.handler = adapter;
        }
        adapter.add(handler);
    }

    @Override
    public OutboundListHandler getHandler() {
        return handler;
    }

    public DSMap getState() {
        return state;
    }

    /**
     * Reads the v1 response.
     */
    @Override
    public synchronized void handleResponse(DSMap response) {
        try {
            DSList updates = response.getList("updates");
            if (updates != null) {
                String name;
                DSList list;
                DSMap map;
                for (DSElement elem : updates) {
                    if (elem.isList()) {
                        list = (DSList) elem;
                        name = DSPath.decodeName(list.getString(0));
                        if (name.equals("$is")) {
                            state.clear();
                        }
                        DSElement e = list.remove(1);
                        state.put(name, e);
                        handler.onUpdate(name, e);
                    } else if (elem.isMap()) {
                        map = (DSMap) elem;
                        name = DSPath.decodeName(map.getString("name"));
                        String change = map.getString("change");
                        if ("remove".equals(change)) {
                            state.remove(name);
                            handler.onRemove(name);
                        }
                    } else {
                        throw new DSRequestException(
                                "Unexpected list update entry: " + elem.toString());
                    }
                }
            }
            if ("open".equals(response.getString("stream"))) {
                initialized = true;
                handler.onInitialized();
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

    ///////////////////////////////////////////////////////////////////////////
    // Inner Classes
    ///////////////////////////////////////////////////////////////////////////

    class HandlerAdapter implements OutboundListHandler {

        private ConcurrentLinkedQueue<OutboundListHandler> handlers = new ConcurrentLinkedQueue<>();

        public HandlerAdapter(OutboundListHandler first) {
            handlers.add(first);
        }

        public void add(final OutboundListHandler handler) {
            handlers.add(handler);
            handler.onInit(getPath(), null, new OutboundStream() {
                @Override
                public void closeStream() {
                    remove(handler);
                }

                @Override
                public boolean isStreamOpen() {
                    return DSOutboundListStub.this.isStreamOpen();
                }
            });
            for (DSMap.Entry e : state) {
                handler.onUpdate(e.getKey(), e.getValue());
            }
            if (initialized) {
                handler.onInitialized();
            }
        }

        @Override
        public void onClose() {
            for (OutboundListHandler h : handlers) {
                try {
                    h.onClose();
                } catch (Exception x) {
                    getRequester().error(h.toString(), x);
                }
            }
            handlers.clear();
        }

        @Override
        public void onError(ErrorType type, String msg) {
            for (OutboundListHandler h : handlers) {
                try {
                    h.onError(type, msg);
                } catch (Exception x) {
                    getRequester().error(h.toString(), x);
                }
            }
        }

        @Override
        public void onInit(String path, DSIValue params, OutboundStream stream) {
            throw new IllegalStateException("This should not have been called");
        }

        @Override
        public void onInitialized() {
            for (OutboundListHandler h : handlers) {
                try {
                    h.onInitialized();
                } catch (Exception x) {
                    getRequester().error(h.toString(), x);
                }
            }
        }

        @Override
        public void onRemove(String name) {
            for (OutboundListHandler h : handlers) {
                try {
                    h.onRemove(name);
                } catch (Exception x) {
                    getRequester().error(h.toString(), x);
                }
            }
        }

        @Override
        public void onUpdate(String name, DSElement value) {
            for (OutboundListHandler h : handlers) {
                try {
                    h.onUpdate(name, value);
                } catch (Exception x) {
                    getRequester().error(h.toString(), x);
                }
            }
        }

        private void remove(OutboundListHandler handler) {
            handlers.remove(handler);
            try {
                handler.onClose();
            } catch (Exception x) {
                getRequester().error(handler.toString(), x);
            }
            if (handlers.isEmpty()) {
                DSOutboundListStub.this.closeStream();
            }
        }
    }

}
