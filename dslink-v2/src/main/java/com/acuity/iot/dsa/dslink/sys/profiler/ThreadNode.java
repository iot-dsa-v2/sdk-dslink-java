package com.acuity.iot.dsa.dslink.sys.profiler;

import java.lang.management.ManagementFactory;
import java.lang.management.PlatformManagedObject;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.util.*;
import org.iot.dsa.node.DSIValue;
import org.iot.dsa.node.DSInfo;
import org.iot.dsa.node.DSList;
import org.iot.dsa.node.DSMap;
import org.iot.dsa.node.DSString;
import org.iot.dsa.node.DSValueType;
import org.iot.dsa.node.action.*;
import org.iot.dsa.node.action.ActionSpec.ResultType;

public class ThreadNode extends MXBeanNode {

    private ThreadMXBean mxbean;
    private Map<Long, ThreadInfoNode> infoNodes = new HashMap<Long, ThreadInfoNode>();

    @Override
    protected void declareDefaults() {
        super.declareDefaults();
        DSAbstractAction act = new DSAbstractAction() {
            @Override
            public ActionResult invoke(DSInfo info, ActionInvocation invocation) {
                return ((ThreadNode) info.getParent()).findDeadlocked(info);
            }

            @Override
            public void prepareParameter(DSInfo info, DSMap parameter) {
            }
        };
        act.setResultType(ResultType.VALUES);
        act.addValueResult("Result", DSValueType.STRING);
        declareDefault("Find Deadlocked Threads", act);

        act = new DSAbstractAction() {
            @Override
            public ActionResult invoke(DSInfo info, ActionInvocation invocation) {
                return ((ThreadNode) info.getParent()).findMonitorDeadlocked(info);
            }

            @Override
            public void prepareParameter(DSInfo info, DSMap parameter) {
            }
        };
        act.setResultType(ResultType.VALUES);
        act.addValueResult("Result", DSValueType.STRING);
        declareDefault("Find Monitor Deadlocked Threads", act);
        
        act = new DSAbstractAction() {
            
            @Override
            public void prepareParameter(DSInfo info, DSMap parameter) {
            }
            
            @Override
            public ActionResult invoke(DSInfo info, ActionInvocation invocation) {
                return ((ThreadNode) info.getParent()).getThreadDump(info, invocation.getParameters());
            }
        };
        act.addParameter("Dump Locked Monitors", DSValueType.BOOL, "If true, dump all locked monitors");
        act.addParameter("Dump Locked Synschronizers", DSValueType.BOOL, "If true, dump all locked ownable synchronizers");
        act.addValueResult("Thread Dump", DSValueType.STRING).setEditor("textarea");
        act.setResultType(ResultType.VALUES);
        declareDefault("Get Thread Dump", act);
    }

    protected ActionResult getThreadDump(DSInfo info, DSMap parameters) {
        boolean lockedMonitors = parameters.getBoolean("Dump Locked Monitors");
        boolean lockedSynchronizers = parameters.getBoolean("Dump Locked Synschronizers");
        ThreadInfo[] threads = mxbean.dumpAllThreads(lockedMonitors, lockedSynchronizers);
        StringBuilder dump = new StringBuilder();
        for (ThreadInfo thread : threads) {
            dump.append(thread.toString());
            dump.append('\n');
            //ProfilerUtils.stackTraceToString(thread.getStackTrace());
        }
        return new DSActionValues(info.getAction()).addResult(DSString.valueOf(dump.toString()));
    }

    public ActionResult findDeadlocked(DSInfo actionInfo) {
        long[] ids = mxbean.findDeadlockedThreads();
        return returnDeadlocked(actionInfo, ids != null ? ids : new long[0]);
    }

    public ActionResult findMonitorDeadlocked(DSInfo actionInfo) {
        long[] ids = mxbean.findMonitorDeadlockedThreads();
        return returnDeadlocked(actionInfo, ids != null ? ids : new long[0]);
    }

    private ActionResult returnDeadlocked(DSInfo actionInfo, long[] deadlockedIds) {
        final DSAbstractAction action = actionInfo.getAction();
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
            public Iterator<DSIValue> getValues() {
                return values.iterator();
            }
        };
    }

    @Override
    public void setupMXBean() {
        mxbean = ManagementFactory.getThreadMXBean();
    }

    @Override
    public void refreshImpl() {
        clear();
        long[] ids = mxbean.getAllThreadIds();
        DSList l = new DSList();
        for (long id : ids) {
            l.add(id);
        }
        putProp("AllThreadIds", l);
        ThreadInfo[] infos = mxbean.getThreadInfo(ids, false, false);
        for (ThreadInfo info : infos) {
            long id = info.getThreadId();
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
    }

    @Override
    public PlatformManagedObject getMXBean() {
        return mxbean;
    }

    @Override
    public Class<? extends PlatformManagedObject> getMXInterface() {
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
