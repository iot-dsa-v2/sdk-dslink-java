package org.iot.dsa.dslink;

import com.acuity.iot.dsa.dslink.protocol.v1.DS1Session;
import com.acuity.iot.dsa.dslink.test.V1TestLink;
import org.iot.dsa.dslink.requester.SimpleListHandler;
import org.iot.dsa.node.DSInt;
import org.iot.dsa.node.DSMap;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author Aaron Hansen
 */
public class MultipleListTest {

    // Fields
    // ------

    private DSLink link;

    // Methods
    // -------

    @Test
    public void test() throws Exception {
        link = new V1TestLink(new MyMain());
        doit();
    }

    private void doit() throws Exception {
        int emt = DS1Session.END_MSG_THRESHOLD;
        DS1Session.END_MSG_THRESHOLD = 1000;
        Thread t = new Thread(link, "DSLink Runner");
        t.start();
        link.getConnection().waitForConnection(5000);
        Assert.assertTrue(link.getConnection().isConnected());
        Assert.assertTrue(link.getMain().isStable());
        DSIRequester requester = link.getConnection().getRequester();
        SimpleListHandler handler1 = (SimpleListHandler) requester.list("/main",
                                                                        new MyHandler());
        handler1.waitForInitialized(5000);
        Assert.assertTrue(link.getMain().isSubscribed());
        Assert.assertTrue(handler1.isInitialized());
        Assert.assertTrue(handler1.hasUpdates());
        for (int i = 1000; --i >= 0; ) {
            DSMap map = (DSMap) handler1.getUpdate("int" + i);
            Assert.assertNotNull(map);
        }
        SimpleListHandler handler2 = (SimpleListHandler) requester.list("/main",
                                                                        new MyHandler());
        handler2.waitForInitialized(5000);
        Assert.assertTrue(link.getMain().isSubscribed());
        Assert.assertTrue(handler2.isInitialized());
        Assert.assertTrue(handler2.hasUpdates());
        for (int i = 1000; --i >= 0; ) {
            DSMap map = (DSMap) handler2.getUpdate("int" + i);
            Assert.assertNotNull(map);
        }
        handler1.getStream().closeStream();
        handler1.waitForClose(5000);
        Assert.assertFalse(handler1.getStream().isStreamOpen());
        Assert.assertTrue(link.getMain().isSubscribed());
        handler2.getStream().closeStream();
        handler2.waitForClose(5000);
        Assert.assertFalse(handler2.getStream().isStreamOpen());
        long end = System.currentTimeMillis() + 10000;
        while (link.getMain().isSubscribed()) {
            try {
                Thread.sleep(100);
            } catch (Exception x) {
            }
            if (System.currentTimeMillis() > end) {
                break;
            }
        }
        Assert.assertFalse(link.getMain().isSubscribed());
        link.shutdown();
        DS1Session.END_MSG_THRESHOLD = emt;
    }

    // Inner Classes
    // -------------

    class MyHandler extends SimpleListHandler {

    }

    public static class MyMain extends DSMainNode {

        @Override
        protected void onStarted() {
            for (int i = 1000; --i >= 0; ) {
                put("int" + i, DSInt.valueOf(i));
            }
        }

    }

}
