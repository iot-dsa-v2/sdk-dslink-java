package org.iot.dsa.dslink.requester;

/**
 * Mechanism for the requester to close outbound requests.
 *
 * @author Daniel Shapiro, Aaron Hansen
 */
public interface OutboundStream {

    /**
     * Allows the requester to close the stream.
     */
    void closeStream();

    /**
     * Whether or not the request is open.
     */
    boolean isStreamOpen();

}
