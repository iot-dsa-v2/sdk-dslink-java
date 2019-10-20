package org.iot.dsa.dslink;

import com.acuity.iot.dsa.dslink.test.V1TestLink;
import com.acuity.iot.dsa.dslink.test.V2TestLink;
import java.util.ArrayList;
import java.util.Collection;
import org.iot.dsa.dslink.Action.ResultsType;
import org.iot.dsa.dslink.requester.SimpleInvokeHandler;
import org.iot.dsa.node.DSIValue;
import org.iot.dsa.node.DSInfo;
import org.iot.dsa.node.DSList;
import org.iot.dsa.node.DSMap;
import org.iot.dsa.node.DSMetadata;
import org.iot.dsa.node.DSString;
import org.iot.dsa.node.action.DSAction;
import org.iot.dsa.node.action.DSIActionRequest;
import org.iot.dsa.table.DSIResultsCursor;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author Aaron Hansen
 */
public class ClosedTableTest {

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
            synchronized (ClosedTableTest.this) {
                ClosedTableTest.this.notifyAll();
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
                "/main/getTable", null, new SimpleInvokeHandler());
        res.waitForClose(5000);
        Assert.assertTrue(res.isClosed());
        Assert.assertEquals(res.getColumnCount(), 3);
        Assert.assertTrue(res.hasUpdates());
        DSMetadata meta = new DSMetadata();
        meta.setMap(res.getColumnMetadata(0));
        Assert.assertTrue(meta.getName().equals("column0"));
        meta.setMap(res.getColumnMetadata(1));
        Assert.assertTrue(meta.getName().equals("column1"));
        meta.setMap(res.getColumnMetadata(2));
        Assert.assertTrue(meta.getName().equals("column2"));
        ArrayList<DSList> updates = res.getUpdates();
        Assert.assertTrue(updates.size() == 3);
        DSList row = updates.get(0);
        Assert.assertTrue(row.get(0).toString().equals("1_0"));
        Assert.assertTrue(row.get(1).toString().equals("1_1"));
        Assert.assertTrue(row.get(2).toString().equals("1_2"));
        row = updates.get(1);
        Assert.assertTrue(row.get(0).toString().equals("2_0"));
        Assert.assertTrue(row.get(1).toString().equals("2_1"));
        Assert.assertTrue(row.get(2).toString().equals("2_2"));
        row = updates.get(2);
        Assert.assertTrue(row.get(0).toString().equals("3_0"));
        Assert.assertTrue(row.get(1).toString().equals("3_1"));
        Assert.assertTrue(row.get(2).toString().equals("3_2"));
        link.shutdown();
    }

    // Inner Classes
    // -------------

    static class ClosedTable implements DSIResultsCursor {

        int count = 0;

        @Override
        public int getColumnCount() {
            return 3;
        }

        @Override
        public void getColumnMetadata(int index, DSMap bucket) {
            new DSMetadata(bucket).setName("column" + index).setType(DSString.NULL);
        }

        @Override
        public DSIValue getValue(int index) {
            return DSString.format("%s_%s", count, index);
        }

        @Override
        public boolean next() {
            return ++count < 4;
        }

    }

    public static class MyMain extends DSMainNode {

        @Override
        public DSInfo<?> getVirtualAction(DSInfo<?> target, String name) {
            return virtualInfo(name, new DSAction() {
                @Override
                public ActionResults invoke(DSIActionRequest req) {
                    return makeResults(req, new ClosedTable());
                }
            }.setResultsType(ResultsType.TABLE));
        }

        @Override
        public void getVirtualActions(DSInfo<?> target, Collection<String> bucket) {
            bucket.add("getTable");
        }

    }

}
