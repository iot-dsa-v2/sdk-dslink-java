package org.iot.dsa.dslink.template;

import org.iot.dsa.DSRuntime;
import org.iot.dsa.dslink.DSLink;
import org.iot.dsa.dslink.DSLinkConfig;
import org.iot.dsa.dslink.DSRootNode;
import org.iot.dsa.node.DSBool;
import org.iot.dsa.node.DSElement;
import org.iot.dsa.node.DSFlexEnum;
import org.iot.dsa.node.DSInfo;
import org.iot.dsa.node.DSInt;
import org.iot.dsa.node.DSJavaEnum;
import org.iot.dsa.node.DSList;
import org.iot.dsa.node.DSString;
import org.iot.dsa.node.action.ActionInvocation;
import org.iot.dsa.node.action.ActionResult;
import org.iot.dsa.node.action.DSAction;

/**
 * Simple example of a link root node.
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
    private DSRuntime.Timer timer;

    ///////////////////////////////////////////////////////////////////////////
    // Constructors
    ///////////////////////////////////////////////////////////////////////////

    ///////////////////////////////////////////////////////////////////////////
    // Methods
    ///////////////////////////////////////////////////////////////////////////

    @Override
    protected void declareDefaults() {
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
     * Cancel the active timer.
     */
    @Override
    protected synchronized void onUnsubscribed() {
        timer.cancel();
        timer = null;
    }

    /**
     * Called by an internal timer, increments the 'first' child on a one second interval, only when
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

    public enum MyEnum {
        On,
        Off,
        Auto
    }

    ///////////////////////////////////////////////////////////////////////////
    // Initialization
    ///////////////////////////////////////////////////////////////////////////

}
