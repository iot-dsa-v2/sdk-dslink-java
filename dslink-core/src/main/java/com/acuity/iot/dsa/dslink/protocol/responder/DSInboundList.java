package com.acuity.iot.dsa.dslink.protocol.responder;

import com.acuity.iot.dsa.dslink.protocol.DSStream;
import com.acuity.iot.dsa.dslink.protocol.message.MessageWriter;
import com.acuity.iot.dsa.dslink.protocol.message.OutboundMessage;
import com.acuity.iot.dsa.dslink.protocol.message.RequestPath;
import java.util.Iterator;
import org.iot.dsa.DSRuntime;
import org.iot.dsa.dslink.DSIResponder;
import org.iot.dsa.dslink.responder.ApiObject;
import org.iot.dsa.dslink.responder.InboundListRequest;
import org.iot.dsa.dslink.responder.OutboundListResponse;
import org.iot.dsa.node.DSElement;
import org.iot.dsa.node.DSIEnum;
import org.iot.dsa.node.DSIValue;
import org.iot.dsa.node.DSInfo;
import org.iot.dsa.node.DSList;
import org.iot.dsa.node.DSMap;
import org.iot.dsa.node.DSMap.Entry;
import org.iot.dsa.node.DSMetadata;
import org.iot.dsa.node.DSNode;
import org.iot.dsa.node.DSPath;
import org.iot.dsa.node.DSValueType;
import org.iot.dsa.node.action.ActionSpec;
import org.iot.dsa.node.action.DSAction;
import org.iot.dsa.node.event.DSIEvent;
import org.iot.dsa.node.event.DSISubscriber;
import org.iot.dsa.node.event.DSInfoTopic;
import org.iot.dsa.node.event.DSTopic;

/**
 * List implementation for a responder.
 *
 * @author Aaron Hansen
 */
public class DSInboundList extends DSInboundRequest
        implements DSISubscriber, DSStream, InboundListRequest, OutboundMessage,
        OutboundListResponse, Runnable {

    ///////////////////////////////////////////////////////////////////////////
    // Constants
    ///////////////////////////////////////////////////////////////////////////

    private static final int STATE_INIT = 0;
    private static final int STATE_CHILDREN = 1;
    private static final int STATE_UPDATES = 2;
    private static final int STATE_CLOSE_PENDING = 3;
    private static final int STATE_CLOSED = 4;

    ///////////////////////////////////////////////////////////////////////////
    // Fields
    ///////////////////////////////////////////////////////////////////////////

    private StringBuilder cacheBuf = new StringBuilder();
    private DSMap cacheMap = new DSMap();
    private DSMetadata cacheMeta = new DSMetadata(cacheMap);
    private Iterator<ApiObject> children;
    private Exception closeReason;
    private DSInfo info;
    private DSNode node;
    private OutboundListResponse response;
    private boolean enqueued = false;
    private int state = STATE_INIT;
    private boolean stream = true;
    private Update updateHead;
    private Update updateTail;

    ///////////////////////////////////////////////////////////////////////////
    // Methods in alphabetical order
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Override point for v2.
     */
    protected void beginMessage(MessageWriter writer) {
        writer.getWriter().beginMap().key("rid").value(getRequestId());
    }

    /**
     * Override point for v2.
     */
    protected void beginUpdates(MessageWriter writer) {
        writer.getWriter().key("updates").beginList();
    }

    @Override
    public void childAdded(ApiObject child) {
        if (!isClosed()) {
            enqueue(new Update(child, true));
        }
    }

    @Override
    public void childRemoved(ApiObject child) {
        if (!isClosed()) {
            enqueue(new Update(child, false));
        }
    }

    @Override
    public void close() {
        if (!isOpen()) {
            return;
        }
        state = STATE_CLOSE_PENDING;
        enqueueResponse();
        fine(fine() ? getPath() + " list closed locally" : null);
    }

    @Override
    public void close(Exception reason) {
        if (!isOpen()) {
            return;
        }
        state = STATE_CLOSE_PENDING;
        closeReason = reason;
        enqueueResponse();
        fine(fine() ? getPath() + " list closed locally" : null);
    }

    /**
     * Remove an update from the queue.
     */
    private synchronized Update dequeue() {
        if (updateHead == null) {
            return null;
        }
        Update ret = null;
        ret = updateHead;
        if (updateHead == updateTail) {
            updateHead = null;
            updateTail = null;
        } else {
            updateHead = updateHead.next;
        }
        ret.next = null;
        return ret;
    }

    private void doClose() {
        state = STATE_CLOSED;
        getResponder().removeRequest(getRequestId());
        if (response == null) {
            return;
        }
        DSRuntime.run(new Runnable() {
            @Override
            public void run() {
                try {
                    response.onClose();
                } catch (Exception x) {
                    error(getPath(), x);
                }
            }
        });
    }

    /**
     * Override point for v2.
     */
    protected void encode(String key, DSElement value, MessageWriter writer) {
        writer.getWriter().beginList().value(key).value(value).endList();
    }

    /**
     * Override point for v2.
     */
    protected void encode(String key, String value, MessageWriter writer) {
        writer.getWriter().beginList().value(key).value(value).endList();
    }

    /**
     * Override point for v2.
     */
    protected void endMessage(MessageWriter writer, Boolean streamOpen) {
        if (streamOpen != null) {
            writer.getWriter().key("stream").value(streamOpen ? "open" : "closed");
        }
        writer.getWriter().endMap();
    }

    /**
     * Override point for v2.
     */
    protected void endUpdates(MessageWriter writer) {
        writer.getWriter().endList();
    }

    /**
     * Override point for v2.
     */
    protected String encodeName(String name) {
        cacheBuf.setLength(0);
        if (DSPath.encodeNameV1(name, cacheBuf)) {
            return cacheBuf.toString();
        }
        return name;
    }

    protected void encodeChild(ApiObject child, MessageWriter writer) {
        String name = child.getName();
        String safe = encodeName(name);
        DSMap map = new DSMap();
        child.getMetadata(cacheMap.clear());
        DSElement e = cacheMap.remove(DSMetadata.DISPLAY_NAME);
        if (e != null) {
            map.put("$name", e);
        } else if (!safe.equals(name)) {
            map.put("$name", name);
        }
        e = cacheMap.remove("$is");
        if (e != null) {
            map.put("$is", e);
        } else {
            map.put("$is", "node");
        }
        if (child.isAction()) {
            ActionSpec action = child.getAction();
            e = cacheMap.remove("$invokable");
            if (e != null) {
                map.put("$invokable", e);
            } else {
                map.put("$invokable", action.getPermission().toString());
            }
        } else if (child.isValue()) {
            e = cacheMap.remove("$type");
            if (e != null) {
                map.put("$type", e);
            } else {
                map.put("$type", encodeType(child.getValue(), cacheMeta));
            }
            if (!child.isReadOnly()) {
                e = cacheMap.remove("$writable");
                if (e != null) {
                    map.put("$writable", e);
                } else {
                    map.put("$writable", child.isAdmin() ? "config" : "write");
                }
            }
        } else if (child.isAdmin()) {
            e = cacheMap.remove("$permission");
            if (e != null) {
                map.put("$permission", e);
            } else {
                map.put("$permission", "config");
            }
        }
        encode(safe, map, writer);
        cacheMap.clear();
    }

    /**
     * Encode all the meta data about the root target of a list request.
     */
    private void encodeTarget(ApiObject object, MessageWriter writer) {
        if (object instanceof DSInfo) {
            DSMetadata.getMetadata((DSInfo) object, cacheMap.clear());
        } else {
            object.getMetadata(cacheMap.clear());
        }
        DSElement e = cacheMap.remove("$is");
        if (e == null) {
            encode("$is", "node", writer);
        } else {
            encode("$is", e, writer);

        }
        e = cacheMap.get("$name");
        if (e == null) {
            encode("$name", encodeName(object.getName()), writer);
        } else {
            encode("$name", e, writer);
        }
        if (object.isAction()) {
            encodeTargetAction(object, writer);
        } else if (object.isValue()) {
            encodeTargetValue(object, writer);
        } else if (object.isAdmin()) {
            e = cacheMap.remove("$permission");
            if (e == null) {
                encode("$permission", "config", writer);
            } else {
                encode("$permission", e, writer);
            }
        }
        encodeTargetMetadata(cacheMap, writer);
        cacheMap.clear();
    }

    /**
     * Called by encodeTarget for actions.
     */
    private void encodeTargetAction(ApiObject object, MessageWriter writer) {
        DSInfo info = null;
        if (object instanceof DSInfo) {
            info = (DSInfo) object;
        }
        ActionSpec action = object.getAction();
        DSAction dsAction = null;
        if (action instanceof DSAction) {
            dsAction = (DSAction) action;
        }
        DSElement e = cacheMap.remove("$invokable");
        if (e == null) {
            encode("$invokable", action.getPermission().toString(), writer);
        } else {
            encode("$invokable", e, writer);
        }
        e = cacheMap.remove("params");
        if (e == null) {
            DSList list = new DSList();
            Iterator<DSMap> params = action.getParameters();
            if (params != null) {
                DSMap param;
                while (params.hasNext()) {
                    param = params.next();
                    fixRange(param);
                    if (dsAction != null) {
                        dsAction.prepareParameter(info, param);
                    }
                    if (param.hasParent()) {
                        param = param.copy();
                    }
                    list.add(param);
                }
            }
            encode("$params", list, writer);
        } else {
            encode("$params", e, writer);
        }
        if (action.getResultType().isValues()) {
            e = cacheMap.remove("$columns");
            if (e == null) {
                DSList list = new DSList();
                Iterator<DSMap> cols = action.getValueResults();
                if (cols != null) {
                    DSMap param;
                    while (cols.hasNext()) {
                        param = cols.next();
                        fixRange(param);
                        if (dsAction != null) {
                            dsAction.prepareParameter(info, param);
                        }
                        if (param.hasParent()) {
                            param = param.copy();
                        }
                        list.add(param);
                    }
                }
                encode("$columns", list, writer);
            } else {
                encode("$columns", e, writer);
            }
        }
        e = cacheMap.remove("$result");
        if (e != null) {
            encode("$result", e, writer);
        } else if (!action.getResultType().isVoid()) {
            encode("$result", action.getResultType().toString(), writer);
        }
    }

    /**
     * Called by encodeTarget, encodes meta-data as configs.
     */
    private void encodeTargetMetadata(DSMap metadata, MessageWriter writer) {
        Entry entry;
        String name;
        for (int i = 0, len = metadata.size(); i < len; i++) {
            entry = metadata.getEntry(i);
            name = entry.getKey();
            switch (name.charAt(0)) {
                case '$':
                case '@':
                    break;
                default:
                    cacheBuf.setLength(0);
                    cacheBuf.append("$");
                    cacheBuf.append(encodeName(name));
                    name = cacheBuf.toString();

            }
            encode(name, entry.getValue(), writer);
        }
    }

    /**
     * Called by encodeTarget for values.
     */
    private void encodeTargetValue(ApiObject object, MessageWriter writer) {
        DSElement e = cacheMap.remove("$type");
        if (e != null) {
            encode("$type", e, writer);
        } else {
            encode("$type", encodeType(object.getValue(), cacheMeta), writer);
        }
        e = cacheMap.remove("$writable");
        if (e != null) {
            encode("$writable", e, writer);
        } else if (!object.isReadOnly()) {
            encode("$writable", object.isAdmin() ? "config" : "write", writer);
        }
    }

    private DSElement encodeType(DSIValue value, DSMetadata meta) {
        String type = meta.getType();
        if (getResponder().isV1()) {
            if ((type == null) && (value != null)) {
                meta.setType(value);
            }
        } else {
            if ((type == null) && (value != null)) {
                if (value instanceof DSIEnum) {
                    meta.getMap().put(DSMetadata.TYPE, DSValueType.STRING.toString());
                } else {
                    meta.setType(value);
                }
            }
        }
        fixRange(meta.getMap());
        DSElement e = cacheMap.remove(DSMetadata.TYPE);
        if (e == null) {
            throw new IllegalArgumentException("Missing type");
        }
        return e;
    }

    /**
     * Override point for v2.
     */
    protected void encodeUpdate(Update update, MessageWriter writer) {
        if (update.added) {
            encodeChild(update.child, writer);
        } else {
            writer.getWriter().beginMap()
                  .key("name").value(encodeName(update.child.getName()))
                  .key("change").value("remove")
                  .endMap();
        }
    }

    private void enqueue(Update update) {
        if (!isOpen()) {
            return;
        }
        synchronized (this) {
            if (updateHead == null) {
                updateHead = update;
                updateTail = update;
            } else {
                updateTail.next = update;
                updateTail = update;
            }
            if (enqueued) {
                return;
            }
        }
        getResponder().sendResponse(this);
    }

    /**
     * Enqueues in the session.
     */
    private void enqueueResponse() {
        synchronized (this) {
            if (enqueued) {
                return;
            }
            enqueued = true;
        }
        getResponder().sendResponse(this);
    }

    /**
     * Combines boolean and enum ranges into the type name.
     */
    private DSMap fixRange(DSMap arg) {
        String type = arg.getString(DSMetadata.TYPE);
        if ("bool".equals(type)) {
            DSList range = (DSList) arg.remove(DSMetadata.BOOLEAN_RANGE);
            if ((range == null) || (range.size() != 2)) {
                return arg;
            } else {
                cacheBuf.setLength(0);
                cacheBuf.append(type);
                cacheBuf.append('[');
                cacheBuf.append(range.get(0).toString());
                cacheBuf.append(',');
                cacheBuf.append(range.get(1).toString());
                cacheBuf.append(']');
                if (getResponder().isV1()) {
                    arg.put(DSMetadata.TYPE, cacheBuf.toString());
                } else {
                    arg.put(DSMetadata.EDITOR, cacheBuf.toString());
                }
            }
        } else if ("enum".equals(type) || "string".equals(type)) {
            DSList range = (DSList) arg.remove(DSMetadata.ENUM_RANGE);
            if (range == null) {
                return arg;
            }
            cacheBuf.setLength(0);
            cacheBuf.append("enum");
            cacheBuf.append('[');
            for (int i = 0, len = range.size(); i < len; i++) {
                if (i > 0) {
                    cacheBuf.append(',');
                }
                cacheBuf.append(range.get(i).toString());
            }
            cacheBuf.append(']');
            if (getResponder().isV1()) {
                arg.put(DSMetadata.TYPE, cacheBuf.toString());
            } else {
                arg.put(DSMetadata.EDITOR, cacheBuf.toString());
            }
        }
        return arg;
    }

    @Override
    public ApiObject getTarget() {
        return info;
    }

    private boolean isClosed() {
        return state == STATE_CLOSED;
    }

    private boolean isClosePending() {
        return state == STATE_CLOSE_PENDING;
    }

    /**
     * Not closed or closed pending.
     */
    @Override
    public boolean isOpen() {
        return (state != STATE_CLOSE_PENDING) && (state != STATE_CLOSED);
    }

    @Override
    public void onClose() {
        if (node != null) {
            node.unsubscribe(DSNode.INFO_TOPIC, null, this);
        }
    }

    @Override
    public void onEvent(DSTopic topic, DSIEvent event, DSNode node, DSInfo child,
                        Object... params) {
        switch ((DSInfoTopic.Event) event) {
            case CHILD_ADDED:
                childAdded(child);
                break;
            case CHILD_REMOVED:
                childRemoved(child);
                break;
            case METADATA_CHANGED: //TODO
                break;
            default:
        }
    }

    @Override
    public void onUnsubscribed(DSTopic topic, DSNode node, DSInfo child) {
        close();
    }

    @Override
    public void onClose(Integer requestId) {
        if (isClosed()) {
            return;
        }
        state = STATE_CLOSED;
        fine(debug() ? getPath() + " list closed" : null);
        synchronized (this) {
            updateHead = updateTail = null;
        }
        doClose();
    }

    @Override
    public void run() {
        try {
            RequestPath path = new RequestPath(getPath(), getLink());
            if (path.isResponder()) {
                DSIResponder responder = (DSIResponder) path.getTarget();
                setPath(path.getPath());
                response = responder.onList(this);
            } else {
                info = path.getInfo();
                if (info.isNode()) {
                    node = info.getNode();
                    node.subscribe(DSNode.INFO_TOPIC, null, this);
                }
                response = this;
            }
        } catch (Exception x) {
            error(getPath(), x);
            close(x);
            return;
        }
        if (response == null) {
            close();
        } else {
            enqueueResponse();
        }
    }

    /**
     * V2 only, set to false to auto close after sending the initial state.
     */
    public DSInboundList setStream(boolean stream) {
        this.stream = stream;
        return this;
    }

    @Override
    public void write(MessageWriter writer) {
        enqueued = false;
        if (isClosed()) {
            return;
        }
        if (isClosePending() && (updateHead == null) && (closeReason != null)) {
            getResponder().sendError(this, closeReason);
            doClose();
            return;
        }
        int last = state;
        beginMessage(writer);
        switch (state) {
            case STATE_INIT:
                beginUpdates(writer);
                writeInit(writer);
                break;
            case STATE_CHILDREN:
                beginUpdates(writer);
                writeChildren(writer);
                break;
            case STATE_CLOSE_PENDING:
            case STATE_UPDATES:
                beginUpdates(writer);
                writeUpdates(writer);
                break;
        }
        endUpdates(writer);
        if (state != last) {
            if (state == STATE_UPDATES) {
                if (stream) {
                    endMessage(writer, Boolean.TRUE);
                } else {
                    endMessage(writer, Boolean.FALSE);
                    doClose();
                }
            }
        } else if (isClosePending() && (updateHead == null)) {
            if (closeReason != null) {
                getResponder().sendError(this, closeReason);
            } else {
                endMessage(writer, Boolean.FALSE);
            }
            doClose();
        } else {
            endMessage(writer, null);
        }
    }

    private void writeChildren(MessageWriter writer) {
        if (children != null) {
            ApiObject child;
            while (children.hasNext()) {
                child = children.next();
                if (!child.isHidden()) {
                    encodeChild(child, writer);
                }
                if (getResponder().shouldEndMessage()) {
                    enqueueResponse();
                    return;
                }
            }
        }
        children = null;
        state = STATE_UPDATES;
    }

    private void writeInit(MessageWriter writer) {
        ApiObject target = response.getTarget();
        encodeTarget(target, writer);
        if (target.hasChildren()) {
            state = STATE_CHILDREN;
            children = target.getChildren();
            writeChildren(writer);
        } else {
            state = STATE_UPDATES;
            writeUpdates(writer);
        }
    }

    private void writeUpdates(MessageWriter writer) {
        DSResponder responder = getResponder();
        Update update;
        while (isOpen()) {
            update = dequeue();
            if (update == null) {
                break;
            }
            encodeUpdate(update, writer);
            if (responder.shouldEndMessage()) {
                enqueueResponse();
                break;
            }
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // Inner Classes
    ///////////////////////////////////////////////////////////////////////////

    protected static class Update {

        public boolean added;
        public ApiObject child;
        public Update next;

        Update(ApiObject child, boolean added) {
            this.child = child;
            this.added = added;
        }
    }

}
