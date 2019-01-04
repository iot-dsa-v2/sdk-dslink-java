package com.acuity.iot.dsa.dslink.protocol.responder;

import com.acuity.iot.dsa.dslink.protocol.DSSession;
import com.acuity.iot.dsa.dslink.protocol.DSStream;
import com.acuity.iot.dsa.dslink.protocol.message.MessageWriter;
import com.acuity.iot.dsa.dslink.protocol.message.OutboundMessage;
import com.acuity.iot.dsa.dslink.protocol.message.DSTarget;
import org.iot.dsa.DSRuntime;
import org.iot.dsa.dslink.DSIResponder;
import org.iot.dsa.dslink.DSPermissionException;
import org.iot.dsa.dslink.DSRequestException;
import org.iot.dsa.dslink.responder.InboundInvokeRequest;
import org.iot.dsa.io.DSIWriter;
import org.iot.dsa.node.DSInfo;
import org.iot.dsa.node.DSList;
import org.iot.dsa.node.DSMap;
import org.iot.dsa.node.action.ActionResult;
import org.iot.dsa.node.action.ActionSpec;
import org.iot.dsa.node.action.ActionTable;
import org.iot.dsa.node.action.ActionValues;
import org.iot.dsa.node.action.DSAction;
import org.iot.dsa.security.DSPermission;
import org.iot.dsa.table.DSIRow;
import org.iot.dsa.table.DSIRowCursor;

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

    private Exception closeReason;
    private boolean enqueued = false;
    private DSMap parameters;
    private DSPermission permission;
    private ActionResult result;
    private int state = STATE_INIT;
    private boolean stream = true;
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
    public void clearAllRows() {
        enqueueUpdate(new Update(null, UpdateType.REFRESH));
    }

    @Override
    public void close() {
        if (!isOpen()) {
            return;
        }
        state = STATE_CLOSE_PENDING;
        enqueueResponse();
        debug(debug() ? getPath() + " invoke closed locally" : null);
    }

    @Override
    public void close(Exception reason) {
        if (!isOpen()) {
            return;
        }
        closeReason = reason;
        state = STATE_CLOSE_PENDING;
        enqueueResponse();
        debug(debug() ? getPath() + " invoke closed locally" : null);
    }

    /**
     * Any parameters supplied by the requester for the invocation, or null.
     */
    @Override
    public DSMap getParameters() {
        return parameters;
    }

    @Override
    public DSPermission getPermission() {
        return permission;
    }

    @Override
    public void insert(int index, DSList[] rows) {
        enqueueUpdate(new Update(rows, index, -1, UpdateType.INSERT));
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
        debug(debug() ? getPath() + " invoke closed" : null);
        synchronized (this) {
            updateHead = updateTail = null;
        }
        doClose();
    }

    @Override
    public void replace(int index, int len, DSList... rows) {
        if (len < 1) {
            throw new IllegalArgumentException("Invalid length: " + len);
        }
        enqueueUpdate(new Update(rows, index, len, UpdateType.REPLACE));
    }

    /**
     * Invokes the action and will then enqueueUpdate the outgoing response.
     */
    public void run() {
        try {
            DSTarget path = new DSTarget(getPath(), getLink());
            if (path.isResponder()) {
                DSIResponder responder = (DSIResponder) path.getTarget();
                setPath(path.getPath());
                result = responder.onInvoke(this);
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
                result = path.getNode().invoke(info, path.getParentInfo(), this);
            }
        } catch (Exception x) {
            error(getPath(), x);
            close(x);
            return;
        }
        if (result == null) {
            close();
        } else {
            enqueueResponse();
        }
    }

    @Override
    public void send(DSList row) {
        enqueueUpdate(new Update(row));
    }

    /**
     * For v2 only, set to false to auto close the stream after sending the initial state.
     */
    public DSInboundInvoke setStream(boolean stream) {
        this.stream = stream;
        return this;
    }

    @Override
    public void write(DSSession session, MessageWriter writer) {
        enqueued = false;
        if (isClosed()) {
            return;
        }
        if (isClosePending() && (updateHead == null) && (closeReason != null)) {
            getResponder().sendError(this, closeReason);
            doClose();
            return;
        }
        writeBegin(writer);
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
        if (isClosePending() && (updateHead == null)) {
            if (closeReason != null) {
                getResponder().sendError(this, closeReason);
            } else {
                writeClose(writer);
            }
            doClose();
        }
        writeEnd(writer);
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
        if (result == null) {
            return;
        }
        DSRuntime.run(new Runnable() {
            @Override
            public void run() {
                try {
                    result.onClose();
                } catch (Exception x) {
                    error(getPath(), x);
                }
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
        if (result instanceof ActionTable) {
            DSMap tableMeta = ((ActionTable) result).getMetadata();
            if (tableMeta == null) {
                tableMeta = new DSMap();
            }
            ActionSpec action = result.getAction();
            out.key("meta")
               .beginMap()
               .key("mode").value(action.getResultType().isStream() ? "stream" : "append")
               .key("meta").value(tableMeta)
               .endMap();
            out.key("columns").beginList();
            ActionTable tbl = (ActionTable) result;
            DSMap col = new DSMap();
            for (int i = 0, len = tbl.getColumnCount(); i < len; i++) {
                tbl.getMetadata(i, col.clear());
                out.value(col);
            }
            out.endList();
        } else if (result instanceof ActionValues) {
            out.key("columns").beginList();
            ActionValues vals = (ActionValues) result;
            DSMap col = new DSMap();
            for (int i = 0, len = vals.getColumnCount(); i < len; i++) {
                vals.getMetadata(i, col.clear());
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
        if (result instanceof DSIRowCursor) {
            DSResponder session = getResponder();
            DSIRowCursor cur = (DSIRowCursor) result;
            int cols = cur.getColumnCount();
            while (cur.next()) {
                out.beginList();
                for (int i = 0; i < cols; i++) {
                    out.value(cur.getValue(i).toElement());
                }
                out.endList();
                if (session.shouldEndMessage()) {
                    out.endList();
                    enqueueResponse();
                    return;
                }
            }
        } else if (result instanceof DSIRow) {
            DSIRow row = (DSIRow) result;
            int cols = row.getColumnCount();
            out.beginList();
            for (int i = 0; i < cols; i++) {
                out.value(row.getValue(i).toElement());
            }
            out.endList();
        }
        out.endList();
        if ((result == null) || !result.getAction().getResultType().isOpen() || !stream) {
            writeClose(writer);
            state = STATE_CLOSED;
            doClose();
        } else {
            writeOpen(writer);
            state = STATE_UPDATES;
        }
    }

    private void writeUpdates(MessageWriter writer) {
        DSIWriter out = writer.getWriter();
        Update update = updateHead; //peak ahead
        if (update == null) {
            return;
        }
        if (update.type != null) {
            out.key("meta")
               .beginMap()
               .key(update.typeKey()).value(update.typeValue())
               .key("meta").beginMap().endMap()
               .endMap();
        }
        out.key(getRowsName()).beginList();
        DSResponder responder = getResponder();
        while (true) {
            update = dequeueUpdate();
            if (update.rows != null) {
                for (DSList row : update.rows) {
                    out.value(row);
                }
            } else if (update.row != null) {
                out.value(update.row);
            }
            if ((updateHead == null) || (updateHead.type != null)) {
                break;
            }
            if (responder.shouldEndMessage()) {
                enqueueResponse();
                break;
            }
        }
        out.endList();
    }

    ///////////////////////////////////////////////////////////////////////////
    // Inner Classes
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Used to described more complex updates.
     */
    protected enum UpdateType {
        INSERT("insert"),
        REFRESH("refresh"),
        REPLACE("replace");

        private String display;

        UpdateType(String display) {
            this.display = display;
        }

        public String toString() {
            return display;
        }
    }

    /**
     * Describes an update to be sent to the requester.
     */
    protected static class Update {

        int beginIndex = -1;
        int endIndex = -1;
        Update next;
        DSList row;
        DSList[] rows;
        UpdateType type;

        Update(DSList row) {
            this.row = row;
        }

        Update(DSList row, UpdateType type) {
            this.row = row;
            this.type = type;
        }

        Update(DSList[] rows, int index, int len, UpdateType type) {
            this.rows = rows;
            this.beginIndex = index;
            if (len > 0) {
                this.endIndex = index + len - 1; //inclusive end
            }
            this.type = type;
        }

        String typeKey() {
            if ((type == UpdateType.INSERT) || (type == UpdateType.REPLACE)) {
                return "modify";
            } else {
                return "mode";
            }
        }

        String typeValue() {
            if (type == UpdateType.REFRESH) {
                return type.toString();
            }
            if (type == UpdateType.INSERT) {
                return type.toString() + " " + beginIndex;
            }
            return type.toString() + " " + beginIndex + "-" + endIndex;
        }
    }

}
