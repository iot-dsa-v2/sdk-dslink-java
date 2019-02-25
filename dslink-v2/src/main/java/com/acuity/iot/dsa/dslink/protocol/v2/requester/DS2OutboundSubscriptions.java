package com.acuity.iot.dsa.dslink.protocol.v2.requester;

import com.acuity.iot.dsa.dslink.protocol.message.MessageWriter;
import com.acuity.iot.dsa.dslink.protocol.requester.DSOutboundSubscriptions;
import com.acuity.iot.dsa.dslink.protocol.requester.DSRequester;
import com.acuity.iot.dsa.dslink.protocol.v2.DS2MessageWriter;
import com.acuity.iot.dsa.dslink.protocol.v2.DS2Session;
import com.acuity.iot.dsa.dslink.protocol.v2.MessageConstants;

/**
 * Manages all subscriptions for a requester.
 *
 * @author Aaron Hansen
 */
public class DS2OutboundSubscriptions extends DSOutboundSubscriptions implements MessageConstants {

    DS2OutboundSubscriptions(DSRequester requester) {
        super(requester);
    }

    @Override
    protected void doBeginSubscribe(MessageWriter writer) {
    }

    @Override
    protected void doBeginUnsubscribe(MessageWriter writer) {
    }

    @Override
    protected void doEndMessage(MessageWriter writer) {
    }

    @Override
    protected void doWriteSubscribe(MessageWriter writer, String path, Integer sid, int qos) {
        DS2MessageWriter ds2 = (DS2MessageWriter) writer;
        DS2Session session = (DS2Session) getRequester().getSession();
        ds2.init(sid, session.getAckToSend());
        ds2.setMethod(MSG_SUBSCRIBE_REQ);
        ds2.addStringHeader(HDR_TARGET_PATH, path);
        ds2.addByteHeader(HDR_QOS, (byte) qos);
        ds2.write(getRequester().getTransport());
    }

    /**
     * Override point for v2.
     */
    protected void doWriteUnsubscribe(MessageWriter writer, Integer sid) {
        requester().sendClose(sid);
    }

    private DS2Requester requester() {
        return (DS2Requester) getRequester();
    }

}
