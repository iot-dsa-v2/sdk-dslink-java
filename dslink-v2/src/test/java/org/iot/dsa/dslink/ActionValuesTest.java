package org.iot.dsa.dslink;

import com.acuity.iot.dsa.dslink.test.V1TestLink;
import com.acuity.iot.dsa.dslink.test.V2TestLink;
import java.util.ArrayList;
import java.util.Collection;
import org.iot.dsa.dslink.requester.SimpleInvokeHandler;
import org.iot.dsa.node.DSInfo;
import org.iot.dsa.node.DSList;
import org.iot.dsa.node.DSLong;
import org.iot.dsa.node.DSMetadata;
import org.iot.dsa.node.action.DSAction;
import org.iot.dsa.node.action.DSIActionRequest;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author Aaron Hansen
 */
public class ActionValuesTest {

    // Fields
    // ------

    private static boolean success = false;

    // Methods
    // -------

    @Test
    public void test() throws Exception {
        doit(new V1TestLink(new MyMain()));
        doit(new V2TestLink(new MyMain()));
    }

    private void doit(DSLink link) throws Exception {
        success = false;
        link.getConnection().subscribe((event, node, child, data) -> {
            success = true;
            synchronized (ActionValuesTest.this) {
                ActionValuesTest.this.notifyAll();
            }
        }, DSLinkConnection.CONNECTED_EVENT, null);
        Thread t = new Thread(link, "DSLink Runner");
        t.start();
        synchronized (this) {
            this.wait(5000);
        }
        Assert.assertTrue(success);
        success = false;
        DSIRequester requester = link.getConnection().getRequester();
        SimpleInvokeHandler res = (SimpleInvokeHandler) requester.invoke(
                "/main/getValues", null, new SimpleInvokeHandler());
        res.waitForClose(5000);
        Assert.assertTrue(res.getColumnCount() == 3);
        Assert.assertTrue(res.hasUpdates());
        DSMetadata meta = new DSMetadata();
        meta.setMap(res.getColumnMetadata(0));
        Assert.assertTrue(meta.getName().equals("column0"));
        meta.setMap(res.getColumnMetadata(1));
        Assert.assertTrue(meta.getName().equals("column1"));
        meta.setMap(res.getColumnMetadata(2));
        Assert.assertTrue(meta.getName().equals("column2"));
        ArrayList<DSList> updates = res.getUpdates();
        Assert.assertTrue(updates.size() == 1);
        DSList row = updates.get(0);
        Assert.assertTrue(row.get(0).toLong() == 0);
        Assert.assertTrue(row.get(1).toLong() == 1);
        Assert.assertTrue(row.get(2).toLong() == 2);
        link.shutdown();
    }

    // Inner Classes
    // -------------

    public static class MyMain extends DSMainNode {

        @Override
        public DSInfo getVirtualAction(DSInfo target, String name) {
            return virtualInfo(name, new DSAction() {
                @Override
                public ActionResults invoke(DSIActionRequest req) {
                    return makeResults(req, DSLong.valueOf(0), DSLong.valueOf(1),
                                       DSLong.valueOf(2));
                }

                {//can't have constructor, so use initializer
                    addColumnMetadata("column0", DSLong.NULL);
                    addColumnMetadata("column1", DSLong.NULL);
                    addColumnMetadata("column2", DSLong.NULL);
                    setResultsType(ResultsType.VALUES);
                }
            });
        }

        @Override
        public void getVirtualActions(DSInfo target, Collection<String> bucket) {
            bucket.add("getValues");
        }

    }

}
