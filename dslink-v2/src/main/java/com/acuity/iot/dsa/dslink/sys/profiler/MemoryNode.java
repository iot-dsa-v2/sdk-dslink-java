package com.acuity.iot.dsa.dslink.sys.profiler;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.util.ArrayList;
import java.util.List;

public class MemoryNode extends MXBeanNode {

    private static List<String> overriden = new ArrayList<>();
    private MemoryMXBean mxbean;

    @Override
    public Object getMXBean() {
        return mxbean;
    }

    @Override
    public Class<?> getMXInterface() {
        return MemoryMXBean.class;
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
        mxbean = ManagementFactory.getMemoryMXBean();
    }

}
