package com.acuity.iot.dsa.dslink.protocol;

/**
 * Can be closed locally and remotely.
 *
 * @author Aaron Hansen
 */
public interface DSStream {

    /**
     * Use to close locally.
     */
    void close();

    /**
     * Called when closed by external forces, such as by the requester or the connection being
     * closed.
     */
    void onClose(Integer requestId);

}
