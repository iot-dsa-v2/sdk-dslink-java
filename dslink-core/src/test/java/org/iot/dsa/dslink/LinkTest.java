package org.iot.dsa.dslink;

import com.acuity.iot.dsa.dslink.DSTransport;
import com.acuity.iot.dsa.dslink.protocol.protocol_v1.DS1ConnectionInit;
import com.acuity.iot.dsa.dslink.protocol.protocol_v1.DS1LinkConnection;
import com.acuity.iot.dsa.dslink.protocol.protocol_v1.DS1Session;
import org.iot.dsa.DSRuntime;
import org.iot.dsa.dslink.requester.OutboundListHandler;
import org.iot.dsa.dslink.requester.OutboundListStub;
import org.iot.dsa.node.DSInt;
import org.iot.dsa.node.DSMap;
import org.junit.Test;

/**
 * @author Aaron Hansen
 */
public class LinkTest {

    // Constants
    // ---------

    // Fields
    // ------

    private DSLink link;

    // Constructors
    // ------------

    // Methods
    // -------

    public void onConnectionClose(DSLinkConnection connection) {
    }

    public void onConnectionOpen(DSLinkConnection connection) {
    }

    @Test
    public void theTest() throws Exception {
        DSLinkConfig cfg = new DSLinkConfig();
        cfg.setDslinkJson(new DSMap().put("configs", new DSMap()));
        cfg.setLinkName("dslink-java-testing");
        cfg.setRequester(true);
        cfg.setConfig(DSLinkConfig.CFG_CONNECTION_TYPE, MyConnection.class.getName());
        cfg.setConfig(DSLinkConfig.CFG_STABLE_DELAY, 1);
        DSLink link = new DSLink()
                .setSaveEnabled(false)
                .setNodes(new MyRoot())
                .init(cfg);
        link.getConnection().addListener(this);
        DSRuntime.run(link);
        //TODO how know when link is connected?
        Thread.sleep(2000);
        DSIRequester requester = link.getConnection().getRequester();

        OutboundListHandler req = new OutboundListHandler() {
            @Override
            public void onClose() {
                System.out.println("Success!");
            }

            @Override
            public void onResponse(DSMap response) {
                handleList(response);
            }
        };
        req.setPath("/Nodes");
        OutboundListStub stub = requester.list(req);
        synchronized (this) {
            try {
                wait(10000);
            } catch (Exception x) {
            }
        }
        stub.close();
        synchronized (this) {
            try {
                wait(10000);
            } catch (Exception x) {
            }
        }
        link.stop();
    }

    private synchronized void handleList(DSMap response) {
        System.out.println(response.toString());
        notify();
    }

    private void testMine() {
    }

    // Inner Classes
    // -------------

    public static class MyRoot extends DSRootNode {

        public void declareDefaults() {
            declareDefault("int", DSInt.valueOf(0));
        }
    }

    public static class MyConnection extends DS1LinkConnection {

        protected DS1ConnectionInit initializeConnection() throws Exception {
            DS1ConnectionInit init = new DS1ConnectionInit();
            return init;
        }

        protected DS1Session makeSession(DS1ConnectionInit init) {
            DS1Session ret = new DS1Session();
            ret.setRequesterAllowed();
            return ret;
        }

        protected DSTransport makeTransport(DS1ConnectionInit init) {
            TestTransport transport = new TestTransport();
            transport.setConnection(this);
            return transport;
        }
    }

}
