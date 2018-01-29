package com.acuity.iot.dsa.dslink;

import com.acuity.iot.dsa.dslink.protocol.message.OutboundMessage;
import com.acuity.iot.dsa.dslink.transport.DSTransport;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;
import org.iot.dsa.dslink.DSIRequester;
import org.iot.dsa.dslink.DSLinkConnection;
import org.iot.dsa.node.DSNode;

/**
 * The state of a connection to a broker as well as a protocol implementation. Not intended for link
 * implementors.
 *
 * @author Aaron Hansen
 */
public abstract class DSSession extends DSNode {

    ///////////////////////////////////////////////////////////////////////////
    // Fields
    ///////////////////////////////////////////////////////////////////////////

    private boolean connected = false;
    private DSLinkConnection connection;
    private Logger logger;
    private Object outgoingMutex = new Object();
    private List<OutboundMessage> outgoingRequests = new LinkedList<OutboundMessage>();
    private List<OutboundMessage> outgoingResponses = new LinkedList<OutboundMessage>();
    protected boolean requesterAllowed = false;

    ///////////////////////////////////////////////////////////////////////////
    // Methods
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Can be called by the subclass to force exit the run method.
     */
    public void disconnect() {
        if (!connected) {
            return;
        }
        connected = false;
        getTransport().close();
        synchronized (outgoingMutex) {
            notifyAll();
        }
        info(getPath() + " locally closed");
    }

    /**
     * The subclass should read and process a single message.  Throw an exception to indicate
     * an error.
     */
    protected abstract void doRecvMessage() throws Exception;

    /**
     * The subclass should send a single message.  Throw an exception to indicate
     * an error.
     */
    protected abstract void doSendMessage() throws Exception;

    /**
     * Can return null.
     */
    protected OutboundMessage dequeueOutgoingResponse() {
        synchronized (outgoingMutex) {
            if (!outgoingResponses.isEmpty()) {
                return outgoingResponses.remove(0);
            }
        }
        return null;
    }

    /**
     * Can return null.
     */
    protected OutboundMessage dequeueOutgoingRequest() {
        synchronized (outgoingMutex) {
            if (!outgoingRequests.isEmpty()) {
                return outgoingRequests.remove(0);
            }
        }
        return null;
    }

    /**
     * Add a message to the outgoing request queue.
     */
    public void enqueueOutgoingRequest(OutboundMessage arg) {
        if (connected) {
            if (!requesterAllowed) {
                throw new IllegalStateException("Requests forbidden");
            }
            synchronized (outgoingMutex) {
                outgoingRequests.add(arg);
                outgoingMutex.notify();
            }
        }
    }

    /**
     * Add a message to the outgoing response queue.
     */
    public void enqueueOutgoingResponse(OutboundMessage arg) {
        if (connected) {
            synchronized (outgoingMutex) {
                outgoingResponses.add(arg);
                outgoingMutex.notify();
            }
        }
    }

    public DSLinkConnection getConnection() {
        return connection;
    }

    @Override
    public Logger getLogger() {
        if (logger == null) {
            logger = Logger.getLogger(getConnection().getLink().getLinkName() + ".session");
        }
        return logger;
    }

    public abstract DSIRequester getRequester();

    public DSTransport getTransport() {
        return getConnection().getTransport();
    }

    /**
     * True if there are any outbound requests or responses queued up.
     */
    protected final boolean hasMessagesToSend() {
        if (!outgoingResponses.isEmpty()) {
            return true;
        }
        if (!outgoingRequests.isEmpty()) {
            return true;
        }
        return false;
    }

    protected boolean hasOutgoingRequests() {
        return !outgoingRequests.isEmpty();
    }

    protected boolean hasOutgoingResponses() {
        return !outgoingResponses.isEmpty();
    }

    /**
     * Override point, this returns the result of hasMessagesToSend.
     */
    protected boolean hasSomethingToSend() {
        return hasMessagesToSend();
    }

    protected boolean isConnected() {
        return connected;
    }

    /**
     * Can be used to waking up a sleeping writer.
     */
    protected void notifyOutgoing() {
        synchronized (outgoingMutex) {
            outgoingMutex.notify();
        }
    }

    /**
     * Override point, called when the previous connection can be resumed. The the transport will
     * have already been set.
     */
    public void onConnect() {
        connected = true;
    }

    /**
     * Override point, when a connection attempt failed.
     */
    public void onConnectFail() {
        connected = false;
    }

    /**
     * Override point, called after the connection is closed.
     */
    public void onDisconnect() {
        synchronized (outgoingMutex) {
            outgoingRequests.clear();
            outgoingResponses.clear();
        }
    }

    /**
     * Called when the broker signifies that requests are allowed.
     */
    public void setRequesterAllowed() {
        requesterAllowed = true;
    }

    /**
     * Called by the connection, this manages the running state and calls doRun for the specific
     * implementation.  A separate thread is spun off to manage writing.
     */
    public void run() {
        new WriteThread(getConnection().getLink().getLinkName() + " Writer").start();
        while (connected) {
            try {
                doRecvMessage();
            } catch (Exception x) {
                getTransport().close();
                if (connected) {
                    connected = false;
                    severe(getPath(), x);
                }
            }
        }
    }

    /**
     * For use by the connection object.
     */
    public DSSession setConnection(DSLinkConnection connection) {
        this.connection = connection;
        return this;
    }

    ///////////////////////////////////////////////////////////////////////////
    // Inner Classes
    ///////////////////////////////////////////////////////////////////////////

    /**
     * A separate thread is used for writing to the connection.
     */
    private class WriteThread extends Thread {

        WriteThread(String name) {
            super(name);
            setDaemon(true);
        }

        public void run() {
            try {
                while (connected) {
                    synchronized (outgoingMutex) {
                        if (!hasSomethingToSend()) {
                            try {
                                outgoingMutex.wait(5000);
                            } catch (InterruptedException x) {
                                fine(getPath(), x);
                            }
                            continue;
                        }
                    }
                    doSendMessage();
                }
            } catch (Exception x) {
                if (connected) {
                    connected = false;
                    getTransport().close();
                    severe(getPath(), x);
                }
            }
        }
    }

}
