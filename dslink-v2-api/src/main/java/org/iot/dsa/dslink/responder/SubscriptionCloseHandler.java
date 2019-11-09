package org.iot.dsa.dslink.responder;

/**
 * The responder returns this from the subscribe method so that it can be notified when
 * a subscription is closed.
 *
 * @author Aaron Hansen
 * @see org.iot.dsa.dslink.DSIResponder#onSubscribe(InboundSubscribeRequest)
 */
public interface SubscriptionCloseHandler {

    /**
     * Will be called no matter how the subscription is terminated.
     */
    void onClose(Integer subscriptionId);

}
