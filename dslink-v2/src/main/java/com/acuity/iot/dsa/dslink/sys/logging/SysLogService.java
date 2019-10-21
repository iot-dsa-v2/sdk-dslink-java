package com.acuity.iot.dsa.dslink.sys.logging;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import org.iot.dsa.dslink.ActionResults;
import org.iot.dsa.logging.DSLevel;
import org.iot.dsa.node.DSFlexEnum;
import org.iot.dsa.node.DSInfo;
import org.iot.dsa.node.DSList;
import org.iot.dsa.node.DSMap;
import org.iot.dsa.node.DSMetadata;
import org.iot.dsa.node.action.DSAction;
import org.iot.dsa.node.action.DSIActionRequest;

/**
 * @author Daniel Shapiro
 */
public class SysLogService extends StreamableLogNode {

    private DSInfo<DSLevel> levelInfo = (DSInfo<DSLevel>) getInfo("Default Log Level");

    public SysLogService() {
    }

    public DSInfo<DSLevel> getLevelInfo() {
        return levelInfo;
    }

    @Override
    public Logger getLoggerObj() {
        return Logger.getLogger("");
    }

    @Override
    protected void declareDefaults() {
        super.declareDefaults();
        declareDefault("Add Log", getAddLogAction());
        declareDefault("Stream All", getStreamLogAction(true));
        declareDefault("Default Log Level", DSLevel.valueOf(getRootLevel()))
                .setTransient(true);
    }

    @Override
    protected void onChildChanged(DSInfo<?> info) {
        if (info == getLevelInfo()) {
            DSLevel level = (DSLevel) info.get();
            getLoggerObj().setLevel(level.toLevel());
        } else {
            super.onChildChanged(info);
        }
    }

    private void addLog(DSMap parameters) {
        String logName = parameters.getString("Log");
        put(logName, new LoggerNode());
    }

    private DSAction getAddLogAction() {
        DSAction act = new DSAction() {

            @Override
            public ActionResults invoke(DSIActionRequest request) {
                ((SysLogService) request.getTarget()).addLog(request.getParameters());
                return null;
            }

            @Override
            public void prepareParameter(DSInfo<?> target, DSMap parameter) {
                DSMetadata meta = new DSMetadata(parameter);
                if ("Log".equals(meta.getName())) {
                    DSList range = getLogNames();
                    if (range.size() > 0) {
                        meta.setType(DSFlexEnum.valueOf(range.getString(0), range));
                    }
                }
            }
        };
        DSList range = getLogNames();
        act.addParameter("Log", DSFlexEnum.valueOf(range.getString(0), range), null);
        return act;
    }

    private DSList getLogNames() {
        ArrayList<String> list = new ArrayList<>();
        Enumeration<String> logNames = LogManager.getLogManager().getLoggerNames();
        while (logNames.hasMoreElements()) {
            String name = logNames.nextElement();
            if (!name.isEmpty()) {
                list.add(name);
            }
        }
        Collections.sort(list);
        DSList ret = new DSList();
        for (String s : list) {
            ret.add(s);
        }
        return ret;
    }

    private static Level getRootLevel() {
        Logger logger = Logger.getLogger("");
        Level l = Logger.getLogger("").getLevel();
        if (l == null) {
            for (Handler h : logger.getHandlers()) {
                l = h.getLevel();
                if (l != null) {
                    break;
                }
            }
        }
        if (l != null) {
            return l;
        }
        logger.log(Level.WARNING, "Cannot determine root log level");
        return Level.INFO;
    }

}
