package com.acuity.iot.dsa.dslink.sys.cert;

import org.iot.dsa.node.DSBool;
import org.iot.dsa.node.DSInfo;
import org.iot.dsa.node.DSNode;

/**
 * Allows certification management.
 *
 * @author Aaron Hansen
 */
public class SysCertManager extends DSNode {

    // Constants
    // ---------

    private static final String ALLOW_CLIENTS = "Allow_Anonymous_Clients";
    private static final String ALLOW_SERVERS = "Allow_Anonymous_Servers";

    // Fields
    // ------

    private DSInfo allowClients = getInfo(ALLOW_CLIENTS);
    private DSInfo allowServers = getInfo(ALLOW_SERVERS);
    private AnonymousTrustFactory myTrustFactory;

    // Methods
    // -------

    /**
     * True if self signed anonymous client certs are allowed.
     */
    public boolean allowAnonymousClients() {
        return allowClients.getElement().toBoolean();
    }

    /**
     * True if self signed anonymous server certs are allowed.
     */
    public boolean allowAnonymousServers() {
        return allowServers.getElement().toBoolean();
    }

    @Override
    public void declareDefaults() {
        declareDefault(ALLOW_CLIENTS, DSBool.FALSE);
        declareDefault(ALLOW_SERVERS, DSBool.FALSE);
    }

    @Override
    public void onStarted() {
        AnonymousTrustFactory.init(this);
    }

}
