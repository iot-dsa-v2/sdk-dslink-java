package com.acuity.iot.dsa.dslink.sys.logging;

import java.util.LinkedList;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import org.iot.dsa.dslink.Action.ResultsType;
import org.iot.dsa.dslink.ActionResults;
import org.iot.dsa.logging.DSLevel;
import org.iot.dsa.logging.DSLogHandler;
import org.iot.dsa.node.DSInfo;
import org.iot.dsa.node.DSList;
import org.iot.dsa.node.DSMap;
import org.iot.dsa.node.DSMetadata;
import org.iot.dsa.node.DSNode;
import org.iot.dsa.node.DSString;
import org.iot.dsa.node.action.DSAction;
import org.iot.dsa.node.action.DSIActionRequest;

/**
 * @author Daniel Shapiro
 */
public abstract class StreamableLogNode extends DSNode {

    private static DSList levelRange;

    public abstract DSInfo getLevelInfo();

    public abstract Logger getLoggerObj();

    protected DSAction getStreamLogAction() {
        DSAction act = new DSAction() {
            @Override
            public ActionResults invoke(DSIActionRequest req) {
                return ((StreamableLogNode) req.getTarget()).startLogStream(req);
            }
        };
        act.addParameter("Filter", DSString.NULL, "Optional regex filter");
        act.setResultsType(ResultsType.STREAM);
        act.addColumnMetadata("Log", DSString.NULL).setEditor("textarea");
        return act;
    }

    private ActionResults startLogStream(final DSIActionRequest req) {
        final Logger loggerObj = getLoggerObj();
        final String filter = req.getParameters().getString("Filter");
        final List<String> lines = new LinkedList<>();
        final Handler handler = new DSLogHandler() {
            @Override
            protected void write(LogRecord record) {
                String line = toString(this, record);
                if (filter == null || line.matches(filter)) {
                    while (lines.size() > 1000) {
                        lines.remove(0);
                    }
                    lines.add(line);
                    req.sendResults();
                }
            }
        };
        loggerObj.addHandler(handler);
        return new ActionResults() {
            @Override
            public int getColumnCount() {
                return 1;
            }

            @Override
            public void getColumnMetadata(int idx, DSMap bucket) {
                new DSMetadata(bucket).setName("Record").setType(DSString.NULL);
            }

            @Override
            public void getResults(DSList bucket) {
                bucket.add(DSString.valueOf(lines.remove(0)));
            }

            @Override
            public ResultsType getResultsType() {
                return ResultsType.STREAM;
            }

            @Override
            public boolean next() {
                return lines.size() > 0;
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
