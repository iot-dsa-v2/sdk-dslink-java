package com.acuity.iot.dsa.dslink.sys.cert;

import java.util.Collection;
import org.iot.dsa.node.DSInfo;
import org.iot.dsa.node.DSString;
import org.iot.dsa.node.DSValueNode;
import org.iot.dsa.node.action.ActionInvocation;
import org.iot.dsa.node.action.ActionResult;
import org.iot.dsa.node.action.DSAction;
import org.iot.dsa.node.action.DeleteAction;
import org.iot.dsa.node.action.DuplicateAction;
import org.iot.dsa.node.action.RenameAction;

/**
 * @author Daniel Shapiro
 */
public class CertNode extends DSValueNode {

    private static final String VALUE = "value";
    private static final String ALLOW = "Allow";
    private static final String REMOVE = "Remove";
    private SysCertService certManager;
    private DSInfo value = getInfo(VALUE);

    public SysCertService getCertManager() {
        if (certManager == null) {
            certManager = (SysCertService) getAncestor(SysCertService.class);
        }
        return certManager;
    }

    @Override
    public DSInfo getValueChild() {
        return value;
    }

    public CertNode updateValue(String newVal) {
        put(VALUE, newVal);
        return this;
    }
    
    @Override
    public void getVirtualActions(DSInfo target, Collection<String> bucket) {
        super.getVirtualActions(target, bucket);
        if (target.getNode() == this) {
            bucket.remove(DeleteAction.DELETE);
            bucket.remove(RenameAction.RENAME);
            bucket.remove(DuplicateAction.DUPLICATE);
        }
    }

    @Override
    protected void declareDefaults() {
        super.declareDefaults();
        declareDefault(VALUE, DSString.valueOf("")).setPrivate(true).setReadOnly(true);
        declareDefault(ALLOW, new AllowAction());
        declareDefault(REMOVE, new RemoveAction());
    }

    private void allow() {
        getCertManager().allow(getInfo());
    }

    private void remove() {
        getParent().remove(getInfo());
    }

    private static class AllowAction extends DSAction.Parameterless {

        @Override
        public ActionResult invoke(DSInfo target, ActionInvocation invocation) {
            ((CertNode) target.get()).allow();
            return null;
        }

    }

    private static class RemoveAction extends DSAction.Parameterless {

        @Override
        public ActionResult invoke(DSInfo target, ActionInvocation invocation) {
            ((CertNode) target.get()).remove();
            return null;
        }

    }

}
