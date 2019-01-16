package com.acuity.iot.dsa.dslink.sys.profiler;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.util.ArrayList;
import java.util.List;

public class MemoryNode extends MXBeanNode {

    private MemoryMXBean mxbean;
    private static List<String> overriden = new ArrayList<String>();

    @Override
    public Object getMXBean() {
        return mxbean;
    }

    @Override
    public Class<? extends Object> getMXInterface() {
        return MemoryMXBean.class;
    }

    @Override
    public List<String> getOverriden() {
        return overriden;
    }

    @Override
    public void refreshImpl() {
        // TODO Auto-generated method stub
    }

    @Override
    public void setupMXBean() {
        mxbean = ManagementFactory.getMemoryMXBean();
    }

}
