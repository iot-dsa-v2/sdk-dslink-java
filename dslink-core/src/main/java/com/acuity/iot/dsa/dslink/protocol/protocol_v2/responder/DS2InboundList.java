package com.acuity.iot.dsa.dslink.protocol.protocol_v2.responder;

import com.acuity.iot.dsa.dslink.protocol.DSStream;
import com.acuity.iot.dsa.dslink.protocol.message.ErrorResponse;
import com.acuity.iot.dsa.dslink.protocol.message.MessageWriter;
import com.acuity.iot.dsa.dslink.protocol.message.OutboundMessage;
import com.acuity.iot.dsa.dslink.protocol.message.RequestPath;
import java.util.Iterator;
import org.iot.dsa.DSRuntime;
import org.iot.dsa.dslink.DSIResponder;
import org.iot.dsa.dslink.responder.ApiObject;
import org.iot.dsa.dslink.responder.InboundListRequest;
import org.iot.dsa.dslink.responder.OutboundListResponse;
import org.iot.dsa.io.DSIWriter;
import org.iot.dsa.node.DSElement;
import org.iot.dsa.node.DSIValue;
import org.iot.dsa.node.DSInfo;
import org.iot.dsa.node.DSList;
import org.iot.dsa.node.DSMap;
import org.iot.dsa.node.DSMap.Entry;
import org.iot.dsa.node.DSMetadata;
import org.iot.dsa.node.DSNode;
import org.iot.dsa.node.DSPath;
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
class DS2InboundList extends DS2InboundRequest
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
    private Update updateHead;
    private Update updateTail;

    ///////////////////////////////////////////////////////////////////////////
    // Constructors
    ///////////////////////////////////////////////////////////////////////////

    DS2InboundList() {
    }

    ///////////////////////////////////////////////////////////////////////////
    // Methods in alphabetical order
    ///////////////////////////////////////////////////////////////////////////

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
        getResponder().removeInboundRequest(getRequestId());
        if (response == null) {
            return;
        }
        DSRuntime.run(new Runnable() {
            @Override
            public void run() {
                try {
                    response.onClose();
                } catch (Exception x) {
                    severe(getPath(), x);
                }
            }
        });
    }

    private void encodeChild(ApiObject child, DSIWriter out) {
        out.beginList();
        String name = child.getName();
        String displayName = null;
        if (DSPath.encodeName(name, cacheBuf)) {
            displayName = name;
            out.value(cacheBuf.toString());
        } else {
            out.value(name);
        }
        cacheBuf.setLength(0);
        out.beginMap();
        child.getMetadata(cacheMap.clear());
        DSElement e = cacheMap.remove(DSMetadata.DISPLAY_NAME);
        if (e != null) {
            out.key("$name").value(e);
        } else if (displayName != null) {
            out.key("$name").value(displayName);
        }
        e = cacheMap.remove("$is");
        if (e != null) {
            out.key("$is").value(e);
        } else {
            out.key("$is").value("node");
        }
        if (child.isAction()) {
            ActionSpec action = child.getAction();
            e = cacheMap.remove("$invokable");
            if (e != null) {
                out.key("$invokable").value(e);
            } else {
                out.key("$invokable").value(action.getPermission().toString());
            }
        } else if (child.isValue()) {
            out.key("$type");
            e = cacheMap.remove("$type");
            if (e != null) {
                out.value(e);
            } else {
                encodeType(child.getValue(), cacheMeta, out);
            }
            if (!child.isReadOnly()) {
                e = cacheMap.remove("$writable");
                if (e != null) {
                    out.key("$writable").value(e);
                } else {
                    out.key("$writable").value(child.isAdmin() ? "config" : "write");
                }
            }
        } else if (child.isAdmin()) {
            e = cacheMap.remove("$permission");
            if (e != null) {
                out.key("$permission").value(e);
            } else {
                out.key("$permission").value("config");
            }
        }
        out.endMap().endList();
        cacheMap.clear();
    }

    /**
     * Encode all the meta data about the root target of a list request.
     */
    private void encodeTarget(ApiObject object, DSIWriter out) {
        if (object instanceof DSInfo) {
            DSMetadata.getMetadata((DSInfo) object, cacheMap.clear());
        } else {
            object.getMetadata(cacheMap.clear());
        }
        DSElement e = cacheMap.remove("$is");
        if (e == null) {
            out.beginList().value("$is").value("node").endList();
        } else {
            out.beginList().value("$is").value(e).endList();

        }
        e = cacheMap.get("$name");
        if (e == null) {
            String safeName = object.getName();
            if (DSPath.encodeName(safeName, cacheBuf)) {
                safeName = cacheBuf.toString();
            }
            cacheBuf.setLength(0);
            out.beginList().value("$name").value(safeName).endList();
        } else {
            out.beginList().value("$name").value(e).endList();
        }
        if (object.isAction()) {
            encodeTargetAction(object, out);
        } else if (object.isValue()) {
            encodeTargetValue(object, out);
        } else if (object.isAdmin()) {
            e = cacheMap.remove("$permission");
            if (e == null) {
                out.beginList().value("$permission").value("config").endList();
            } else {
                out.beginList().value("$permission").value(e).endList();
            }
        }
        encodeTargetMetadata(cacheMap, out);
        cacheMap.clear();
    }

    /**
     * Called by encodeTarget for actions.
     */
    private void encodeTargetAction(ApiObject object, DSIWriter out) {
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
        out.beginList().value("$invokable");
        if (e == null) {
            out.value(action.getPermission().toString()).endList();
        } else {
            out.value(e).endList();
        }
        e = cacheMap.remove("params");
        out.beginList().value("$params");
        if (e == null) {
            out.beginList();
            Iterator<DSMap> params = action.getParameters();
            if (params != null) {
                DSMap param;
                while (params.hasNext()) {
                    param = params.next();
                    if (dsAction != null) {
                        dsAction.prepareParameter(info, param);
                    }
                    out.value(fixType(param));
                }
            }
            out.endList();
        } else {
            out.value(e);
        }
        out.endList();
        if (action.getResultType().isValues()) {
            e = cacheMap.remove("$columns");
            out.beginList().value("$columns");
            if (e == null) {
                out.beginList();
                Iterator<DSMap> params = action.getParameters();
                if (params != null) {
                    DSMap param;
                    while (params.hasNext()) {
                        param = params.next();
                        if (dsAction != null) {
                            dsAction.prepareParameter(info, param);
                        }
                        out.value(fixType(param));
                    }
                }
                out.endList();
            } else {
                out.value(e);
            }
            out.endList();
        }
        e = cacheMap.remove("$result");
        if (e != null) {
            out.beginList().value("$result").value(e).endList();
        } else if (!action.getResultType().isVoid()) {
            out.beginList().value("$result").value(action.getResultType().toString()).endList();
        }
    }

    /**
     * Called by encodeTarget, encodes meta-data as configs.
     */
    private void encodeTargetMetadata(DSMap metadata, DSIWriter out) {
        if (cacheMap.isEmpty()) {
            return;
        }
        Entry entry;
        String name;
        for (int i = 0, len = cacheMap.size(); i < len; i++) {
            entry = cacheMap.getEntry(i);
            out.beginList();
            name = entry.getKey();
            switch (name.charAt(0)) {
                case '$':
                case '@':
                    out.value(name);
                default:
                    cacheBuf.append("@"); //TODO ?
                    DSPath.encodeName(name, cacheBuf);
                    out.value(cacheBuf.toString());
                    cacheBuf.setLength(0);

            }
            out.value(entry.getValue());
            out.endList();
        }
    }

    /**
     * Called by encodeTarget for values.
     */
    private void encodeTargetValue(ApiObject object, DSIWriter out) {
        DSElement e = cacheMap.remove("$type");
        out.beginList();
        out.value("$type");
        if (e != null) {
            out.value(e);
        } else {
            encodeType(object.getValue(), cacheMeta, out);
        }
        out.endList();
        e = cacheMap.remove("$writable");
        if (e != null) {
            out.beginList()
               .value("$writable")
               .value(e)
               .endList();
        } else if (!object.isReadOnly()) {
            out.beginList()
               .value("$writable")
               .value(object.isAdmin() ? "config" : "write")
               .endList();
        }
    }

    private void encodeType(DSIValue value, DSMetadata meta, DSIWriter out) {
        String type = meta.getType();
        if ((type == null) && (value != null)) {
            meta.setType(value);
        }
        fixType(meta.getMap());
        DSElement e = cacheMap.remove(DSMetadata.TYPE);
        if (e == null) {
            throw new IllegalArgumentException("Missing type");
        }
        out.value(e);
    }

    private void encodeUpdate(Update update, DSIWriter out) {
        if (!isOpen()) {
            return;
        }
        if (update.added) {
            encodeChild(update.child, out);
        } else {
            out.beginMap();
            out.key("name").value(DSPath.encodeName(update.child.getName()));
            out.key("change").value("remove");
            out.endMap();
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
    private DSMap fixType(DSMap arg) {
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
                arg.put(DSMetadata.TYPE, cacheBuf.toString());
            }
        } else if ("enum".equals(type)) {
            DSList range = (DSList) arg.remove(DSMetadata.ENUM_RANGE);
            if (range == null) {
                return arg;
            }
            cacheBuf.setLength(0);
            cacheBuf.append(type);
            cacheBuf.append('[');
            for (int i = 0, len = range.size(); i < len; i++) {
                if (i > 0) {
                    cacheBuf.append(',');
                }
                cacheBuf.append(range.get(i).toString());
            }
            cacheBuf.append(']');
            arg.put(DSMetadata.TYPE, cacheBuf.toString());
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
            severe(getPath(), x);
            close(x);
            return;
        }
        if (response == null) {
            close();
        } else {
            enqueueResponse();
        }
    }

    @Override
    public void onClose(Integer requestId) {
        if (isClosed()) {
            return;
        }
        state = STATE_CLOSED;
        fine(finer() ? getPath() + " list closed" : null);
        synchronized (this) {
            updateHead = updateTail = null;
        }
        doClose();
    }

    @Override
    public void write(MessageWriter writer) {
        DSIWriter out = writer.getWriter();
        enqueued = false;
        if (isClosed()) {
            return;
        }
        if (isClosePending() && (updateHead == null) && (closeReason != null)) {
            ErrorResponse res = new ErrorResponse(closeReason);
            res.parseRequest(getRequest());
            res.write(writer);
            doClose();
            return;
        }
        int last = state;
        out.beginMap();
        out.key("rid").value(getRequestId());
        switch (state) {
            case STATE_INIT:
                out.key("updates").beginList();
                writeInit(out);
                break;
            case STATE_CHILDREN:
                out.key("updates").beginList();
                writeChildren(out);
                break;
            case STATE_CLOSE_PENDING:
            case STATE_UPDATES:
                out.key("updates").beginList();
                writeUpdates(out);
                break;
            default:
                ;
        }
        out.endList();
        if ((state != last) && (state == STATE_UPDATES)) {
            out.key("stream").value("open");
        } else if (isClosePending() && (updateHead == null)) {
            if (closeReason != null) {
                ErrorResponse res = new ErrorResponse(closeReason);
                res.parseRequest(getRequest());
                getResponder().sendResponse(res);
            } else {
                out.key("stream").value("closed");
            }
            doClose();
        }
        out.endMap();
    }

    private void writeChildren(DSIWriter out) {
        if (children != null) {
            ApiObject child;
            while (children.hasNext()) {
                child = children.next();
                if (!child.isHidden()) {
                    encodeChild(child, out);
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

    private void writeInit(DSIWriter out) {
        ApiObject target = response.getTarget();
        encodeTarget(target, out);
        if (target.hasChildren()) {
            state = STATE_CHILDREN;
            children = target.getChildren();
            writeChildren(out);
        } else {
            state = STATE_UPDATES;
            writeUpdates(out);
        }
    }

    private void writeUpdates(DSIWriter out) {
        DS2Responder session = getResponder();
        Update update = dequeue();
        while (update != null) {
            encodeUpdate(update, out);
            if (session.shouldEndMessage()) {
                enqueueResponse();
                break;
            }
            update = dequeue();
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // Inner Classes
    ///////////////////////////////////////////////////////////////////////////

    private static class Update {

        boolean added;
        ApiObject child;
        Update next;

        Update(ApiObject child, boolean added) {
            this.child = child;
            this.added = added;
        }
    }

}
