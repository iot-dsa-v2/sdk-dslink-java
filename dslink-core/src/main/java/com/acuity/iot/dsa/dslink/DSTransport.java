package com.acuity.iot.dsa.dslink;

import org.iot.dsa.dslink.DSLinkConnection;
import org.iot.dsa.io.DSReader;
import org.iot.dsa.io.DSWriter;
import org.iot.dsa.node.DSMap;

/**
 * Binds an abstract DSLinkConnection to a specific implementation.  Examples of
 * transports would be sockets, websockets and http.
 *
 * @author Aaron Hansen
 */
public interface DSTransport {

    /**
     * Called at the start of a new outbound message.
     *
     * @return This
     */
    public DSTransport beginMessage();

    /**
     * Close the actual connection and clean up resources.  Calling when already
     * closed will have no effect.
     *
     * @return This
     */
    public DSTransport close();

    public DSLinkConnection getConnection();

    /**
     * Signifies the end of an outgoing message. Needed because websockets are frame
     * based.
     *
     * @return This
     */
    public DSTransport endMessage();

    public DSReader getReader();

    public DSWriter getWriter();

    public boolean isOpen();

    /**
     * Establish the underlying connection.  Calling when already open will have
     * no effect.
     *
     * @return This
     */
    public DSTransport open();

    /**
     * @return This
     */
    public DSTransport setConnection(DSLinkConnection connection);

    /**
     * The complete url used to establish the connection.
     *
     * @return This
     */
    public DSTransport setConnectionUrl(String url);

    /**
     * The number of millis the input stream will block on a read before throwing
     * a DSIoException.
     *
     * @param millis Timeout in millis, use zero or less for indefinite.
     * @return This
     */
    public DSTransport setReadTimeout(long millis);

    /**
     * Whether or not to continue the current message.  Implementations might
     * have a limit on message size, this should return true before the limit is reached.
     */
    public boolean shouldEndMessage();

    /////////////////////////////////////////////////////////////////
    // Inner Classes
    /////////////////////////////////////////////////////////////////

    /**
     * Responsible for creating the appropriate transport.  When launching a link, the
     * config transportFactory needs to be set to an implementation of this.  The implmentation
     * must support the public no-arg constructor.
     */
    public interface Factory {

        public DSTransport makeTransport(DSMap connectionInitializationResponse);

    }

} //class
