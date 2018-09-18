package com.acuity.iot.dsa.dslink.sys.profiler;

import java.lang.management.MemoryManagerMXBean;
import java.util.ArrayList;
import java.util.List;

public class MemoryManagerNode extends MXBeanNode {

    private MemoryManagerMXBean mxbean;

    public MemoryManagerNode() {
    }

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
    public Object getMXBean() {
        return mxbean;
    }

    @Override
    public Class<? extends Object> getMXInterface() {
        return MemoryManagerMXBean.class;
    }

    private static List<String> overriden = new ArrayList<String>();

    @Override
    public List<String> getOverriden() {
        return overriden;
    }

}
