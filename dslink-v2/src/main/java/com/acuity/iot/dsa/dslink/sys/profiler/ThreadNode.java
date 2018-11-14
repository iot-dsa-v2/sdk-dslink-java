package com.acuity.iot.dsa.dslink.sys.profiler;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.iot.dsa.node.DSIValue;
import org.iot.dsa.node.DSInfo;
import org.iot.dsa.node.DSList;
import org.iot.dsa.node.DSMap;
import org.iot.dsa.node.DSString;
import org.iot.dsa.node.DSValueType;
import org.iot.dsa.node.action.ActionInvocation;
import org.iot.dsa.node.action.ActionResult;
import org.iot.dsa.node.action.ActionSpec;
import org.iot.dsa.node.action.ActionSpec.ResultType;
import org.iot.dsa.node.action.ActionValues;
import org.iot.dsa.node.action.DSAction;
import org.iot.dsa.node.action.DSActionValues;

public class ThreadNode extends MXBeanNode {

    private ThreadMXBean mxbean;
    private Map<Long, ThreadInfoNode> infoNodes = new HashMap<Long, ThreadInfoNode>();

    @Override
    protected void declareDefaults() {
        super.declareDefaults();
        DSAction act = new DSAction.Parameterless() {
            @Override
            public ActionResult invoke(DSInfo target, ActionInvocation invocation) {
                return ((ThreadNode) target.get()).findDeadlocked(this);
            }
        };
        act.setResultType(ResultType.VALUES);
        act.addColumnMetadata("Result", DSValueType.STRING);
        declareDefault("Find Deadlocked Threads", act);
        act = new DSAction.Parameterless() {
            @Override
            public ActionResult invoke(DSInfo target, ActionInvocation invocation) {
                return ((ThreadNode) target.getParent()).findMonitorDeadlocked(this);
            }
        };
        act.setResultType(ResultType.VALUES);
        act.addColumnMetadata("Result", DSValueType.STRING);
        declareDefault("Find Monitor Deadlocked Threads", act);
        act = new DSAction.Parameterless() {

            @Override
            public ActionResult invoke(DSInfo target, ActionInvocation invocation) {
                return ((ThreadNode) target.get())
                        .getThreadDump(this, invocation.getParameters());
            }
        };
        act.addParameter("Dump Locked Monitors", DSValueType.BOOL,
                         "If true, dump all locked monitors");
        act.addParameter("Dump Locked Synschronizers", DSValueType.BOOL,
                         "If true, dump all locked ownable synchronizers");
        act.addColumnMetadata("Thread Dump", DSValueType.STRING).setEditor("textarea");
        act.setResultType(ResultType.VALUES);
        declareDefault("Get Thread Dump", act);
    }

    protected ActionResult getThreadDump(DSAction action, DSMap parameters) {
        boolean lockedMonitors = parameters.getBoolean("Dump Locked Monitors");
        boolean lockedSynchronizers = parameters.getBoolean("Dump Locked Synschronizers");
        ThreadInfo[] threads = mxbean.dumpAllThreads(lockedMonitors, lockedSynchronizers);
        StringBuilder dump = new StringBuilder();
        for (ThreadInfo thread : threads) {
            dump.append(thread.toString());
            dump.append('\n');
            //ProfilerUtils.stackTraceToString(thread.getStackTrace());
        }
        return new DSActionValues(action).addResult(DSString.valueOf(dump.toString()));
    }

    public ActionResult findDeadlocked(DSAction action) {
        long[] ids = mxbean.findDeadlockedThreads();
        return returnDeadlocked(action, ids != null ? ids : new long[0]);
    }

    public ActionResult findMonitorDeadlocked(DSAction action) {
        long[] ids = mxbean.findMonitorDeadlockedThreads();
        return returnDeadlocked(action, ids != null ? ids : new long[0]);
    }

    private ActionResult returnDeadlocked(final DSAction action, long[] deadlockedIds) {
        DSList l = new DSList();
        for (long id : deadlockedIds) {
            l.add(id);
        }
        final List<DSIValue> values = Collections.singletonList((DSIValue) l);
        return new ActionValues() {

            @Override
            public void onClose() {
            }

            @Override
            public ActionSpec getAction() {
                return action;
            }

            @Override
            public int getColumnCount() {
                return values.size();
            }

            @Override
            public void getMetadata(int idx, DSMap bucket) {
                action.getColumnMetadata(idx, bucket);
            }

            @Override
            public DSIValue getValue(int idx) {
                return values.get(idx);
            }
        };
    }

    @Override
    public void setupMXBean() {
        mxbean = ManagementFactory.getThreadMXBean();
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
                DSInfo dsinfo = put(
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
    public Object getMXBean() {
        return mxbean;
    }

    @Override
    public Class<? extends Object> getMXInterface() {
        return ThreadMXBean.class;
    }

    private static List<String> overriden = new ArrayList<String>();

    static {
        overriden.add("AllThreadIds");
    }

    @Override
    public List<String> getOverriden() {
        return overriden;
    }

}
