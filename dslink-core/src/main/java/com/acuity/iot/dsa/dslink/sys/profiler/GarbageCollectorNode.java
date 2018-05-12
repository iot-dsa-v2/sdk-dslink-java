package com.acuity.iot.dsa.dslink.sys.profiler;

import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.PlatformManagedObject;
import java.util.ArrayList;
import java.util.List;
import org.iot.dsa.node.DSString;

public class GarbageCollectorNode extends MXBeanNode {

    private GarbageCollectorMXBean mxbean;

    public GarbageCollectorNode() {
    }

    public GarbageCollectorNode(GarbageCollectorMXBean mxbean) {
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
        putProp("CollectionTime",
                DSString.valueOf(ProfilerUtils.millisToString(mxbean.getCollectionTime())));
    }

    @Override
    public PlatformManagedObject getMXBean() {
        return mxbean;
    }

    @Override
    public Class<? extends PlatformManagedObject> getMXInterface() {
        return GarbageCollectorMXBean.class;
    }

    private static List<String> overriden = new ArrayList<String>();

    static {
        overriden.add("CollectionTime");
    }

    @Override
    public List<String> getOverriden() {
        return overriden;
    }

}
