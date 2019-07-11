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
    private DSStatus testStatus;

    // Methods
    // -------

    @Test
    public void test() throws Exception {
        doit(new V1TestLink(new MyMain()));
        doit(new V2TestLink(new MyMain()));
    }

    private void doit(DSLink link) throws Exception {
        testStatus = null;
        Thread t = new Thread(link, "DSLink Runner");
        t.start();
        link.getConnection().waitForConnection(5000);
        Assert.assertTrue(link.getConnection().isConnected());
        DSIRequester requester = link.getConnection().getRequester();
        requester.subscribe("/main/abc", DSLong.valueOf(0), new MyHandler());
        long end = System.currentTimeMillis() + 10000;
        synchronized (mutex) {
            while (((testStatus == null) || (!testStatus.isOk()))
                    && (System.currentTimeMillis() < end)) {
                mutex.wait(1000);
            }
        }
        Assert.assertTrue(testStatus.isOk());
        testStatus = null;
        link.getConnection().setEnabled(false);
        link.getConnection().disconnect();
        end = System.currentTimeMillis() + 10000;
        while (link.getConnection().isConnected() && (System.currentTimeMillis() < end)) {
            mutex.wait(100);
        }
        Assert.assertFalse(link.getConnection().isConnected());
        end = System.currentTimeMillis() + 10000;
        synchronized (mutex) {
            while (((testStatus == null) || (!testStatus.isDown()))
                    && (System.currentTimeMillis() < end)) {
                mutex.wait(1000);
            }
        }
        Assert.assertTrue(testStatus.isDown());
        testStatus = null;
        link.getConnection().setEnabled(true);
        link.getConnection().connect();
        link.getConnection().waitForConnection(10000);
        Assert.assertTrue(link.getConnection().isConnected());
        end = System.currentTimeMillis() + 10000;
        synchronized (mutex) {
            while (((testStatus == null) || (!testStatus.isOk()))
                    && (System.currentTimeMillis() < end)) {
                mutex.wait(1000);
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
