package com.acuity.iot.dsa.dslink.sys.profiler;

import java.lang.management.MemoryPoolMXBean;
import java.lang.reflect.Method;
import java.util.List;
import org.iot.dsa.DSRuntime;
import org.iot.dsa.DSRuntime.Timer;
import org.iot.dsa.node.DSIObject;
import org.iot.dsa.node.DSInfo;
import org.iot.dsa.node.DSNode;
import org.iot.dsa.node.DSString;
import org.iot.dsa.node.action.ActionInvocation;
import org.iot.dsa.node.action.ActionResult;
import org.iot.dsa.node.action.DSAction;

public abstract class MXBeanNode extends DSNode implements Runnable {

    private Timer pollTimer;

    private static DSAction refreshAction = new DSAction.Parameterless() {
        @Override
        public ActionResult invoke(DSInfo target, ActionInvocation invocation) {
            ((MXBeanNode) target.getObject()).refresh();
            return null;
        }

    };

    public void discover() {
        Object bean = getMXBean();
        Class<? extends Object> clazz = getMXInterface();
        for (Method meth : clazz.getMethods()) {
            String methName = meth.getName();
            if (meth.getParameterCount() == 0 && meth.getReturnType() != Void.TYPE) {
                String name;
                if (methName.startsWith("get")) {
                    name = methName.substring(3);
                } else if (methName.startsWith("is")) {
                    name = methName.substring(2);
                } else {
                    continue;
                }
                if (!name.isEmpty() && !getOverriden().contains(name)) {
                    try {
                        Object o = meth.invoke(bean);
                        putProp(name,
                                o != null ? ProfilerUtils.objectToDSIValue(o) : DSString.EMPTY);
                    } catch (Exception e) {
                        if (bean instanceof MemoryPoolMXBean && (name.startsWith("UsageThreshold")
                                || name.startsWith("CollectionUsageThreshold"))) {
                            // ignore
                        } else {
                            debug("Exception when invoking " + clazz.getName() + "." + meth
                                    .getName() + " on " + getName() + ": ", e);
                        }
                    }
                }
            }
        }
    }

    public abstract Object getMXBean();

    public abstract Class<? extends Object> getMXInterface();

    public abstract List<String> getOverriden();

    public abstract void refreshImpl();

    @Override
    public void run() {
        if (isTreeSubscribed()) {
            refresh();
        }
    }

    public abstract void setupMXBean();

    @Override
    protected void declareDefaults() {
        super.declareDefaults();
        declareDefault("Refresh", refreshAction);
    }

    @Override
    protected void onStable() {
        setupMXBean();
        refresh();
        setupPolling();
    }

    @Override
    protected void onStopped() {
        pollTimer.cancel();
    }

    protected void putProp(String name, DSIObject obj) {
        put(name, obj).setReadOnly(true).setTransient(true);
    }

    private void refresh() {
        refreshImpl();
        discover();
    }

    private void setupPolling() {
        pollTimer = DSRuntime.run(this, 0, 5000);
    }
}
