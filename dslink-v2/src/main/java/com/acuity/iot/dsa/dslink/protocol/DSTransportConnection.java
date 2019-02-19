package com.acuity.iot.dsa.dslink.protocol;

import com.acuity.iot.dsa.dslink.transport.DSTransport;
import org.iot.dsa.dslink.DSLinkConnection;

/**
 * The sole purpose of this class is to keep the transport out of the public api.
 *
 * @author Aaron Hansen
 */
public abstract class DSTransportConnection extends DSLinkConnection {

    public abstract DSTransport getTransport();

    @Override
    protected void doDisconnect() {
        try {
            DSTransport t = getTransport();
            if ((t != null) && t.isOpen()) {
                t.close();
            }
        } catch (Exception x) {
            error(getPath(), x);
        }
    }

}
