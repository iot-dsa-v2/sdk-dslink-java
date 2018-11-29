package org.iot.dsa.dslink;

import com.acuity.iot.dsa.dslink.test.V1TestLink;
import com.acuity.iot.dsa.dslink.test.V2TestLink;
import java.util.Collection;
import org.iot.dsa.DSRuntime;
import org.iot.dsa.dslink.requester.SimpleInvokeHandler;
import org.iot.dsa.node.DSIValue;
import org.iot.dsa.node.DSInfo;
import org.iot.dsa.node.DSList;
import org.iot.dsa.node.DSMap;
import org.iot.dsa.node.DSMetadata;
import org.iot.dsa.node.DSNode;
import org.iot.dsa.node.DSString;
import org.iot.dsa.node.action.ActionInvocation;
import org.iot.dsa.node.action.ActionResult;
import org.iot.dsa.node.action.ActionSpec;
import org.iot.dsa.node.action.ActionTable;
import org.iot.dsa.node.action.DSAction;
import org.iot.dsa.node.event.DSIEvent;
import org.iot.dsa.node.event.DSISubscriber;
import org.iot.dsa.node.event.DSITopic;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author Aaron Hansen
 */
public class StreamTableTest {

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
        link.getConnection().subscribe(DSLinkConnection.CONNECTED, null, new DSISubscriber() {
            @Override
            public void onEvent(DSNode node, DSInfo child, DSIEvent event) {
                success = true;
                synchronized (StreamTableTest.this) {
                    StreamTableTest.this.notifyAll();
                }
            }

            @Override
            public void onUnsubscribed(DSITopic topic, DSNode node, DSInfo child) {
            }
        });
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
        DSList row = res.getUpdate(5000);
        Assert.assertFalse(res.isClosed());
        Assert.assertTrue(res.getColumnCount() == 3);
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
        Assert.assertFalse(res.isClosed());
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
        public DSInfo getDynamicAction(DSInfo target, String name) {
            return actionInfo(name, new DSAction.Parameterless() {
                @Override
                public ActionResult invoke(DSInfo target, ActionInvocation invocation) {
                    return new OpenTable(this, invocation);
                }

                {//can't have constructor, so use initializer
                    setResultType(ResultType.STREAM_TABLE);
                }
            });
        }

        @Override
        public void getDynamicActions(DSInfo target, Collection<String> bucket) {
            bucket.add("getTable");
        }

    }

    static class OpenTable implements ActionTable {

        private DSAction action;
        boolean closed = false;
        int count = 0;
        private ActionInvocation invocation;

        OpenTable(DSAction action, ActionInvocation invocation) {
            this.action = action;
            this.invocation = invocation;
        }

        @Override
        public ActionSpec getAction() {
            return action;
        }

        @Override
        public int getColumnCount() {
            return 3;
        }

        @Override
        public void getMetadata(int index, DSMap bucket) {
            new DSMetadata(bucket).setName("column" + index).setType(DSString.NULL);
        }

        @Override
        public DSIValue getValue(int index) {
            return null;
        }

        @Override
        public boolean next() {
            DSRuntime.run(() -> {
                DSList row = new DSList();
                row.add("1_0");
                row.add("1_1");
                row.add("1_2");
                invocation.send(row);
            });
            DSRuntime.runDelayed(() -> {
                DSList row = new DSList();
                row.add("2_0");
                row.add("2_1");
                row.add("2_2");
                invocation.send(row);
            }, 10);
            DSRuntime.runDelayed(() -> {
                DSList row = new DSList();
                row.add("3_0");
                row.add("3_1");
                row.add("3_2");
                invocation.send(row);
                invocation.close();
            }, 100);
            return false;
        }

        @Override
        public void onClose() {
            closed = true;
        }
    }

}
