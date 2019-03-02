package com.acuity.iot.dsa.dslink.sys.profiler;

import java.lang.management.GarbageCollectorMXBean;
import java.util.ArrayList;
import java.util.List;
import org.iot.dsa.node.DSString;

public class GarbageCollectorNode extends MXBeanNode {

    private static List<String> overriden = new ArrayList<String>();
    private GarbageCollectorMXBean mxbean;

    public GarbageCollectorNode() {
    }

    public GarbageCollectorNode(GarbageCollectorMXBean mxbean) {
        this.mxbean = mxbean;
    }

    @Override
    public Object getMXBean() {
        return mxbean;
    }

    @Override
    public Class<? extends Object> getMXInterface() {
        return GarbageCollectorMXBean.class;
    }

    @Override
    public List<String> getOverriden() {
        return overriden;
    }

    @Override
    public void refreshImpl() {
        putProp("CollectionTime",
                DSString.valueOf(ProfilerUtils.millisToString(mxbean.getCollectionTime())));
    }

    @Override
    public void setupMXBean() {
        if (mxbean == null) {
            getParent().remove(getInfo());
        }
    }

    static {
        overriden.add("CollectionTime");
    }

}
