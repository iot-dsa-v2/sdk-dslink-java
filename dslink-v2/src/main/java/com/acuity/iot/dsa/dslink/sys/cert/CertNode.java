package com.acuity.iot.dsa.dslink.sys.cert;

import org.iot.dsa.node.DSInfo;
import org.iot.dsa.node.DSString;
import org.iot.dsa.node.DSValueNode;
import org.iot.dsa.node.action.ActionInvocation;
import org.iot.dsa.node.action.ActionResult;
import org.iot.dsa.node.action.DSAction;

/**
 * @author Daniel Shapiro
 */
public class CertNode extends DSValueNode {
    
    private static final String VALUE = "value";
    private static final String ALLOW = "Allow";
    private static final String REMOVE = "Remove";
    
    private DSInfo value = getInfo(VALUE);
    private DSInfo allow = getInfo(ALLOW);
    private DSInfo remove = getInfo(REMOVE);
    
    private SysCertService certManager;
    
    @Override
    protected void declareDefaults() {
        super.declareDefaults();
        declareDefault(VALUE, DSString.valueOf("")).setHidden(true).setReadOnly(true);
        declareDefault(ALLOW, DSAction.DEFAULT);
        declareDefault(REMOVE, DSAction.DEFAULT);
    }
    
    public CertNode updateValue(String newVal) {
        put(VALUE, newVal);
        return this;
    }

    @Override
    public DSInfo getValueChild() {
        return value;
    }
    
    @Override
    public ActionResult onInvoke(DSInfo action, ActionInvocation invocation) {
        if (action == remove) {
            remove();
        } else if (action == allow) {
            allow();
        } else {
            super.onInvoke(action, invocation);
        }
        return null;
    }
    
    private void remove() {
        getParent().remove(getInfo());
    }
    
    private void allow() {
        getCertManager().allow(getInfo());
    }
    
    public SysCertService getCertManager() {
        if (certManager == null) {
            certManager = (SysCertService) getAncestor(SysCertService.class);
        }
        return certManager;
    }

}
