package org.iot.dsa.dslink;

import com.acuity.iot.dsa.dslink.DSTransport;
import com.acuity.iot.dsa.dslink.protocol.protocol_v1.DS1ConnectionInit;
import com.acuity.iot.dsa.dslink.protocol.protocol_v1.DS1LinkConnection;
import com.acuity.iot.dsa.dslink.protocol.protocol_v1.DS1Session;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.logging.Level;
import org.iot.dsa.io.DSByteBuffer;
import org.iot.dsa.io.DSIReader;
import org.iot.dsa.io.DSIWriter;
import org.iot.dsa.io.DSIoException;
import org.iot.dsa.io.json.JsonReader;
import org.iot.dsa.io.json.JsonWriter;
import org.iot.dsa.util.DSException;

/**
 * Routes requests and responses back to self.
 *
 * @author Aaron Hansen
 */
public class TestConnection extends DS1LinkConnection {

    ///////////////////////////////////////////////////////////////////////////
    // Constants
    ///////////////////////////////////////////////////////////////////////////

    ///////////////////////////////////////////////////////////////////////////
    // Fields
    ///////////////////////////////////////////////////////////////////////////

    /////////////////////////////////////////////////////////////////
    // Constructors
    /////////////////////////////////////////////////////////////////

    /////////////////////////////////////////////////////////////////
    // Methods
    /////////////////////////////////////////////////////////////////

    protected DS1ConnectionInit initializeConnection() throws Exception {
        DS1ConnectionInit init = new DS1ConnectionInit();
        return init;
    }

    /**
     * Looks at the connection initialization response to determine the protocol implementation.
     */
    protected DS1Session makeSession(DS1ConnectionInit init) {
        DS1Session ret = new DS1Session();
        ret.setRequesterAllowed();
        return ret;
    }

    /**
     * Looks at the connection initialization response to determine the type of transport then
     * instantiates the correct type fom the config.
     */
    protected DSTransport makeTransport(DS1ConnectionInit init) {
        TestTransport transport = new TestTransport();
        transport.setConnection(this);
        transport.setReadTimeout(getLink().getConfig().getConfig(
                DSLinkConfig.CFG_READ_TIMEOUT, 60000));
        return transport;
    }

    /////////////////////////////////////////////////////////////////
    // Inner Classes
    /////////////////////////////////////////////////////////////////

    /////////////////////////////////////////////////////////////////
    // Initialization
    /////////////////////////////////////////////////////////////////

}
