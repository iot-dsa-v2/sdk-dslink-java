package com.acuity.iot.dsa.dslink.protocol.v2.requester;

import com.acuity.iot.dsa.dslink.io.msgpack.MsgpackReader;
import com.acuity.iot.dsa.dslink.protocol.DSSession;
import com.acuity.iot.dsa.dslink.protocol.message.MessageWriter;
import com.acuity.iot.dsa.dslink.protocol.requester.DSOutboundListStub;
import com.acuity.iot.dsa.dslink.protocol.requester.DSRequester;
import com.acuity.iot.dsa.dslink.protocol.v2.DS2MessageReader;
import com.acuity.iot.dsa.dslink.protocol.v2.DS2MessageWriter;
import com.acuity.iot.dsa.dslink.protocol.v2.MessageConstants;
import com.acuity.iot.dsa.dslink.protocol.v2.MultipartWriter;
import java.io.IOException;
import java.io.InputStream;
import org.iot.dsa.dslink.requester.OutboundListHandler;
import org.iot.dsa.node.DSBytes;
import org.iot.dsa.node.DSElement;
import org.iot.dsa.node.DSPath;
import org.iot.dsa.util.DSException;

public class DS2OutboundListStub extends DSOutboundListStub
        implements DS2OutboundStub, MessageConstants {

    private MultipartWriter multipart;
    private byte state = STS_INITIALIZING;

    DS2OutboundListStub(DSRequester requester,
                        Integer requestId,
                        String path,
                        OutboundListHandler handler) {
        super(requester, requestId, path, handler);
    }

    @Override
    public void handleResponse(DS2MessageReader response) {
        OutboundListHandler handler = getHandler();
        try {
            MsgpackReader reader = response.getBodyReader();
            InputStream in = response.getBody();
            int bodyLen = response.getBodyLength();
            String name;
            DSElement value = null;
            while (bodyLen > 0) {
                int len = DSBytes.readShort(in, false);
                bodyLen -= len;
                name = reader.readUTF(len);
                len = DSBytes.readShort(in, false);
                bodyLen -= len;
                bodyLen -= 4; //the two lengths
                if (len == 0) {
                    handler.onRemove(name);
                } else {
                    reader.reset();
                    value = reader.getElement();
                    handler.onUpdate(DSPath.decodeName(name), value);
                }
            }
        } catch (IOException x) {
            DSException.throwRuntime(x);
        }
        if (state == STS_INITIALIZING) {
            Byte status = (Byte) response.getHeader(MessageConstants.HDR_STATUS);
            if (status.byteValue() == STS_OK) {
                state = STS_OK;
                getHandler().onInitialized();
            }
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
        out.setMethod(MSG_LIST_REQ);
        out.addStringHeader(HDR_TARGET_PATH, getPath());
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
