package org.iot.dsa.dslink;

import com.acuity.iot.dsa.dslink.test.V1TestLink;
import com.acuity.iot.dsa.dslink.test.V2TestLink;
import java.util.ArrayList;
import java.util.Collection;
import org.iot.dsa.dslink.requester.SimpleInvokeHandler;
import org.iot.dsa.node.DSIValue;
import org.iot.dsa.node.DSInfo;
import org.iot.dsa.node.DSList;
import org.iot.dsa.node.DSLong;
import org.iot.dsa.node.DSMap;
import org.iot.dsa.node.DSMetadata;
import org.iot.dsa.node.DSString;
import org.iot.dsa.node.action.ActionInvocation;
import org.iot.dsa.node.action.ActionResult;
import org.iot.dsa.node.action.ActionSpec;
import org.iot.dsa.node.action.ActionValues;
import org.iot.dsa.node.action.DSAction;
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
        link.getConnection().subscribe((topic, node, child, data) -> {
            success = true;
            synchronized (ActionValuesTest.this) {
                ActionValuesTest.this.notifyAll();
            }
        }, DSLinkConnection.CONNECTED, null);
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
        Assert.assertTrue(res.isClosed());
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
        Assert.assertTrue(row.get(0).toInt() == 0);
        Assert.assertTrue(row.get(1).toInt() == 1);
        Assert.assertTrue(row.get(2).toInt() == 2);
        link.shutdown();
    }

    // Inner Classes
    // -------------

    public static class MyMain extends DSMainNode {

        @Override
        public DSInfo getVirtualAction(DSInfo target, String name) {
            return actionInfo(name, new DSAction.Parameterless() {
                @Override
                public ActionResult invoke(DSInfo target, ActionInvocation invocation) {
                    return new Values(this);
                }

                {//can't have constructor, so use initializer
                    setResultType(ResultType.VALUES);
                }
            });
        }

        @Override
        public void getVirtualActions(DSInfo target, Collection<String> bucket) {
            bucket.add("getValues");
        }

    }

    static class Values implements ActionValues {

        private DSAction action;
        boolean closed = false;

        Values(DSAction action) {
            this.action = action;
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
            return DSLong.valueOf(index);
        }

        @Override
        public void onClose() {
            closed = true;
        }
    }

}
