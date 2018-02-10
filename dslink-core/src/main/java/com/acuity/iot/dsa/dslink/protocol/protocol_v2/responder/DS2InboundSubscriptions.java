package com.acuity.iot.dsa.dslink.protocol.protocol_v2.responder;

import com.acuity.iot.dsa.dslink.protocol.message.MessageWriter;
import com.acuity.iot.dsa.dslink.protocol.responder.DSInboundSubscription;
import com.acuity.iot.dsa.dslink.protocol.responder.DSInboundSubscriptions;
import com.acuity.iot.dsa.dslink.protocol.responder.DSResponder;

/**
 * Subscribe implementation for the responder.
 *
 * @author Aaron Hansen
 */
public class DS2InboundSubscriptions extends DSInboundSubscriptions {

    ///////////////////////////////////////////////////////////////////////////
    // Constants
    ///////////////////////////////////////////////////////////////////////////

    ///////////////////////////////////////////////////////////////////////////
    // Fields
    ///////////////////////////////////////////////////////////////////////////

    ///////////////////////////////////////////////////////////////////////////
    // Constructors
    ///////////////////////////////////////////////////////////////////////////

    public DS2InboundSubscriptions(DSResponder responder) {
        super(responder);
    }

    ///////////////////////////////////////////////////////////////////////////
    // Methods in alphabetical order
    ///////////////////////////////////////////////////////////////////////////

    @Override
    protected DSInboundSubscription makeSubscription(Integer sid, String path, int qos) {
        return new DS2InboundSubscription(this, sid, path, qos);
    }

    @Override
    protected void writeBegin(MessageWriter writer) {
    }

    @Override
    protected void writeEnd(MessageWriter writer) {
    }

}
