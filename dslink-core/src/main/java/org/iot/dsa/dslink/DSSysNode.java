package org.iot.dsa.dslink;

import com.acuity.iot.dsa.dslink.protocol.protocol_v1.DS1LinkConnection;
import com.acuity.iot.dsa.dslink.protocol.protocol_v2.DS2LinkConnection;
import org.iot.dsa.node.DSInfo;
import org.iot.dsa.node.DSNode;
import org.iot.dsa.node.action.ActionInvocation;
import org.iot.dsa.node.action.ActionResult;
import org.iot.dsa.node.action.DSAction;
import org.iot.dsa.util.DSException;

/**
 * The root of the system nodes.
 *
 * @author Aaron Hansen
 */
public class DSSysNode extends DSNode {

    static final String CONNECTION = "connection";
    static final String SAVE = "save";
    static final String STOP = "stop";

    private DSInfo connection = getInfo(CONNECTION);
    private DSInfo save = getInfo(SAVE);
    private DSInfo stop = getInfo(STOP);

    @Override
    protected void declareDefaults() {
        declareDefault(CONNECTION, new DSNode()).setTransient(true);
        declareDefault(SAVE, DSAction.DEFAULT);
        declareDefault(STOP, DSAction.DEFAULT);
    }

    public DSLinkConnection getConnection() {
        return (DSLinkConnection) connection.getObject();
    }

    public DSLink getLink() {
        return (DSLink) getParent();
    }

    void init() {
        DSLinkConfig config = getLink().getConfig();
        try {
            String ver = config.getDsaVersion();
            DSLinkConnection conn;
            if (ver.startsWith("1")) {
                String type = config.getConfig(DSLinkConfig.CFG_CONNECTION_TYPE, null);
                if (type != null) {
                    config(config() ? "Connection type: " + type : null);
                    conn = (DSLinkConnection) Class.forName(type).newInstance();
                } else {
                    conn = new DS1LinkConnection();
                }
            } else { //2
                conn = new DS2LinkConnection();
            }
            put(connection, conn);
        } catch (Exception x) {
            DSException.throwRuntime(x);
        }
    }

    @Override
    public ActionResult onInvoke(DSInfo action, ActionInvocation invocation) {
        if (action == save) {
            getLink().save();
        } else if (action == stop) {
            getLink().shutdown();
        } else {
            super.onInvoke(action, invocation);
        }
        return null;
    }

    @Override
    protected void validateParent(DSNode node) {
        if (node instanceof DSLink) {
            return;
        }
        throw new IllegalArgumentException("Invalid parent: " + node.getClass().getName());
    }

}
