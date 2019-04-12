package org.iot.dsa.dslink;

import com.acuity.iot.dsa.dslink.protocol.v1.DS1Session;
import com.acuity.iot.dsa.dslink.test.V1TestLink;
import com.acuity.iot.dsa.dslink.test.V2TestLink;
import org.iot.dsa.dslink.requester.SimpleListHandler;
import org.iot.dsa.node.DSBool;
import org.iot.dsa.node.DSInt;
import org.iot.dsa.node.DSMap;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author Aaron Hansen
 */
public class LargeListTest {

    // Fields
    // ------

    private DSLink link;

    // Methods
    // -------

    @Test
    public void test() throws Exception {
        link = new V1TestLink(new MyMain());
        doit();
        link = new V2TestLink(new MyMain());
        link.getOptions().setLogLevel("trace");
        doit();
    }

    private void doit() throws Exception {
        int emt = DS1Session.END_MSG_THRESHOLD;
        DS1Session.END_MSG_THRESHOLD = 1000;
        link.getConnection().subscribe((event, node, child, data) -> {
            synchronized (LargeListTest.this) {
                LargeListTest.this.notifyAll();
            }
        }, DSLinkConnection.CONNECTED_EVENT, null);
        Thread t = new Thread(link, "DSLink Runner");
        t.start();
        synchronized (this) {
            this.wait(5000);
        }
        Assert.assertTrue(link.getConnection().isConnected());
        Assert.assertTrue(link.getMain().isStable());
        DSIRequester requester = link.getConnection().getRequester();
        SimpleListHandler handler = (SimpleListHandler) requester.list("/main",
                                                                       new SimpleListHandler());
        handler.waitForInitialized(5000);
        Assert.assertTrue(link.getMain().isSubscribed());
        Assert.assertTrue(handler.isInitialized());
        Assert.assertTrue(handler.hasUpdates());
        for (int i = 1000; --i >= 0; ) {
            DSMap map = (DSMap) handler.getUpdate("int"+i);
            Assert.assertNotNull(map);
        }
        //handler.getStream().closeStream();
        //handler.waitForCallback(5000);
        //Assert.assertFalse(link.getMain().isSubscribed());
        Thread.sleep(100);
        link.shutdown();
        DS1Session.END_MSG_THRESHOLD = emt;
    }

    // Inner Classes
    // -------------

    public static class MyMain extends DSMainNode {

        @Override
        protected void onStarted() {
            for (int i = 1000; --i >= 0; ) {
                put("int" + i, DSInt.valueOf(i));
            }
        }

    }

}
