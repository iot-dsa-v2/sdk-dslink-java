package org.iot.dsa.dslink.responder;

import org.iot.dsa.dslink.DSIResponder;
import org.iot.dsa.node.DSIValue;
import org.iot.dsa.node.DSStatus;
import org.iot.dsa.time.DSDateTime;

/**
 * The details about an incoming subscribe request passed to the responder.
 *
 * @author Aaron Hansen
 * @see DSIResponder#onSubscribe(InboundSubscribeRequest)
 */
public interface InboundSubscribeRequest extends InboundRequest {

    /**
     * Allows the responder to forcefully terminate the subscription.
     */
    public void close();

    /**
     * Unique subscription id for this path.
     */
    public Integer getSubscriptionId();

    /**
     * The responder should call this when first received and then whenever the value or status
     * changes.
     */
    public void update(DSDateTime timestamp, DSIValue value, DSStatus quality);

}
