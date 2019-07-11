package com.acuity.iot.dsa.dslink.test;

import com.acuity.iot.dsa.dslink.protocol.DSRootLink;
import com.acuity.iot.dsa.dslink.protocol.v2.DS2LinkConnection;
import com.acuity.iot.dsa.dslink.transport.DSTransport;
import com.acuity.iot.dsa.dslink.transport.PipedTransport;
import org.iot.dsa.dslink.DSLinkOptions;
import org.iot.dsa.dslink.DSMainNode;
import org.iot.dsa.node.DSMap;

/**
 * Routes requests and responses back to self.
 *
 * @author Aaron Hansen
 */
public class V2TestLink extends DSRootLink {

    public V2TestLink() {
    }

    public V2TestLink(DSMainNode MainNode) {
        setMain(MainNode);
        DSLinkOptions cfg = new DSLinkOptions();
        cfg.setDslinkMap(new DSMap().put("configs", new DSMap()));
        cfg.setLinkName("dslink-java-testing");
        cfg.setConfig(DSLinkOptions.CFG_CONNECTION_TYPE, TestConnection.class.getName());
        cfg.setConfig(DSLinkOptions.CFG_STABLE_DELAY, 1);
        init(cfg);
        getSys().getBackupService().setEnabled(false);
    }

    public static class TestConnection extends DS2LinkConnection {

        @Override
        public void checkConfig() {
        }

        @Override
        public String getPathInBroker() {
            return "";
        }

        @Override
        protected void initializeConnection() {
        }

        @Override
        protected DSTransport makeTransport() {
            return new PipedTransport().setText(false);
        }

        @Override
        protected void onConnected() {
            super.onConnected();
            getSession().setRequesterAllowed(true);
        }

    }

}
