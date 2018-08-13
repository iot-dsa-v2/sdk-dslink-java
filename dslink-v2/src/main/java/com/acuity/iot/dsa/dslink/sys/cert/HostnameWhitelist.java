package com.acuity.iot.dsa.dslink.sys.cert;

import org.iot.dsa.node.DSBool;
import org.iot.dsa.node.DSIValue;
import org.iot.dsa.node.DSInfo;
import org.iot.dsa.node.DSJavaEnum;
import org.iot.dsa.node.DSMap;
import org.iot.dsa.node.DSNode;
import org.iot.dsa.node.DSValueType;
import org.iot.dsa.node.action.ActionInvocation;
import org.iot.dsa.node.action.ActionResult;
import org.iot.dsa.node.action.DSAbstractAction;

public class HostnameWhitelist extends DSNode {
    
    public static enum WhitelistValue {
        ALLOWED, FORBIDDEN;
    }
    
    public static enum WhitelistOption {
        ALLOWED, FORBIDDEN, REMOVE;
    }
    
    private static final String ENABLED = "Enabled";
    private static final String ADD_HOSTNAME = "Add Hostname";
    
    private DSInfo enabled = getInfo(ENABLED);
    
    @Override
    protected void declareDefaults() {
        super.declareDefaults();
        declareDefault(ENABLED, DSBool.FALSE);
        declareDefault(ADD_HOSTNAME, getAddHostnameAction());
    }
    
    public boolean isEnabled() {
        return enabled.getElement().toBoolean();
    }

    private DSAbstractAction getAddHostnameAction() {
        DSAbstractAction act = new DSAbstractAction() {
            
            @Override
            public void prepareParameter(DSInfo info, DSMap parameter) {
            }
            
            @Override
            public ActionResult invoke(DSInfo info, ActionInvocation invocation) {
                ((HostnameWhitelist) info.getParent()).addHostname(invocation.getParameters());
                return null;
            }
        };
        act.addParameter("Hostname", DSValueType.STRING, null);
        act.addParameter("Status", DSJavaEnum.valueOf(WhitelistValue.ALLOWED), "Whether this hostname should be whitelisted or blacklisted");
        return act;
    }
    
    private void addHostname(DSMap parameters) {
        String hostname = parameters.getString("Hostname");
        String statusStr = parameters.getString("Status");
        WhitelistOption option = WhitelistOption.valueOf(statusStr);
        put(hostname, DSJavaEnum.valueOf(option));
    }
    
    public WhitelistValue checkHostname(String hostname) {
        DSIValue value = getValue(hostname);
        String str = null;
        if (value != null) {
            str = value.toElement().toString();
        }
        try {
            return WhitelistValue.valueOf(str);
        } catch (Exception e) {
            return null;
        }
    }
    
    protected void onChildChanged(DSInfo info) {
        if (info.isValue()) {
            String val = info.getValue().toElement().toString();
            if (WhitelistOption.REMOVE.name().equals(val)) {
                remove(info);
            }
        }
    }
    

}
