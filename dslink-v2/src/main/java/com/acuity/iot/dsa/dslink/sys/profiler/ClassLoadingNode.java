package com.acuity.iot.dsa.dslink.sys.profiler;

import java.lang.management.ClassLoadingMXBean;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.List;

public class ClassLoadingNode extends MXBeanNode {

    private static List<String> overriden = new ArrayList<String>();
    private ClassLoadingMXBean mxbean;

    @Override
    public Object getMXBean() {
        return mxbean;
    }

    @Override
    public Class<? extends Object> getMXInterface() {
        return ClassLoadingMXBean.class;
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
        mxbean = ManagementFactory.getClassLoadingMXBean();
    }

}
