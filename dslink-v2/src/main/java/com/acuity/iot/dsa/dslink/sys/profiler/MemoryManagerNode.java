package com.acuity.iot.dsa.dslink.sys.profiler;

import java.lang.management.MemoryManagerMXBean;
import java.util.ArrayList;
import java.util.List;

public class MemoryManagerNode extends MXBeanNode {

    private static List<String> overriden = new ArrayList<>();
    private MemoryManagerMXBean mxbean;

    public MemoryManagerNode() {
    }

    public MemoryManagerNode(MemoryManagerMXBean mxbean) {
        this.mxbean = mxbean;
    }

    @Override
    public Object getMXBean() {
        return mxbean;
    }

    @Override
    public Class<?> getMXInterface() {
        return MemoryManagerMXBean.class;
    }

    @Override
    public List<String> getOverriden() {
        return overriden;
    }

    @Override
    public void refreshImpl() {
    }

    @Override
    public void setupMXBean() {
        if (mxbean == null) {
            getParent().remove(getInfo());
        }
    }

}
