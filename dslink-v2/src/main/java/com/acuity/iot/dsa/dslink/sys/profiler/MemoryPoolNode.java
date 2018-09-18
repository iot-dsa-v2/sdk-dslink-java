package com.acuity.iot.dsa.dslink.sys.profiler;

import java.lang.management.MemoryPoolMXBean;
import java.util.ArrayList;
import java.util.List;

public class MemoryPoolNode extends MXBeanNode {

    private MemoryPoolMXBean mxbean;

    public MemoryPoolNode() {
    }

    public MemoryPoolNode(MemoryPoolMXBean mxbean) {
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
        return MemoryPoolMXBean.class;
    }

    private static List<String> overriden = new ArrayList<String>();

    @Override
    public List<String> getOverriden() {
        return overriden;
    }

}
