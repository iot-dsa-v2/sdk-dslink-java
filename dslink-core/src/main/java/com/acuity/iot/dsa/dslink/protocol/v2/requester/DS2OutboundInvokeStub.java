package com.acuity.iot.dsa.dslink.protocol.v2.requester;

import com.acuity.iot.dsa.dslink.protocol.message.MessageWriter;
import com.acuity.iot.dsa.dslink.protocol.requester.DSOutboundInvokeStub;
import com.acuity.iot.dsa.dslink.protocol.requester.DSRequester;
import com.acuity.iot.dsa.dslink.protocol.v2.DS2MessageReader;
import com.acuity.iot.dsa.dslink.protocol.v2.DS2MessageWriter;
import com.acuity.iot.dsa.dslink.protocol.v2.MessageConstants;
import com.acuity.iot.dsa.dslink.protocol.v2.MultipartWriter;
import com.acuity.iot.dsa.dslink.transport.DSBinaryTransport;
import org.iot.dsa.dslink.requester.OutboundInvokeHandler;
import org.iot.dsa.node.DSMap;

public class DS2OutboundInvokeStub extends DSOutboundInvokeStub
        implements DS2OutboundStub, MessageConstants {

    MultipartWriter multipart;

    protected DS2OutboundInvokeStub(DSRequester requester,
                                    Integer requestId,
                                    String path,
                                    DSMap params,
                                    OutboundInvokeHandler handler) {
        super(requester, requestId, path, params, handler);
    }

    public void handleResponse(DS2MessageReader response) {
        if (response.getBodyLength() > 0) {
            handleResponse(response.getBodyReader().getMap());
        }
    }

    @Override
    public void write(MessageWriter writer) {
        DS2MessageWriter out = (DS2MessageWriter) writer;
        if (multipart != null) {
            if (multipart.update(out, getSession().getNextAck())) {
                getRequester().sendRequest(this);
            }
            return;
        }
        int ack = getSession().getNextAck();
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
            out.write((DSBinaryTransport) getRequester().getTransport());
        }
    }

}
