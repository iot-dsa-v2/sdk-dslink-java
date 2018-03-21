package com.acuity.iot.dsa.dslink.test;

import com.acuity.iot.dsa.dslink.protocol.v1.DS1ConnectionInit;
import com.acuity.iot.dsa.dslink.protocol.v1.DS1LinkConnection;
import com.acuity.iot.dsa.dslink.protocol.v1.DS1Session;
import com.acuity.iot.dsa.dslink.transport.DSTransport;
import java.util.logging.Level;
import org.iot.dsa.dslink.DSLink;
import org.iot.dsa.dslink.DSLinkConfig;
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
        setSaveEnabled(false);
        setNodes(MainNode);
        DSLinkConfig cfg = new DSLinkConfig();
        cfg.setDslinkJson(new DSMap().put("configs", new DSMap()));
        cfg.setLinkName("dslink-java-testing");
        cfg.setLogLevel(Level.FINEST);
        cfg.setConfig(DSLinkConfig.CFG_CONNECTION_TYPE, TestConnection.class.getName());
        cfg.setConfig(DSLinkConfig.CFG_STABLE_DELAY, 1);
        init(cfg);
    }

    public static class TestConnection extends DS1LinkConnection {

        protected DS1ConnectionInit initializeConnection() {
            return new DS1ConnectionInit();
        }

        /**
         * Looks at the connection initialization response to determine the protocol
         * implementation.
         */
        protected DS1Session makeSession(DS1ConnectionInit init) {
            DS1Session ret = new DS1Session(TestConnection.this);
            ret.setRequesterAllowed();
            return ret;
        }

        /**
         * Looks at the connection initialization response to determine the type of transport then
         * instantiates the correct type fom the config.
         */
        protected DSTransport makeTransport(DS1ConnectionInit init) {
            TestTransport transport = new TestTransport();
            transport.setConnection(this);
            transport.setReadTimeout(getLink().getConfig().getConfig(
                    DSLinkConfig.CFG_READ_TIMEOUT, 60000));
            setTransport(transport);
            return transport;
        }

    }
}
