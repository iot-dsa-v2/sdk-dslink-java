package org.iot.dsa.dslink.requester;

/**
 * Return value from DSIRequester.invoke().
 *
 * @author Daniel Shapiro, Aaron Hansen
 */
public interface OutboundInvokeStub {

    /**
     * Allows the requester to close the stream.
     */
    public void close();

}
