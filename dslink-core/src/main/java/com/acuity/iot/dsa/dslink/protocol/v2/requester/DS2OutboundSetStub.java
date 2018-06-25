package com.acuity.iot.dsa.dslink.protocol.v2.requester;

import com.acuity.iot.dsa.dslink.protocol.message.MessageWriter;
import com.acuity.iot.dsa.dslink.protocol.requester.DSOutboundSetStub;
import com.acuity.iot.dsa.dslink.protocol.requester.DSRequester;
import com.acuity.iot.dsa.dslink.protocol.v2.DS2MessageReader;
import com.acuity.iot.dsa.dslink.protocol.v2.DS2MessageWriter;
import com.acuity.iot.dsa.dslink.protocol.v2.MessageConstants;
import com.acuity.iot.dsa.dslink.protocol.v2.MultipartWriter;
import com.acuity.iot.dsa.dslink.transport.DSBinaryTransport;
import org.iot.dsa.dslink.requester.OutboundRequestHandler;
import org.iot.dsa.node.DSIValue;
import org.iot.dsa.node.DSPath;

public class DS2OutboundSetStub extends DSOutboundSetStub
        implements DS2OutboundStub, MessageConstants {

    private MultipartWriter multipart;

    protected DS2OutboundSetStub(DSRequester requester,
                                 Integer requestId,
                                 String path,
                                 DSIValue value,
                                 OutboundRequestHandler handler) {
        super(requester, requestId, path, value, handler);
    }

    @Override
    public void handleResponse(DS2MessageReader response) {
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
        out.setMethod(MSG_SET_REQ);
        String path = getPath();
        int idx = 1 + path.lastIndexOf('/');
        if ((idx > 0) && (path.length() > idx)) {
            if (path.charAt(idx) == '@') {
                String[] elems = DSPath.decodePath(path);
                idx = elems.length - 1;
                String attr = elems[idx];
                path = DSPath.encodePath(path.charAt(0) == '/', elems, idx);
                out.addStringHeader(HDR_ATTRIBUTE_FIELD, attr);
            }
        }
        out.addStringHeader(HDR_TARGET_PATH, path);
        out.getBody().put((byte) 0, (byte) 0);
        out.getWriter().value(getValue().toElement());
        if (out.requiresMultipart()) {
            multipart = out.makeMultipart();
            multipart.update(out, ack);
            getRequester().sendRequest(this);
        } else {
            out.write((DSBinaryTransport) getRequester().getTransport());
        }

    }
}