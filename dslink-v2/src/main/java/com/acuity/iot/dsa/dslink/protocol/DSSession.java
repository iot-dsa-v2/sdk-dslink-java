package com.acuity.iot.dsa.dslink.protocol;

import com.acuity.iot.dsa.dslink.protocol.message.OutboundMessage;
import com.acuity.iot.dsa.dslink.protocol.responder.DSResponder;
import com.acuity.iot.dsa.dslink.transport.DSTransport;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import org.iot.dsa.conn.DSConnection;
import org.iot.dsa.conn.DSIConnected;
import org.iot.dsa.dslink.DSIRequester;
import org.iot.dsa.dslink.DSLinkConnection;
import org.iot.dsa.node.DSBool;
import org.iot.dsa.node.DSInfo;
import org.iot.dsa.node.DSNode;
import org.iot.dsa.util.DSException;

/**
 * The state of a connection to a broker as well as a protocol implementation. Not intended for link
 * implementors.
 *
 * @author Aaron Hansen
 */
public abstract class DSSession extends DSNode implements DSIConnected {

    ///////////////////////////////////////////////////////////////////////////
    // Class Fields
    ///////////////////////////////////////////////////////////////////////////

    protected static final String REQUESTER = "Requester";
    protected static final String REQUESTER_ALLOWED = "Requester Allowed";
    protected static final String RESPONDER = "Responder";

    private static final int MAX_MSG_ID = Integer.MAX_VALUE;
    private static final long MSG_TIMEOUT = 60000;

    ///////////////////////////////////////////////////////////////////////////
    // Instance Fields
    ///////////////////////////////////////////////////////////////////////////

    private int ackRcvd = -1;
    private int ackToSend = -1;
    private boolean connected = false;
    private DSLinkConnection connection;
    private long lastTimeRecv;
    private long lastTimeSend;
    private int messageId = 0;
    private int nextMessage = 1;
    private final Object outgoingMutex = new Object();
    private List<OutboundMessage> outgoingRequests = new LinkedList<OutboundMessage>();
    private List<OutboundMessage> outgoingResponses = new LinkedList<OutboundMessage>();
    private DSInfo requesterAllowed = getInfo(REQUESTER_ALLOWED);
    private ReadThread readThread;
    private WriteThread writeThread;

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
     * Add a message to the outgoing request queue.
     */
    public void enqueueOutgoingRequest(OutboundMessage arg) {
        if (connected) {
            if (!isRequesterAllowed()) {
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
     * Last ack received from the broker, or -1.
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

    /**
     * The current (last) message ID generated.
     */
    public int getMessageId() {
        return messageId;
    }

    public abstract DSIRequester getRequester();

    public abstract DSResponder getResponder();

    public DSTransport getTransport() {
        return getConnection().getTransport();
    }

    public boolean isRequesterAllowed() {
        return requesterAllowed.getElement().toBoolean();
    }

    @Override
    public void onChange(DSConnection connection) {
        switch (connection.getConnectionState()) {
            case CONNECTED:
                onConnected();
                break;
            case DISCONNECTED:
                onDisconnected();
                break;
            case DISCONNECTING:
                onDisconnecting();
                break;
            //case CONNECTING:
        }
    }

    /**
     * Called when the broker signifies that requests are allowed.
     */
    public void setRequesterAllowed(boolean allowed) {
        put(requesterAllowed, DSBool.valueOf(allowed));
    }

    public abstract boolean shouldEndMessage();

    ///////////////////////////////////////////////////////////////////////////
    // Protected Methods
    ///////////////////////////////////////////////////////////////////////////

    @Override
    protected void declareDefaults() {
        super.declareDefaults();
        declareDefault(REQUESTER_ALLOWED, DSBool.FALSE);
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
        return getLogName("session");
    }

    protected int getMissingAcks() {
        return messageId - ackRcvd - 1;
    }

    /**
     * Returns the next new message id.
     */
    protected synchronized int getNextMessageId() {
        messageId = nextMessage;
        if (++nextMessage > MAX_MSG_ID) {
            nextMessage = 1;
        }
        return messageId;
    }

    protected boolean hasAckToSend() {
        return ackToSend > 0;
    }

    protected abstract boolean hasPingToSend();

    /**
     * Override point, this returns the result of hasMessagesToSend.
     */
    protected boolean hasSomethingToSend() {
        if (ackToSend >= 0) {
            return true;
        }
        if (hasPingToSend()) {
            return true;
        }
        if (waitingForAcks()) {
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

    /**
     * Can be used to waking up a sleeping writer.
     */
    protected void notifyOutgoing() {
        synchronized (outgoingMutex) {
            outgoingMutex.notify();
        }
    }

    protected int numOutgoingRequests() {
        return outgoingRequests.size();
    }

    protected int numOutgoingResponses() {
        return outgoingResponses.size();
    }

    /**
     * Creates the starts the read and write threads.
     */
    protected void onConnected() {
        connected = true;
        lastTimeRecv = lastTimeSend = System.currentTimeMillis();
        readThread = new ReadThread(getConnection().getLink().getLinkName() + " Reader");
        writeThread = new WriteThread(getConnection().getLink().getLinkName() + " Writer");
        readThread.start();
        writeThread.start();
    }

    /**
     * Clear the outgoing queues and waits for the the read and write threads to exit.
     */
    protected void onDisconnected() {
        synchronized (outgoingMutex) {
            outgoingRequests.clear();
            outgoingResponses.clear();
            outgoingMutex.notifyAll();
        }
        try {
            writeThread.join();
        } catch (Exception x) {
            debug(getPath(), x);
        }
        try {
            readThread.join();
        } catch (Exception x) {
            debug(getPath(), x);
        }
        writeThread = null;
        readThread = null;
    }

    /**
     * Sets the connected state to false so that the read and write threads will exit cleanly.
     */
    protected void onDisconnecting() {
        if (!connected) {
            return;
        }
        connected = false;
        notifyOutgoing();
    }

    protected void requeueOutgoingRequest(OutboundMessage arg) {
        synchronized (outgoingMutex) {
            outgoingRequests.add(arg);
        }
    }

    protected void requeueOutgoingResponse(OutboundMessage arg) {
        synchronized (outgoingMutex) {
            outgoingResponses.add(arg);
        }
    }

    /**
     * Call for each incoming message id that needs to be acked.
     */
    protected void setAckRcvd(int ackRcvd) {
        if (ackRcvd < this.ackRcvd) {
            debug(debug() ? String.format("Ack rcvd %s < last %s", ackRcvd, this.ackRcvd) : null);
        }
        this.ackRcvd = ackRcvd;
        notifyOutgoing();
    }

    /**
     * Call for each incoming message id that needs to be acked.
     */
    protected void setAckToSend(int ackToSend) {
        if (ackToSend > 0) {
            this.ackToSend = ackToSend;
            notifyOutgoing();
        }
    }

    protected boolean waitingForAcks() {
        boolean ret = getMissingAcks() > 8;
        if (ret) {
            debug(debug() ? "Waiting for " + getMissingAcks() + " acks" : null);
        }
        return ret;
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
     * Receives messages.
     */
    private class ReadThread extends Thread {

        ReadThread(String name) {
            super(name);
            setDaemon(true);
        }

        public void run() {
            DSLinkConnection conn = getConnection();
            try {
                while (connected) {
                    verifyLastSend();
                    doRecvMessage();
                    conn.connOk();
                    lastTimeRecv = System.currentTimeMillis();
                }
            } catch (Exception x) {
                if (connected) {
                    connected = false;
                    error(getPath(), x);
                    conn.connDown(DSException.makeMessage(x));
                }
            }
        }
    }

    /**
     * Sends messages.
     */
    private class WriteThread extends Thread {

        WriteThread(String name) {
            super(name);
            setDaemon(true);
        }

        public void run() {
            DSLinkConnection conn = getConnection();
            try {
                while (connected) {
                    verifyLastRead();
                    synchronized (outgoingMutex) {
                        if (!hasSomethingToSend()) {
                            try {
                                outgoingMutex.wait(5000);
                            } catch (InterruptedException x) {
                                debug(getPath(), x);
                            }
                            continue;
                        }
                    }
                    doSendMessage();
                    conn.connOk();
                    lastTimeSend = System.currentTimeMillis();
                }
            } catch (Exception x) {
                if (connected) {
                    connected = false;
                    error(getPath(), x);
                    conn.connDown(DSException.makeMessage(x));
                }
            }
        }
    }

}
