package com.acuity.iot.dsa.dslink.protocol.responder;

import com.acuity.iot.dsa.dslink.protocol.DSSession;
import com.acuity.iot.dsa.dslink.protocol.DSStream;
import com.acuity.iot.dsa.dslink.protocol.message.DSTarget;
import com.acuity.iot.dsa.dslink.protocol.message.MessageWriter;
import com.acuity.iot.dsa.dslink.protocol.message.OutboundMessage;
import org.iot.dsa.DSRuntime;
import org.iot.dsa.dslink.Action;
import org.iot.dsa.dslink.ActionResults;
import org.iot.dsa.dslink.DSIResponder;
import org.iot.dsa.dslink.DSPermissionException;
import org.iot.dsa.dslink.DSRequestException;
import org.iot.dsa.dslink.responder.InboundInvokeRequest;
import org.iot.dsa.io.DSIWriter;
import org.iot.dsa.node.DSInfo;
import org.iot.dsa.node.DSList;
import org.iot.dsa.node.DSMap;
import org.iot.dsa.security.DSPermission;

/**
 * Invoke implementation for a responder.
 *
 * @author Aaron Hansen
 */
public class DSInboundInvoke extends DSInboundRequest
        implements DSStream, InboundInvokeRequest, OutboundMessage, Runnable {

    ///////////////////////////////////////////////////////////////////////////
    // Class Fields
    ///////////////////////////////////////////////////////////////////////////

    private static final int STATE_INIT = 0;
    private static final int STATE_ROWS = 1;
    private static final int STATE_UPDATES = 2;
    private static final int STATE_CLOSE_PENDING = 3;
    private static final int STATE_CLOSED = 4;

    ///////////////////////////////////////////////////////////////////////////
    // Instance Fields
    ///////////////////////////////////////////////////////////////////////////

    private DSInfo action;
    private Exception closeReason;
    private boolean enqueued = false;
    private DSMap parameters;
    private DSPermission permission;
    private ActionResults results;
    private int state = STATE_INIT;
    private boolean stream = true;
    private DSInfo target;
    private Update updateHead;
    private Update updateTail;

    ///////////////////////////////////////////////////////////////////////////
    // Constructors
    ///////////////////////////////////////////////////////////////////////////

    public DSInboundInvoke(DSMap parameters, DSPermission permission) {
        this.parameters = parameters;
        this.permission = permission;
    }

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
     * For a broker to use as a pass-thru mechanism.
     */
    public void enqueue(DSMap raw) {
        state = STATE_UPDATES;
        enqueueUpdate(new Update(raw));
    }

    @Override
    public void enqueueResults() {
        synchronized (this) {
            if (enqueued) {
                return;
            }
            enqueued = true;
        }
        getResponder().sendResponse(this);
    }

    @Override
    public DSInfo getActionInfo() {
        return action;
    }

    @Override
    public DSMap getParameters() {
        return parameters;
    }

    @Override
    public DSPermission getPermission() {
        return permission;
    }

    @Override
    public DSInfo getTargetInfo() {
        return target;
    }

    public boolean isClosed() {
        return state == STATE_CLOSED;
    }

    @Override
    public boolean isOpen() {
        return (state != STATE_CLOSED) && (state != STATE_CLOSE_PENDING);
    }

    @Override
    public void onClose(Integer requestId) {
        if (isClosed()) {
            return;
        }
        state = STATE_CLOSED;
        synchronized (this) {
            updateHead = updateTail = null;
        }
        doClose();
    }

    /**
     * Invokes the action and will then enqueueUpdate the outgoing response.
     */
    public void run() {
        try {
            DSTarget path = new DSTarget(getPath(), getLink().getRootNode());
            if (path.isResponder()) {
                DSIResponder responder = (DSIResponder) path.getTarget();
                setPath(path.getPath());
                results = responder.onInvoke(this);
                if (results instanceof RawActionResult) {
                    state = STATE_UPDATES;
                    return;
                }
            } else {
                DSInfo info = path.getTargetInfo();
                if (!info.isAction()) {
                    throw new DSRequestException("Not an action " + path.getPath());
                }
                if (info.isAdmin()) {
                    if (!permission.isConfig()) {
                        throw new DSPermissionException("Config permission required");
                    }
                } else if (!info.isReadOnly()) {
                    if (DSPermission.WRITE.isGreaterThan(permission)) {
                        throw new DSPermissionException("Write permission required");
                    }
                } else {
                    if (DSPermission.READ.isGreaterThan(permission)) {
                        throw new DSPermissionException("Read permission required");
                    }
                }
                action = info;
                target = path.getParentInfo();
                results = path.getNode().invoke(this);
            }
        } catch (Exception x) {
            error(getPath(), x);
            close(x);
            return;
        }
        if (results == null) {
            close();
        } else {
            enqueueResponse();
        }
    }

    /**
     * For v2 only, set to false to auto close the stream after sending the initial state.
     */
    public DSInboundInvoke setStream(boolean stream) {
        this.stream = stream;
        return this;
    }

    @Override
    public boolean write(DSSession session, MessageWriter writer) {
        enqueued = false;
        if (isClosed()) {
            return false;
        }
        if (isClosePending() && (updateHead == null) && (closeReason != null)) {
            getResponder().sendError(this, closeReason);
            doClose();
            return false;
        }
        writeBegin(writer);
        if (results != null) {
            switch (state) {
                case STATE_INIT:
                    writeColumns(writer);
                    writeInitialResults(writer);
                    break;
                case STATE_ROWS:
                    writeInitialResults(writer);
                    break;
                case STATE_CLOSE_PENDING:
                case STATE_UPDATES:
                    writeUpdates(writer);
                    break;
            }
        }
        if (isClosePending() && (updateHead == null)) {
            if (closeReason != null) {
                getResponder().sendError(this, closeReason);
            } else {
                writeClose(writer);
            }
            doClose();
        }
        writeEnd(writer);
        return true;
    }

    ///////////////////////////////////////////////////////////////////////////
    // Protected Methods
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Override point for v2, handles v1.
     */
    protected void writeBegin(MessageWriter writer) {
        writer.getWriter().beginMap().key("rid").value(getRequestId());
    }

    /**
     * Override point for v2, handles v1.
     */
    protected void writeClose(MessageWriter writer) {
        writer.getWriter().key("stream").value("closed");
    }

    /**
     * Override point for v2, handles v1.
     */
    protected void writeEnd(MessageWriter writer) {
        writer.getWriter().endMap();
    }

    /**
     * Override point for v2, handles v1.
     */
    protected void writeOpen(MessageWriter writer) {
        writer.getWriter().key("stream").value("open");
    }

    ///////////////////////////////////////////////////////////////////////////
    // Package / Private Methods
    ///////////////////////////////////////////////////////////////////////////

    private synchronized Update dequeueUpdate() {
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

    /**
     * Handles the transition to close.
     */
    private void doClose() {
        state = STATE_CLOSED;
        getResponder().removeRequest(getRequestId());
        if (results == null) {
            return;
        }
        DSRuntime.run(() -> {
            try {
                results.onClose();
            } catch (Exception x) {
                error(getPath(), x);
            }
        });
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

    private void enqueueUpdate(Update update) {
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
     * Returns "updates" for v1.
     */
    private String getRowsName() {
        return "updates";
    }

    private boolean isClosePending() {
        return state == STATE_CLOSE_PENDING;
    }

    private void writeColumns(MessageWriter writer) {
        DSIWriter out = writer.getWriter();
        Action.ResultsType type = results.getResultsType();
        if (type.isTable() || type.isStream()) {
            out.key("meta")
               .beginMap()
               .key("mode").value(type.isStream() ? "stream" : "append")
               .key("meta").beginMap().endMap()
               .endMap();
        }
        if (results.getColumnCount() > 0) {
            out.key("columns").beginList();
            DSMap col = new DSMap();
            for (int i = 0, len = results.getColumnCount(); i < len; i++) {
                results.getColumnMetadata(i, col.clear());
                out.value(col);
            }
            out.endList();
        } else {
            out.key("columns").beginList().endList();
        }
    }

    private void writeInitialResults(MessageWriter writer) {
        DSIWriter out = writer.getWriter();
        state = STATE_ROWS;
        out.key(getRowsName()).beginList();
        if (results != null) {
            DSList bucket = new DSList();
            DSResponder session = getResponder();
            while (results.next()) {
                results.getResults(bucket.clear());
                out.value(bucket);
                if (session.shouldEndMessage()) {
                    out.endList();
                    enqueueResponse();
                    return;
                }
            }
        }
        out.endList();
        if ((results == null) || results.getResultsType().isVoid() || !stream) {
            writeClose(writer);
            state = STATE_CLOSED;
            doClose();
        } else {
            writeOpen(writer);
            state = STATE_UPDATES;
        }
    }

    /**
     * Only called by writeUpdates.
     */
    private void writeRaw(MessageWriter writer) {
        Update update = dequeueUpdate();
        if (update == null) {
            return;
        }
        DSIWriter out = writer.getWriter();
        DSMap map;
        map = update.raw;
        map.remove("rid");
        for (DSMap.Entry e : map) {
            out.key(e.getKey());
            out.value(e.getValue());
        }
        String stream = map.get("stream", "");
        if (stream.equals("closed")) {
            doClose();
        }
    }

    private void writeUpdates(MessageWriter writer) {
        DSIWriter out = writer.getWriter();
        if (updateHead != null) {
            writeRaw(writer);
            return;
        }
        if (results.next()) {
            DSList bucket = new DSList();
            DSResponder session = getResponder();
            out.key(getRowsName()).beginList();
            do {
                results.getResults(bucket.clear());
                out.value(bucket);
                if (session.shouldEndMessage()) {
                    out.endList();
                    enqueueResponse();
                    return;
                }
            } while (results.next());
            out.endList();
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // Inner Classes
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Used as a pass through on the broker.
     */
    public interface RawActionResult extends ActionResults {

    }

    /**
     * Describes an update to be sent to the requester.
     */
    protected static class Update {

        Update next;
        DSMap raw;

        Update(DSMap raw) {
            this.raw = raw;
        }

    }

}
