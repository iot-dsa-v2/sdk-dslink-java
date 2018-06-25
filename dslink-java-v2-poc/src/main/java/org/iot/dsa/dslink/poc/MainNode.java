package org.iot.dsa.dslink.poc;

import org.iot.dsa.DSRuntime;
import org.iot.dsa.dslink.DSMainNode;
import org.iot.dsa.dslink.requester.ErrorType;
import org.iot.dsa.dslink.requester.OutboundListHandler;
import org.iot.dsa.dslink.requester.OutboundStream;
import org.iot.dsa.node.*;
import org.iot.dsa.node.action.*;

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
    private DSInfo valuesAction = getInfo("Values Action");
    private DSRuntime.Timer timer;

    ///////////////////////////////////////////////////////////////////////////
    // Methods
    ///////////////////////////////////////////////////////////////////////////

    @Override
    protected void declareDefaults() {
        super.declareDefaults();
        declareDefault("Incrementing Int", DSLong.valueOf(1)).setReadOnly(true);
        declareDefault("Writable Boolean", DSBool.valueOf(true)).setAdmin(true);
        declareDefault("Writable Enum",
                       DSFlexEnum.valueOf("On",
                                          DSList.valueOf("Off", "On", "Auto", "Has Space")));
        declareDefault("Java Enum", DSJavaEnum.valueOf(MyEnum.Off));
        declareDefault("Message", DSString.EMPTY).setReadOnly(true);
        DSAction action = new DSAction();
        action.addParameter("Arg",
                            DSJavaEnum.valueOf(MyEnum.Off),
                            "My action description");
        declareDefault("Reset", action);
        declareDefault("Test", DSAction.DEFAULT);
        action = new DSAction();
        action.setResultType(ActionSpec.ResultType.VALUES);
        action.addValueResult("bool", DSBool.TRUE);
        action.addValueResult("long", DSLong.valueOf(0));
        declareDefault("Values Action", action);
        //declareDefault("T./,;'<>?:\"[%]{/}bc", DSString.valueOf("abc")).setTransient(true);
        //notice the missing chars from above, dglux gets funky with the chars: <>?:\"
        declareDefault("T./,;'[%]{/}bc", DSString.valueOf("abc")).setTransient(true);
        declareDefault("Test_List", new DSAbstractAction() {
            @Override
            public ActionResult invoke(DSInfo info, ActionInvocation invocation) {
                System.out.println("invoking");
                ((DSMainNode)info.getParent()).getLink().getConnection().getRequester()
                                .list(
                        "/downstream/bogus/path", new ListHandler());
                return null;
            }

            @Override
            public void prepareParameter(DSInfo info, DSMap parameter) {
            }
        });
    }

    private class ListHandler implements OutboundListHandler {
        OutboundStream stream;
        @Override
        public void onInit(String path, OutboundStream stream) {
            System.out.println("onInit");
            this.stream = stream;
        }

        @Override
        public void onInitialized() {
            System.out.println("list initialized");
            stream.closeStream();
        }

        @Override
        public void onRemove(String name) {
            System.out.println("list remove " + name);
        }

        @Override
        public void onUpdate(String name, DSElement value) {
            System.out.print(name);
            System.out.print(": ");
            System.out.println(String.valueOf(value));
        }

        @Override
        public void onClose() {
            System.out.println("list closed");
        }

        @Override
        public void onError(ErrorType type, String msg) {
            System.out.println("list error " + type + ", " + msg);
        }
    }

    @Override
    public ActionResult onInvoke(DSInfo actionInfo, ActionInvocation invocation) {
        if (actionInfo == this.reset) {
            put(incrementingInt, DSElement.make(0));
            DSMap map = invocation.getParameters();
            DSElement arg = null;
            if (map != null) {
                arg = invocation.getParameters().get("Arg");
                put("Message", arg);
            }
            clear();
            return null;
        } else if (actionInfo == this.test) {
            DSRuntime.run(new Runnable() {
                @Override
                public void run() {
                    onTest();
                }
            });
            return null;
        } else if (actionInfo == this.valuesAction) {
            return new DSActionValues(this.valuesAction.getAction())
                    .addResult(DSBool.TRUE)
                    .addResult(DSLong.valueOf(1234));
        }
        return super.onInvoke(actionInfo, invocation);
    }

    /**
     * Start the update timer.  This only updates when something is interested in this node.
     */
    @Override
    protected synchronized void onSubscribed() {
        timer = DSRuntime.run(this, System.currentTimeMillis() + 1000l, 1000l);
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

    /**
     * Cancel the active timer.
     */
    @Override
    protected synchronized void onUnsubscribed() {
        timer.cancel();
        timer = null;
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

    public enum MyEnum {
        On,
        Off,
        Auto
    }

}
