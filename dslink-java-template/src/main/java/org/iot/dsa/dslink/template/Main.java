package org.iot.dsa.dslink.template;

import org.iot.dsa.DSRuntime;
import org.iot.dsa.dslink.DSLink;
import org.iot.dsa.dslink.DSLinkConfig;
import org.iot.dsa.dslink.DSRootNode;
import org.iot.dsa.node.DSBool;
import org.iot.dsa.node.DSElement;
import org.iot.dsa.node.DSFlexEnum;
import org.iot.dsa.node.DSIObject;
import org.iot.dsa.node.DSInfo;
import org.iot.dsa.node.DSInt;
import org.iot.dsa.node.DSJavaEnum;
import org.iot.dsa.node.DSList;
import org.iot.dsa.node.DSNode;
import org.iot.dsa.node.DSString;
import org.iot.dsa.node.action.ActionInvocation;
import org.iot.dsa.node.action.ActionResult;
import org.iot.dsa.node.action.DSAction;

/**
 * Link main class and root node.
 *
 * @author Aaron Hansen
 */
public class Main extends DSRootNode implements Runnable {

    ///////////////////////////////////////////////////////////////////////////
    // Constants
    ///////////////////////////////////////////////////////////////////////////

    ///////////////////////////////////////////////////////////////////////////
    // Fields
    ///////////////////////////////////////////////////////////////////////////

    private DSInfo incrementingInt = getInfo("Incrementing Int");
    private DSInfo reset = getInfo("Reset");
    private DSInfo test = getInfo("Test");
    private DSRuntime.Timer timer;

    ///////////////////////////////////////////////////////////////////////////
    // Constructors
    ///////////////////////////////////////////////////////////////////////////

    ///////////////////////////////////////////////////////////////////////////
    // Methods
    ///////////////////////////////////////////////////////////////////////////

    @Override
    protected void declareDefaults() {
        super.declareDefaults();
        declareDefault("Incrementing Int", DSInt.valueOf(1)).setReadOnly(true);
        declareDefault("Writable Boolean", DSBool.valueOf(true));
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
        declareDefault("Test", new DSAction());
    }

    /**
     * Launch the link.
     */
    public static void main(String[] args) throws Exception {
        DSLinkConfig cfg = new DSLinkConfig(args);
        DSLink link = new DSLink(cfg);
        link.run();
    }

    /**
     * Handles the reset action.
     */
    @Override
    public ActionResult onInvoke(DSInfo actionInfo, ActionInvocation invocation) {
        if (actionInfo == this.reset) {
            put(incrementingInt, DSInt.valueOf(0));
            DSElement arg = invocation.getParameters().get("Arg");
            put("Message", arg);
            return null;
        } else if (actionInfo == this.test) {
            DSRuntime.run(new Runnable() {
                @Override
                public void run() {
                    onTest();
                }
            });
            return null;
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
     * Build a large database
     */
    private void onTest() {
        long start = System.currentTimeMillis();
        clear();
        long now = System.currentTimeMillis();
        long dur = now - start;
        info("Clear duration: " + dur);
        try {
            Thread.sleep(5000);
        } catch (Exception x) {
        }
        info("Beginning Add");
        start = System.currentTimeMillis();
        DSNode node = new TestNode();
        add("test", node);
        onTest1(node);
        onTest2(node);
        DSIObject obj;
        for (int i = 0, len = node.childCount(); i < len; i++) {
            obj = node.get(i);
            if (obj instanceof DSNode) {
                onTest2((DSNode) obj);
            }
        }
        now = System.currentTimeMillis();
        dur = now - start;
        info("Add duration: " + dur);
        start = System.currentTimeMillis();
        getLink().saveNodes();
        now = System.currentTimeMillis();
        dur = now - start;
        info("Save duration: " + dur);
        try {
            Thread.sleep(5000);
        } catch (Exception x) {
        }
    }

    private void onTest1(DSNode folder) {
        String name = folder.getName();
        DSNode tmp;
        for (int i = 0; i < 100; i++) {
            tmp = new TestNode();
            folder.add(name + i, tmp);
        }
    }

    private void onTest2(DSNode folder) {
        String name = folder.getName();
        DSIObject obj;
        for (int i = 0, len = folder.childCount(); i < len; i++) {
            obj = folder.get(i);
            if (obj instanceof DSNode) {
                onTest1((DSNode) obj);
            }
        }
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
        DSInt value = (DSInt) incrementingInt.getValue();
        put(incrementingInt, DSInt.valueOf(value.toInt() + 1));
    }

    ///////////////////////////////////////////////////////////////////////////
    // Inner Classes
    ///////////////////////////////////////////////////////////////////////////

    public static class TestNode extends DSNode {

        public void declareDefaults() {
            super.declareDefaults();
            declareDefault("Incrementing Int", DSInt.valueOf(1)).setReadOnly(true);
            declareDefault("Writable Boolean", DSBool.valueOf(true));
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
        }
    }

    public enum MyEnum {
        On,
        Off,
        Auto
    }

    ///////////////////////////////////////////////////////////////////////////
    // Initialization
    ///////////////////////////////////////////////////////////////////////////

}
