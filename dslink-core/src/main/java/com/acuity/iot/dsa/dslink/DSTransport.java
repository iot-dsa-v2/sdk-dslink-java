package com.acuity.iot.dsa.dslink;

import java.util.logging.Logger;
import org.iot.dsa.dslink.DSLinkConnection;
import org.iot.dsa.io.DSIReader;
import org.iot.dsa.io.DSIWriter;
import org.iot.dsa.node.DSLong;
import org.iot.dsa.node.DSMap;
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

    private Logger logger;

    ///////////////////////////////////////////////////////////////////////////
    // Constructors
    ///////////////////////////////////////////////////////////////////////////

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

    public abstract DSLinkConnection getConnection();

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

    public abstract DSIReader getReader();

    public abstract DSIWriter getWriter();

    public abstract boolean isOpen();

    /**
     * Establish the underlying connection.  Calling when already open will have no effect.
     *
     * @return This
     */
    public abstract DSTransport open();

    /**
     * @return This
     */
    public abstract DSTransport setConnection(DSLinkConnection connection);

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

    /**
     * Whether or not to continue the current message.  Implementations might have a limit on
     * message size, this should return true before the limit is reached.
     */
    public abstract boolean shouldEndMessage();

    /////////////////////////////////////////////////////////////////
    // Inner Classes
    /////////////////////////////////////////////////////////////////

    /**
     * Responsible for creating the appropriate transport.  When launching a link, the config
     * transportFactory needs to be set to an implementation of this.  The implmentation must
     * support the public no-arg constructor.
     */
    public interface Factory {

        public DSTransport makeTransport(DSMap connectionInitializationResponse);

    }

} //class
