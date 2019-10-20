package com.acuity.iot.dsa.dslink.sys.profiler;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.iot.dsa.dslink.Action.ResultsType;
import org.iot.dsa.dslink.ActionResults;
import org.iot.dsa.node.DSBool;
import org.iot.dsa.node.DSIValue;
import org.iot.dsa.node.DSInfo;
import org.iot.dsa.node.DSList;
import org.iot.dsa.node.DSLong;
import org.iot.dsa.node.DSMap;
import org.iot.dsa.node.DSString;
import org.iot.dsa.node.action.DSAction;
import org.iot.dsa.node.action.DSIActionRequest;

public class ThreadNode extends MXBeanNode {

    private static List<String> overriden = new ArrayList<String>();
    private Map<Long, ThreadInfoNode> infoNodes = new HashMap<Long, ThreadInfoNode>();
    private ThreadMXBean mxbean;

    public ActionResults findDeadlocked(DSIActionRequest req) {
        long[] ids = mxbean.findDeadlockedThreads();
        return returnDeadlocked(req, ids != null ? ids : new long[0]);
    }

    public ActionResults findMonitorDeadlocked(DSIActionRequest req) {
        long[] ids = mxbean.findMonitorDeadlockedThreads();
        return returnDeadlocked(req, ids != null ? ids : new long[0]);
    }

    @Override
    public Object getMXBean() {
        return mxbean;
    }

    @Override
    public Class<? extends Object> getMXInterface() {
        return ThreadMXBean.class;
    }

    @Override
    public List<String> getOverriden() {
        return overriden;
    }

    @Override
    public void refreshImpl() {
        long[] ids = mxbean.getAllThreadIds();
        DSList l = new DSList();
        for (long id : ids) {
            l.add(id);
        }
        putProp("AllThreadIds", l);
        ThreadInfo[] infos = mxbean.getThreadInfo(ids, false, false);
        Set<Long> prevIds = new HashSet<Long>(infoNodes.keySet());
        for (ThreadInfo info : infos) {
            long id = info.getThreadId();
            prevIds.remove(id);
            ThreadInfoNode infoNode = infoNodes.get(id);
            if (infoNode == null) {
                DSInfo<?> dsinfo = put(
                        info.getThreadName() + " #" + id,
                        new ThreadInfoNode(id)).setTransient(true);
                infoNode = (ThreadInfoNode) dsinfo.getNode();
                infoNodes.put(id, infoNode);
            }
            infoNode.update(info, mxbean.getThreadCpuTime(id), mxbean.getThreadUserTime(id));
        }
        for (long id : prevIds) {
            ThreadInfoNode infoNode = infoNodes.get(id);
            ThreadInfo info = mxbean.getThreadInfo(id);
            if (infoNode != null && info != null) {
                infoNode.update(info, mxbean.getThreadCpuTime(id), mxbean.getThreadUserTime(id));
            } else {
                ThreadInfoNode removed = infoNodes.remove(id);
                if (removed != null) {
                    remove(removed.getInfo());
                }
            }

        }
    }

    @Override
    public void setupMXBean() {
        mxbean = ManagementFactory.getThreadMXBean();
    }

    @Override
    protected void declareDefaults() {
        super.declareDefaults();
        DSAction act = new DSAction() {
            @Override
            public ActionResults invoke(DSIActionRequest req) {
                return ((ThreadNode) req.getTarget()).findDeadlocked(req);
            }
        };
        act.setResultsType(ResultsType.VALUES);
        act.addColumnMetadata("Result", DSString.NULL);
        declareDefault("Find Deadlocked Threads", act);
        act = new DSAction() {
            @Override
            public ActionResults invoke(DSIActionRequest req) {
                return ((ThreadNode) req.getTarget()).findMonitorDeadlocked(req);
            }
        };
        act.setResultsType(ResultsType.VALUES);
        act.addColumnMetadata("Result", DSString.NULL);
        declareDefault("Find Monitor Deadlocked Threads", act);
        act = new DSAction() {

            @Override
            public ActionResults invoke(DSIActionRequest req) {
                return ((ThreadNode) req.getTargetInfo().get()).getThreadDump(this, req);
            }
        };
        act.addParameter("Dump Locked Monitors", DSBool.NULL,
                         "If true, dump all locked monitors");
        act.addParameter("Dump Locked Synschronizers", DSBool.NULL,
                         "If true, dump all locked ownable synchronizers");
        act.addColumnMetadata("Thread Dump", DSString.NULL).setEditor("textarea");
        act.setResultsType(ResultsType.VALUES);
        declareDefault("Get Thread Dump", act);
    }

    protected ActionResults getThreadDump(DSAction action, DSIActionRequest req) {
        DSMap parameters = req.getParameters();
        boolean lockedMonitors = parameters.getBoolean("Dump Locked Monitors");
        boolean lockedSynchronizers = parameters.getBoolean("Dump Locked Synschronizers");
        ThreadInfo[] threads = mxbean.dumpAllThreads(lockedMonitors, lockedSynchronizers);
        StringBuilder dump = new StringBuilder();
        for (ThreadInfo thread : threads) {
            dump.append(thread.toString());
            dump.append('\n');
            //ProfilerUtils.stackTraceToString(thread.getStackTrace());
        }
        return action.makeResults(req, DSString.valueOf(dump.toString()));
    }

    private ActionResults returnDeadlocked(final DSIActionRequest req, long[] deadlockedIds) {
        int len = deadlockedIds.length;
        DSIValue[] ary = new DSIValue[len];
        for (int i = 0; i < len; i++) {
            ary[i] = DSLong.valueOf(deadlockedIds[i]);
        }
        return req.getAction().makeResults(req, ary);
    }

    static {
        overriden.add("AllThreadIds");
    }

}
