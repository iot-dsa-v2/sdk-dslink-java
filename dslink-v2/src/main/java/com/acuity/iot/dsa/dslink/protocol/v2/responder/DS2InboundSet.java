package com.acuity.iot.dsa.dslink.protocol.v2.responder;

import com.acuity.iot.dsa.dslink.protocol.responder.DSInboundSet;
import com.acuity.iot.dsa.dslink.protocol.v2.CloseMessage;
import com.acuity.iot.dsa.dslink.protocol.v2.MessageConstants;
import org.iot.dsa.node.DSElement;
import org.iot.dsa.security.DSPermission;

public class DS2InboundSet extends DSInboundSet implements MessageConstants {

    public DS2InboundSet(DSElement value, DSPermission permission) {
        super(value, permission);
    }

    @Override
    protected void sendClose() {
        getResponder().sendResponse(
                new CloseMessage(getSession(), getRequestId(), (byte) MSG_SET_RES));
    }

}
