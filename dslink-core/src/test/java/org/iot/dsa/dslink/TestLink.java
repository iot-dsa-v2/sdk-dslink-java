package org.iot.dsa.dslink;

import com.acuity.iot.dsa.dslink.DSConnection;
import org.iot.dsa.node.DSMap;
import org.iot.dsa.node.DSNode;

/**
 * Standalone link, routes requests and responses back to self.
 *
 * @author Aaron Hansen
 */
public abstract class TestLink extends DSNode implements DSResponder {

    ///////////////////////////////////////////////////////////////////////////
    // Constants
    ///////////////////////////////////////////////////////////////////////////

    ///////////////////////////////////////////////////////////////////////////
    // Fields
    ///////////////////////////////////////////////////////////////////////////

    ///////////////////////////////////////////////////////////////////////////
    // Constructors
    ///////////////////////////////////////////////////////////////////////////

    ///////////////////////////////////////////////////////////////////////////
    // Methods
    ///////////////////////////////////////////////////////////////////////////

    public static void main(String[] args) throws Exception {
        DSLinkConfig cfg = new DSLinkConfig(args)
                .setConfig(DSLinkConfig.CFG_CONNECTION_TYPE, MyConnection.class.getName())
                .setConfig(DSLinkConfig.CFG_TRANSPORT_FACTORY, TestTransport.class.getName());
        DSLink link = new DSLink(cfg);
        link.start();
    }

    ///////////////////////////////////////////////////////////////////////////
    // Inner Classes
    ///////////////////////////////////////////////////////////////////////////

    public static class MyConnection extends DSConnection {

        @Override
        protected DSMap initializeConnection() {
            return new DSMap()
                    .put("dsId",
                         "broker-dsa-FEuG-dsvoy3Mfh-DY4ZLqxWdcjA9mky2MyCd0DmqTMw")
                    .put("publicKey",
                         "BG4OYopcM2q09amKRKsc8N99ns5dybnBYG4Fi8bQVf6fKjyT_KRlPMJCs-3zvnSbBCXzS5fZfi88JuiLYwJY0gc")
                    .put("wsUri", "/ws")
                    .put("httpUri", "/http")
                    .put("tempKey",
                         "BARngwlfjwD7goZHCh_4iWsP0e3JszsvOtovn1UyPnqZLlSOyoUH1v_Lop0oUFClpVhlzsWAAqur6S8apZaBe4I")
                    .put("salt", "0x205")
                    .put("path", "/downstream/link")
                    .put("version", "1.1.2")
                    .put("updateInterval", 200)
                    .put("format", "json");
        }

    }

    ///////////////////////////////////////////////////////////////////////////
    // Initialization
    ///////////////////////////////////////////////////////////////////////////

} //class
