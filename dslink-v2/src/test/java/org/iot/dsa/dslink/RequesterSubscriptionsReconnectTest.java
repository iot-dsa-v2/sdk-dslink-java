package org.iot.dsa.dslink;

import com.acuity.iot.dsa.dslink.test.V1TestLink;
import com.acuity.iot.dsa.dslink.test.V2TestLink;
import org.iot.dsa.dslink.requester.AbstractSubscribeHandler;
import org.iot.dsa.node.DSElement;
import org.iot.dsa.node.DSLong;
import org.iot.dsa.node.DSStatus;
import org.iot.dsa.time.DSDateTime;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author Aaron Hansen
 */
public class RequesterSubscriptionsReconnectTest {

    // Fields
    // ------

    private Object mutex = this;
    private boolean success = false;
    private DSStatus testStatus;

    // Methods
    // -------

    @Test
    public void test() throws Exception {
        doit(new V1TestLink(new MyMain()));
        doit(new V2TestLink(new MyMain()));
    }

    private void doit(DSLink link) throws Exception {
        success = false;
        testStatus = null;
        link.getConnection().subscribe((event, node, child, data) -> {
            success = true;
            synchronized (mutex) {
                notifyAll();
            }
        }, DSLinkConnection.CONNECTED_EVENT, null);
        synchronized (mutex) {
            Thread t = new Thread(link, "DSLink Runner");
            t.start();
            mutex.wait(5000);
        }
        Assert.assertTrue(link.getConnection().isConnected());
        DSIRequester requester = link.getConnection().getRequester();
        requester.subscribe("/main/abc", DSLong.valueOf(0), new MyHandler());
        synchronized (mutex) {
            mutex.wait(20000);
        }
        Assert.assertTrue(testStatus.isOk());
        testStatus = null;
        link.getConnection().setEnabled(false);
        link.getConnection().disconnect();
        synchronized (mutex) {
            if (link.getConnection().isConnected()) {
                mutex.wait(20000);
            }
        }
        Assert.assertFalse(link.getConnection().isConnected());
        synchronized (mutex) {
            while (testStatus == null) {
                mutex.wait(20000);
            }
        }
        Assert.assertTrue(testStatus.isDown());
        testStatus = null;
        link.getConnection().setEnabled(true);
        link.getConnection().connect();
        synchronized (mutex) {
            if (!link.getConnection().isConnected()) {
                mutex.wait(20000);
            }
        }
        Assert.assertTrue(link.getConnection().isConnected());
        synchronized (mutex) {
            while ((testStatus == null) || testStatus.isDown()) {
                mutex.wait(20000);
            }
        }
        Assert.assertTrue(testStatus.isOk());
        link.shutdown();
    }

    // Inner Classes
    // -------------

    public class MyHandler extends AbstractSubscribeHandler {

        @Override
        public void onUpdate(DSDateTime dateTime, DSElement value, DSStatus status) {
            testStatus = status;
            synchronized (mutex) {
                mutex.notify();
            }
        }
    }

    public static class MyMain extends DSMainNode {

        protected void declareDefaults() {
            super.declareDefaults();
            declareDefault("abc", DSLong.valueOf(0), "foo");
        }

    }
}
