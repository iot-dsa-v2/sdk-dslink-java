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
 * Links that also the root of the node tree.  These links have sys and upstream children.
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

    private DSInfo main;
    private DSInfo sys = getInfo(SYS);
    private DSInfo upstream;

    ///////////////////////////////////////////////////////////////////////////
    // Constructors
    ///////////////////////////////////////////////////////////////////////////

    ///////////////////////////////////////////////////////////////////////////
    // Public Methods
    ///////////////////////////////////////////////////////////////////////////

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
    protected void declareDefaults() {
        super.declareDefaults();
        declareDefault(SYS, new DSSysNode(),
                       "Services common to all links in the process.").setAdmin(true);
    }

    @Override
    protected DSLink init(DSLinkOptions config) {
        super.init(config);
        main = getInfo(MAIN);
        if (main == null) {
            String type = getOptions().getMainType();
            if (type != null) {
                debug("Main node type: " + type);
                DSNode node = (DSNode) DSUtil.newInstance(type);
                main = put(MAIN, node);
            }
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
