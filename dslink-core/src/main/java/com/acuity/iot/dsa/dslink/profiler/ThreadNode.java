package com.acuity.iot.dsa.dslink.profiler;

import java.lang.management.ManagementFactory;
import java.lang.management.PlatformManagedObject;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.iot.dsa.node.DSIValue;
import org.iot.dsa.node.DSInfo;
import org.iot.dsa.node.DSList;
import org.iot.dsa.node.DSValueType;
import org.iot.dsa.node.action.ActionInvocation;
import org.iot.dsa.node.action.ActionResult;
import org.iot.dsa.node.action.ActionSpec;
import org.iot.dsa.node.action.ActionValues;
import org.iot.dsa.node.action.ActionSpec.ResultType;
import org.iot.dsa.node.action.DSAction;

public class ThreadNode extends MXBeanNode {

    private ThreadMXBean mxbean;
    private Map<Long, ThreadInfoNode> infoNodes = new HashMap<Long, ThreadInfoNode>();

    @Override
    protected void declareDefaults() {
        super.declareDefaults();
        DSAction act = new DSAction() {
            @Override
            public ActionResult invoke(DSInfo info, ActionInvocation invocation) {
                return ((ThreadNode) info.getParent()).findDeadlocked(info);
            }
        };
        act.setResultType(ResultType.VALUES);
        act.addValueResult("Result", DSValueType.STRING);
        declareDefault("Find Deadlocked Threads", act);

        act = new DSAction() {
            @Override
            public ActionResult invoke(DSInfo info, ActionInvocation invocation) {
                return ((ThreadNode) info.getParent()).findMonitorDeadlocked(info);
            }
        };
        act.setResultType(ResultType.VALUES);
        act.addValueResult("Result", DSValueType.STRING);
        declareDefault("Find Monitor Deadlocked Threads", act);
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
        final DSAction action = actionInfo.getAction();
        DSList l = new DSList();
        for (long id : deadlockedIds) {
            l.add(id);
        }
        final List<DSIValue> values = Collections.singletonList((DSIValue) l);
        return new ActionValues() {

            @Override
            public void onClose() {}

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
                DSInfo dsinfo =
                        add(info.getThreadName(), new ThreadInfoNode(id)).setTransient(true);
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
