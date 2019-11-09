package org.iot.dsa.dslink;

import com.acuity.iot.dsa.dslink.test.V1TestLink;
import com.acuity.iot.dsa.dslink.test.V2TestLink;
import org.iot.dsa.dslink.requester.SimpleInvokeHandler;
import org.iot.dsa.node.DSBool;
import org.iot.dsa.node.DSInt;
import org.iot.dsa.node.DSMap;
import org.iot.dsa.node.DSNode;
import org.iot.dsa.node.action.DSAction;
import org.iot.dsa.node.action.DSIActionRequest;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author Aaron Hansen
 */
public class RequesterInvokeTest {

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

    private void doit(DSLink link) {
        success = false;
        Thread t = new Thread(link, "DSLink Runner");
        t.start();
        link.getConnection().waitForConnection(5000);
        Assert.assertTrue(link.getConnection().isConnected());
        DSIRequester requester = link.getConnection().getRequester();
        SimpleInvokeHandler res = (SimpleInvokeHandler) requester.invoke(
                "/main/simpleAction", null, new SimpleInvokeHandler());
        res.waitForClose(1000);
        res = (SimpleInvokeHandler) requester.invoke(
                "/main/simpleParam",
                new DSMap().put("param", true),
                new SimpleInvokeHandler());
        res.waitForClose(1000);
        Assert.assertTrue(success);
        try {
            success = false;
            res = (SimpleInvokeHandler) requester.invoke(
                    "/main/exception",
                    new DSMap().put("param", true),
                    new SimpleInvokeHandler());
            res.waitForClose(5000);
        } catch (Exception x) {
            success = true;
        }
        Assert.assertTrue(success);
        link.shutdown();
    }

    // Inner Classes
    // -------------

    public static class ANode extends DSNode {

        @Override
        public void declareDefaults() {
            declareDefault("int", DSInt.valueOf(0));
        }

        @Override
        public ActionResults invoke(DSIActionRequest request) {
            return null;
        }

    }

    public static class MyMain extends DSMainNode {

        @Override
        public void declareDefaults() {
            declareDefault("anode", new ANode());
            declareDefault("simpleAction", DSAction.DEFAULT);
            DSAction action = new DSAction();
            action.addParameter("param", DSBool.NULL, "a desc");
            declareDefault("simpleParam", action);
            declareDefault("exception", DSAction.DEFAULT);
        }

        @Override
        public ActionResults invoke(DSIActionRequest request) {
            String name = request.getActionInfo().getName();
            if (name.equals("simpleAction")) {
                success = true;
            } else if (name.equals("simpleParam")) {
                DSMap params = request.getParameters();
                success = params.get("param", false);
            } else if (name.equals("exception")) {
                throw new IllegalStateException("Expected exception");
            }
            return null;
        }

    }

}
