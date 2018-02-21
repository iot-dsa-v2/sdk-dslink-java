package com.acuity.iot.dsa.dslink.protocol.v2.responder;

import com.acuity.iot.dsa.dslink.protocol.v2.CloseMessage;
import com.acuity.iot.dsa.dslink.protocol.v2.MessageConstants;
import com.acuity.iot.dsa.dslink.protocol.responder.DSInboundSet;
import org.iot.dsa.node.DSElement;
import org.iot.dsa.security.DSPermission;

public class DS2InboundSet extends DSInboundSet implements MessageConstants {

    public DS2InboundSet(DSElement value, DSPermission permission) {
        super(value, permission);
    }

    @Override
    protected void sendClose() {
        getResponder().sendResponse(new CloseMessage((DS2Responder) getResponder(),
                                                     getRequestId(),
                                                     (byte) MSG_SET_RES));
    }

}
