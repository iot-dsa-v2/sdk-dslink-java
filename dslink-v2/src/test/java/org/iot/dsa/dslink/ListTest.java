package org.iot.dsa.dslink;

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
public class ListTest {

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
        doit();
    }

    private void doit() throws Exception {
        Thread t = new Thread(link, "DSLink Runner");
        t.start();
        link.getConnection().waitForConnection(5000);
        Assert.assertTrue(link.getConnection().isConnected());
        DSIRequester requester = link.getConnection().getRequester();
        SimpleListHandler handler = (SimpleListHandler) requester.list("/main",
                                                                       new SimpleListHandler());
        handler.waitForInitialized(5000);
        Assert.assertTrue(handler.isInitialized());
        Assert.assertTrue(handler.hasUpdates());
        DSMap map = (DSMap) handler.getUpdate("int");
        Assert.assertNotNull(map);
        map = (DSMap) handler.getUpdate("bool");
        Assert.assertNotNull(map);
        link.shutdown();
        link = null;
    }

    // Inner Classes
    // -------------

    public static class MyMain extends DSMainNode {

        public void declareDefaults() {
            declareDefault("int", DSInt.valueOf(0));
            declareDefault("bool", DSBool.TRUE);
        }

    }

}
