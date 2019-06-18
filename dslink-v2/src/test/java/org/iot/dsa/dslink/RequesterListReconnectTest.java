package org.iot.dsa.dslink;

import com.acuity.iot.dsa.dslink.test.V1TestLink;
import com.acuity.iot.dsa.dslink.test.V2TestLink;
import org.iot.dsa.dslink.requester.SimpleListHandler;
import org.iot.dsa.node.DSLong;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author Aaron Hansen
 */
public class RequesterListReconnectTest {

    // Fields
    // ------

    private Object mutex = this;

    // Methods
    // -------

    @Test
    public void test() throws Exception {
        doit(new V1TestLink(new MyMain()));
        //doit(new V2TestLink(new MyMain()));
    }

    private void doit(DSLink link) throws Exception {
        link.getConnection().subscribe((event, node, child, data) -> {
            synchronized (mutex) {
                mutex.notifyAll();
            }
        }, DSLinkConnection.CONNECTED_EVENT, null);
        synchronized (mutex) {
            Thread t = new Thread(link, "DSLink Runner");
            t.start();
            mutex.wait(5000);
        }
        Assert.assertTrue(link.getConnection().isConnected());
        DSIRequester requester = link.getConnection().getRequester();
        MyHandler h = (MyHandler) requester.list("/main", new MyHandler());
        h.waitForInitialized(20000);
        Assert.assertTrue(h.hasUpdates());
        h.getUpdates();
        Assert.assertFalse(h.hasUpdates());
        link.getConnection().setEnabled(false);
        link.getConnection().disconnect();
        synchronized (mutex) {
            if (link.getConnection().isConnected()) {
                mutex.wait(20000);
            }
        }
        Assert.assertFalse(link.getConnection().isConnected());
        link.getConnection().setEnabled(true);
        link.getConnection().connect();
        synchronized (mutex) {
            if (!link.getConnection().isConnected()) {
                mutex.wait(20000);
            }
        }
        Assert.assertTrue(link.getConnection().isConnected());
        Assert.assertTrue(h.hasUpdates());
    }

    // Inner Classes
    // -------------

    public class MyHandler extends SimpleListHandler {

    }

    public static class MyMain extends DSMainNode {

        protected void declareDefaults() {
            super.declareDefaults();
            declareDefault("abc", DSLong.valueOf(0), "foo");
        }

    }
}
