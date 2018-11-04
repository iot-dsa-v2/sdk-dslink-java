package com.acuity.iot.dsa.dslink.test;

import com.acuity.iot.dsa.dslink.protocol.v1.DS1ConnectionInit;
import com.acuity.iot.dsa.dslink.protocol.v1.DS1LinkConnection;
import com.acuity.iot.dsa.dslink.transport.DSTransport;
import org.iot.dsa.dslink.DSLink;
import org.iot.dsa.dslink.DSLinkOptions;
import org.iot.dsa.dslink.DSMainNode;
import org.iot.dsa.node.DSMap;

/**
 * Routes requests and responses back to self.
 *
 * @author Aaron Hansen
 */
public class V1TestLink extends DSLink {

    public V1TestLink() {
    }

    public V1TestLink(DSMainNode MainNode) {
        getSys().getBackupService().setEnabled(false);
        setNodes(MainNode);
        DSLinkOptions cfg = new DSLinkOptions();
        cfg.setDslinkMap(new DSMap().put("configs", new DSMap()));
        cfg.setLinkName("dslink-java-testing");
        //cfg.setLogLevel(Level.FINEST);
        cfg.setConfig(DSLinkOptions.CFG_CONNECTION_TYPE, TestConnection.class.getName());
        cfg.setConfig(DSLinkOptions.CFG_STABLE_DELAY, 1);
        init(cfg);
    }

    public static class TestConnection extends DS1LinkConnection {

        public String getPathInBroker() {
            return "";
        }

        protected DS1ConnectionInit initializeConnection() {
            return new DS1ConnectionInit();
        }

        /**
         * Looks at the connection initialization response to determine the type of transport then
         * instantiates the correct type fom the config.
         */
        protected DSTransport makeTransport(DS1ConnectionInit init) {
            TestTransport transport = new TestTransport();
            transport.setConnection(this);
            transport.setReadTimeout(getLink().getOptions().getConfig(
                    DSLinkOptions.CFG_READ_TIMEOUT, 60000));
            setTransport(transport);
            return transport;
        }

    }
}
