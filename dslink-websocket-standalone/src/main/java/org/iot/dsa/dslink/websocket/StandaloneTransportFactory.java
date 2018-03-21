package org.iot.dsa.dslink.websocket;

import com.acuity.iot.dsa.dslink.transport.DSBinaryTransport;
import com.acuity.iot.dsa.dslink.transport.DSTextTransport;
import com.acuity.iot.dsa.dslink.transport.DSTransport.Factory;
import org.iot.dsa.dslink.DSLinkConnection;
import org.iot.dsa.logging.DSLogger;

/**
 * Websocket client implementation of DSTransport.Factory based on Tyrus, the reference
 * implementation of JSR 356.
 *
 * @author Aaron Hansen
 */
public class StandaloneTransportFactory extends DSLogger implements Factory {

    @Override
    public DSBinaryTransport makeBinaryTransport(DSLinkConnection conn) {
        return new WsBinaryTransport();
    }

    @Override
    public DSTextTransport makeTextTransport(DSLinkConnection conn) {
        return new WsTextTransport();
    }

}
