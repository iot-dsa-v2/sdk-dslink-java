package com.acuity.iot.dsa.dslink.sys.profiler;

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.PlatformManagedObject;
import java.util.ArrayList;
import java.util.List;

public class OperatingSystemNode extends MXBeanNode {

    private OperatingSystemMXBean mxbean;

    @Override
    public void setupMXBean() {
        mxbean = ManagementFactory.getOperatingSystemMXBean();
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
        return OperatingSystemMXBean.class;
    }

    private static List<String> overriden = new ArrayList<String>();

    @Override
    public List<String> getOverriden() {
        return overriden;
    }

}
