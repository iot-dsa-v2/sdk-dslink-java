package com.acuity.iot.dsa.dslink.sys.logging;

import java.util.logging.Logger;
import org.iot.dsa.node.DSIObject;
import org.iot.dsa.node.DSInfo;
import org.iot.dsa.node.DSMap;
import org.iot.dsa.node.action.ActionInvocation;
import org.iot.dsa.node.action.ActionResult;
import org.iot.dsa.node.action.DSAbstractAction;

/**
 * @author Daniel Shapiro
 */
public class LoggerNode extends StreamableLogNode {

    private DSInfo levelInfo = getInfo("Log Level");
    private Logger logger;

    public LoggerNode() {
    }

    public DSInfo getLevelInfo() {
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
        declareDefault("Remove", getRemoveAction());
        declareDefault("Stream Log", getStreamLogAction());
    }

    private DSIObject getRemoveAction() {
        DSAbstractAction act = new DSAbstractAction() {

            @Override
            public ActionResult invoke(DSInfo info, ActionInvocation invocation) {
                ((LoggerNode) info.getParent()).remove();
                return null;
            }

            @Override
            public void prepareParameter(DSInfo info, DSMap parameter) {
            }
        };
        return act;
    }

    @Override
    protected void onChildChanged(DSInfo info) {
        if (info == getLevelInfo()) {
            LoggerNodeLevel level = (LoggerNodeLevel) info.getObject();
            getLoggerObj().setLevel(level.toLevel());
        } else {
            super.onChildChanged(info);
        }
    }

    @Override
    protected void onStable() {
        super.onStable();
        DSInfo info = getLevelInfo();
        LoggerNodeLevel level = (LoggerNodeLevel) info.getObject();
        getLoggerObj().setLevel(level.toLevel());
    }

    private void remove() {
        getLoggerObj().setLevel(null);
        getParent().remove(getInfo());
    }

}
