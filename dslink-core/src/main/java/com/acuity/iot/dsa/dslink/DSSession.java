package com.acuity.iot.dsa.dslink;

import com.acuity.iot.dsa.dslink.protocol.message.OutboundMessage;
import com.acuity.iot.dsa.dslink.transport.DSTransport;
import java.util.LinkedList;
import java.util.List;
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
    // Constants
    ///////////////////////////////////////////////////////////////////////////

    private static final int MAX_MSG_ID = 2147483647;

    ///////////////////////////////////////////////////////////////////////////
    // Fields
    ///////////////////////////////////////////////////////////////////////////

    private int nextAck = -1;
    private int nextMessage = 1;
    private boolean connected = false;
    private DSLinkConnection connection;
    private Object outgoingMutex = new Object();
    private List<OutboundMessage> outgoingRequests = new LinkedList<OutboundMessage>();
    private List<OutboundMessage> outgoingResponses = new LinkedList<OutboundMessage>();
    protected boolean requesterAllowed = false;

    ///////////////////////////////////////////////////////////////////////////
    // Constructors
    ///////////////////////////////////////////////////////////////////////////

    public DSSession() {
    }

    public DSSession(DSLinkConnection connection) {
        this.connection = connection;
    }

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
    protected String getLogName() {
        return "Session";
    }

    /**
     * The next ack id, or -1.
     */
    public synchronized int getNextAck() {
        int ret = nextAck;
        nextAck = -1;
        return ret;
    }

    /**
     * Returns the next new message id.
     */
    public synchronized int getNextMessageId() {
        int ret = nextMessage;
        if (++nextMessage > MAX_MSG_ID) {
            nextMessage = 1;
        }
        return ret;
    }

    public abstract DSIRequester getRequester();

    public DSTransport getTransport() {
        return getConnection().getTransport();
    }

    protected boolean hasAckToSend() {
        return nextAck > 0;
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
        if (nextAck > 0) {
            return true;
        }
        if (!outgoingResponses.isEmpty()) {
            return true;
        }
        if (!outgoingRequests.isEmpty()) {
            return true;
        }
        return false;
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
     * Call for each incoming message id that needs to be acked.
     */
    public synchronized void setNextAck(int nextAck) {
        if (nextAck > 0) {
            this.nextAck = nextAck;
            notifyOutgoing();
        }
    }

    /**
     * Called when the broker signifies that requests are allowed.
     */
    public void setRequesterAllowed() {
        requesterAllowed = true;
    }

    public abstract boolean shouldEndMessage();

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
                    error(getPath(), x);
                }
            }
        }
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
                                warn(getPath(), x);
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
                    error(getPath(), x);
                }
            }
        }
    }

}
