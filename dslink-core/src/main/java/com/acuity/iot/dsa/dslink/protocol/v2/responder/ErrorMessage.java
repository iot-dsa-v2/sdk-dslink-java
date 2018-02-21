package com.acuity.iot.dsa.dslink.protocol.v2.responder;

import com.acuity.iot.dsa.dslink.protocol.message.MessageWriter;
import com.acuity.iot.dsa.dslink.protocol.message.OutboundMessage;
import com.acuity.iot.dsa.dslink.protocol.v2.DS2MessageWriter;
import com.acuity.iot.dsa.dslink.protocol.v2.MessageConstants;
import com.acuity.iot.dsa.dslink.protocol.responder.DSInboundRequest;
import com.acuity.iot.dsa.dslink.transport.DSBinaryTransport;
import org.iot.dsa.dslink.DSInvalidPathException;
import org.iot.dsa.dslink.DSPermissionException;
import org.iot.dsa.dslink.DSRequestException;
import org.iot.dsa.util.DSException;

/**
 * Responder uses to close streams without errors.
 *
 * @author Aaron Hansen
 */
class ErrorMessage implements MessageConstants, OutboundMessage {

    private DSInboundRequest req;
    private Throwable reason;

    public ErrorMessage(DSInboundRequest req, Throwable reason) {
        this.req = req;
        this.reason = reason;
    }

    @Override
    public void write(MessageWriter writer) {
        DS2MessageWriter out = (DS2MessageWriter) writer;
        out.init(req.getRequestId(), req.getSession().getNextAck());
        if (req instanceof DS2InboundInvoke) {
            out.setMethod((byte) MSG_INVOKE_RES);
        } else if (req instanceof DS2InboundList) {
            out.setMethod((byte) MSG_LIST_RES);
        } else if (req instanceof DS2InboundSet) {
            out.setMethod((byte) MSG_SET_RES);
        } else {
            out.setMethod((byte) MSG_CLOSE);
        }
        if (reason instanceof DSRequestException) {
            if (reason instanceof DSInvalidPathException) {
                out.addHeader((byte) MessageConstants.HDR_STATUS,
                              MessageConstants.STS_NOT_AVAILABLE);
            } else if (reason instanceof DSPermissionException) {
                out.addHeader((byte) MessageConstants.HDR_STATUS,
                              MessageConstants.STS_PERMISSION_DENIED);
            } else {
                out.addHeader((byte) MessageConstants.HDR_STATUS,
                              MessageConstants.STS_INVALID_MESSAGE);
            }
        } else {
            out.addHeader((byte) MessageConstants.HDR_STATUS, MessageConstants.STS_INTERNAL_ERR);
        }
        out.addHeader((byte) HDR_ERROR_DETAIL, DSException.makeMessage(reason));
        out.write((DSBinaryTransport) req.getResponder().getTransport());
    }

}
