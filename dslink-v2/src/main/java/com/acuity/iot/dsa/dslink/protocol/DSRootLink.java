package com.acuity.iot.dsa.dslink.protocol;

import com.acuity.iot.dsa.dslink.protocol.v1.DS1LinkConnection;
import com.acuity.iot.dsa.dslink.protocol.v2.DS2LinkConnection;
import org.iot.dsa.dslink.DSLink;
import org.iot.dsa.dslink.DSLinkConnection;
import org.iot.dsa.dslink.DSLinkOptions;
import org.iot.dsa.dslink.DSMainNode;
import org.iot.dsa.dslink.DSSysNode;
import org.iot.dsa.node.DSInfo;
import org.iot.dsa.node.DSNode;
import org.iot.dsa.util.DSUtil;

/**
 * Link implementation for standalone v1 and v2 links.
 *
 * @author Aaron Hansen
 */
public class DSRootLink extends DSLink {

    ///////////////////////////////////////////////////////////////////////////
    // Class Fields
    ///////////////////////////////////////////////////////////////////////////

    ///////////////////////////////////////////////////////////////////////////
    // Instance Fields
    ///////////////////////////////////////////////////////////////////////////

    private String dsId;
    private DSKeys keys;
    private DSInfo main;
    private DSInfo sys;
    private DSInfo upstream;

    ///////////////////////////////////////////////////////////////////////////
    // Constructors
    ///////////////////////////////////////////////////////////////////////////

    ///////////////////////////////////////////////////////////////////////////
    // Public Methods
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Returns the unique id of the connection.  This is the link name + '-' + the hash of the
     * public key in base64.
     *
     * @return Never null, and url safe.
     */
    public String getDsId() {
        if (dsId == null) {
            StringBuilder buf = new StringBuilder();
            buf.append(getLinkName());
            buf.append('-');
            buf.append(getKeys().encodePublicHashDsId());
            dsId = buf.toString();
        }
        return dsId;
    }

    /**
     * Public / private keys of the link, used to prove identity with brokers.
     */
    public DSKeys getKeys() {
        return keys;
    }

    @Override
    public DSMainNode getMain() {
        return (DSMainNode) main.getNode();
    }

    public DSSysNode getSys() {
        return (DSSysNode) sys.get();
    }

    @Override
    public DSLinkConnection getUpstream() {
        return (DSLinkConnection) upstream.get();
    }

    ///////////////////////////////////////////////////////////////////////////
    // Protected Methods
    ///////////////////////////////////////////////////////////////////////////

    @Override
    protected DSLink init(DSLinkOptions config) {
        super.init(config);
        keys = config.getKeys();
        main = getInfo(MAIN);
        if (main == null) {
            String type = getOptions().getMainType();
            if (type != null) {
                debug("Main node type: " + type);
                DSNode node = (DSNode) DSUtil.newInstance(type);
                main = put(MAIN, node);
            }
        }
        sys = getInfo(SYS);
        if (sys == null) {
            sys = put(SYS, new DSSysNode()).setAdmin(true);
        }
        String ver = config.getDsaVersion();
        DSLinkConnection conn;
        String type = config.getConfig(DSLinkOptions.CFG_CONNECTION_TYPE, null);
        if (type != null) {
            conn = (DSLinkConnection) DSUtil.newInstance(type);
        } else if (ver.startsWith("1")) {
            conn = new DS1LinkConnection();
        } else { //2
            conn = new DS2LinkConnection();
        }
        debug(debug() ? "Connection type: " + conn.getClass().getName() : null);
        upstream = put(UPSTREAM, conn);
        upstream.setTransient(true);
        return this;
    }

    ///////////////////////////////////////////////////////////////////////////
    // Package / Private Methods
    ///////////////////////////////////////////////////////////////////////////

    ///////////////////////////////////////////////////////////////////////////
    // Inner Classes
    ///////////////////////////////////////////////////////////////////////////

    ///////////////////////////////////////////////////////////////////////////
    // Initialization
    ///////////////////////////////////////////////////////////////////////////

}
