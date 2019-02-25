package org.iot.dsa.dslink.websocket;

import com.acuity.iot.dsa.dslink.transport.DSTransport.Factory;
import org.iot.dsa.dslink.DSLinkConnection;

/**
 * Websocket client implementation of DSTransport.Factory based on Tyrus, the reference
 * implementation of JSR 356.
 *
 * @author Aaron Hansen
 */
public class StandaloneTransportFactory implements Factory {

    @Override
    public ClientTransport makeTransport(DSLinkConnection conn) {
        return new ClientTransport();
    }

}
