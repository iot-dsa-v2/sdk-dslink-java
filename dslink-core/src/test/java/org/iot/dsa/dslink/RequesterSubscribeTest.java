package org.iot.dsa.dslink;

import org.iot.dsa.DSRuntime;
import org.iot.dsa.dslink.requester.AbstractSubscribeHandler;
import org.iot.dsa.dslink.requester.SimpleRequestHandler;
import org.iot.dsa.node.DSElement;
import org.iot.dsa.node.DSInt;
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
                    @Override
                    public void onUpdate(DSDateTime dateTime, DSElement value, DSStatus status) {
                        success = value.equals(DSInt.valueOf(10));
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
        requester.set("/Nodes/int", DSInt.valueOf(10), SimpleRequestHandler.DEFAULT);
    }

    public void onDisconnect(DSLinkConnection connection) {
    }

    @Test
    public void theTest() throws Exception {
        link = new TestLink(root = new MyRoot());
        link.getConnection().addListener(this);
        DSRuntime.run(link);
        Assert.assertFalse(root.isSubscribed());
        synchronized (this) {
            this.wait(5000);
        }
        Assert.assertTrue(success);
        Assert.assertTrue(root.isSubscribed());
        success = false;
        handler.getStream().closeStream();
        Assert.assertTrue(success);
        synchronized (root) {
            root.wait(5000);
        }
        Assert.assertFalse(root.isSubscribed());
        link.stop();
        link = null;
    }

    // Inner Classes
    // -------------

    public static class MyRoot extends DSRootNode {

        public void declareDefaults() {
            declareDefault("int", DSInt.valueOf(0));
        }

        @Override
        public synchronized void onUnsubscribed() {
            notifyAll();
        }

    }

}
