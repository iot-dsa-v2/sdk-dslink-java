package org.iot.dsa.dslink.poc;

import java.util.Collection;
import org.iot.dsa.DSRuntime;
import org.iot.dsa.dslink.DSMainNode;
import org.iot.dsa.dslink.requester.ErrorType;
import org.iot.dsa.dslink.requester.OutboundListHandler;
import org.iot.dsa.dslink.requester.OutboundStream;
import org.iot.dsa.node.DSBool;
import org.iot.dsa.node.DSDouble;
import org.iot.dsa.node.DSElement;
import org.iot.dsa.node.DSFlexEnum;
import org.iot.dsa.node.DSInfo;
import org.iot.dsa.node.DSInt;
import org.iot.dsa.node.DSJavaEnum;
import org.iot.dsa.node.DSList;
import org.iot.dsa.node.DSLong;
import org.iot.dsa.node.DSMap;
import org.iot.dsa.node.DSNode;
import org.iot.dsa.node.DSString;
import org.iot.dsa.node.action.ActionInvocation;
import org.iot.dsa.node.action.ActionResult;
import org.iot.dsa.node.action.ActionSpec;
import org.iot.dsa.node.action.DSAction;
import org.iot.dsa.node.action.DSAction.Parameterless;
import org.iot.dsa.node.action.DSActionValues;

/**
 * Link main class and node.
 *
 * @author Aaron Hansen
 */
public class MainNode extends DSMainNode implements Runnable {

    ///////////////////////////////////////////////////////////////////////////
    // Constants
    ///////////////////////////////////////////////////////////////////////////

    private static final boolean TEST_DEFAULTS = false;

    ///////////////////////////////////////////////////////////////////////////
    // Fields
    ///////////////////////////////////////////////////////////////////////////

    private DSInfo incrementingInt = getInfo("Incrementing Int");
    private DSInfo reset = getInfo("Reset");
    private DSInfo test = getInfo("Test");
    private DSRuntime.Timer timer;
    private DSInfo valuesAction = getInfo("Values Action");

    ///////////////////////////////////////////////////////////////////////////
    // Methods
    ///////////////////////////////////////////////////////////////////////////

    @Override
    public DSInfo getVirtualAction(DSInfo target, String name) {
        if (target == incrementingInt) {
            if (name.equals("Reset")) {
                return actionInfo(name, new DSAction.Parameterless() {
                    @Override
                    public ActionResult invoke(DSInfo target, ActionInvocation invocation) {
                        put(incrementingInt, DSElement.make(0));
                        return null;
                    }
                });
            }
        } else if (target.get() == this) {
            if (name.equals("Foobar")) {
                return actionInfo(name, new Parameterless() {
                    @Override
                    public ActionResult invoke(DSInfo target, ActionInvocation invocation) {
                        System.out.println("Hi Mom");
                        return null;
                    }
                });
            }
        }
        return super.getVirtualAction(target, name);
    }

    @Override
    public void getVirtualActions(DSInfo target, Collection<String> bucket) {
        if (target == incrementingInt) {
            bucket.add("Reset");
        } else if (target.get() == this) {
            bucket.add("Foobar");
        }
        super.getVirtualActions(target, bucket);
    }

    @Override
    public ActionResult invoke(DSInfo action, DSInfo target, ActionInvocation request) {
        if (action == this.reset) {
            put(incrementingInt, DSElement.make(0));
            DSMap map = request.getParameters();
            DSElement arg = null;
            if (map != null) {
                arg = request.getParameters().get("Arg");
                put("Message", arg);
            }
            clear();
            return null;
        } else if (action == this.test) {
            DSRuntime.run(() -> onTest());
            return null;
        } else if (action == this.valuesAction) {
            return new DSActionValues(this.valuesAction.getAction())
                    .addResult(DSBool.TRUE)
                    .addResult(DSLong.valueOf(1234));
        }
        return super.invoke(action, target, request);
    }

    /**
     * Called by an internal timer, increments an integer child on a one second interval, only when
     * this node is subscribed.
     */
    @Override
    public void run() {
        DSElement value = incrementingInt.getValue().toElement();
        put(incrementingInt, DSElement.make(value.toInt() + 1));
    }

    public void test(boolean print) {
        remove("test");
        DSNode root;
        long startMem = testMemUsed();
        if (print) {
            info("Memory before: " + testFormat(startMem));
        }
        long time = System.currentTimeMillis();
        root = testMakeNode();
        put("test", root);
        for (int i = 0; i < 10; i++) {
            DSNode iNode = testMakeNode();
            root.add("test" + i, iNode);
            for (int j = 0; j < 100; j++) {
                DSNode jNode = testMakeNode();
                iNode.add("test" + j, jNode);
                for (int k = 0; k < 100; k++) {
                    jNode.add("test" + k, testMakeNode());
                }
            }
        }
        time = System.currentTimeMillis() - time;
        long endMem = testMemUsed();
        if (print) {
            info("Memory after: " + testFormat(endMem));
            info("Memory used: " + testFormat(endMem - startMem));
            info("Create time: " + time + "ms");
        }
        time = System.currentTimeMillis();
        testTraverse(root);
        time = System.currentTimeMillis() - time;
        endMem = testMemUsed();
        if (print) {
            info("Traverse time: " + time + "ms");
            info("Memory used: " + testFormat(endMem - startMem));
        }
    }

    @Override
    protected void declareDefaults() {
        super.declareDefaults();
        declareDefault("Incrementing Int", DSLong.valueOf(1)).setReadOnly(true)
                                                             .getMetadata()
                                                             .setDescription(
                                                                     "this is a description")
                                                             .setUnit("ms");
        declareDefault("Writable Boolean", DSBool.valueOf(true)).setAdmin(true);
        declareDefault("Writable Enum",
                       DSFlexEnum.valueOf("On",
                                          DSList.valueOf("Off", "On", "Auto", "Has Space")));
        declareDefault("Java Enum", DSJavaEnum.valueOf(MyEnum.Off));
        declareDefault("Message", DSString.EMPTY).setReadOnly(true);
        DSAction action = new DSAction.Noop();
        action.addParameter("Arg",
                            DSJavaEnum.valueOf(MyEnum.Off),
                            "My action description");
        declareDefault("Reset", action);
        declareDefault("Test", DSAction.DEFAULT);
        action = new DSAction.Noop();
        action.setResultType(ActionSpec.ResultType.VALUES);
        action.addColumnMetadata("bool", DSBool.TRUE);
        action.addColumnMetadata("long", DSLong.valueOf(0));
        declareDefault("Values Action", action);
        //declareDefault("T./,;'<>?:\"[%]{/}bc", DSString.valueOf("abc")).setTransient(true);
        //notice the missing chars from above, dglux gets funky with the chars: <>?:\"
        declareDefault("T./,;'[%]{/}bc", DSString.valueOf("abc")).setTransient(true);
    }

    /**
     * Cancel an active timer if there is one.
     */
    @Override
    protected void onStopped() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    /**
     * Start the update timer.  This only updates when something is interested in this node.
     */
    @Override
    protected synchronized void onSubscribed() {
        timer = DSRuntime.run(this, System.currentTimeMillis() + 1000l, 1000l);
    }

    /**
     * Cancel the active timer.
     */
    @Override
    protected synchronized void onUnsubscribed() {
        timer.cancel();
        timer = null;
    }

    /**
     * Action impl, build a large databases.
     */
    private void onTest() {
        info("Start Test: " + new java.util.Date());
        info("Test run (1/3) ...");
        test(false);
        info("Test run (2/3) ...");
        test(false);
        info("Test run (3/3) ...");
        test(true);
        info("Test Finished: " + new java.util.Date());
    }

    private String testFormat(long mem) {
        return (mem / 1000) + "KB";
    }

    private DSNode testMakeNode() {
        DSNode ret;
        if (TEST_DEFAULTS) {
            ret = new TestNode();
        } else {
            ret = new DSNode();
            ret.put("string", "abc");
            ret.put("int", DSInt.valueOf(1000));
            ret.put("double", DSDouble.valueOf(12345.1d));
            ret.put("boolean", DSBool.valueOf(true));
        }
        return ret;
    }

    private long testMemUsed() {
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

    private void testTraverse(DSNode node) {
        DSInfo info = node.getFirstInfo();
        while (info != null) {
            if (info.isNode()) {
                testTraverse(info.getNode());
            }
            info = info.next();
        }
    }


    public enum MyEnum {
        On,
        Off,
        Auto
    }

    ///////////////////////////////////////////////////////////////////////////
    // Inner Classes
    ///////////////////////////////////////////////////////////////////////////

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
