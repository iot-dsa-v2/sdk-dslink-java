package com.acuity.iot.dsa.dslink.sys.profiler;

import java.lang.management.PlatformManagedObject;
import java.lang.reflect.Method;
import java.util.List;
import org.iot.dsa.node.DSIObject;
import org.iot.dsa.node.DSInfo;
import org.iot.dsa.node.DSNode;
import org.iot.dsa.node.DSString;
import org.iot.dsa.node.action.ActionInvocation;
import org.iot.dsa.node.action.ActionResult;
import org.iot.dsa.node.action.DSAction;

public abstract class MXBeanNode extends DSNode {

    private static DSAction refreshAction = new DSAction() {
        @Override
        public ActionResult invoke(DSInfo info, ActionInvocation invocation) {
            ((MXBeanNode) info.getParent()).refresh();
            return null;
        }
    };

    @Override
    protected void declareDefaults() {
        super.declareDefaults();
        declareDefault("Refresh", refreshAction);
    }

    @Override
    protected void onStable() {
        setupMXBean();
        refresh();
    }

    private void refresh() {
        refreshImpl();
        discover();
    }

    public abstract void setupMXBean();

    public abstract void refreshImpl();

    public abstract PlatformManagedObject getMXBean();

    public abstract Class<? extends PlatformManagedObject> getMXInterface();

    public abstract List<String> getOverriden();

    public void discover() {
        PlatformManagedObject bean = getMXBean();
        Class<? extends PlatformManagedObject> clazz = getMXInterface();
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
                        warn(e);
                    }
                }
            }
        }
    }

    protected void putProp(String name, DSIObject obj) {
        put(name, obj).setReadOnly(true).setTransient(true);
    }

}
