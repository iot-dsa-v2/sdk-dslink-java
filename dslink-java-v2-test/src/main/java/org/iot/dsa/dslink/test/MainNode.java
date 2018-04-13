package org.iot.dsa.dslink.test;

import org.iot.dsa.DSRuntime;
import org.iot.dsa.dslink.DSMainNode;
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
        action = new DSAction();
        action.addParameter("Arg",
                            DSString.valueOf(""),
                            "My action description");
        declareDefault("Foo", action);
        action = new DSAction();
        action.setResultType(ActionSpec.ResultType.VALUES);
        action.addValueResult("Big-String", DSValueType.STRING);
        declareDefault("Get-Big-String", action);
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
        } else if (actionInfo.getName().equals("Get-Big-String")) {
            DSActionValues ret = new DSActionValues(actionInfo.getAction());
            ret.addResult(BIG_STRING);
            return ret;
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

    public static final DSString BIG_STRING = DSString.valueOf(
            "Aaron Lorem ipsum dolor sit amet, consectetur " +
            "adipiscing elit. Sed porta rhoncus ornare. Vestibulum tristique " +
            "sollicitudin tortor et porta. Fusce suscipit sagittis scelerisque. Cras " +
            "dapibus mollis facilisis. Donec ultrices velit pharetra tellus lobortis, " +
            "euismod accumsan eros aliquet. Nullam metus ipsum, mollis et leo aliquam, " +
            " cursus feugiat ipsum. Nullam vitae leo eget ante facilisis ultricies. " +
            "Curabitur magna nibh, sagittis sed porttitor pellentesque, porttitor eu " +
            "libero. Fusce placerat odio sed finibus suscipit. Maecenas viverra ut " +
            "ligula eget finibus. Mauris eget sem est. Duis iaculis sem id mauris " +
            "dapibus pharetra. Quisque sed facilisis metus. Aliquam maximus urna at " +
            "risus facilisis congue. Morbi euismod massa et placerat rutrum. " +
            "Suspendisse rutrum nibh ut leo tincidunt viverra. Duis neque leo, " +
            "vestibulum ac turpis ut, sagittis posuere felis. Vestibulum et pharetra " +
            "ligula. Vestibulum tellus elit, pulvinar dapibus lorem et, laoreet " +
            "hendrerit nisi. Curabitur lacinia lectus posuere, sodales est quis, " +
            "egestas augue. Praesent quis vestibulum est. Ut sed ante pulvinar mi " +
            "vulputate fermentum. Quisque ut ex blandit, tincidunt tortor in, consequat" +
            " est. Pellentesque sodales dolor vitae molestie laoreet. Nunc eleifend " +
            "faucibus turpis, vel facilisis velit dignissim sit amet. Duis nibh elit, " +
            "semper vel porta a, faucibus quis purus. Suspendisse accumsan hendrerit " +
            "nisl eu ornare. Vestibulum sed ornare purus, quis volutpat diam. Quisque " +
            "vitae erat at massa mattis pellentesque. Quisque vestibulum erat id " +
            "porttitor lacinia. Suspendisse semper orci in tempor efficitur. Nunc " +
            "vehicula pharetra neque sit amet eleifend. Ut lacinia, quam quis volutpat " +
            "porta, neque tellus pretium risus, sed lacinia tortor neque non est. " +
            "Vivamus sodales vitae turpis eu varius. Donec vulputate laoreet dolor " +
            "posuere ullamcorper. Suspendisse potenti. Praesent sed mauris suscipit " +
            "sapien cursus facilisis quis in dolor. Vestibulum consequat nulla vel " +
            "libero placerat, eu elementum odio pretium. Proin vitae ipsum ac ante " +
            "pharetra tempus. Praesent id facilisis metus. Curabitur condimentum eu " +
            "massa non hendrerit. Nunc eu turpis at est consequat pharetra eu quis " +
            "lectus. Mauris at viverra erat. Morbi tempor, ex finibus ullamcorper " +
            "accumsan, risus erat semper nisi, eget semper mauris mauris vitae justo. " +
            "Proin auctor erat ut neque vulputate porttitor. Vestibulum ante ipsum " +
            "primis in faucibus orci luctus et ultrices posuere cubilia Curae; Morbi " +
            "eget ultricies magna. Vivamus non porttitor nibh. Nunc vulputate ultrices " +
            "semper. Phasellus sagittis pulvinar massa in luctus. Quisque convallis " +
            "sagittis dolor a varius. In accumsan, sem pretium mattis sodales, massa " +
            "erat dapibus mauris, at porta mi odio et magna. Maecenas sollicitudin " +
            "quam felis, in semper lectus feugiat vel. Quisque laoreet velit et tellus" +
            " fermentum volutpat. Pellentesque habitant morbi tristique senectus et " +
            "netus et malesuada fames ac turpis egestas. Orci varius natoque penatibus " +
            "et magnis dis parturient montes, nascetur ridiculus mus. Curabitur eu " +
            "commodo risus. Suspendisse eget nibh vehicula, rhoncus justo vitae, " +
            "accumsan odio. Curabitur tortor felis, feugiat vel neque eget, faucibus " +
            "suscipit nisi. Fusce dignissim nulla vitae erat tempor maximus. Nam " +
            "gravida eros at nunc iaculis condimentum. Nunc varius, neque at hendrerit " +
            "rhoncus, ante felis pretium metus, at sollicitudin sem magna eget augue. " +
            "Sed ante odio, gravida aliquam euismod ut, hendrerit eleifend orci. Donec" +
            " et euismod tortor, in ornare nisl. Integer leo augue, ornare elementum " +
            "enim ut, pellentesque blandit justo. Phasellus a suscipit eros, eu " +
            "sollicitudin quam. END.");
}
