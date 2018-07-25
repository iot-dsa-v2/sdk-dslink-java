package com.acuity.iot.dsa.dslink.test;

import com.acuity.iot.dsa.dslink.protocol.v2.DS2LinkConnection;
import com.acuity.iot.dsa.dslink.transport.DSBinaryTransport;
import org.iot.dsa.dslink.DSLink;
import org.iot.dsa.dslink.DSLinkConfig;
import org.iot.dsa.dslink.DSMainNode;
import org.iot.dsa.node.DSMap;

/**
 * Routes requests and responses back to self.
 *
 * @author Aaron Hansen
 */
public class V2TestLink extends DSLink {

    public V2TestLink() {
    }

    public V2TestLink(DSMainNode MainNode) {
        setSaveEnabled(false);
        setNodes(MainNode);
        DSLinkConfig cfg = new DSLinkConfig();
        cfg.setDslinkJson(new DSMap().put("configs", new DSMap()));
        cfg.setLinkName("dslink-java-testing");
        cfg.setConfig(DSLinkConfig.CFG_CONNECTION_TYPE, TestConnection.class.getName());
        cfg.setConfig(DSLinkConfig.CFG_STABLE_DELAY, 1);
        init(cfg);
    }

    public static class TestConnection extends DS2LinkConnection {

        public String getPathInBroker() {
            return "";
        }

        @Override
        protected DSBinaryTransport makeTransport() {
            TestTransport transport = new TestTransport();
            transport.setConnection(this);
            transport.setReadTimeout(getLink().getConfig().getConfig(
                    DSLinkConfig.CFG_READ_TIMEOUT, 60000));
            return transport;
        }

        @Override
        protected void onConnected() {
            super.onConnected();
            getSession().setRequesterAllowed(true);
        }

        protected void performHandshake() {
        }
    }

}
