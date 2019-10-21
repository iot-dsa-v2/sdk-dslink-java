package com.acuity.iot.dsa.dslink.sys.cert;

import org.iot.dsa.dslink.ActionResults;
import org.iot.dsa.node.DSBool;
import org.iot.dsa.node.DSIValue;
import org.iot.dsa.node.DSInfo;
import org.iot.dsa.node.DSJavaEnum;
import org.iot.dsa.node.DSMap;
import org.iot.dsa.node.DSNode;
import org.iot.dsa.node.DSString;
import org.iot.dsa.node.action.DSAction;
import org.iot.dsa.node.action.DSIActionRequest;

/**
 * @author Daniel Shapiro
 */
public class HostnameWhitelist extends DSNode {

    private static final String ENABLED = "Enabled";
    private static final String ADD_HOSTNAME = "Add Hostname";
    private DSInfo<?> enabled = getInfo(ENABLED);

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

    public boolean isEnabled() {
        return enabled.getElement().toBoolean();
    }

    @Override
    protected void declareDefaults() {
        super.declareDefaults();
        declareDefault(ENABLED, DSBool.FALSE);
        declareDefault(ADD_HOSTNAME, getAddHostnameAction());
    }

    @Override
    protected void onChildChanged(DSInfo<?> info) {
        if (info.isValue()) {
            String val = info.getValue().toElement().toString();
            if (WhitelistOption.REMOVE.name().equals(val)) {
                remove(info);
            }
        }
    }

    private void addHostname(DSMap parameters) {
        String hostname = parameters.getString("Hostname");
        String statusStr = parameters.getString("Status");
        WhitelistOption option = WhitelistOption.valueOf(statusStr);
        put(hostname, DSJavaEnum.valueOf(option));
    }

    private DSAction getAddHostnameAction() {
        DSAction act = new DSAction() {

            @Override
            public ActionResults invoke(DSIActionRequest request) {
                ((HostnameWhitelist) request.getTarget()).addHostname(request.getParameters());
                return null;
            }

        };
        act.addParameter("Hostname", DSString.NULL, null);
        act.addParameter("Status", DSJavaEnum.valueOf(WhitelistValue.ALLOWED),
                         "Whether this hostname should be whitelisted or blacklisted");
        return act;
    }

    public enum WhitelistOption {
        ALLOWED, FORBIDDEN, REMOVE;
    }

    public enum WhitelistValue {
        ALLOWED, FORBIDDEN;
    }


}
