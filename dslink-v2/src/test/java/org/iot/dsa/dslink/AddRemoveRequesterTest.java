package org.iot.dsa.dslink;

import com.acuity.iot.dsa.dslink.test.V1TestLink;
import java.util.Map;
import org.iot.dsa.dslink.requester.SimpleListHandler;
import org.iot.dsa.dslink.requester.SimpleSubscribeHandler;
import org.iot.dsa.node.DSElement;
import org.iot.dsa.node.DSInt;
import org.iot.dsa.node.DSLong;
import org.iot.dsa.node.DSNode;
import org.iot.dsa.node.DSStatus;
import org.iot.dsa.node.DSString;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author Aaron Hansen
 */
public class AddRemoveRequesterTest {

    // Methods
    // -------

    @Test
    public void test() throws Exception {
        DSLink link = new V1TestLink(new MyMain());
        new Thread(link, "DSLink Runner").start();
        link.getConnection().waitForConnection(5000);
        Assert.assertTrue(link.getConnection().isConnected());
        //Logger.getLogger("transport").setLevel(Level.ALL);
        listSimpleValue(link);
        listSimpleNode(link);
        listDeepValue(link);
        listDeepNode(link);
        subSimpleValue(link);
        subDeepValue(link);
        link.shutdown();
    }

    private void listDeepNode(DSLink link) {
        DSIRequester requester = link.getConnection().getRequester();

        //subscribe to non-existent node
        SimpleListHandler h = (SimpleListHandler) requester
                .list("/main/def/abc", new SimpleListHandler());
        h.waitForInitialized(5000);
        Map<String, DSElement> updates = h.reset();
        Assert.assertTrue(updates.containsKey("$disconnectedTs"));

        //add the value
        DSNode node = new DSNode();
        node.put("abc", new DSNode());
        link.getMain().put("def", node);
        h.waitForInitialized(5000);
        updates = h.reset();
        Assert.assertTrue(updates.get("$is").equals(DSString.valueOf("node")));

        //remove the value
        link.getMain().remove("def");
        h.waitForInitialized(5000);
        updates = h.reset();
        Assert.assertTrue(updates.containsKey("$disconnectedTs"));
        h.getStream().closeStream();
    }

    private void listDeepValue(DSLink link) {
        DSIRequester requester = link.getConnection().getRequester();

        //subscribe to non-existent value
        SimpleListHandler h = (SimpleListHandler) requester
                .list("/main/def/abc", new SimpleListHandler());
        h.waitForInitialized(5000);
        Map<String, DSElement> updates = h.reset();
        Assert.assertTrue(updates.containsKey("$disconnectedTs"));

        //add the value
        DSNode node = new DSNode();
        node.put("abc", DSLong.valueOf(123));
        link.getMain().put("def", node);
        h.waitForInitialized(5000);
        updates = h.reset();
        Assert.assertTrue(updates.get("$type").equals(DSString.valueOf("number")));

        //remove the value
        link.getMain().remove("def");
        h.waitForInitialized(5000);
        updates = h.reset();
        Assert.assertTrue(updates.containsKey("$disconnectedTs"));
        h.getStream().closeStream();
    }

    private void listSimpleNode(DSLink link) {
        DSIRequester requester = link.getConnection().getRequester();

        //subscribe to non-existent node
        SimpleListHandler h = (SimpleListHandler) requester
                .list("/main/def", new SimpleListHandler());
        h.waitForInitialized(5000);
        Map<String, DSElement> updates = h.reset();
        Assert.assertTrue(updates.containsKey("$disconnectedTs"));

        //add the node
        link.getMain().put("def", new DSNode());
        h.waitForInitialized(5000);
        updates = h.reset();
        Assert.assertTrue(updates.get("$is").equals(DSString.valueOf("node")));

        //remove the node
        link.getMain().remove("def");
        h.waitForInitialized(5000);
        updates = h.reset();
        Assert.assertTrue(updates.containsKey("$disconnectedTs"));
        h.getStream().closeStream();
    }

    private void listSimpleValue(DSLink link) {
        DSIRequester requester = link.getConnection().getRequester();

        //subscribe to non-existent value
        SimpleListHandler h = (SimpleListHandler) requester
                .list("/main/def", new SimpleListHandler());
        h.waitForInitialized(5000);
        Map<String, DSElement> updates = h.reset();
        Assert.assertTrue(updates.containsKey("$disconnectedTs"));

        //add the value
        link.getMain().put("def", DSLong.valueOf(123));
        h.waitForInitialized(5000);
        updates = h.reset();
        Assert.assertTrue(updates.get("$type").equals(DSString.valueOf("number")));

        //remove the value
        link.getMain().remove("def");
        h.waitForInitialized(5000);
        updates = h.reset();
        Assert.assertTrue(updates.containsKey("$disconnectedTs"));
        h.getStream().closeStream();
    }

    private void subDeepValue(DSLink link) {
        DSIRequester requester = link.getConnection().getRequester();

        //subscribe to non-existent value
        SimpleSubscribeHandler h = (SimpleSubscribeHandler) requester.subscribe(
                "/main/def/abc", DSInt.valueOf(0), new SimpleSubscribeHandler());
        SimpleSubscribeHandler.Update update = h.nextUpdate(5000);
        Assert.assertTrue(update.status == DSStatus.unknown);

        //add the value
        DSNode node = new DSNode();
        node.add("abc", DSLong.valueOf(123));
        link.getMain().put("def", node);
        update = h.nextUpdate(5000);
        Assert.assertTrue(update.status == DSStatus.ok);

        //remove the value
        /*
        link.getMain().remove("def");
        update = h.nextUpdate(5000);
        Assert.assertTrue(update.status == DSStatus.unknown);
        h.getStream().closeStream();
        */
    }

    private void subSimpleValue(DSLink link) {
        DSIRequester requester = link.getConnection().getRequester();

        //subscribe to non-existent value
        SimpleSubscribeHandler h = (SimpleSubscribeHandler) requester.subscribe(
                "/main/def", DSInt.valueOf(0), new SimpleSubscribeHandler());
        SimpleSubscribeHandler.Update update = h.nextUpdate(5000);
        Assert.assertTrue(update.status == DSStatus.unknown);

        //add the value
        link.getMain().put("def", DSLong.valueOf(123));
        update = h.nextUpdate(5000);
        Assert.assertTrue(update.status == DSStatus.ok);

        //remove the value
        link.getMain().remove("def");
        update = h.nextUpdate(5000);
        Assert.assertTrue(update.status == DSStatus.unknown);
        h.getStream().closeStream();
    }

    // Inner Classes
    // -------------

    public static class MyMain extends DSMainNode {

        @Override
        protected void declareDefaults() {
            super.declareDefaults();
            declareDefault("abc", DSLong.valueOf(0), "foo");
        }

    }
}
