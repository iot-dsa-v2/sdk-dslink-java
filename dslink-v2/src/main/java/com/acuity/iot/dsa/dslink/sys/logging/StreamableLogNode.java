package com.acuity.iot.dsa.dslink.sys.logging;

import java.util.Collections;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import org.iot.dsa.logging.DSLevel;
import org.iot.dsa.logging.DSLogHandler;
import org.iot.dsa.node.DSIValue;
import org.iot.dsa.node.DSInfo;
import org.iot.dsa.node.DSList;
import org.iot.dsa.node.DSMap;
import org.iot.dsa.node.DSMetadata;
import org.iot.dsa.node.DSNode;
import org.iot.dsa.node.DSValueType;
import org.iot.dsa.node.action.ActionInvocation;
import org.iot.dsa.node.action.ActionResult;
import org.iot.dsa.node.action.ActionSpec;
import org.iot.dsa.node.action.ActionSpec.ResultType;
import org.iot.dsa.node.action.ActionTable;
import org.iot.dsa.node.action.DSAction;

/**
 * @author Daniel Shapiro
 */
public abstract class StreamableLogNode extends DSNode {

    private static DSList levelRange;

    public abstract DSInfo getLevelInfo();

    public abstract Logger getLoggerObj();

    protected DSAction getStreamLogAction() {
        DSAction act = new DSAction.Parameterless() {

            @Override
            public ActionResult invoke(DSInfo target, ActionInvocation invocation) {
                return ((StreamableLogNode) target.get()).startLogStream(this, target, invocation);
            }

        };
        act.addParameter("Filter", DSValueType.STRING, "Optional regex filter");
        act.setResultType(ResultType.STREAM_TABLE);
        act.addColumnMetadata("Log", DSValueType.STRING).setEditor("textarea");
        return act;
    }

    private ActionTable startLogStream(final DSAction action, final DSInfo targetInfo,
                                       final ActionInvocation invocation) {
        final Logger loggerObj = getLoggerObj();
        final String filter = invocation.getParameters().getString("Filter");
        final Handler handler = new DSLogHandler() {
            @Override
            protected void write(LogRecord record) {
                String line = toString(this, record);
                if (filter == null || line.matches(filter)) {
                    invocation.send(new DSList().add(line));
                }
            }
        };
        loggerObj.addHandler(handler);
        final DSMap col = new DSMetadata().setName("Record").setType(DSValueType.STRING).getMap();
        final List<DSMap> columns = Collections.singletonList(col);

        return new ActionTable() {

            @Override
            public ActionSpec getAction() {
                return action;
            }

            @Override
            public int getColumnCount() {
                return columns.size();
            }

            @Override
            public void getMetadata(int col, DSMap bucket) {
                bucket.putAll(columns.get(col));
            }

            @Override
            public DSIValue getValue(int col) {
                return null;
            }

            @Override
            public boolean next() {
                return false;
            }

            @Override
            public void onClose() {
                handler.close();
                loggerObj.removeHandler(handler);
            }
        };
    }

    static {
        levelRange = new DSList();
        DSLevel.ALL.getEnums(levelRange);
    }

}
