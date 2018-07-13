package com.acuity.iot.dsa.dslink.protocol;

import com.acuity.iot.dsa.dslink.protocol.message.OutboundMessage;
import com.acuity.iot.dsa.dslink.protocol.responder.DSResponder;
import com.acuity.iot.dsa.dslink.transport.DSTransport;
import java.io.IOException;
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
    // Class Fields
    ///////////////////////////////////////////////////////////////////////////

    private static final int MAX_MSG_ID = Integer.MAX_VALUE;
    private static final long MSG_TIMEOUT = 60000;

    ///////////////////////////////////////////////////////////////////////////
    // Instance Fields
    ///////////////////////////////////////////////////////////////////////////
    private int ackRcvd = 0;
    private int ackToSend = -1;
    private boolean connected = false;
    private DSLinkConnection connection;
    private long lastTimeRecv;
    private long lastTimeSend;
    private int nextMessage = 1;
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
    // Public Methods
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

    /**
     * Last ack received from the broker, or 0 if no ack received.
     */
    public int getAckRcvd() {
        return ackRcvd;
    }

    /**
     * The next ack id to send, or -1.
     */
    public synchronized int getAckToSend() {
        int ret = ackToSend;
        ackToSend = -1;
        return ret;
    }

    public DSLinkConnection getConnection() {
        return connection;
    }

    public int getMissingAcks() {
        return nextMessage - ackRcvd - 1;
    }

    public abstract DSIRequester getRequester();

    public abstract DSResponder getResponder();

    public DSTransport getTransport() {
        return getConnection().getTransport();
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
     * Called by the connection, this manages the running state and calls doRun for the specific
     * implementation.  A separate thread is spun off to manage writing.
     */
    public void run() {
        lastTimeRecv = lastTimeSend = System.currentTimeMillis();
        new WriteThread(getConnection().getLink().getLinkName() + " Writer").start();
        while (connected) {
            try {
                verifyLastSend();
                doRecvMessage();
                lastTimeRecv = System.currentTimeMillis();
            } catch (Exception x) {
                getTransport().close();
                if (connected) {
                    connected = false;
                    error(getPath(), x);
                }
            }
        }
    }

    /**
     * Call for each incoming message id that needs to be acked.
     */
    public synchronized void setAckRcvd(int ackRcvd) {
        this.ackRcvd = ackRcvd;
        notifyOutgoing();
    }

    /**
     * Call for each incoming message id that needs to be acked.
     */
    public synchronized void setAckToSend(int ackToSend) {
        if (ackToSend > 0) {
            this.ackToSend = ackToSend;
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

    ///////////////////////////////////////////////////////////////////////////
    // Protected Methods
    ///////////////////////////////////////////////////////////////////////////

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
     * The subclass should read and process a single message.  Throw an exception to indicate
     * an error.
     */
    protected abstract void doRecvMessage() throws Exception;

    /**
     * The subclass should send a single message.  Throw an exception to indicate
     * an error.
     */
    protected abstract void doSendMessage() throws Exception;

    @Override
    protected String getLogName() {
        return "Session";
    }

    protected boolean hasAckToSend() {
        return ackToSend > 0;
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
        if (ackToSend > 0) {
            return true;
        }
        if (getMissingAcks() > 7) {
            return false;
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
     * Returns the next new message id.
     */
    protected synchronized int getNextMessageId() {
        int ret = nextMessage;
        if (++nextMessage > MAX_MSG_ID) {
            nextMessage = 1;
        }
        return ret;
    }

    /**
     * Can be used to waking up a sleeping writer.
     */
    protected void notifyOutgoing() {
        synchronized (outgoingMutex) {
            outgoingMutex.notify();
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // Package / Private Methods
    ///////////////////////////////////////////////////////////////////////////

    private void verifyLastRead() throws IOException {
        if ((System.currentTimeMillis() - lastTimeRecv) > MSG_TIMEOUT) {
            throw new IOException("No message received in " + MSG_TIMEOUT + "ms");
        }
    }

    private void verifyLastSend() throws IOException {
        if ((System.currentTimeMillis() - lastTimeSend) > MSG_TIMEOUT) {
            throw new IOException("No message sent in " + MSG_TIMEOUT + "ms");
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
                    verifyLastRead();
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
                    lastTimeSend = System.currentTimeMillis();
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
