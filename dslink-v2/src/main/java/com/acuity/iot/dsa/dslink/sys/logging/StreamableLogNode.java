package com.acuity.iot.dsa.dslink.sys.logging;

import java.util.LinkedList;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.Level;
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
import org.iot.dsa.time.DSDateTime;

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
        act.addParameter("Log Name", DSString.NULL, "Optional log name to filter by");
        act.addDefaultParameter("Log Level", LoggerNodeLevel.ALL, "Log level filter");
        act.addParameter("Filter", DSString.NULL, "Optional regex filter");
        act.setResultsType(ResultsType.STREAM);
        act.addColumnMetadata("Timestamp", DSDateTime.NULL);
        act.addColumnMetadata("Log Name", DSString.NULL);
        act.addColumnMetadata("Level", LoggerNodeLevel.ALL);
        act.addColumnMetadata("Message", DSString.NULL).setEditor("textarea");
        return act;
    }

    private ActionResults startLogStream(final DSIActionRequest req) {
        final Logger loggerObj = getLoggerObj();
        final String name = req.getParameters().getString("Log Name");
        final LoggerNodeLevel level = LoggerNodeLevel.make(req.getParameters().getString("Log Level"));
        final String filter = req.getParameters().getString("Filter");
        final List<DSList> lines = new LinkedList<>();
        final Handler handler = new DSLogHandler() {
            @Override
            protected void write(LogRecord record) {
            	String recordName = record.getLoggerName();
            	Level recordLevel = record.getLevel();
            	String recordMsg = record.getMessage();
                DSDateTime ts = DSDateTime.valueOf(record.getMillis());
                if (levelMatches(recordLevel, level.toLevel()) && 
                        (name == null || name.isEmpty() || name.equals(recordName)) &&
                        (filter == null || filter.isEmpty() || recordMsg.matches(filter))) {
                    
                    while (lines.size() > 1000) {
                        lines.remove(0);
                    }
                    lines.add(DSList.valueOf(ts.toString(), recordName, LoggerNodeLevel.valueOf(recordLevel).toString(), recordMsg));
                    req.sendResults();
                }
            }
        };
        loggerObj.addHandler(handler);
        return new ActionResults() {
            @Override
            public int getColumnCount() {
                return 4;
            }

            @Override
            public void getColumnMetadata(int idx, DSMap bucket) {
                if (idx == 0) {
                    new DSMetadata(bucket).setName("Timestamp").setType(DSDateTime.NULL);
                } else if (idx == 1) {
                    new DSMetadata(bucket).setName("Log Name").setType(DSString.NULL);
                } else if (idx == 2) {
                    new DSMetadata(bucket).setName("Level").setType(LoggerNodeLevel.ALL);
                } else if (idx == 3) {
                    new DSMetadata(bucket).setName("Message").setType(DSString.NULL);
                }
            }

            @Override
            public void getResults(DSList bucket) {
                bucket.addAll(lines.remove(0));
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
    
    public static boolean levelMatches(Level msgLevel, Level desiredLevel) {
        return msgLevel.intValue() >= desiredLevel.intValue();
    }

    static {
        levelRange = new DSList();
        DSLevel.ALL.getEnums(levelRange);
    }

}
