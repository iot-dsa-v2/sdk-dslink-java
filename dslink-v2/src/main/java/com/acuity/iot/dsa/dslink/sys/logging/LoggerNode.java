package com.acuity.iot.dsa.dslink.sys.logging;

import java.util.logging.Logger;
import org.iot.dsa.node.DSInfo;

/**
 * @author Daniel Shapiro
 */
public class LoggerNode extends StreamableLogNode {

    private DSInfo<?> levelInfo = getInfo("Log Level");
    private Logger logger;

    public LoggerNode() {
    }

    public DSInfo<?> getLevelInfo() {
        return levelInfo;
    }

    public Logger getLoggerObj() {
        if (logger == null) {
            logger = Logger.getLogger(getName());
        }
        return logger;
    }

    @Override
    protected void declareDefaults() {
        super.declareDefaults();
        declareDefault("Log Level", LoggerNodeLevel.DEFAULT);
        declareDefault("Stream Log", getStreamLogAction(false));
    }

    @Override
    protected void onChildChanged(DSInfo<?> info) {
        if (info == getLevelInfo()) {
            LoggerNodeLevel level = (LoggerNodeLevel) info.get();
            getLoggerObj().setLevel(level.toLevel());
        } else {
            super.onChildChanged(info);
        }
    }

    @Override
    protected void onStable() {
        super.onStable();
        DSInfo<?> info = getLevelInfo();
        LoggerNodeLevel level = (LoggerNodeLevel) info.get();
        getLoggerObj().setLevel(level.toLevel());
    }

}
