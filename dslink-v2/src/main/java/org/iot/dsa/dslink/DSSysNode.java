package org.iot.dsa.dslink;

import com.acuity.iot.dsa.dslink.sys.backup.SysBackupService;
import com.acuity.iot.dsa.dslink.sys.cert.SysCertService;
import com.acuity.iot.dsa.dslink.sys.logging.SysLogService;
import com.acuity.iot.dsa.dslink.sys.profiler.SysProfiler;
import org.iot.dsa.node.DSInfo;
import org.iot.dsa.node.DSNode;
import org.iot.dsa.node.action.ActionInvocation;
import org.iot.dsa.node.action.ActionResult;
import org.iot.dsa.node.action.DSAction;

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
    static final String OPEN_PROFILER = "Open Profiler";
    static final String CLOSE_PROFILER = "Close Profiler";

    private DSInfo backups = getInfo(BACKUPS);
    private DSInfo connection = getInfo(CONNECTION);
    private DSInfo profiler = null;
    private DSInfo profilerToggle;

    public SysBackupService getBackupService() {
        return (SysBackupService) backups.get();
    }

    public DSLinkConnection getConnection() {
        return (DSLinkConnection) connection.get();
    }

    public DSLink getLink() {
        return (DSLink) getParent();
    }

    @Override
    protected void declareDefaults() {
        declareDefault(STOP, new StopAction());
        declareDefault(CERTIFICATES, new SysCertService());
        declareDefault(LOGGING, new SysLogService());
        declareDefault(BACKUPS, new SysBackupService());
    }

    @Override
    protected void onStable() {
        profiler = getInfo(PROFILER);
        if (profiler == null) {
            profilerToggle = put(OPEN_PROFILER, new ToggleAction()).setTransient(true);
        } else {
            profilerToggle = put(CLOSE_PROFILER, new ToggleAction()).setTransient(true);
        }
    }

    @Override
    protected void validateParent(DSNode node) {
        if (node instanceof DSLink) {
            return;
        }
        throw new IllegalArgumentException("Invalid parent: " + node.getClass().getName());
    }

    private void closeProfiler() {
        if (profiler != null) {
            remove(profiler);
            profiler = null;
        }
        if (profilerToggle != null) {
            remove(profilerToggle);
        }
        profilerToggle = put(OPEN_PROFILER, new ToggleAction()).setTransient(true);
    }

    private void openProfiler() {
        profiler = put(PROFILER, new SysProfiler());
        if (profilerToggle != null) {
            remove(profilerToggle);
        }
        profilerToggle = put(CLOSE_PROFILER, new ToggleAction()).setTransient(true);
    }

    private class StopAction extends DSAction.Parameterless {

        @Override
        public ActionResult invoke(DSInfo target, ActionInvocation invocation) {
            ((DSSysNode) target.get()).getLink().shutdown();
            return null;
        }

    }

    private class ToggleAction extends DSAction.Parameterless {

        @Override
        public ActionResult invoke(DSInfo target, ActionInvocation invocation) {
            if (profiler == null) {
                ((DSSysNode) target.get()).openProfiler();
            } else {
                ((DSSysNode) target.get()).closeProfiler();
            }
            return null;
        }

    }

}
