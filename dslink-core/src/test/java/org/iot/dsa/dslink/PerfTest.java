package org.iot.dsa.dslink;

import org.iot.dsa.node.DSBool;
import org.iot.dsa.node.DSDouble;
import org.iot.dsa.node.DSInfo;
import org.iot.dsa.node.DSInt;
import org.iot.dsa.node.DSNode;
import org.iot.dsa.node.DSString;
import org.iot.dsa.node.action.DSAction;
import org.junit.Test;

/**
 * @author Aaron Hansen
 */
public class PerfTest {

    private static final boolean WITH_DEFAULTS = true;
    private DSNode root;

    private String format(long mem) {
        return (mem / 1000) + "KB";
    }

    private void iterate(DSNode node) {
        DSInfo info = node.getFirstInfo();
        while (info != null) {
            if (info.isNode()) {
                iterate(info.getNode());
            }
            info = info.next();
        }
    }

    private DSNode makeNode() {
        if (WITH_DEFAULTS) {
            return new TestNode();
        }
        DSNode ret = new DSNode();
        ret.put("string", "abc");
        ret.put("int", DSInt.valueOf(1000));
        ret.put("double", DSDouble.valueOf(12345.1d));
        ret.put("boolean", DSBool.valueOf(true));
        return ret;
    }

    private long memoryUsed() {
        Runtime rt = Runtime.getRuntime();
        long mem = rt.totalMemory() - rt.freeMemory();
        long tmp;
        for (int i = 2; --i >= 0; ) {
            System.gc();
            tmp = rt.totalMemory() - rt.freeMemory();
            if (tmp < mem) {
                mem = tmp;
            }
            try {
                Thread.sleep(1000);
            } catch (Exception x) {
                x.printStackTrace();
            }
            tmp = rt.totalMemory() - rt.freeMemory();
            if (tmp < mem) {
                mem = tmp;
            }
        }
        return mem;
    }

    //@Test
    public void execute() {
        System.out.println("\nStarting " + new java.util.Date());
        test(false);
        test(false);
        test(true);
    }

    public void test(boolean print) {
        root = null;
        long before = memoryUsed();
        if (print) {
            System.out.println("Memory before: " + format(before));
        }
        long time = System.currentTimeMillis();
        root = makeNode();
        for (int i = 0; i < 10; i++) {
            DSNode iNode = makeNode();
            root.add("test" + i, iNode);
            for (int j = 0; j < 100; j++) {
                DSNode jNode = makeNode();
                iNode.add("test" + j, jNode);
                for (int k = 0; k < 100; k++) {
                    jNode.add("test" + k, makeNode());
                }
            }
        }
        time = System.currentTimeMillis() - time;
        long after = memoryUsed();
        if (print) {
            System.out.println("Memory after: " + format(after));
            System.out.println("Memory used: " + format(after - before));
            System.out.println("Create time: " + time + "ms");
        }
        time = System.currentTimeMillis();
        iterate(root);
        time = System.currentTimeMillis() - time;
        after = memoryUsed();
        if (print) {
            System.out.println("Iterate time: " + time + "ms");
            System.out.println("Memory used: " + format(after - before));
        }
    }

    public static class TestNode extends DSNode {

        public void declareDefaults() {
            super.declareDefaults();
            declareDefault("string", DSString.valueOf("abc"));
            declareDefault("int", DSInt.valueOf(1000));
            declareDefault("double", DSDouble.valueOf(12345.1d));
            declareDefault("boolean", DSBool.valueOf(true));
            declareDefault("action", DSAction.DEFAULT);
        }
    }
}
