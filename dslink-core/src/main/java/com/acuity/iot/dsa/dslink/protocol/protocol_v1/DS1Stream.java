package com.acuity.iot.dsa.dslink.protocol.protocol_v1;

/**
 * Can be closed locally and remotely.
 *
 * @author Aaron Hansen
 */
public interface DS1Stream {

    /**
     * Use to close locally.
     */
    public void close();

    /**
     * Called when closed by external forces, such as by the requester or the connection being
     * closed.
     */
    public void onClose(Integer requestId);

}
