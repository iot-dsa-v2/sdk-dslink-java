package com.acuity.iot.dsa.dslink.protocol;

import com.acuity.iot.dsa.dslink.protocol.v1.DS1LinkConnection;
import com.acuity.iot.dsa.dslink.protocol.v2.DS2LinkConnection;
import com.acuity.iot.dsa.dslink.sys.DSSysNode;
import org.iot.dsa.dslink.DSLink;
import org.iot.dsa.dslink.DSLinkConnection;
import org.iot.dsa.dslink.DSLinkOptions;
import org.iot.dsa.dslink.DSMainNode;
import org.iot.dsa.node.DSInfo;
import org.iot.dsa.node.DSNode;
import org.iot.dsa.util.DSUtil;

/**
 * Links that are also the roots of a node tree.  These links have sys and upstream children.
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

    private DSInfo<?> main;
    private DSInfo<?> sys = getInfo(SYS);
    private DSInfo<?> upstream;

    ///////////////////////////////////////////////////////////////////////////
    // Constructors
    ///////////////////////////////////////////////////////////////////////////

    ///////////////////////////////////////////////////////////////////////////
    // Public Methods
    ///////////////////////////////////////////////////////////////////////////

    @Override
    public DSLinkConnection getConnection() {
        return (DSLinkConnection) upstream.get();
    }

    @Override
    public DSMainNode getMain() {
        return (DSMainNode) main.getNode();
    }

    public DSSysNode getSys() {
        return (DSSysNode) sys.get();
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
        main = getInfo(getMainName());
        if (main == null) {
            String type = getOptions().getMainType();
            if (type != null) {
                debug("Main node type: " + type);
                DSNode node = (DSNode) DSUtil.newInstance(type);
                main = put(getMainName(), node).setLocked(true);
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
        upstream = put(UPSTREAM, conn).setTransient(true).setLocked(true);
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
