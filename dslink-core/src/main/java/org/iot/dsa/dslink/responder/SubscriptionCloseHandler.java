package org.iot.dsa.dslink.responder;

/**
 * The responder returns this from the subscription request notification method so the
 * link can notify the responder whenever a subscription is terminated.
 *
 * @author Aaron Hansen
 */
public interface SubscriptionCloseHandler {

    /**
     * Will be called no matter how the subscription is terminated.
     */
    public void onClose(Integer subscriptionId);

}
