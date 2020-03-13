package org.iot.dsa.dslink;

import com.acuity.iot.dsa.dslink.test.V1TestLink;
import org.iot.dsa.node.DSInt;
import org.iot.dsa.node.DSNode;
import org.testng.Assert;
import org.testng.annotations.Test;

public class SubscriptionTest {

    private static boolean success = false;
    private V1TestLink link;

    @Test
    public void theTest() throws Exception {
        link = new V1TestLink(new DSMainNode());
        Thread t = new Thread(link, "DSLink Runner");
        t.start();
        link.getConnection().waitForConnection(5000);
        Assert.assertTrue(link.getConnection().isConnected());

        DSMainNode main = link.getMain();
        main.put("yesDuplicates", DSInt.valueOf(0));
        main.put("noDuplicates", DSInt.valueOf(0)).setIgnoreEquals(true);

        main.subscribe((event, node, child, data) -> {
            Assert.assertTrue(child.isValue());
            Assert.assertTrue(child.getValue().toElement().isNumber());
            synchronized (SubscriptionTest.this) {
                success = !success;
                Assert.assertEquals(node, main);
                SubscriptionTest.this.notifyAll();
            }
        }, DSNode.VALUE_CHANGED_EVENT, null);

        success = false;
        main.put("yesDuplicates", DSInt.valueOf(0));
        synchronized (SubscriptionTest.this) {
            if (!success) {
                SubscriptionTest.this.wait(5000);
            }
        }
        Assert.assertTrue(success);
        success = true;
        main.put("noDuplicates", DSInt.valueOf(0));
        synchronized (SubscriptionTest.this) {
            if (success) { //should have to wait
                SubscriptionTest.this.wait(3000);
            }
        }
        Assert.assertTrue(success);
    }

}
