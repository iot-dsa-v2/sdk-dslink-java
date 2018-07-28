package org.iot.dsa.dslink;

import com.acuity.iot.dsa.dslink.protocol.v1.DS1LinkConnection;
import com.acuity.iot.dsa.dslink.protocol.v2.DS2LinkConnection;
import com.acuity.iot.dsa.dslink.sys.backup.SysBackupService;
import com.acuity.iot.dsa.dslink.sys.cert.SysCertManager;
import com.acuity.iot.dsa.dslink.sys.logging.SysLogService;
import com.acuity.iot.dsa.dslink.sys.profiler.SysProfiler;
import org.iot.dsa.node.DSInfo;
import org.iot.dsa.node.DSNode;
import org.iot.dsa.node.DSNull;
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

    static final String CERTIFICATES = "Certificates";
    static final String CONNECTION = "Connection";
    static final String STOP = "Stop";
    static final String PROFILER = "Profiler";
    static final String LOGGING = "Logging";
    static final String BACKUPS = "Backups";

    private DSInfo connection = getInfo(CONNECTION);
    private DSInfo stop = getInfo(STOP);

    @Override
    protected void declareDefaults() {
        declareDefault(STOP, DSAction.DEFAULT);
        declareDefault(CERTIFICATES, new SysCertManager());
        declareDefault(CONNECTION, DSNull.NULL).setTransient(true);
        declareDefault(PROFILER, new SysProfiler()).setTransient(true);
        declareDefault(LOGGING, new SysLogService());
        declareDefault(BACKUPS, new SysBackupService());
    }

    public DSLinkConnection getConnection() {
        return (DSLinkConnection) connection.getObject();
    }

    public DSLink getLink() {
        return (DSLink) getParent();
    }

    @Override
    protected String getLogName() {
        return getLogName("sys");
    }

    void init() {
        DSLinkConfig config = getLink().getConfig();
        try {
            String ver = config.getDsaVersion();
            DSLinkConnection conn;
            String type = config.getConfig(DSLinkConfig.CFG_CONNECTION_TYPE, null);
            if (type != null) {
                conn = (DSLinkConnection) Class.forName(type).newInstance();
            } else if (ver.startsWith("1")) {
                conn = new DS1LinkConnection();
            } else { //2
                conn = new DS2LinkConnection();
            }
            debug(debug() ? "Connection type: " + conn.getClass().getName() : null);
            put(connection, conn);
        } catch (Exception x) {
            DSException.throwRuntime(x);
        }
    }

    @Override
    public ActionResult onInvoke(DSInfo action, ActionInvocation invocation) {
        if (action == stop) {
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
