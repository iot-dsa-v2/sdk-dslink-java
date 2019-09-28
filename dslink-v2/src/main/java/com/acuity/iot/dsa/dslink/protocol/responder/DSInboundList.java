package com.acuity.iot.dsa.dslink.protocol.responder;

import com.acuity.iot.dsa.dslink.protocol.DSSession;
import com.acuity.iot.dsa.dslink.protocol.DSStream;
import com.acuity.iot.dsa.dslink.protocol.message.DSTarget;
import com.acuity.iot.dsa.dslink.protocol.message.MessageWriter;
import com.acuity.iot.dsa.dslink.protocol.message.OutboundMessage;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import org.iot.dsa.DSRuntime;
import org.iot.dsa.dslink.Action;
import org.iot.dsa.dslink.DSIResponder;
import org.iot.dsa.dslink.Node;
import org.iot.dsa.dslink.Value;
import org.iot.dsa.dslink.responder.InboundListRequest;
import org.iot.dsa.dslink.responder.OutboundListResponse;
import org.iot.dsa.node.DSElement;
import org.iot.dsa.node.DSIEnum;
import org.iot.dsa.node.DSIMetadata;
import org.iot.dsa.node.DSIValue;
import org.iot.dsa.node.DSInfo;
import org.iot.dsa.node.DSList;
import org.iot.dsa.node.DSMap;
import org.iot.dsa.node.DSMap.Entry;
import org.iot.dsa.node.DSMetadata;
import org.iot.dsa.node.DSNode;
import org.iot.dsa.node.DSPath;
import org.iot.dsa.node.DSString;
import org.iot.dsa.node.event.DSEvent;
import org.iot.dsa.node.event.DSISubscriber;
import org.iot.dsa.node.event.DSISubscription;
import org.iot.dsa.time.DSDateTime;
import org.iot.dsa.util.DSException;

/**
 * List implementation for a responder.
 *
 * @author Aaron Hansen
 */
public class DSInboundList extends DSInboundRequest
        implements DSISubscriber, DSStream, InboundListRequest, OutboundMessage,
        OutboundListResponse, Runnable {

    ///////////////////////////////////////////////////////////////////////////
    // Instance Fields
    ///////////////////////////////////////////////////////////////////////////

    private static final int STATE_INIT = 0;
    private static final int STATE_UPDATES = 1;
    private static final int STATE_CLOSE_PENDING = 2;
    private static final int STATE_CLOSED = 3;

    ///////////////////////////////////////////////////////////////////////////
    // Class Fields
    ///////////////////////////////////////////////////////////////////////////

    private StringBuilder cacheBuf = new StringBuilder();
    private DSMap cacheMap = new DSMap();
    private DSMetadata cacheMeta = new DSMetadata(cacheMap);
    private Exception closeReason;
    private boolean enqueued = false;
    private OutboundListResponse response;
    private boolean sendOpen = false;
    private int state = STATE_INIT;
    private DSISubscription subscription;
    private DSTarget target;
    private List<DSElement> updates = new LinkedList<>();

    ///////////////////////////////////////////////////////////////////////////
    // Public Methods
    ///////////////////////////////////////////////////////////////////////////

    @Override
    public boolean canWrite(DSSession session) {
        return true;
    }

    @Override
    public void close() {
        if (!isOpen()) {
            return;
        }
        state = STATE_CLOSE_PENDING;
        enqueueResponse();
    }

    @Override
    public void close(Exception reason) {
        if (!isOpen()) {
            return;
        }
        closeReason = reason;
        close();
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
        if (subscription != null) {
            subscription.close();
            subscription = null;
        }
    }

    @Override
    public void onClose(Integer requestId) {
        if (isClosed()) {
            return;
        }
        state = STATE_CLOSED;
        doClose();
    }

    @Override
    public void onClosed(DSISubscription subscription) {
        this.subscription = null;
        close();
    }

    @Override
    public void onEvent(DSEvent event, DSNode node, DSInfo child, DSIValue data) {
        if (!isOpen()) {
            return;
        }
        switch (event.getEventId()) {
            case DSNode.CHILD_RENAMED:
                sendRemove(data.toString());
            case DSNode.CHILD_ADDED:
                encodeChild(child);
                break;
            case DSNode.CHILD_REMOVED:
                sendRemove(child.getName());
                break;
        }
    }

    @Override
    public void openStream() {
        sendOpen = true;
    }

    @Override
    public void run() {
        try {
            target = new DSTarget(getPath(), getLink().getRootNode());
            if (target.isResponder()) {
                DSIResponder responder = (DSIResponder) target.getTarget();
                setPath(target.getPath());
                response = responder.onList(this);
                state = STATE_UPDATES;
            } else {
                response = this;
                encodeTarget(target.getTargetInfo());
                encodeChildren(target.getTargetInfo());
                subscription = target.getNode().subscribe(this);
                openStream();
            }
        } catch (Exception x) {
            sendMetadata("$disconnectedTs", DSDateTime.now().toElement());
            error(getPath(), x);
            close(x);
            return;
        }
    }

    @Override
    public void sendAction(String name, String displayName, boolean admin, boolean readonly) {
        if (!isOpen()) {
            throw new IllegalStateException("List stream closed");
        }
        DSMap map = new DSMap();
        map.put("$is", "node");
        if ((displayName != null) && !displayName.isEmpty()) {
            map.put("$name", displayName);
        }
        if (admin) {
            map.put("$invokable", "config");
        } else if (!readonly) {
            map.put("$invokable", "write");
        } else {
            map.put("$invokable", "read");
        }
        enqueue(encodeName(name, cacheBuf), map);
    }

    @Override
    public void sendMetadata(String name, DSElement value) {
        if (!isOpen()) {
            throw new IllegalStateException("List stream closed");
        }
        enqueue(encodeName(name, cacheBuf), value);
    }

    @Override
    public void sendNode(String name, String displayName, boolean admin) {
        if (!isOpen()) {
            throw new IllegalStateException("List stream closed");
        }
        DSMap map = new DSMap();
        map.put("$is", "node");
        if ((displayName != null) && !displayName.isEmpty()) {
            map.put("$name", displayName);
        }
        if (admin) {
            map.put("$permission", "config");
        }
        enqueue(encodeName(name, cacheBuf), map);
    }

    @Override
    public void sendRemove(String name) {
        if (!isOpen()) {
            throw new IllegalStateException("List stream closed");
        }
        DSMap map = new DSMap();
        map.put("name", encodeName(name, cacheBuf));
        map.put("change", "remove");
        enqueueResponse(map);
    }

    @Override
    public void sendTarget(Node object) {
        if (!isOpen()) {
            throw new IllegalStateException("List stream closed");
        }
        if (object instanceof Action) {
            encodeAction((Action) object);
        } else if (object instanceof Value) {
            encodeValue((Value) object);
        } else {
            encodeNode(object);
        }
    }

    @Override
    public void sendValue(String name,
                          String displayName,
                          DSIValue type,
                          boolean admin,
                          boolean readonly) {
        if (!isOpen()) {
            throw new IllegalStateException("List stream closed");
        }
        DSMap map = new DSMap();
        map.put("$is", "node");
        if ((displayName != null) && !displayName.isEmpty()) {
            map.put("$name", displayName);
        }
        cacheMeta.clear();
        if (type instanceof DSIMetadata) {
            ((DSIMetadata) type).getMetadata(cacheMeta.getMap());
        }
        map.put("$type", encodeType(type, cacheMeta));
        if (readonly) {
            if (admin) {
                map.put("$permission", "config");
            } else {
                map.put("$permission", "read");
            }
        } else {
            if (admin) {
                map.put("$writable", "config");
            } else {
                map.put("$writable", "write");
            }
        }
        enqueue(encodeName(name, cacheBuf), map);
    }

    @Override
    public boolean write(DSSession session, MessageWriter writer) {
        synchronized (this) {
            enqueued = false;
        }
        if (isClosed()) {
            return false;
        }
        boolean hasUpdates = false;
        synchronized (updates) {
            hasUpdates = !updates.isEmpty();
        }
        if (!isOpen() && (closeReason != null) && !hasUpdates) {
            getResponder().sendError(this, closeReason);
            doClose();
            return false;
        }
        beginMessage(writer);
        if (hasUpdates) {
            beginUpdates(writer);
            writeUpdates(writer);
            endUpdates(writer);
        }
        synchronized (updates) {
            hasUpdates = !updates.isEmpty();
        }
        if (hasUpdates) {
            endMessage(writer, null);
            enqueueResponse();
        } else if (!isOpen()) {
            endMessage(writer, Boolean.FALSE);
            doClose();
        } else {
            if (sendOpen) {
                sendOpen = false;
                endMessage(writer, Boolean.TRUE);
            } else {
                endMessage(writer, null);
            }
        }
        return true;
    }

    ///////////////////////////////////////////////////////////////////////////
    // Protected Methods
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

    /**
     * Override point for v2.
     */
    protected String encodeName(String name, StringBuilder buf) {
        buf.setLength(0);
        if (DSPath.encodeNameV1(name, buf)) {
            return buf.toString();
        }
        return name;
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
    protected void enqueue(String key, DSElement value) {
        enqueueResponse(new DSList().add(key).add(value));
    }

    /**
     * Override point for v2.
     */
    protected void enqueue(String key, String value) {
        enqueueResponse(new DSList().add(key).add(value));
    }

    /**
     * Adds an update to be sent to the requester.
     */
    protected void enqueueResponse(DSElement update) {
        synchronized (updates) {
            updates.add(update);
        }
        enqueueResponse();
    }

    ///////////////////////////////////////////////////////////////////////////
    // Package / Private Methods
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Remove an update from the queue.
     */
    private DSElement dequeue() {
        synchronized (updates) {
            if (updates.isEmpty()) {
                return null;
            }
            return updates.remove(0);
        }
    }

    private void doClose() {
        state = STATE_CLOSED;
        getResponder().removeRequest(getRequestId());
        if (response == null) {
            return;
        }
        DSRuntime.run(() -> {
            try {
                response.onClose();
            } catch (Exception x) {
                error(getPath(), x);
            }
        });
    }

    private void encodeAction(Action action) {
        action.getMetadata(cacheMap.clear());
        encodeAction(action, cacheMap);
    }

    private void encodeAction(DSInfo action) {
        DSMetadata.getMetadata(action, cacheMap.clear());
        encodeAction(action.getAction(), cacheMap);
    }

    private void encodeAction(Action action, DSMap cacheMap) {
        DSElement e = cacheMap.remove("$name");
        if (e == null) {
            e = cacheMap.remove(DSMetadata.DISPLAY_NAME);
        }
        if (e != null) {
            enqueue("$name", e);
        }
        e = cacheMap.remove("$invokable");
        if (e != null) {
            enqueue("$invokable", e);
        } else {
            if (action.isAdmin()) {
                enqueue("$invokable", "config");
            } else if (!action.isReadOnly()) {
                enqueue("$invokable", "write");
            } else {
                enqueue("$invokable", "read");
            }
        }
        e = cacheMap.remove("$params");
        if (e != null) {
            enqueue("$params", e);
        } else {
            DSList list = new DSList();
            for (int i = 0, len = action.getParameterCount(); i < len; i++) {
                DSMap param = new DSMap();
                action.getParameterMetadata(i, param);
                fixRangeTypes(param);
                list.add(param);
            }
            enqueue("$params", list);
        }
        e = cacheMap.remove("$columns");
        if (e != null) {
            enqueue("$columns", e);
        } else if (action.getColumnCount() > 0) {
            DSList list = new DSList();
            for (int i = 0, len = action.getColumnCount(); i < len; i++) {
                DSMap col = new DSMap();
                action.getColumnMetadata(i, col);
                fixRangeTypes(col);
                list.add(col);
            }
            enqueue("$columns", list);
        }
        e = cacheMap.remove("$result");
        if (e != null) {
            enqueue("$result", e);
        } else if (!action.getResultsType().isVoid()) {
            enqueue("$result", action.getResultsType().toString());
        }
        encodeTargetMetadata(cacheMap);
        cacheMap.clear();
    }

    private void encodeChild(DSInfo child) {
        if (child.isAction()) {
            sendAction(child.getName(), child.getMetadata().getDisplayName(),
                       child.isAdmin(), child.isReadOnly());
        } else if (child.isValue()) {
            sendValue(child.getName(), child.getMetadata().getDisplayName(),
                      child.getValue(), child.isAdmin(), child.isReadOnly());
        } else {
            sendNode(child.getName(), child.getMetadata().getDisplayName(),
                     child.isAdmin());
        }

    }

    private void encodeChildren(DSInfo info) {
        Iterator<String> it = info.getChildren();
        DSInfo child;
        while (it.hasNext()) {
            child = info.getChild(it.next());
            if (!child.isPrivate()) {
                encodeChild(child);
            }
        }
    }

    private void encodeNode(DSInfo object) {
        DSMetadata.getMetadata(object, cacheMap.clear());
        DSElement e = cacheMap.remove("$is");
        if (e == null) {
            enqueue("$is", "node");
        } else {
            enqueue("$is", e);
        }
        e = cacheMap.remove("$name");
        if (e == null) {
            e = cacheMap.remove(DSMetadata.DISPLAY_NAME);
        }
        if (e != null) {
            enqueue("$name", e);
        }
        e = cacheMap.remove("$permission");
        if (e != null) {
            enqueue("$permission", e);
        } else if (object.isAdmin()) {
            enqueue("$permission", "config");
        }
        encodeTargetMetadata(cacheMap);
        cacheMap.clear();
    }

    private void encodeNode(Node object) {
        object.getMetadata(cacheMap.clear());
        DSElement e = cacheMap.remove("$is");
        if (e == null) {
            enqueue("$is", "node");
        } else {
            enqueue("$is", e);
        }
        e = cacheMap.remove("$name");
        if (e == null) {
            e = cacheMap.remove(DSMetadata.DISPLAY_NAME);
        }
        if (e != null) {
            enqueue("$name", e);
        }
        e = cacheMap.remove("$permission");
        if (e != null) {
            enqueue("$permission", e);
        } else if (object.isAdmin()) {
            enqueue("$permission", "config");
        }
        encodeTargetMetadata(cacheMap);
        cacheMap.clear();
    }

    private void encodeTarget(DSInfo target) {
        if (target.isAction()) {
            encodeAction(target);
        } else if (target.isValue()) {
            encodeValue(target);
        } else {
            encodeNode(target);
        }
    }

    /**
     * Called by encodeTarget, encodes meta-data as configs.
     */
    private void encodeTargetMetadata(DSMap metadata) {
        String name;
        Entry entry = metadata.firstEntry();
        while (entry != null) {
            name = entry.getKey();
            switch (name.charAt(0)) {
                case '$':
                case '@':
                    break;
                default:
                    encodeName(name, cacheBuf);
                    cacheBuf.insert(0, '@');
                    name = cacheBuf.toString();

            }
            enqueue(name, entry.getValue());
            entry = entry.next();
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
                    meta.setType(DSString.NULL);
                } else {
                    meta.setType(value);
                }
            }
        }
        fixRangeTypes(meta.getMap());
        DSElement e = cacheMap.remove(DSMetadata.TYPE);
        if (e == null) {
            throw new IllegalArgumentException("Missing type");
        }
        return e;
    }

    /**
     * Encode all the meta data about the root target of a list request.
     */
    private void encodeValue(DSInfo object) {
        DSMetadata.getMetadata(object, cacheMap.clear());
        DSElement e = cacheMap.remove("$is");
        if (e == null) {
            enqueue("$is", "node");
        } else {
            enqueue("$is", e);
        }
        e = cacheMap.remove("$name");
        if (e == null) {
            e = cacheMap.remove(DSMetadata.DISPLAY_NAME);
        }
        if (e != null) {
            enqueue("$name", e);
        }
        e = cacheMap.remove("$type");
        if (e != null) {
            enqueue("$type", e);
        } else {
            enqueue("$type", encodeType(object.getValue(), cacheMeta));
        }
        e = cacheMap.remove("$writable");
        if (e != null) {
            enqueue("$writable", e);
        } else if (!object.isReadOnly()) {
            enqueue("$writable", object.isAdmin() ? "config" : "write");
        }
        e = cacheMap.remove("$permission");
        if (e != null) {
            enqueue("$permission", e);
        } else if (object.isAdmin()) {
            enqueue("$permission", "config");
        }
        encodeTargetMetadata(cacheMap);
        cacheMap.clear();
    }

    /**
     * Encode all the meta data about the root target of a list request.
     */
    private void encodeValue(Value object) {
        object.getMetadata(cacheMap.clear());
        DSElement e = cacheMap.remove("$is");
        if (e == null) {
            enqueue("$is", "node");
        } else {
            enqueue("$is", e);
        }
        e = cacheMap.remove("$name");
        if (e == null) {
            e = cacheMap.remove(DSMetadata.DISPLAY_NAME);
        }
        if (e != null) {
            enqueue("$name", e);
        }
        e = cacheMap.remove("$type");
        if (e != null) {
            enqueue("$type", e);
        } else {
            enqueue("$type", encodeType(object.toElement(), cacheMeta));
        }
        e = cacheMap.remove("$writable");
        if (e != null) {
            enqueue("$writable", e);
        } else if (!object.isReadOnly()) {
            enqueue("$writable", object.isAdmin() ? "config" : "write");
        }
        e = cacheMap.remove("$permission");
        if (e != null) {
            enqueue("$permission", e);
        } else if (object.isAdmin()) {
            enqueue("$permission", "config");
        }
        encodeTargetMetadata(cacheMap);
        cacheMap.clear();
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
     * Properly formats boolean and enum ranges for v1 and v2.
     */
    private void fixRangeTypes(DSMap arg) {
        try {
            String type = arg.getString(DSMetadata.TYPE);
            if ("bool".equals(type)) {
                DSList range = (DSList) arg.remove(DSMetadata.BOOLEAN_RANGE);
                if ((range != null) && (range.size() == 2)) {
                    String utf8 = DSString.UTF8.toString();
                    cacheBuf.setLength(0);
                    cacheBuf.append(type);
                    cacheBuf.append('[');
                    cacheBuf.append(URLEncoder.encode(range.get(0).toString(), utf8));
                    cacheBuf.append(',');
                    cacheBuf.append(URLEncoder.encode(range.get(1).toString(), utf8));
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
                    return;
                }
                cacheBuf.setLength(0);
                cacheBuf.append("enum");
                cacheBuf.append('[');
                String utf8 = DSString.UTF8.toString();
                for (int i = 0, len = range.size(); i < len; i++) {
                    if (i > 0) {
                        cacheBuf.append(',');
                    }
                    cacheBuf.append(URLEncoder.encode(range.get(i).toString(), utf8));
                }
                cacheBuf.append(']');
                if (getResponder().isV1()) {
                    arg.put(DSMetadata.TYPE, cacheBuf.toString());
                } else {
                    arg.put(DSMetadata.EDITOR, cacheBuf.toString());
                }
            }
        } catch (UnsupportedEncodingException x) {
            DSException.throwRuntime(x);
        }
    }

    private boolean isClosed() {
        return state == STATE_CLOSED;
    }

    private void writeUpdates(MessageWriter writer) {
        DSResponder responder = getResponder();
        DSElement update;
        while (isOpen()) {
            update = dequeue();
            if (update == null) {
                break;
            }
            writer.getWriter().value(update);
            if (responder.shouldEndMessage()) {
                break;
            }
        }
    }

}
