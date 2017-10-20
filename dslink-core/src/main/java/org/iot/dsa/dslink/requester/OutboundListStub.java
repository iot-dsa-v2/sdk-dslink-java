package org.iot.dsa.dslink.requester;

/**
 * Return value from DSIRequester.list().
 *
 * @author Daniel Shapiro, Aaron Hansen
 */
public interface OutboundListStub {

    /**
     * Allows the requester to close the stream.
     */
    public void close();

}
