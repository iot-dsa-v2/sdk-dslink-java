package org.iot.dsa.dslink.requester;

import org.iot.dsa.node.DSMap;

/**
 * Parameter to the subscribe method on DSIRequester.  Provides details about the subscription as
 * well as callbacks for various state changes.
 *
 * @author Daniel Shapiro, Aaron Hansen
 */
public abstract class OutboundSubscribeRequest extends OutboundRequest {

    ///////////////////////////////////////////////////////////////////////////
    // Fields
    ///////////////////////////////////////////////////////////////////////////

    private int qos;

    ///////////////////////////////////////////////////////////////////////////
    // Methods in alphabetical order
    ///////////////////////////////////////////////////////////////////////////

    /**
     * The quality of service.
     */
    public int getQos() {
        return qos;
    }

    /**
     * Callback - single entry point for all responses to the request.
     */
    public abstract void onUpdate(DSMap response);

    public OutboundSubscribeRequest setQos(int qos) {
        this.qos = qos;
        return this;
    }

}
