package org.iot.dsa.dslink.requester;

/**
 * Return value from DSIRequester.subscribe().
 *
 * @author Daniel Shapiro, Aaron Hansen
 */
public interface OutboundSubscribeStub {

    /**
     * Allows the requester to close the subscription.
     */
    public void close();

}
