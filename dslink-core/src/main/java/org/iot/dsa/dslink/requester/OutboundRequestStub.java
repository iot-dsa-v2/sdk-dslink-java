package org.iot.dsa.dslink.requester;

/**
 * Mechanism to close outbound streaming requests.
 *
 * @author Daniel Shapiro, Aaron Hansen
 */
public interface OutboundRequestStub {

    /**
     * Allows the requester to close the stream.
     */
    public void closeRequest();

    /**
     * ID assigned to the request by the requester.
     */
    public Integer getRequestId();

    /**
     * Whether or not the request is open.
     */
    public boolean isRequestOpen();

}
