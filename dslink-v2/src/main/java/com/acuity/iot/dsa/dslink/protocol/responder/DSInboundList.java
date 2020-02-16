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
import org.iot.dsa.dslink.Action;
import org.iot.dsa.dslink.DSIResponder;
import org.iot.dsa.dslink.Node;
import org.iot.dsa.dslink.Value;
import org.iot.dsa.dslink.responder.InboundListRequest;
import org.iot.dsa.dslink.responder.ListCloseHandler;
import org.iot.dsa.node.DSElement;
import org.iot.dsa.node.DSElementType;
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
        implements DSISubscriber, DSStream, InboundListRequest, OutboundMessage, Runnable {

    ///////////////////////////////////////////////////////////////////////////
    // Instance Fields
    ///////////////////////////////////////////////////////////////////////////

    ///////////////////////////////////////////////////////////////////////////
    // Class Fields
    ///////////////////////////////////////////////////////////////////////////

    private StringBuilder cacheBuf = new StringBuilder();
    private DSMap cacheMap = new DSMap();
    private DSMetadata cacheMeta = new DSMetadata(cacheMap);
    private boolean enqueued = false;
    private ListCloseHandler response;
    private boolean sendStreamOpen = false;
    private StreamState state = StreamState.CLOSED;
    private DSISubscription subscription;
    private DSTarget target;
    private final List<DSElement> updates = new LinkedList<>();

    ///////////////////////////////////////////////////////////////////////////
    // Public Methods
    ///////////////////////////////////////////////////////////////////////////

    @Override
    public boolean canWrite(DSSession session) {
        return true;
    }

    @Override
    public void close() {
        if (isOpen()) {
            //we don't actually close the stream because an object could be added back at this path
            send("$disconnectedTs", DSDateTime.now().toElement());
            sendStreamOpen = true;
            state = StreamState.DISCONNECTED;
        }
        if (subscription != null) {
            subscription.close();
            subscription = null;
        }
        if (response != null) {
            try {
                response.onClose();
            } catch (Exception x) {
                error(getPath(), x);
            }
            response = null;
        }
    }

    @Override
    public void close(Exception reason) {
        if (isOpen()) {
            getResponder().sendError(this, reason);
        }
        close();
    }

    @Override
    public boolean isOpen() {
        return state.isOpen();
    }

    /**
     * This is the only way to really close a list request, called only when the connection is
     * closed or the requester has explicitly closed it.
     */
    @Override
    public void onClose(Integer requestId) {
        if (!isOpen()) {
            return;
        }
        state = StreamState.CLOSED;
        synchronized (updates) {
            updates.clear();
        }
        getResponder().removeRequest(getRequestId());
        close();
    }

    @Override
    public void onClosed(DSISubscription subscription) {
        if (subscription == this.subscription) {
            this.subscription = null;
            close();
        }
    }

    @Override
    public void onEvent(DSEvent event, DSNode node, DSInfo<?> child, DSIValue data) {
        if (!isOpen()) {
            return;
        }
        switch (event.getEventId()) {
            case DSNode.CHILD_RENAMED:
                if (target.getTarget() == node) {
                    sendRemove(data.toString());
                }
                //fall through to add the child
            case DSNode.CHILD_ADDED:
                if (target.getTarget() == node) {
                    encodeChild(child);
                }
                break;
            case DSNode.CHILD_REMOVED:
                if (target.getTargetInfo() == child) {
                    subscription.close();
                    subscription = null;
                    close();
                } else if (target.getTarget() == node) {
                    sendRemove(child.getName());
                }
                break;
            case DSNode.STOPPED:
                subscription.close();
                subscription = null;
                close();
        }
    }

    @Override
    public void run() {
        if (subscription != null) {
            subscription.close();
            subscription = null;
        }
        if (response != null) {
            try {
                response.onClose();
            } catch (Exception x) {
                error(getPath(), x);
            }
            response = null;
        }
        try {
            state = StreamState.OPEN;
            target = new DSTarget(getPath(), getLink().getRootNode());
            if (target.isResponder()) {
                DSIResponder responder = (DSIResponder) target.getTarget();
                setPath(target.getPath());
                response = responder.onList(this);
            } else {
                encodeTarget(target.getTargetInfo());
                encodeChildren(target.getTargetInfo());
                subscription = target.getNode().subscribe(this);
                listComplete();
            }
        } catch (Exception x) {
            debug(getPath(), x);
            close();
        }
    }

    @Override
    public void send(String name, DSElement value) {
        if (!isOpen()) {
            throw new IllegalStateException("List stream closed");
        }
        enqueue(encodeName(name, cacheBuf), value);
    }

    @Override
    public void sendChildAction(final String name, String displayName, boolean admin,
                                boolean readonly) {
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
    public void sendChildNode(final String name, String displayName, boolean admin) {
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
    public void sendChildValue(final String name,
                               String displayName,
                               DSElementType type,
                               boolean admin,
                               boolean readonly) {
        if (!isOpen()) {
            throw new IllegalStateException("List stream not open");
        }
        DSMap map = new DSMap();
        map.put("$is", "node");
        if ((displayName != null) && !displayName.isEmpty()) {
            map.put("$name", displayName);
        }
        map.put("$type", encodeType(type));
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
    public void listComplete() {
        sendStreamOpen = true;
        enqueueResponse();
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
    public void sendTarget(Node node) {
        if (!isOpen()) {
            throw new IllegalStateException("List stream closed");
        }
        if (node instanceof Action) {
            encodeAction((Action) node);
        } else if (node instanceof Value) {
            encodeValue((Value) node);
        } else {
            encodeNode(node);
        }
    }

    @Override
    public boolean write(DSSession session, MessageWriter writer) {
        if (state.isClosed()) { //can be disconnected
            return false;
        }
        boolean hasUpdates;
        synchronized (updates) {
            hasUpdates = !updates.isEmpty() || sendStreamOpen;
        }
        beginMessage(writer);
        if (hasUpdates) {
            beginUpdates(writer);
            writeUpdates(writer);
            endUpdates(writer);
        }
        synchronized (updates) {
            enqueued = false;
            hasUpdates = !updates.isEmpty();
        }
        if (hasUpdates) {
            endMessage(writer, false);
            enqueueResponse();
        } else {
            endMessage(writer, sendStreamOpen);
            if (sendStreamOpen) {
                sendStreamOpen = false;
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
    protected void endMessage(MessageWriter writer, boolean streamOpen) {
        if (streamOpen) {
            writer.getWriter().key("stream").value("open");
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
        enqueueResponse(new DSList().add(key).add(ensureUnparented(value)));
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
        if (!state.isClosed()) {
            synchronized (updates) {
                updates.add(update);
            }
            enqueueResponse();
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // Package / Private Methods
    ///////////////////////////////////////////////////////////////////////////

    private DSElement dequeueUpdate() {
        synchronized (updates) {
            if (updates.isEmpty()) {
                return null;
            }
            return updates.remove(0);
        }
    }

    private void encodeAction(Action action) {
        action.getMetadata(cacheMap.clear());
        encodeAction(action, cacheMap);
    }

    private void encodeAction(DSInfo<?> action) {
        action.getMetadata(cacheMap.clear());
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
        DSMetadata md = new DSMetadata();
        e = cacheMap.remove("$params");
        if (e != null) {
            enqueue("$params", e);
        } else {
            DSList list = new DSList();
            DSMap map = md.getMap();
            for (int i = 0, len = action.getParameterCount(); i < len; i++) {
                map.clear();
                action.getParameterMetadata(i, map);
                map.put("type", encodeType(md.getDefault(), md));
                list.add(map.copy());
            }
            enqueue("$params", list);
        }
        e = cacheMap.remove("$columns");
        if (e != null) {
            enqueue("$columns", e);
        } else if (action.getColumnCount() > 0) {
            DSList list = new DSList();
            DSMap col = md.getMap();
            for (int i = 0, len = action.getColumnCount(); i < len; i++) {
                col.clear();
                action.getColumnMetadata(i, col);
                col.put("type", encodeType(md.getDefault(), md));
                list.add(col.copy());
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

    private void encodeChild(DSInfo<?> child) {
        if (child.isAction()) {
            sendChildAction(child.getName(), child.getMetadata().getDisplayName(),
                            child.isAdmin(), child.isReadOnly());
        } else if (child.isValue()) {
            sendChildValue(child.getName(), child.getMetadata().getDisplayName(),
                           child.getElement().getElementType(), child.isAdmin(),
                           child.isReadOnly());
        } else {
            sendChildNode(child.getName(), child.getMetadata().getDisplayName(), child.isAdmin());
        }
    }

    private void encodeChildren(DSInfo<?> info) {
        Iterator<String> it = info.getChildren();
        DSInfo<?> child;
        while (it.hasNext()) {
            child = info.getChild(it.next());
            if (!child.isPrivate()) {
                encodeChild(child);
            }
        }
    }

    private void encodeNode(DSInfo<?> object) {
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

    private void encodeNode(Node node) {
        node.getMetadata(cacheMap.clear());
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
        } else if (node.isAdmin()) {
            enqueue("$permission", "config");
        }
        encodeTargetMetadata(cacheMap);
        cacheMap.clear();
    }

    private void encodeTarget(DSInfo<?> target) {
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
                    cacheBuf.insert(0, '$');
                    name = cacheBuf.toString();

            }
            enqueue(name, entry.getValue());
            entry = entry.next();
        }
    }

    private DSElement encodeType(Value value) {
        String type = encodeType(value.getType());
        cacheMap.put(DSMetadata.TYPE, DSString.valueOf(type));
        fixRangeTypes(cacheMap);
        DSElement e = cacheMap.remove(DSMetadata.TYPE);
        if (e == null) {
            throw new IllegalArgumentException("Missing type");
        }
        return e;
    }

    private DSElement encodeType(DSIValue value, DSMetadata meta) {
        String type = meta.getType();
        if ((type == null) && (value != null)) {
            meta.setType(value);
            type = meta.getType();
        }
        DSMap map = meta.getMap();
        type = encodeType(DSElementType.valueFor(type));
        map.put(DSMetadata.TYPE, type);
        fixRangeTypes(map);
        DSElement e = map.remove(DSMetadata.TYPE);
        if (e == null) {
            throw new IllegalArgumentException("Missing type");
        }
        return e;
    }

    /**
     * Convert from element type to dsa type.
     */
    private String encodeType(DSElementType et) {
        switch (et) {
            case BOOLEAN:
                return "bool";
            case DOUBLE:
            case LONG:
                return "number";
            case LIST:
                return "array";
            case MAP:
                return "map";
            case BYTES:
                return "binary";
            case STRING:
                return "string";
            default:
                return "dynamic";
        }
    }

    /**
     * Encode all the meta data about the root target of a list request.
     */
    private void encodeValue(DSInfo<?> object) {
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
    private void encodeValue(Value value) {
        cacheMap.clear();
        value.getMetadata(cacheMap);
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
            enqueue("$type", encodeType(value));
        }
        e = cacheMap.remove("$writable");
        if (e != null) {
            enqueue("$writable", e);
        } else if (!value.isReadOnly()) {
            enqueue("$writable", value.isAdmin() ? "config" : "write");
        }
        e = cacheMap.remove("$permission");
        if (e != null) {
            enqueue("$permission", e);
        } else if (value.isAdmin()) {
            enqueue("$permission", "config");
        }
        encodeTargetMetadata(cacheMap);
        cacheMap.clear();
    }

    /**
     * Enqueues in the session.
     */
    private void enqueueResponse() {
        synchronized (updates) {
            if (enqueued) {
                return;
            }
            enqueued = true;
        }
        getResponder().sendResponse(this);
    }

    private DSElement ensureUnparented(DSElement value) {
        if (value.isGroup()) {
            if (value.isList() && value.toList().hasParent()) {
                value = value.copy();
            } else if (value.isMap() && value.toMap().hasParent()) {
                value = value.copy();
            }
        }
        return value;
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

    private void writeUpdates(MessageWriter writer) {
        DSResponder responder = getResponder();
        DSElement update;
        while (!state.isClosed()) {
            update = dequeueUpdate();
            if (update == null) {
                break;
            }
            writer.getWriter().value(update);
            if (update.isList()) { //DGLux bug workaround
                DSList list = update.toList();
                if (list.size() > 1) {
                    if (list.get(0).isString()) {
                        if ("$disconnectedTs".equals(list.getString(0))) {
                            break;
                        }
                    }
                }
            }
            if (responder.shouldEndMessage()) {
                break;
            }
        }
    }

}
