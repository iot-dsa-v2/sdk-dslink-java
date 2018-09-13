package org.iot.dsa.dslink;

import com.acuity.iot.dsa.dslink.test.TestLink;
import org.iot.dsa.conn.DSConnection.DSConnectionEvent;
import org.iot.dsa.dslink.requester.SimpleInvokeHandler;
import org.iot.dsa.node.DSInfo;
import org.iot.dsa.node.DSInt;
import org.iot.dsa.node.DSMap;
import org.iot.dsa.node.DSNode;
import org.iot.dsa.node.DSValueType;
import org.iot.dsa.node.action.ActionInvocation;
import org.iot.dsa.node.action.ActionResult;
import org.iot.dsa.node.action.DSAction;
import org.iot.dsa.node.event.DSIEvent;
import org.iot.dsa.node.event.DSISubscriber;
import org.iot.dsa.node.event.DSTopic;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author Aaron Hansen
 */
public class RequesterInvokeTest {

    // Fields
    // ------

    private static boolean success = false;
    private DSLink link;
    private MyMain root;

    // Methods
    // -------

    @Test
    public void theTest() throws Exception {
        link = new TestLink(root = new MyMain());
        link.getConnection().subscribe(DSLinkConnection.CONN_TOPIC, new DSISubscriber() {
            @Override
            public void onEvent(DSNode node, DSInfo child, DSIEvent event) {
                if (event == DSConnectionEvent.CONNECTED) {
                    success = true;
                    synchronized (RequesterInvokeTest.this) {
                        RequesterInvokeTest.this.notifyAll();
                    }
                }
            }

            @Override
            public void onUnsubscribed(DSTopic topic, DSNode node, DSInfo child) {
            }
        });
        success = false;
        Thread t = new Thread(link, "DSLink Runner");
        t.start();
        synchronized (this) {
            this.wait(5000);
        }
        Assert.assertTrue(success);
        success = false;
        DSIRequester requester = link.getConnection().getRequester();
        SimpleInvokeHandler res = (SimpleInvokeHandler) requester.invoke(
                "/main/simpleAction", null, new SimpleInvokeHandler());
        res.getResult(1000);
        res = (SimpleInvokeHandler) requester.invoke(
                "/main/simpleParam",
                new DSMap().put("param", true),
                new SimpleInvokeHandler());
        res.getResult(1000);
        Assert.assertTrue(success);
        res = (SimpleInvokeHandler) requester.invoke(
                "/main/exception",
                new DSMap().put("param", true),
                new SimpleInvokeHandler());
        success = false;
        try {
            res.getResult(1000);
        } catch (Exception x) {
            success = true;
        }
        Assert.assertTrue(success);
    }

    // Inner Classes
    // -------------

    public static class MyMain extends DSMainNode {

        @Override
        public void declareDefaults() {
            declareDefault("anode", new ANode());
            declareDefault("simpleAction", DSAction.DEFAULT);
            DSAction action = new DSAction();
            action.addParameter("param", DSValueType.BOOL, "a desc");
            declareDefault("simpleParam", action);
            declareDefault("exception", DSAction.DEFAULT);
        }

        @Override
        public ActionResult onInvoke(DSInfo action, ActionInvocation invocation) {
            String name = action.getName();
            if (name.equals("simpleAction")) {
                success = true;
            } else if (name.equals("simpleParam")) {
                DSMap params = invocation.getParameters();
                success = params.get("param", false);
            } else if (name.equals("exception")) {
                throw new IllegalStateException("my message");
            }
            return null;
        }

    }

    public static class ANode extends DSNode {

        @Override
        public void declareDefaults() {
            declareDefault("int", DSInt.valueOf(0));
        }

        @Override
        public ActionResult onInvoke(DSInfo action, ActionInvocation invocation) {
            return null;
        }

    }

}
