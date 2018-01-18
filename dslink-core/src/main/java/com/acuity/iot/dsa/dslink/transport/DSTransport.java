package com.acuity.iot.dsa.dslink.transport;

import java.util.logging.Logger;
import org.iot.dsa.dslink.DSLinkConnection;
import org.iot.dsa.node.DSLong;
import org.iot.dsa.node.DSNode;
import org.iot.dsa.node.DSString;

/**
 * Binds an DSLinkConnection to a specific transport implementation.  Examples of transports would
 * be sockets, websockets and http.
 *
 * @author Aaron Hansen
 */
public abstract class DSTransport extends DSNode {

    ///////////////////////////////////////////////////////////////////////////
    // Constants
    ///////////////////////////////////////////////////////////////////////////

    static String CONNECTION_URL = "Connection URL";
    static String READ_TIMEOUT = "Read Timeout";

    ///////////////////////////////////////////////////////////////////////////
    // Fields
    ///////////////////////////////////////////////////////////////////////////

    private DSLinkConnection connection;
    private Logger logger;

    ///////////////////////////////////////////////////////////////////////////
    // Methods
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Called at the start of a new outbound message.
     *
     * @return This
     */
    public abstract DSTransport beginMessage();

    /**
     * Close the actual connection and clean up resources.  Calling when already closed will have no
     * effect.
     *
     * @return This
     */
    public abstract DSTransport close();

    @Override
    protected void declareDefaults() {
        declareDefault(CONNECTION_URL, DSString.NULL).setReadOnly(true);
        declareDefault(READ_TIMEOUT, DSLong.valueOf(60000)).setReadOnly(true);
    }

    /**
     * Signifies the end of an outgoing message. Needed because websockets are frame based.
     *
     * @return This
     */
    public abstract DSTransport endMessage();

    public DSLinkConnection getConnection() {
        return connection;
    }

    public String getConnectionUrl() {
        return String.valueOf(get(CONNECTION_URL));
    }

    @Override
    public Logger getLogger() {
        if (logger == null) {
            logger = Logger.getLogger(getConnection().getLink().getLinkName() + ".transport");
        }
        return logger;
    }

    public long getReadTimeout() {
        return ((DSLong) get(READ_TIMEOUT)).toLong();
    }

    public abstract boolean isOpen();

    /**
     * The size of the current message (in what depends on the type of transport).
     */
    public abstract int messageSize();

    /**
     * Establish the underlying connection.  Calling when already open will have no effect.
     *
     * @return This
     */
    public abstract DSTransport open();

    /**
     * @return This
     */
    public DSTransport setConnection(DSLinkConnection connection) {
        this.connection = connection;
        return this;
    }

    /**
     * The complete url used to establish the connection.
     *
     * @return This
     */
    public DSTransport setConnectionUrl(String url) {
        put(CONNECTION_URL, DSString.valueOf(url)).setReadOnly(true);
        return this;
    }

    /**
     * The number of millis the input stream will block on a read before throwing a DSIoException.
     *
     * @param millis Timeout in millis, use zero or less for indefinite.
     * @return This
     */
    public DSTransport setReadTimeout(long millis) {
        put(READ_TIMEOUT, DSLong.valueOf(millis)).setReadOnly(true);
        return this;
    }

    /////////////////////////////////////////////////////////////////
    // Inner Classes
    /////////////////////////////////////////////////////////////////

    /**
     * Responsible for creating the appropriate transport.  When launching a link, the config
     * wsTransportFactory needs to be set to an implementation of this.  The implementation must
     * support the public no-arg constructor.
     */
    public interface Factory {

        public DSBinaryTransport makeBinaryTransport(DSLinkConnection conn);

        public DSTextTransport makeTextTransport(DSLinkConnection conn);

    }

}
