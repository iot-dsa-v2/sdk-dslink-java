package com.acuity.iot.dsa.dslink.sys.profiler;

import java.lang.management.ManagementFactory;
import java.lang.management.PlatformManagedObject;
import java.lang.management.RuntimeMXBean;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.iot.dsa.node.DSNode;
import org.iot.dsa.node.DSString;

public class RuntimeNode extends MXBeanNode {

    private RuntimeMXBean mxbean;
    private DSNode systemNode;

    @Override
    protected void declareDefaults() {
        super.declareDefaults();
        declareDefault("SystemProperties", new DSNode());
    }

    @Override
    protected void onStable() {
        systemNode = getNode("SystemProperties");
        super.onStable();
    }

    @Override
    public void setupMXBean() {
        mxbean = ManagementFactory.getRuntimeMXBean();
    }

    @Override
    public void refreshImpl() {
        Map<String, String> properties = mxbean.getSystemProperties();
        for (Entry<String, String> entry : properties.entrySet()) {
            systemNode.put(entry.getKey(), DSString.valueOf(entry.getValue())).setReadOnly(true)
                      .setTransient(true);
        }
        putProp("StartTime",
                DSString.valueOf(Instant.ofEpochMilli(mxbean.getStartTime()).toString()));
        putProp("Uptime", DSString.valueOf(ProfilerUtils.millisToString(mxbean.getUptime())));
    }

    @Override
    public PlatformManagedObject getMXBean() {
        return mxbean;
    }

    @Override
    public Class<? extends PlatformManagedObject> getMXInterface() {
        return RuntimeMXBean.class;
    }


    private static List<String> overriden = new ArrayList<String>();

    static {
        overriden.add("SystemProperties");
        overriden.add("StartTime");
        overriden.add("Uptime");
    }

    @Override
    public List<String> getOverriden() {
        return overriden;
    }

}
