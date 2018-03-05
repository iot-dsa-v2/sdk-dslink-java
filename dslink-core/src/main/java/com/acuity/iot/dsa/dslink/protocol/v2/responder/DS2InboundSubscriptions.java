package com.acuity.iot.dsa.dslink.protocol.v2.responder;

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
        DSInboundSubscription ret = new DS2InboundSubscription(this, sid, path, qos);
        ret.setResponder(getResponder());
        ret.setSession(getResponder().getSession());
        return ret;
    }

    @Override
    protected void writeBegin(MessageWriter writer) {
    }

    @Override
    protected void writeEnd(MessageWriter writer) {
    }

}
