package com.acuity.iot.dsa.dslink.sys.profiler;

import java.lang.management.CompilationMXBean;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.List;
import org.iot.dsa.node.DSString;

public class CompilationNode extends MXBeanNode {

    private static List<String> overriden = new ArrayList<>();
    private CompilationMXBean mxbean;

    @Override
    public Object getMXBean() {
        return mxbean;
    }

    @Override
    public Class<?> getMXInterface() {
        return CompilationMXBean.class;
    }

    @Override
    public List<String> getOverriden() {
        return overriden;
    }

    @Override
    public void refreshImpl() {
        putProp("TotalCompilationTime",
                DSString.valueOf(ProfilerUtils.millisToString(mxbean.getTotalCompilationTime())));
    }

    @Override
    public void setupMXBean() {
        mxbean = ManagementFactory.getCompilationMXBean();
    }

    static {
        overriden.add("TotalCompilationTime");
    }

}
