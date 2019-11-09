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
import org.iot.dsa.time.DSDateTime;

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

    private HandlerAdapter adapter = new HandlerAdapter();
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
        adapter.add(handler);
    }

    ///////////////////////////////////////////////////////////////////////////
    // Public Methods
    ///////////////////////////////////////////////////////////////////////////

    public void addHandler(OutboundListHandler handler) {
        adapter.add(handler);
    }

    @Override
    public OutboundListHandler getHandler() {
        return adapter;
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
            boolean disconnected = false;
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
                            initialized = false;
                            state.clear();
                        }
                        if (name.equals("$disconnectedTs")) {
                            disconnected = true;
                        }
                        DSElement e = list.remove(1);
                        state.put(name, e);
                        adapter.onUpdate(name, e);
                    } else if (elem.isMap()) {
                        map = (DSMap) elem;
                        name = DSPath.decodeName(map.getString("name"));
                        String change = map.getString("change");
                        if ("remove".equals(change)) {
                            state.remove(name);
                            adapter.onRemove(name);
                        }
                    } else {
                        throw new DSRequestException(
                                "Unexpected list update entry: " + elem.toString());
                    }
                }
            }
            if (disconnected || "open".equals(response.getString("stream"))) {
                initialized = true;
                adapter.onInitialized();
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
    // Package Methods
    ///////////////////////////////////////////////////////////////////////////

    synchronized void disconnected() {
        initialized = false;
        String name = "$disconnectedTs";
        DSElement value = DSDateTime.now().toElement();
        state.put(name, value);
        try {
            adapter.onUpdate(name, value);
        } catch (Exception x) {
            getRequester().error(getRequester().getPath(), x);
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // Inner Classes
    ///////////////////////////////////////////////////////////////////////////

    public interface HandlerAdapterStream extends OutboundStream {

        DSMap getState();

    }

    class HandlerAdapter implements OutboundListHandler {

        private ConcurrentLinkedQueue<OutboundListHandler> handlers = new ConcurrentLinkedQueue<>();

        public HandlerAdapter() {
        }

        public void add(final OutboundListHandler handler) {
            handlers.add(handler);
            handler.onInit(getPath(), null, new HandlerAdapterStream() {
                boolean open = true;

                @Override
                public void closeStream() {
                    open = false;
                    remove(handler);
                }

                @Override
                public DSMap getState() {
                    return state;
                }

                @Override
                public boolean isStreamOpen() {
                    return open;
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
        public DSIValue getParameters() {
            return null;
        }

        @Override
        public String getPath() {
            return DSOutboundListStub.this.getPath();
        }

        @Override
        public OutboundStream getStream() {
            throw new IllegalStateException("This should not have been called");
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
