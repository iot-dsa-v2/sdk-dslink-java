package com.acuity.iot.dsa.dslink.protocol.protocol_v1.responder;

import com.acuity.iot.dsa.dslink.protocol.message.CloseMessage;
import com.acuity.iot.dsa.dslink.protocol.message.ErrorResponse;
import org.iot.dsa.dslink.responder.InboundSetRequest;
import org.iot.dsa.node.DSElement;
import org.iot.dsa.node.DSMap;
import org.iot.dsa.security.DSPermission;

class DS1InboundSet extends DS1InboundRequest implements InboundSetRequest, Runnable {

    private DSElement value;
    private DSPermission permission;

    DS1InboundSet(DSMap request) {
        setRequest(request);
        String permit = request.get("permit", "config");
        permission = DSPermission.forString(permit);
        value = request.get("value");
    }

    @Override
    public DSPermission getPermission() {
        return permission;
    }

    @Override
    public DSElement getValue() {
        return value;
    }

    @Override
    public void run() {
        try {
            getResponder().onSet(this);
            getSession().sendResponse(
                    new CloseMessage(getRequestId()).setMethod(null).setStream("closed"));
        } catch (Exception x) {
            severe(getPath(), x);
            ErrorResponse err = new ErrorResponse(x);
            err.parseRequest(getRequest());
            getSession().sendResponse(err);
        }
    }

}
