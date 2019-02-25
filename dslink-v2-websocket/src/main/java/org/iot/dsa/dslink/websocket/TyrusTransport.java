package org.iot.dsa.dslink.websocket;

import com.acuity.iot.dsa.dslink.sys.cert.SysCertService;
import com.acuity.iot.dsa.dslink.transport.DSTransportWs;
import java.net.URI;
import javax.websocket.ClientEndpoint;
import org.glassfish.tyrus.client.ClientManager;
import org.glassfish.tyrus.client.ClientProperties;
import org.glassfish.tyrus.client.SslContextConfigurator;
import org.glassfish.tyrus.client.SslEngineConfigurator;
import org.iot.dsa.util.DSException;

/**
 * Websocket client implementation of DSTextTransport based on Tyrus, the reference implementation
 * of JSR 356.
 *
 * @author Aaron Hansen
 */
@ClientEndpoint
public class TyrusTransport extends DSTransportWs {

    ///////////////////////////////////////////////////////////////////////////
    // Fields
    ///////////////////////////////////////////////////////////////////////////

    private ClientManager client;

    /////////////////////////////////////////////////////////////////
    // Methods - In alphabetical order by method name.
    /////////////////////////////////////////////////////////////////

    @Override
    public void open() {
        try {
            if (isOpen()) {
                return;
            }
            if (client == null) {
                client = ClientManager.createClient();
            }
            client.setDefaultMaxBinaryMessageBufferSize(64 * 1024);
            client.setDefaultMaxTextMessageBufferSize(64 * 1024);
            URI connUri = new URI(getConnectionUrl());
            if ("wss".equalsIgnoreCase(connUri.getScheme())) {
                SslEngineConfigurator sslEngineConfigurator = new SslEngineConfigurator(
                        new SslContextConfigurator());
                sslEngineConfigurator
                        .setHostnameVerifier(SysCertService.getInstance().getHostnameVerifier());
                client.getProperties()
                      .put(ClientProperties.SSL_ENGINE_CONFIGURATOR, sslEngineConfigurator);
            }
            client.connectToServer(this, connUri);
        } catch (Exception x) {
            DSException.throwRuntime(x);
        }
    }

}
