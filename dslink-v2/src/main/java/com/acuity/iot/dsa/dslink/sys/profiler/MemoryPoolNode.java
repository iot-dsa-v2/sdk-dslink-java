package com.acuity.iot.dsa.dslink.sys.profiler;

import java.lang.management.MemoryPoolMXBean;
import java.util.ArrayList;
import java.util.List;

public class MemoryPoolNode extends MXBeanNode {

    private static List<String> overriden = new ArrayList<String>();
    private MemoryPoolMXBean mxbean;

    public MemoryPoolNode() {
    }

    public MemoryPoolNode(MemoryPoolMXBean mxbean) {
        this.mxbean = mxbean;
    }

    @Override
    public Object getMXBean() {
        return mxbean;
    }

    @Override
    public Class<? extends Object> getMXInterface() {
        return MemoryPoolMXBean.class;
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
