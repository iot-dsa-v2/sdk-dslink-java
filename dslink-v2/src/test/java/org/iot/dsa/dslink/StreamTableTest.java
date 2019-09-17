package org.iot.dsa.dslink;

import com.acuity.iot.dsa.dslink.test.V1TestLink;
import com.acuity.iot.dsa.dslink.test.V2TestLink;
import java.util.Collection;
import org.iot.dsa.DSRuntime;
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
public class StreamTableTest {

    // Fields
    // ------

    // Methods
    // -------

    @Test
    public void test() throws Exception {
        doit(new V1TestLink(new MyMain()));
        doit(new V2TestLink(new MyMain()));
    }

    private void doit(DSLink link) throws Exception {
        Thread t = new Thread(link, "DSLink Runner");
        t.start();
        link.getConnection().waitForConnection(5000);
        Assert.assertTrue(link.getConnection().isConnected());
        DSIRequester requester = link.getConnection().getRequester();
        SimpleInvokeHandler res = (SimpleInvokeHandler) requester.invoke(
                "/main/getTable", null, new SimpleInvokeHandler());
        DSList row = res.getUpdate(5000);
        Assert.assertEquals(res.getColumnCount(), 3);
        DSMetadata meta = new DSMetadata();
        meta.setMap(res.getColumnMetadata(0));
        Assert.assertTrue(meta.getName().equals("column0"));
        meta.setMap(res.getColumnMetadata(1));
        Assert.assertTrue(meta.getName().equals("column1"));
        meta.setMap(res.getColumnMetadata(2));
        Assert.assertTrue(meta.getName().equals("column2"));
        Assert.assertTrue(row.get(0).toString().equals("1_0"));
        Assert.assertTrue(row.get(1).toString().equals("1_1"));
        Assert.assertTrue(row.get(2).toString().equals("1_2"));
        row = res.getUpdate(5000);
        Assert.assertTrue(row.get(0).toString().equals("2_0"));
        Assert.assertTrue(row.get(1).toString().equals("2_1"));
        Assert.assertTrue(row.get(2).toString().equals("2_2"));
        row = res.getUpdate(5000);
        Assert.assertTrue(row.get(0).toString().equals("3_0"));
        Assert.assertTrue(row.get(1).toString().equals("3_1"));
        Assert.assertTrue(row.get(2).toString().equals("3_2"));
        res.waitForClose(5000);
        Assert.assertTrue(res.isClosed());
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
                    return makeResults(req, new Stream(req), false);
                }
            }.setResultsType(ResultsType.STREAM));
        }

        @Override
        public void getVirtualActions(DSInfo target, Collection<String> bucket) {
            bucket.add("getTable");
        }

    }

    static class Stream implements DSIResultsCursor {

        private DSIActionRequest req;
        DSList row = new DSList();

        Stream(DSIActionRequest req) {
            this.req = req;
        }

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
            return row.removeFirst();
        }

        @Override
        public boolean next() {
            return !row.isEmpty();
        }

        {
            DSRuntime.runDelayed(() -> {
                row.add("1_0");
                row.add("1_1");
                row.add("1_2");
                req.enqueueResults();
            }, 100);
            DSRuntime.runDelayed(() -> {
                row.add("2_0");
                row.add("2_1");
                row.add("2_2");
                req.enqueueResults();
            }, 200);
            DSRuntime.runDelayed(() -> {
                row.add("3_0");
                row.add("3_1");
                row.add("3_2");
                req.enqueueResults();
                req.close();
            }, 300);
        }
    }

}
