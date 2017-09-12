package com.acuity.iot.dsa.dslink.protocol.protocol_v1.responder;

import com.acuity.iot.dsa.dslink.protocol.message.ErrorResponse;
import com.acuity.iot.dsa.dslink.protocol.message.OutboundMessage;
import com.acuity.iot.dsa.dslink.protocol.protocol_v1.DS1Stream;
import java.util.Iterator;
import org.iot.dsa.DSRuntime;
import org.iot.dsa.dslink.responder.ApiObject;
import org.iot.dsa.dslink.responder.InboundListRequest;
import org.iot.dsa.dslink.responder.OutboundListResponse;
import org.iot.dsa.io.DSIWriter;
import org.iot.dsa.node.DSIValue;
import org.iot.dsa.node.DSList;
import org.iot.dsa.node.DSMap;
import org.iot.dsa.node.DSMap.Entry;
import org.iot.dsa.node.DSMetadata;
import org.iot.dsa.node.DSPath;
import org.iot.dsa.node.action.ActionSpec;

/**
 * List implementation for a responder.
 *
 * @author Aaron Hansen
 */
class DS1InboundList extends DS1InboundRequest
        implements DS1Stream, InboundListRequest, OutboundMessage, Runnable {

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
    private DSList cacheList = new DSList();
    private DSMap cacheMap = new DSMap();
    private DSMetadata cacheMeta = new DSMetadata(cacheMap);
    private Iterator<ApiObject> children;
    private Exception closeReason;
    private OutboundListResponse response;
    private boolean enqueued = false;
    private int state = STATE_INIT;
    private Update updateHead;
    private Update updateTail;

    ///////////////////////////////////////////////////////////////////////////
    // Constructors
    ///////////////////////////////////////////////////////////////////////////

    DS1InboundList() {
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
        getSession().removeInboundRequest(getRequestId());
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
        if (displayName != null) {
            out.key("$name").value(displayName);
        }
        child.getMetadata(cacheMap.clear());
        if (cacheMap.contains("is")) {
            out.key("$is").value(cacheMap.getString("is"));
        } else {
            out.key("$is").value("node");
        }
        if (child.isAction()) {
            ActionSpec action = child.getAction();
            if (cacheMap.contains("invokable")) {
                out.key("$invokable").value(cacheMap.get("invokable"));
            } else {
                out.key("$invokable").value(action.getPermission().toString());
            }
        } else if (child.isValue()) {
            out.key("$type");
            encodeType(child.getValue(), cacheMeta, out);
            if (!child.isReadOnly()) {
                out.key("$writable").value(child.isConfig() ? "config" : "write");
            }
        }
        out.endMap().endList();
        cacheMap.clear();
    }

    /**
     * Encode all the meta data about the root target of a list request.
     */
    private void encodeTarget(ApiObject object, DSIWriter out) {
        object.getMetadata(cacheMap.clear());
        if (cacheMap.contains("is")) {
            out.beginList().value("$is").value(cacheMap.getString("is")).endList();
            cacheMap.remove("is");
        } else {
            out.beginList().value("$is").value("node").endList();
        }
        String safeName = object.getName();
        if (DSPath.encodeName(safeName, cacheBuf)) {
            safeName = cacheBuf.toString();
        }
        cacheBuf.setLength(0);
        out.beginList().value("$name").value(safeName).endList();
        if (object.isAction()) {
            encodeTargetAction(object.getAction(), out);
        } else if (object.isValue()) {
            encodeTargetValue(object, out);
        }
        encodeTargetMetadata(cacheMap, out);
        cacheMap.clear();
    }

    private void encodeType(DSIValue value, DSMetadata meta, DSIWriter out) {
        String type = meta.getType();
        if ((type == null) && (value != null)) {
            meta.setType(value);
        }
        fixType(cacheMap);
        out.value(cacheMap.remove(DSMetadata.TYPE));
    }

    /**
     * Called by encodeTarget for actions.
     */
    private void encodeTargetAction(ActionSpec action, DSIWriter out) {
        if (!cacheMap.contains("invokable")) {
            out.beginList()
               .value("$invokable")
               .value(action.getPermission().toString())
               .endList();
        }
        if (!cacheMap.contains("params")) {
            out.beginList().value("$params").beginList();
            Iterator<DSMap> params = action.getParameters();
            if (params != null) {
                while (params.hasNext()) {
                    out.value(fixType(params.next()));
                }
            }
            out.endList().endList();
        }
        if (action.getResultType().isValues()) {
            if (!cacheMap.contains("columns")) {
                out.beginList().value("$columns").beginList();
                Iterator<DSMap> it = action.getValueResults();
                if (it != null) {
                    while (it.hasNext()) {
                        out.value(fixType(it.next()));
                    }
                }
                out.endList().endList();
            }
        }
        if (!cacheMap.contains("result")) {
            if (!action.getResultType().isVoid()) {
                out.beginList()
                   .value("$result")
                   .value(action.getResultType().toString())
                   .endList();
            }
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
        for (int i = 0, len = cacheMap.size(); i < len; i++) {
            entry = cacheMap.getEntry(i);
            out.beginList();
            cacheBuf.append("$");
            DSPath.encodeName(entry.getKey(), cacheBuf);
            out.value(cacheBuf.toString());
            cacheBuf.setLength(0);
            out.value(entry.getValue());
            out.endList();
        }
    }

    /**
     * Called by encodeTarget for values.
     */
    private void encodeTargetValue(ApiObject object, DSIWriter out) {
        DSIValue value = object.getValue();
        if (!cacheMap.contains("type")) {
            out.beginList();
            out.value("$type");
            encodeType(object.getValue(), cacheMeta, out);
            out.endList();
        }
        if (!cacheMap.contains("writable")) {
            if (!object.isReadOnly()) {
                out.beginList()
                   .value("$writable")
                   .value(object.isConfig() ? "config" : "write")
                   .endList();
            }
        }
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
        getSession().sendResponse(this);
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
        getSession().sendResponse(this);
    }

    /**
     * Combines boolean and enum ranges into the type.
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
    public void run() {
        try {
            response = getResponder().onList(this);
        } catch (Exception x) {
            severe(getPath(), x);
            close(x);
        }
        enqueueResponse();
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
    public void write(DSIWriter out) {
        enqueued = false;
        if (isClosed()) {
            return;
        }
        if (isClosePending() && (updateHead == null) && (closeReason != null)) {
            ErrorResponse res = new ErrorResponse(closeReason);
            res.parseRequest(getRequest());
            res.write(out);
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
                getSession().sendResponse(res);
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
                encodeChild(child, out);
                if (getSession().shouldEndMessage()) {
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
        Update update;
        DS1ResponderSession session = getSession();
        while (!session.shouldEndMessage()) {
            update = dequeue();
            if (update == null) {
                break;
            }
            encodeUpdate(update, out);
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

    ///////////////////////////////////////////////////////////////////////////
    // Initialization
    ///////////////////////////////////////////////////////////////////////////

} //Template
