package com.acuity.iot.dsa.dslink.protocol.v2.requester;

import com.acuity.iot.dsa.dslink.protocol.DSSession;
import com.acuity.iot.dsa.dslink.protocol.message.MessageWriter;
import com.acuity.iot.dsa.dslink.protocol.requester.DSOutboundInvokeStub;
import com.acuity.iot.dsa.dslink.protocol.requester.DSRequester;
import com.acuity.iot.dsa.dslink.protocol.v2.DS2MessageReader;
import com.acuity.iot.dsa.dslink.protocol.v2.DS2MessageWriter;
import com.acuity.iot.dsa.dslink.protocol.v2.MessageConstants;
import com.acuity.iot.dsa.dslink.protocol.v2.MultipartWriter;
import org.iot.dsa.dslink.requester.OutboundInvokeHandler;
import org.iot.dsa.node.DSMap;

public class DS2OutboundInvokeStub extends DSOutboundInvokeStub
        implements DS2OutboundStub, MessageConstants {

    MultipartWriter multipart;

    DS2OutboundInvokeStub(DSRequester requester,
                          Integer requestId,
                          String path,
                          DSMap params,
                          OutboundInvokeHandler handler) {
        super(requester, requestId, path, params, handler);
    }

    @Override
    public void handleResponse(DS2MessageReader response) {
        if (response.getBodyLength() > 0) {
            handleResponse(response.getBodyReader().getMap());
        }
    }

    @Override
    public boolean write(DSSession session, MessageWriter writer) {
        DS2MessageWriter out = (DS2MessageWriter) writer;
        if (multipart != null) {
            if (multipart.update(out, getSession().getAckToSend())) {
                getRequester().sendRequest(this);
            }
            return true;
        }
        int ack = getSession().getAckToSend();
        out.init(getRequestId(), ack);
        out.setMethod(MSG_INVOKE_REQ);
        out.addStringHeader(HDR_TARGET_PATH, getPath());
        DSMap params = getParams();
        if (params != null) {
            out.getWriter().value(params);
        }
        if (out.requiresMultipart()) {
            multipart = out.makeMultipart();
            multipart.update(out, ack);
            getRequester().sendRequest(this);
        } else {
            out.write(getRequester().getTransport());
        }
        return true;
    }

}
