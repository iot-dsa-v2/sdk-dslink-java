package com.acuity.iot.dsa.dslink.profiler;

import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryManagerMXBean;
import java.lang.management.MemoryPoolMXBean;
import org.iot.dsa.node.DSNode;

public class ProfilerNode extends DSNode {

    private DSNode gcNode;
    private DSNode mmNode;
    private DSNode mpNode;

    @Override
    protected void declareDefaults() {
        super.declareDefaults();
        declareDefault("Runtime", new RuntimeNode());
        declareDefault("Class Loading", new ClassLoadingNode());
        declareDefault("Compilation", new CompilationNode());
        declareDefault("Memory", new MemoryNode());
        declareDefault("Operating System", new OperatingSystemNode());
        declareDefault("Thread", new ThreadNode());
        declareDefault("Garbage Collectors", new DSNode());
        declareDefault("Memory Managers", new DSNode());
        declareDefault("Memory Pools", new DSNode());
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
