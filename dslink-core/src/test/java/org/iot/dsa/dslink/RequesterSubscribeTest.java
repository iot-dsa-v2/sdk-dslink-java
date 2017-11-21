package org.iot.dsa.dslink;

import com.acuity.iot.dsa.dslink.test.TestLink;
import org.iot.dsa.DSRuntime;
import org.iot.dsa.dslink.requester.AbstractSubscribeHandler;
import org.iot.dsa.dslink.requester.OutboundSubscribeHandler;
import org.iot.dsa.dslink.requester.SimpleRequestHandler;
import org.iot.dsa.node.DSElement;
import org.iot.dsa.node.DSInt;
import org.iot.dsa.node.DSNode;
import org.iot.dsa.node.DSStatus;
import org.iot.dsa.time.DSDateTime;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author Aaron Hansen
 */
public class RequesterSubscribeTest implements DSLinkConnection.Listener {

    // Fields
    // ------

    private boolean success = false;
    private DSLink link;
    private MyRoot root;
    private AbstractSubscribeHandler handler;

    // Methods
    // -------

    public void onConnect(DSLinkConnection connection) {
        DSIRequester requester = link.getConnection().getRequester();
        success = !root.isSubscribed();
        handler = (AbstractSubscribeHandler) requester.subscribe(
                "/Nodes/int", 0, new AbstractSubscribeHandler() {
                    boolean first = true;
                    @Override
                    public void onUpdate(DSDateTime dateTime, DSElement value, DSStatus status) {
                        if (first) {
                            success = value.equals(DSInt.valueOf(0));
                            first = false;
                        } else {
                            success = value.equals(DSInt.valueOf(10));
                        }
                        synchronized (RequesterSubscribeTest.this) {
                            RequesterSubscribeTest.this.notify();
                        }
                    }

                    @Override
                    public void onClose() {
                        success = true;
                        synchronized (RequesterSubscribeTest.this) {
                            RequesterSubscribeTest.this.notify();
                        }
                    }

                    @Override
                    public void onError(String type, String msg, String detail) {
                        Thread.dumpStack();
                    }
                });
    }

    public void onDisconnect(DSLinkConnection connection) {
    }

    @Test
    public void theTest() throws Exception {
        link = new TestLink(root = new MyRoot());
        link.getConnection().addListener(this);
        DSRuntime.run(link);
        Assert.assertFalse(root.isSubscribed());
        Assert.assertFalse(success);
        //Wait for onConnected to subscribe and receive the first update value of 0
        synchronized (this) {
            this.wait(5000);
        }
        Assert.assertTrue(success);
        Assert.assertTrue(root.isSubscribed());
        //Set the value to 10 and wait for the update
        success = false;
        DSIRequester requester = link.getConnection().getRequester();
        requester.set("/Nodes/int", DSInt.valueOf(10), SimpleRequestHandler.DEFAULT);
        synchronized (this) {
            this.wait(5000);
        }
        Assert.assertTrue(success);
        Assert.assertEquals(root.get("int"),DSInt.valueOf(10));
        success = false;
        //Close stream, validate onClose called
        handler.getStream().closeStream();
        Assert.assertTrue(success);
        synchronized (root) {
            root.wait(5000);
        }
        //Validate that the root was unsubscribed
        Assert.assertFalse(root.isSubscribed());
        //Subscribe a lower value, validate onSubscribe.
        ANode node = (ANode) root.getNode("aNode");
        Assert.assertFalse(node.subscribeCalled);
        Assert.assertFalse(node.unsubscribeCalled);
        AbstractSubscribeHandler handler = (AbstractSubscribeHandler) requester.subscribe(
                "/Nodes/aNode", 0, new AbstractSubscribeHandler() {
                    @Override
                    public void onUpdate(DSDateTime dateTime, DSElement value, DSStatus status) {
                    }

                    @Override
                    public void onClose() {
                    }

                    @Override
                    public void onError(String type, String msg, String detail) {
                    }
                });
        synchronized (node) {
            if (!node.subscribeCalled) {
                node.wait(5000);
            }
        }
        Assert.assertTrue(node.subscribeCalled);
        //Now close the stream and validate unsubscribed.
        Assert.assertFalse(node.unsubscribeCalled);
        handler.getStream().closeStream();
        synchronized (node) {
            if (!node.unsubscribeCalled) {
                node.wait(5000);
            }
        }
        Assert.assertTrue(node.unsubscribeCalled);
        link.stop();
        link = null;
    }

    // Inner Classes
    // -------------

    public static class MyRoot extends DSRootNode {

        public void declareDefaults() {
            declareDefault("int", DSInt.valueOf(0));
            declareDefault("aNode", new ANode());
        }

        @Override
        public synchronized void onUnsubscribed() {
            notifyAll();
        }

    }

    public static class ANode extends DSNode {

        public boolean subscribeCalled = false;
        public boolean unsubscribeCalled = false;

        public void declareDefaults() {
            declareDefault("int", DSInt.valueOf(0));
        }

        @Override
        public synchronized void onSubscribed() {
            subscribeCalled = true;
            notifyAll();
        }

        @Override
        public synchronized void onUnsubscribed() {
            unsubscribeCalled = true;
            notifyAll();
        }

    }

}
