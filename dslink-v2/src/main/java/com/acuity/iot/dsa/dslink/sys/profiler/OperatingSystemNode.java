package com.acuity.iot.dsa.dslink.sys.profiler;

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.util.ArrayList;
import java.util.List;

public class OperatingSystemNode extends MXBeanNode {

    private static List<String> overriden = new ArrayList<String>();
    private OperatingSystemMXBean mxbean;

    @Override
    public Object getMXBean() {
        return mxbean;
    }

    @Override
    public Class<? extends Object> getMXInterface() {
        return OperatingSystemMXBean.class;
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
        mxbean = ManagementFactory.getOperatingSystemMXBean();
    }

}
