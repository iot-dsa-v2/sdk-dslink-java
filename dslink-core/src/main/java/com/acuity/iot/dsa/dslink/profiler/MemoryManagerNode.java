package com.acuity.iot.dsa.dslink.profiler;

import java.lang.management.MemoryManagerMXBean;
import java.lang.management.PlatformManagedObject;
import java.util.ArrayList;
import java.util.List;

public class MemoryManagerNode extends MXBeanNode {

    private MemoryManagerMXBean mxbean;

    public MemoryManagerNode() {}

    public MemoryManagerNode(MemoryManagerMXBean mxbean) {
        this.mxbean = mxbean;
    }

    @Override
    public void setupMXBean() {
        if (mxbean == null) {
            getParent().remove(getInfo());
        }
    }

    @Override
    public void refreshImpl() {
        // TODO Auto-generated method stub

    }

    @Override
    public PlatformManagedObject getMXBean() {
        return mxbean;
    }

    @Override
    public Class<? extends PlatformManagedObject> getMXInterface() {
        return MemoryManagerMXBean.class;
    }

    private static List<String> overriden = new ArrayList<String>();

    @Override
    public List<String> getOverriden() {
        return overriden;
    }

}
