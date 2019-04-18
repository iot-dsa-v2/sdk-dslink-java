package com.acuity.iot.dsa.dslink.protocol.responder;

import com.acuity.iot.dsa.dslink.protocol.DSSession;
import com.acuity.iot.dsa.dslink.protocol.DSStream;
import com.acuity.iot.dsa.dslink.protocol.message.DSTarget;
import com.acuity.iot.dsa.dslink.protocol.message.MessageWriter;
import com.acuity.iot.dsa.dslink.protocol.message.OutboundMessage;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
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
import org.iot.dsa.node.DSString;
import org.iot.dsa.node.DSValueType;
import org.iot.dsa.node.action.ActionSpec;
import org.iot.dsa.node.action.DSAction;
import org.iot.dsa.node.event.DSEvent;
import org.iot.dsa.node.event.DSISubscriber;
import org.iot.dsa.node.event.DSISubscription;
import org.iot.dsa.security.DSPermission;
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
    private static final int STATE_CHILDREN = 1;
    private static final int STATE_UPDATES = 2;
    private static final int STATE_CLOSE_PENDING = 3;
    private static final int STATE_CLOSED = 4;

    ///////////////////////////////////////////////////////////////////////////
    // Class Fields
    ///////////////////////////////////////////////////////////////////////////

    private StringBuilder cacheBuf = new StringBuilder();
    private DSMap cacheMap = new DSMap();
    private DSMetadata cacheMeta = new DSMetadata(cacheMap);
    private Iterator<String> children;
    private Exception closeReason;
    private boolean enqueued = false;
    private DSInfo info;
    private DSNode node;
    private OutboundListResponse response;
    private int state = STATE_INIT;
    private boolean stream = true;
    private DSISubscription subscription;
    private DSTarget target;
    private Update updateHead;
    private Update updateTail;

    ///////////////////////////////////////////////////////////////////////////
    // Public Methods
    ///////////////////////////////////////////////////////////////////////////

    @Override
    public void added(String name, ApiObject child) {
        if (!isClosed()) {
            enqueue(new AddUpdate(name, child));
        }
    }

    @Override
    public boolean canWrite(DSSession session) {
        return true;
    }

    @Override
    public void changed(String name, DSElement value) {
        if (!isClosed()) {
            enqueue(new ChangeUpdate(name, value));
        }
    }

    @Override
    public void close() {
        if (!isOpen()) {
            return;
        }
        state = STATE_CLOSE_PENDING;
        enqueueResponse();
        debug(debug() ? getPath() + " list closed locally" : null);
    }

    @Override
    public void close(Exception reason) {
        if (!isOpen()) {
            return;
        }
        state = STATE_CLOSE_PENDING;
        closeReason = reason;
        enqueueResponse();
        debug(debug() ? getPath() + " list closed locally" : null, reason);
    }

    @Override
    public ApiObject getTarget() {
        return info;
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
        debug(debug() ? getPath() + " list closed" : null);
        synchronized (this) {
            updateHead = updateTail = null;
        }
        doClose();
    }

    @Override
    public void onClosed(DSISubscription subscription) {
        this.subscription = null;
        close();
    }

    @Override
    public void onEvent(DSEvent event, DSNode node, DSInfo child, DSIValue data) {
        switch (event.getEventId()) {
            case DSNode.CHILD_ADDED:
                added(child.getName(), child);
                break;
            case DSNode.CHILD_RENAMED:
                removed(data.toString());
                added(child.getName(), child);
                break;
            case DSNode.CHILD_REMOVED:
                removed(child.getName());
                break;
        }
    }

    @Override
    public void removed(String name) {
        if (!isClosed()) {
            enqueue(new RemoveUpdate(name));
        }
    }

    @Override
    public void run() {
        try {
            target = new DSTarget(getPath(), getLink().getRootNode());
            if (target.isResponder()) {
                DSIResponder responder = (DSIResponder) target.getTarget();
                setPath(target.getPath());
                response = responder.onList(this);
            } else {
                info = target.getTargetInfo();
                if (info == null) {
                    info = new RootInfo((DSNode) target.getTarget());
                }
                if (info.isNode()) {
                    node = info.getNode();
                    this.subscription = node.subscribe(this);
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
    public boolean write(DSSession session, MessageWriter writer) {
        synchronized (this) {
            enqueued = false;
        }
        if (isClosed()) {
            return false;
        }
        if (isClosePending() && (updateHead == null) && (closeReason != null)) {
            getResponder().sendError(this, closeReason);
            doClose();
            return false;
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
                writeChildren(response.getTarget(), writer);
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
            } else {
                endMessage(writer, null);
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
    protected void encode(String key, DSElement value, MessageWriter writer) {
        writer.getWriter().beginList().value(key).value(value).endList();
    }

    /**
     * Override point for v2.
     */
    protected void encode(String key, String value, MessageWriter writer) {
        writer.getWriter().beginList().value(key).value(value).endList();
    }

    protected void encodeChild(String name, ApiObject child, MessageWriter writer) {
        String safeName = encodeName(name, cacheBuf);
        DSMap map = new DSMap();
        child.getMetadata(cacheMap.clear());
        DSElement e = cacheMap.remove("$name");
        if (e == null) {
            e = cacheMap.remove(DSMetadata.DISPLAY_NAME);
        }
        if (e != null) {
            map.put("$name", e);
        } else if (!safeName.equals(name)) {
            map.put("$name", name);
        }
        e = cacheMap.remove("$is");
        if (e != null) {
            map.put("$is", e);
        } else {
            map.put("$is", "node");
        }
        e = cacheMap.remove("$invokable");
        if (e != null) {
            map.put("$invokable", e);
        } else if (child.isAction()) {
            if (child.isAdmin()) {
                map.put("$invokable", DSPermission.CONFIG.toString());
            } else if (child.isReadOnly()) {
                map.put("$invokable", DSPermission.READ.toString());
            } else {
                map.put("$invokable", DSPermission.WRITE.toString());
            }
        }
        e = cacheMap.remove("$type");
        if (e != null) {
            map.put("$type", e);
        } else if ((child instanceof DSInfo) && child.isValue()) {
            map.put("$type", encodeType(((DSInfo) child).getValue(), cacheMeta));
        }
        e = cacheMap.remove("$writable");
        if (e != null) {
            map.put("$writable", e);
        } else if (!child.isReadOnly()) {
            map.put("$writable", child.isAdmin() ? "config" : "write");
        }
        e = cacheMap.remove("$permission");
        if (e != null) {
            map.put("$permission", e);
        } else if (child.isAdmin()) {
            map.put("$permission", "config");
        }
        encode(safeName, map, writer);
        cacheMap.clear();
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
    protected void encodeUpdate(Update update, MessageWriter writer, StringBuilder buf) {
        if (update instanceof AddUpdate) {
            AddUpdate au = (AddUpdate) update;
            encodeChild(au.name, au.child, writer);
        } else if (update instanceof ChangeUpdate) {
            ChangeUpdate cu = (ChangeUpdate) update;
            writer.getWriter().beginMap()
                  .key(encodeName(cu.name, buf))
                  .value(cu.value)
                  .endMap();
        } else {
            writer.getWriter().beginMap()
                  .key("name").value(encodeName(((RemoveUpdate) update).name, buf))
                  .key("change").value("remove")
                  .endMap();
        }
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

    ///////////////////////////////////////////////////////////////////////////
    // Package / Private Methods
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Remove an update from the queue.
     */
    private synchronized Update dequeue() {
        if (updateHead == null) {
            return null;
        }
        Update ret = updateHead;
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
        DSRuntime.run(() -> {
            try {
                response.onClose();
            } catch (Exception x) {
                error(getPath(), x);
            }
        });
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
        e = cacheMap.remove("$name");
        if (e == null) {
            e = cacheMap.remove(DSMetadata.DISPLAY_NAME);
        }
        if (e != null) {
            encode("$name", e, writer);
        }
        /* TODO, maybe stricter encoding in dspath
        else {
            String name = object.getName();
            String safe = encodeName(name, cacheBuf);
            if (!safe.equals(name)) {
                encode("$name", name, writer);
            }
        }
        */
        if (object.isAction()) {
            encodeTargetAction(object, writer);
        } else if (object.isValue()) {
            encodeTargetValue(object, writer);
        }
        e = cacheMap.remove("$permission");
        if (e != null) {
            encode("$permission", e, writer);
        } else if (object.isAdmin()) {
            encode("$permission", "config", writer);
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
        if (e != null) {
            encode("$invokable", e, writer);
        } else {
            if (object.isAdmin()) {
                encode("$invokable", DSPermission.CONFIG.toString(), writer);
            } else if (object.isReadOnly()) {
                encode("$invokable", DSPermission.READ.toString(), writer);
            } else {
                encode("$invokable", DSPermission.WRITE.toString(), writer);
            }
        }
        e = cacheMap.remove("$params");
        if (e != null) {
            encode("$params", e, writer);
        } else {
            DSList list = new DSList();
            for (int i = 0, len = action.getParameterCount(); i < len; i++) {
                DSMap param = new DSMap();
                action.getParameterMetadata(i, param);
                if (dsAction != null) {
                    dsAction.prepareParameter(target.getParentInfo(), param);
                }
                fixRangeTypes(param);
                list.add(param);
            }
            encode("$params", list, writer);
        }
        e = cacheMap.remove("$columns");
        if (e != null) {
            encode("$columns", e, writer);
        } else if (action.getColumnCount() > 0) {
            DSList list = new DSList();
            for (int i = 0, len = action.getColumnCount(); i < len; i++) {
                DSMap col = new DSMap();
                action.getColumnMetadata(i, col);
                fixRangeTypes(col);
                list.add(col);
            }
            encode("$columns", list, writer);
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
            encode(name, entry.getValue(), writer);
            entry = entry.next();
        }
    }

    /**
     * Called by encodeTarget for values.
     */
    private void encodeTargetValue(ApiObject object, MessageWriter writer) {
        DSElement e = cacheMap.remove("$type");
        if (e != null) {
            encode("$type", e, writer);
        } else if (object instanceof DSInfo) {
            encode("$type", encodeType(((DSInfo) object).getValue(), cacheMeta), writer);
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
        fixRangeTypes(meta.getMap());
        DSElement e = cacheMap.remove(DSMetadata.TYPE);
        if (e == null) {
            throw new IllegalArgumentException("Missing type");
        }
        return e;
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
     * Properly formats boolean and enum ranges for v1 and v2.
     */
    private void fixRangeTypes(DSMap arg) {
        try {
            String type = arg.getString(DSMetadata.TYPE);
            if ("bool".equals(type)) {
                DSList range = (DSList) arg.remove(DSMetadata.BOOLEAN_RANGE);
                if ((range == null) || (range.size() != 2)) {
                    return;
                } else {
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

    private boolean isClosePending() {
        return state == STATE_CLOSE_PENDING;
    }

    private boolean isClosed() {
        return state == STATE_CLOSED;
    }

    private void writeChildren(ApiObject target, MessageWriter writer) {
        if (children != null) {
            String name;
            ApiObject child;
            while (children.hasNext()) {
                name = children.next();
                child = target.getChild(name);
                if (child != null) {
                    if (!child.isPrivate()) {
                        encodeChild(name, child, writer);
                    }
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
        state = STATE_CHILDREN;
        children = target.getChildren();
        writeChildren(target, writer);
    }

    private void writeUpdates(MessageWriter writer) {
        DSResponder responder = getResponder();
        Update update;
        while (isOpen()) {
            update = dequeue();
            if (update == null) {
                break;
            }
            encodeUpdate(update, writer, cacheBuf);
            if (responder.shouldEndMessage()) {
                enqueueResponse();
                break;
            }
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // Inner Classes
    ///////////////////////////////////////////////////////////////////////////

    protected static class AddUpdate extends Update {

        public ApiObject child;
        public String name;

        AddUpdate(String name, ApiObject child) {
            this.name = name;
            this.child = child;
        }

    }

    protected static class ChangeUpdate extends Update {

        public String name;
        public DSElement value;

        ChangeUpdate(String name, DSElement value) {
            this.name = name;
            this.value = value;
        }

    }

    protected static class RemoveUpdate extends Update {

        public String name;

        RemoveUpdate(String name) {
            this.name = name;
        }
    }

    private static class RootInfo extends DSInfo {

        RootInfo(DSNode node) {
            super(null, node);
        }
    }

    protected static class Update {

        public Update next;

    }

}
