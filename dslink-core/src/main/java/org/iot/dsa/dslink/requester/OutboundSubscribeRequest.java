package org.iot.dsa.dslink.requester;

import org.iot.dsa.node.DSElement;
import org.iot.dsa.node.DSStatus;
import org.iot.dsa.time.DSDateTime;

/**
 * Parameter to the subscribe method on DSIRequester.  Provides details about the subscription as
 * well as callbacks for various state changes.
 *
 * @author Daniel Shapiro, Aaron Hansen
 */
public abstract class OutboundSubscribeRequest {

    ///////////////////////////////////////////////////////////////////////////
    // Fields
    ///////////////////////////////////////////////////////////////////////////

    private String path;
    private int qos;

    ///////////////////////////////////////////////////////////////////////////
    // Methods in alphabetical order
    ///////////////////////////////////////////////////////////////////////////


    public String getPath() {
        return path;
    }

    /**
     * The quality of service.
     */
    public int getQos() {
        return qos;
    }

    /**
     * Subscription callback.
     */
    public abstract void update(DSDateTime dateTime, DSElement value, DSStatus status);

    public OutboundSubscribeRequest setPath(String path) {
        this.path = path;
        return this;
    }

    public OutboundSubscribeRequest setQos(int qos) {
        this.qos = qos;
        return this;
    }

}
