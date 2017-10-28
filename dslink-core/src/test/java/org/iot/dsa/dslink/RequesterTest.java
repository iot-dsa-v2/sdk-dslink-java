package org.iot.dsa.dslink;

import com.acuity.iot.dsa.dslink.DSTransport;
import com.acuity.iot.dsa.dslink.protocol.protocol_v1.DS1ConnectionInit;
import com.acuity.iot.dsa.dslink.protocol.protocol_v1.DS1LinkConnection;
import com.acuity.iot.dsa.dslink.protocol.protocol_v1.DS1Session;
import java.util.logging.Level;
import org.iot.dsa.DSRuntime;
import org.iot.dsa.dslink.requester.AbstractSubscribeHandler;
import org.iot.dsa.dslink.requester.BasicRequestHandler;
import org.iot.dsa.node.DSElement;
import org.iot.dsa.node.DSInt;
import org.iot.dsa.node.DSMap;
import org.iot.dsa.node.DSStatus;
import org.iot.dsa.time.DSDateTime;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author Aaron Hansen
 */
public class RequesterTest implements DSLinkConnection.Listener {

    // Constants
    // ---------

    // Fields
    // ------

    private boolean success = false;
    private DSLink link;
    private MyRoot root;

    // Constructors
    // ------------

    // Methods
    // -------

    public void onConnect(DSLinkConnection connection) {
        DSIRequester requester = link.getConnection().getRequester();
        requester.subscribe("/Nodes/int", 0, new AbstractSubscribeHandler() {
            @Override
            public void onUpdate(DSDateTime dateTime, DSElement value, DSStatus status) {
                System.out.println("onUpdate!");
                success = value.equals(DSInt.valueOf(10));
                synchronized (RequesterTest.this) {
                    RequesterTest.this.notify();
                }
            }

            @Override
            public void onClose() {
            }

            @Override
            public void onError(DSElement details) {
                Thread.dumpStack();
            }
        });
        requester.set("/Nodes/int", DSInt.valueOf(10), BasicRequestHandler.DEFAULT);
    }

    public void onDisconnect(DSLinkConnection connection) {
    }

    @Test
    public void theTest() throws Exception {
        DSLinkConfig cfg = new DSLinkConfig();
        cfg.setDslinkJson(new DSMap().put("configs", new DSMap()));
        cfg.setLinkName("dslink-java-testing");
        cfg.setRequester(true);
        cfg.setLogLevel(Level.FINEST);
        cfg.setConfig(DSLinkConfig.CFG_CONNECTION_TYPE, MyConnection.class.getName());
        cfg.setConfig(DSLinkConfig.CFG_STABLE_DELAY, 1);
        link = new DSLink()
                .setSaveEnabled(false)
                .setNodes(root = new MyRoot())
                .init(cfg);
        link.getConnection().addListener(this);
        DSRuntime.run(link);
        synchronized (this) {
            this.wait(5000);
        }
        Assert.assertTrue(success);
        link.stop();
        link = null;
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
