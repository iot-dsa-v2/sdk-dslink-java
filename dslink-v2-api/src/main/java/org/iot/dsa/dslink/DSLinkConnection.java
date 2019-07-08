package org.iot.dsa.dslink;

import org.iot.dsa.conn.DSConnection;

/**
 * Abstract representation of a DSA connection.  Subclasses are responsible for providing
 * the transport and session implementations.
 *
 * @author Aaron Hansen
 */
public abstract class DSLinkConnection extends DSConnection {

    ///////////////////////////////////////////////////////////////////////////
    // Class Fields
    ///////////////////////////////////////////////////////////////////////////

    public static final String SESSION = "Session";
    public static final String TRANSPORT = "Transport";

    ///////////////////////////////////////////////////////////////////////////
    // Instance Fields
    ///////////////////////////////////////////////////////////////////////////

    private DSLink link;
    private DSISession session;
    private DSITransport transport;

    ///////////////////////////////////////////////////////////////////////////
    // Public Methods
    ///////////////////////////////////////////////////////////////////////////

    /**
     * The link using this connection.
     */
    public DSLink getLink() {
        if (link == null) {
            link = (DSLink) getAncestor(DSLink.class);
        }
        return link;
    }

    /**
     * The path representing the link node in the broker.
     */
    public abstract String getPathInBroker();

    /**
     * A convenience that calls the same method on the session.
     */
    public DSIRequester getRequester() {
        DSISession s = getSession();
        if (s == null) {
            return null;
        }
        return s.getRequester();
    }

    public DSISession getSession() {
        if (session == null) {
            setSession(makeSession());
        }
        return session;
    }

    public DSITransport getTransport() {
        if (transport == null) {
            setTransport(makeTransport());
        }
        return transport;
    }

    /**
     * A convenience that calls the same method on the session.
     */
    public boolean isRequesterAllowed() {
        return getSession().isRequesterAllowed();
    }

    ///////////////////////////////////////////////////////////////////////////
    // Protected Methods
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Closes the transport.
     */
    @Override
    protected void doDisconnect() {
        try {
            DSITransport t = transport;
            if ((t != null) && t.isOpen()) {
                t.close();
            }
        } catch (Exception x) {
            error(getPath(), x);
        }
    }

    protected abstract DSISession makeSession();

    protected abstract DSITransport makeTransport();

    @Override
    protected void onDisconnected() {
        super.onDisconnected();
        transport = null;
        remove(TRANSPORT);
    }

    protected void setSession(DSISession session) {
        this.session = session;
        put(SESSION, session).setTransient(true);
    }

    protected void setTransport(DSITransport transport) {
        this.transport = transport;
        put(TRANSPORT, transport).setTransient(true);
    }

}