package com.acuity.iot.dsa.dslink.sys.profiler;

import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryManagerMXBean;
import java.lang.management.MemoryPoolMXBean;
import org.iot.dsa.node.DSNode;

public class SysProfiler extends DSNode {

    private DSNode gcNode;
    private DSNode mmNode;
    private DSNode mpNode;

    @Override
    public String getLogName() {
        return getLogName("profiler");
    }

    @Override
    protected void declareDefaults() {
        super.declareDefaults();
        declareDefault("Runtime", new RuntimeNode()).setTransient(true);
        declareDefault("Class Loading", new ClassLoadingNode()).setTransient(true);
        declareDefault("Compilation", new CompilationNode()).setTransient(true);
        declareDefault("Memory", new MemoryNode()).setTransient(true);
        declareDefault("Operating System", new OperatingSystemNode()).setTransient(true);
        declareDefault("Thread", new ThreadNode()).setTransient(true);
        declareDefault("Garbage Collectors", new DSNode()).setTransient(true);
        declareDefault("Memory Managers", new DSNode()).setTransient(true);
        declareDefault("Memory Pools", new DSNode()).setTransient(true);
    }

    @Override
    protected void onStable() {
        gcNode = getNode("Garbage Collectors");
        mmNode = getNode("Memory Managers");
        mpNode = getNode("Memory Pools");
        for (GarbageCollectorMXBean mxbean : ManagementFactory.getGarbageCollectorMXBeans()) {
            gcNode.put(mxbean.getName(), new GarbageCollectorNode(mxbean)).setTransient(true);
        }
        for (MemoryManagerMXBean mxbean : ManagementFactory.getMemoryManagerMXBeans()) {
            mmNode.put(mxbean.getName(), new MemoryManagerNode(mxbean)).setTransient(true);
        }
        for (MemoryPoolMXBean mxbean : ManagementFactory.getMemoryPoolMXBeans()) {
            mpNode.put(mxbean.getName(), new MemoryPoolNode(mxbean)).setTransient(true);
        }
    }

}
