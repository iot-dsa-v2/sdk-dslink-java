package com.acuity.iot.dsa.dslink.protocol;

import com.acuity.iot.dsa.dslink.io.msgpack.MsgpackReader;
import com.acuity.iot.dsa.dslink.io.msgpack.MsgpackWriter;
import com.acuity.iot.dsa.dslink.protocol.message.OutboundMessage;
import com.acuity.iot.dsa.dslink.protocol.responder.DSResponder;
import java.io.IOException;
import java.util.concurrent.ConcurrentLinkedQueue;
import org.iot.dsa.DSRuntime;
import org.iot.dsa.conn.DSConnection;
import org.iot.dsa.conn.DSIConnectionDescendant;
import org.iot.dsa.dslink.DSIRequester;
import org.iot.dsa.dslink.DSISession;
import org.iot.dsa.dslink.DSITransport;
import org.iot.dsa.dslink.DSLinkConnection;
import org.iot.dsa.io.DSIReader;
import org.iot.dsa.io.DSIWriter;
import org.iot.dsa.io.json.Json;
import org.iot.dsa.node.DSBool;
import org.iot.dsa.node.DSInfo;
import org.iot.dsa.node.DSLong;
import org.iot.dsa.node.DSNode;
import org.iot.dsa.util.DSException;

/**
 * The state of a connection to a broker as well as a protocol implementation. Not intended for link
 * implementors.
 *
 * @author Aaron Hansen
 */
public abstract class DSSession extends DSNode implements DSIConnectionDescendant, DSISession {

    ///////////////////////////////////////////////////////////////////////////
    // Class Fields
    ///////////////////////////////////////////////////////////////////////////

    protected static final String LAST_ACK_RCVD = "Ack Rcvd";
    protected static final String LAST_ACK_SENT = "Ack Sent";
    protected static final String LAST_MID_RCVD = "MID Rcvd";
    protected static final String LAST_MID_SENT = "MID Sent";
    protected static final String REQ_QUEUE = "Request Queue";
    protected static final String RES_QUEUE = "Response Queue";
    protected static final String REQUESTER = "Requester";
    protected static final String REQUESTER_ALLOWED = "Requester Allowed";
    protected static final String RESPONDER = "Responder";

    private static final int MAX_MISSING_ACKS = 8;
    private static final int MAX_MSG_ID = Integer.MAX_VALUE;
    private static final long MSG_TIMEOUT = 90000;

    ///////////////////////////////////////////////////////////////////////////
    // Instance Fields
    ///////////////////////////////////////////////////////////////////////////
    private int ackLastSent = -1;
    private int ackRcvd = -1;
    private int ackRequired = 0;
    private int ackToSend = -1;
    private boolean connected = false;
    private DSLinkConnection connection;
    private long lastTimeRecv;
    private long lastTimeSend;
    private long lastUpdateStats;
    private int midRcvd = 0;
    private int midSent = 0;
    private int nextMessage = 1;
    private DSIReader reader;
    private Receiver receiver = new Receiver();
    private ConcurrentLinkedQueue<OutboundMessage> reqQueue = new ConcurrentLinkedQueue<OutboundMessage>();
    private DSInfo requesterAllowed = getInfo(REQUESTER_ALLOWED);
    private ConcurrentLinkedQueue<OutboundMessage> resQueue = new ConcurrentLinkedQueue<OutboundMessage>();
    private Sender sender = new Sender();
    private DSInfo statAckRcvd = getInfo(LAST_ACK_RCVD);
    private DSInfo statAckSent = getInfo(LAST_ACK_SENT);
    private DSInfo statMidRcvd = getInfo(LAST_MID_SENT);
    private DSInfo statMidSent = getInfo(LAST_MID_SENT);
    private DSInfo statReqQ = getInfo(REQ_QUEUE);
    private DSInfo statResQ = getInfo(RES_QUEUE);
    private DSRuntime.Timer updateTimer;
    private DSIWriter writer;

    ///////////////////////////////////////////////////////////////////////////
    // Constructors
    ///////////////////////////////////////////////////////////////////////////

    public DSSession() {
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
                throw new IllegalStateException("Requester not allowed");
            }
            reqQueue.add(arg);
            sendMessage();
        }
        updateStats();
    }

    /**
     * Add a message to the outgoing response queue.
     */
    public void enqueueOutgoingResponse(OutboundMessage arg) {
        if (connected) {
            resQueue.add(arg);
            sendMessage();
        }
        updateStats();
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
        if (ret < 0) {
            return ret;
        }
        ackLastSent = ret;
        ackToSend = -1;
        return ret;
    }

    public DSLinkConnection getConnection() {
        if (connection == null) {
            connection = (DSLinkConnection) getAncestor(DSLinkConnection.class);
        }
        return connection;
    }

    /**
     * The last message ID generated for an outbound message (could still be current).
     */
    public int getMidSent() {
        return midSent;
    }

    public DSIReader getReader() {
        if (reader == null) {
            DSITransport transport = getConnection().getTransport();
            if (transport.isText()) {
                reader = Json.reader(transport.getTextInput());
            } else {
                reader = new MsgpackReader(transport.getBinaryInput());
            }
        }
        return reader;
    }

    public abstract DSIRequester getRequester();

    public abstract DSResponder getResponder();

    public DSITransport getTransport() {
        return getConnection().getTransport();
    }

    public DSIWriter getWriter() {
        if (writer == null) {
            DSITransport transport = getConnection().getTransport();
            if (transport.isText()) {
                writer = Json.writer(transport.getTextOutput());
            } else {
                writer = new MsgpackWriter() {
                    @Override
                    public void onComplete() {
                        writeTo(getTransport().getBinaryOutput());
                    }
                };
            }
        }
        return writer;
    }

    public boolean isRequesterAllowed() {
        return requesterAllowed.getElement().toBoolean();
    }

    @Override
    public void onConnectionChange(DSConnection connection) {
        switch (connection.getConnectionState()) {
            case CONNECTED:
                onConnected();
                break;
            case DISCONNECTED:
                onDisconnected();
                reader = null;
                writer = null;
                break;
            case DISCONNECTING:
                onDisconnecting();
                break;
            //case CONNECTING:
        }
    }

    @Override
    public void recvMessage(boolean async) {
        if (async) {
            DSRuntime.run(receiver);
        } else {
            receiver.run();
        }
    }

    /**
     * Triggers an async write of the next outbound message.
     */
    public void sendMessage() {
        DSRuntime.run(sender);
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
        declareDefault(REQUESTER_ALLOWED, DSBool.FALSE)
                .setReadOnly(true)
                .setTransient(true);
        declareDefault(REQ_QUEUE, DSLong.valueOf(0), "Number outbound requests")
                .setReadOnly(true)
                .setTransient(true);
        declareDefault(RES_QUEUE, DSLong.valueOf(0), "Number of outbound responses")
                .setReadOnly(true)
                .setTransient(true);
        declareDefault(LAST_ACK_RCVD, DSLong.valueOf(0))
                .setReadOnly(true)
                .setTransient(true);
        declareDefault(LAST_ACK_SENT, DSLong.valueOf(0))
                .setReadOnly(true)
                .setTransient(true);
        declareDefault(LAST_MID_RCVD, DSLong.valueOf(0))
                .setReadOnly(true)
                .setTransient(true);
        declareDefault(LAST_MID_SENT, DSLong.valueOf(0))
                .setReadOnly(true)
                .setTransient(true);
    }

    /**
     * Can return null.
     */
    protected OutboundMessage dequeueOutgoingRequest() {
        return reqQueue.poll();
    }

    /**
     * Can return null.
     */
    protected OutboundMessage dequeueOutgoingResponse() {
        return resQueue.poll();
    }

    /**
     * The subclass should read a single message.  Throw an exception to indicate
     * an error.
     */
    protected abstract void doRecvMessage() throws Exception;

    /**
     * The subclass should send a single message.  Throw an exception to indicate
     * an error.
     */
    protected abstract void doSendMessage() throws Exception;

    protected int getMissingAcks() {
        if (ackRequired > 0) {
            return ackRequired - ackRcvd - 1;
        }
        return 0;
    }

    /**
     * Returns the next new message id.
     */
    protected synchronized int getNextMid() {
        midSent = nextMessage;
        if (++nextMessage > MAX_MSG_ID) {
            nextMessage = 1;
        }
        return midSent;
    }

    protected boolean hasAckToSend() {
        return ackToSend > 0;
    }

    protected abstract boolean hasPingToSend();

    /**
     * Override point, this returns the result of hasMessagesToSend.
     */
    protected boolean hasSomethingToSend() {
        if (!getTransport().isOpen()) {
            return false;
        }
        if (hasAckToSend() || hasPingToSend()) {
            return true;
        }
        if (waitingForAcks()) {
            return false;
        }
        if (!resQueue.isEmpty()) {
            for (OutboundMessage msg : resQueue) {
                if (msg.canWrite(this)) {
                    return true;
                }
            }
        }
        if (!reqQueue.isEmpty()) {
            for (OutboundMessage msg : reqQueue) {
                if (msg.canWrite(this)) {
                    return true;
                }
            }
        }
        return false;
    }

    protected int numOutgoingRequests() {
        return reqQueue.size();
    }

    protected int numOutgoingResponses() {
        return resQueue.size();
    }

    /**
     * Creates the starts the read and write threads.
     */
    protected void onConnected() {
        connected = true;
        lastTimeRecv = lastTimeSend = System.currentTimeMillis();
        sendMessage();
    }

    /**
     * Clear the outgoing queues.
     */
    protected void onDisconnected() {
        reqQueue.clear();
        resQueue.clear();
        sendMessage(); //todo why is this here?
    }

    /**
     * Sets the connected state to false so that the read and write threads will exit cleanly.
     */
    protected void onDisconnecting() {
        if (!connected) {
            return;
        }
        connected = false;
        sendMessage();
        //Attempt to exit cleanly, try to get acks for sent messages.
        waitForAcks(1000);
    }

    @Override
    protected void onSubscribed() {
        updateTimer = DSRuntime.run(() -> updateStats(), 0, 1000);
    }

    protected void onUnsubscribed() {
        updateTimer.cancel();
        updateTimer = null;
    }

    protected void requeueOutgoingRequest(OutboundMessage arg) {
        reqQueue.add(arg);
    }

    protected void requeueOutgoingResponse(OutboundMessage arg) {
        resQueue.add(arg);
    }

    /**
     * Called for each incoming ack.
     */
    protected void setAckRcvd(int ackRcvd) {
        if (ackRcvd < this.ackRcvd) {
            debug(debug() ? String.format("Ack rcvd %s < last %s", ackRcvd, this.ackRcvd) : null);
        } else {
            this.ackRcvd = ackRcvd;
        }
        synchronized (receiver) {
            receiver.notify();
        }
    }

    /**
     * Used to indicate that the current message ID requires an ack.
     */
    protected void setAckRequired() {
        ackRequired = midSent;
    }

    /**
     * Call for each incoming message id that needs to be acked.
     */
    protected void setAckToSend(int ackToSend) {
        if (ackToSend > 0) {
            this.ackToSend = ackToSend;
            sendMessage();
        }
    }

    protected void setMidRcvd(int mid) {
        midRcvd = mid;
    }

    protected void updateStats() {
        long now = System.currentTimeMillis();
        if ((now - lastUpdateStats) > 1000) {
            put(statAckRcvd, DSLong.valueOf(ackRcvd));
            put(statAckSent, DSLong.valueOf(ackLastSent));
            put(statReqQ, DSLong.valueOf(reqQueue.size()));
            put(statResQ, DSLong.valueOf(resQueue.size()));
            put(statMidRcvd, DSLong.valueOf(midRcvd));
            put(statMidSent, DSLong.valueOf(midSent));
            lastUpdateStats = now;
        }
    }

    protected boolean waitingForAcks() {
        boolean ret = getMissingAcks() > MAX_MISSING_ACKS;
        if (ret) {
            debug(debug() ? "Waiting for " + getMissingAcks() + " acks" : null);
        }
        return ret;
    }

    ///////////////////////////////////////////////////////////////////////////
    // Private Methods
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

    /* Try to exit cleanly, wait for all acks for sent messages. */
    private void waitForAcks(long timeout) {
        long start = System.currentTimeMillis();
        synchronized (receiver) {
            while (getMissingAcks() > 0) {
                try {
                    receiver.wait(500);
                } catch (InterruptedException x) {
                    warn(getPath(), x);
                }
                if ((System.currentTimeMillis() - start) > timeout) {
                    debug(debug() ? String
                            .format("waitForAcks timeout (%s / %s)", ackRcvd, midSent)
                                  : null);
                    break;
                }
            }
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // Inner Classes
    ///////////////////////////////////////////////////////////////////////////

    private class Receiver implements Runnable {

        private boolean interest = false;
        private boolean recving = false;

        public synchronized boolean isReceiving() {
            return recving;
        }

        @Override
        public void run() {
            synchronized (receiver) {
                if (recving) {
                    interest = true;
                    return;
                }
                interest = false;
                recving = true;
            }
            try {
                do {
                    verifyLastSend();
                    doRecvMessage();
                    getConnection().connOk();
                    lastTimeRecv = System.currentTimeMillis();
                } while (interest);
            } catch (Exception x) {
                if (connected) {
                    connected = false;
                    debug(x);
                    getConnection().connDown(DSException.makeMessage(x));
                }
            }
            recving = false;
        }
    }

    private class Sender implements Runnable {

        private boolean interest = false;
        private boolean sending = false;

        @Override
        public void run() {
            synchronized (sender) {
                if (sending) {
                    interest = true;
                    return;
                }
                interest = false;
                sending = true;
            }
            try {
                verifyLastRead();
                while (hasSomethingToSend()) {
                    doSendMessage();
                    getConnection().connOk();
                    lastTimeSend = System.currentTimeMillis();
                }
            } catch (Exception x) {
                if (connected) {
                    connected = false;
                    debug(x);
                    getConnection().connDown(DSException.makeMessage(x));
                }
            } finally {
                sending = false;
            }
            if (interest) {
                DSRuntime.run(this);
            }
        }

    }

}
