package com.acuity.iot.dsa.dslink;

import com.acuity.iot.dsa.dslink.protocol.message.OutboundMessage;
import com.acuity.iot.dsa.dslink.protocol.protocol_v1.DS1LinkConnection;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.iot.dsa.DSRuntime;
import org.iot.dsa.dslink.DSIRequester;
import org.iot.dsa.io.DSIReader;
import org.iot.dsa.io.DSIWriter;
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

    static final int MAX_MSG_TIME = 3000;

    ///////////////////////////////////////////////////////////////////////////
    // Fields
    ///////////////////////////////////////////////////////////////////////////

    private boolean active = false;
    private DS1LinkConnection connection;
    private long lastMessageSent;
    private Logger logger;
    private Object outgoingMutex = new Object();
    private List<OutboundMessage> outgoingRequests = new LinkedList<OutboundMessage>();
    private List<OutboundMessage> outgoingResponses = new LinkedList<OutboundMessage>();
    private DSIReader reader;
    protected boolean requesterAllowed = false;
    private DSTransport transport;
    private DSIWriter writer;

    ///////////////////////////////////////////////////////////////////////////
    // Constructors
    ///////////////////////////////////////////////////////////////////////////

    ///////////////////////////////////////////////////////////////////////////
    // Methods
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Prepare a new message.  After this is called, beginRequests and/beginResponses maybe called.
     * When the message is complete, endMessage will be called.
     *
     * @see #beginRequests()
     * @see #beginResponses()
     * @see #endMessage()
     */
    protected abstract void beginMessage();

    /**
     * Prepare for the response portion of the message.  This will be followed by one or more calls
     * to writeResponse().  When there are no more responses, endResponses() will be called.
     *
     * @see #writeResponse(OutboundMessage)
     * @see #endResponses()
     */
    protected abstract void beginResponses();

    /**
     * Prepare for the request portion of the message.  This will be followed by one or more calls
     * to writeRequest().  When there are no more responses, endRequests() will be called.
     *
     * @see #writeResponse(OutboundMessage)
     * @see #endResponses()
     */
    protected abstract void beginRequests();

    /**
     * The protocol implementation should read messages and do something with them. The
     * implementation should call isOpen() to determine when to exit this method.
     *
     * @see #isOpen()
     */
    protected abstract void doRead() throws IOException;

    /**
     * The run method thread spawns a thread which then executes this method for writing outgoing
     * requests and responses.
     */
    private void doWrite() {
        long endTime;
        boolean requestsFirst = false;
        DSTransport transport = getTransport();
        lastMessageSent = System.currentTimeMillis();
        DSIWriter writer = getWriter();
        try {
            while (active) {
                synchronized (outgoingMutex) {
                    if (!hasSomethingToSend()) {
                        try {
                            outgoingMutex.wait(1000);
                        } catch (InterruptedException ignore) {
                        }
                        continue;
                    }
                }
                endTime = System.currentTimeMillis() + MAX_MSG_TIME;
                //alternate the send requests or responses first each time
                writer.reset();
                requestsFirst = !requestsFirst;
                transport.beginMessage();
                beginMessage();
                if (hasMessagesToSend()) {
                    send(requestsFirst, endTime);
                    if (active &&
                            (System.currentTimeMillis() < endTime) &&
                            !transport.shouldEndMessage()) {
                        send(!requestsFirst, endTime);
                    }
                }
                endMessage();
                transport.endMessage();
                lastMessageSent = System.currentTimeMillis();
            }
        } catch (Exception x) {
            if (active) {
                getLogger().log(Level.FINE, getConnection().getConnectionId(), x);
                active = false;
                transport.close();
            }
        }
    }

    /**
     * Can be called by the subclass to force close the connection.
     */
    public void close() {
        try {
            active = false;
            synchronized (outgoingMutex) {
                outgoingRequests.clear();
                outgoingResponses.clear();
                outgoingMutex.notify();
            }
        } catch (Exception x) {
            fine(connection.getConnectionId(), x);
        }
    }

    /**
     * Complete the message.
     */
    protected abstract void endMessage();

    /**
     * Complete the outgoing responses part of the message.
     *
     * @see #beginResponses()
     * @see #writeResponse(OutboundMessage)
     */
    protected abstract void endResponses();

    /**
     * Complete the outgoing requests part of the message.
     *
     * @see #beginRequests()
     * @see #writeRequest(OutboundMessage)
     */
    protected abstract void endRequests();

    /**
     * Can return null.
     */
    private OutboundMessage dequeueOutgoingResponse() {
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
    private OutboundMessage dequeueOutgoingRequest() {
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
        if (active) {
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
        if (active) {
            synchronized (outgoingMutex) {
                outgoingResponses.add(arg);
                outgoingMutex.notify();
            }
        }
    }

    public DS1LinkConnection getConnection() {
        return connection;
    }

    /**
     * The time the lastRun message was completed.
     */
    protected long getLastMessageSent() {
        return lastMessageSent;
    }

    @Override
    public Logger getLogger() {
        if (logger == null) {
            logger = Logger.getLogger(getConnection().getLink().getLinkName() + ".session");
        }
        return logger;
    }

    protected DSIReader getReader() {
        if ((reader == null) && (transport != null)) {
            setReader(transport.getReader());
        }
        return reader;
    }

    protected abstract DSRequesterSession getRequesterSession();

    protected abstract DSResponderSession getResponderSession();

    protected DSTransport getTransport() {
        return transport;
    }

    protected DSIWriter getWriter() {
        if ((writer == null) && (transport != null)) {
            setWriter(transport.getWriter());
        }
        return writer;
    }

    /**
     * True if there are outbound messages on the queue.
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

    /**
     * Override point, this returns the result of hasMessagesToSend.
     */
    protected boolean hasSomethingToSend() {
        return hasMessagesToSend();
    }

    /**
     * The subclass check this to determine when to exit the doRun method.
     *
     * @see #doRead()
     */
    protected boolean isOpen() {
        return active && transport.isOpen();
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
     * Override point for reusable sessions.  Does nothing by default.
     */
    public void pause() {
    }

    /**
     * Called when the broker signifies that requests are allowed.
     */
    public void setRequesterAllowed() {
        requesterAllowed = true;
        DSRuntime.run(new Runnable() {
            @Override
            public void run() {
                DSIRequester requester = getConnection().getLink().getRequester();
                if (requester != null) {
                    requester.onConnected(getRequesterSession());
                }
            }
        });
    }

    /**
     * Called by the connection, this manages the running state and calls doRun for the specific
     * implementation.  A separate thread is spun off to manage writing.
     *
     * @see #doRead()
     */
    public void run() {
        synchronized (this) {
            if (active) {
                throw new IllegalStateException(
                        "Protocol already running " + connection.getConnectionId());
            }
            active = true;
        }
        new WriteThread(getConnection().getLink().getLinkName() + " Writer").start();
        try {
            doRead();
        } catch (Exception x) {
            if (active) {
                fine(getConnection().getConnectionId(), x);
            }
        } finally {
            active = false;
            if (requesterAllowed) {
                try {
                    /*
                    DSRequester requester = getConnection().getLink().getRequester();
                    if (requester != null) {
                        requester.onDisconnected(getRequesterSession());
                    }
                    */
                } catch (Exception x) {
                    fine(getConnection().getConnectionId(), x);
                }
            }
        }
    }

    /**
     * Send messages from one of the queues.
     *
     * @param requests Determines which queue to use; True for outgoing requests, false for
     *                 responses.
     * @param endTime  Stop after this time.
     */
    private void send(boolean requests, long endTime) {
        if (requests) {
            if (outgoingRequests.isEmpty()) {
                return;
            }
            beginRequests();
        } else {
            if (outgoingResponses.isEmpty()) {
                return;
            }
            beginResponses();
        }
        OutboundMessage msg = requests ? dequeueOutgoingRequest() : dequeueOutgoingResponse();
        DSTransport transport = getTransport();
        while ((msg != null) && (System.currentTimeMillis() < endTime)) {
            if (requests) {
                writeRequest(msg);
            } else {
                writeResponse(msg);
            }
            if (!transport.shouldEndMessage()) {
                msg = requests ? dequeueOutgoingRequest() : dequeueOutgoingResponse();
            } else {
                msg = null;
            }
        }
        if (requests) {
            endRequests();
        } else {
            endResponses();
        }
    }

    /**
     * Called when there are no outbound messages in the queue.  Can be used for pinging and acks.
     *
     * @return True to send a message anyway.
     */
    protected boolean sendEmptyMessage() {
        return false;
    }

    /**
     * For use by the connection object.
     */
    public DSSession setConnection(DS1LinkConnection connection) {
        this.connection = connection;
        return this;
    }

    /**
     * For use by the connection object.
     */
    public DSSession setReader(DSIReader reader) {
        this.reader = reader;
        return this;
    }

    /**
     * For use by the connection object.
     */
    public DSSession setTransport(DSTransport transport) {
        this.transport = transport;
        return this;
    }

    /**
     * For use by the connection object.
     */
    public DSSession setWriter(DSIWriter writer) {
        this.writer = writer;
        return this;
    }

    /**
     * Write a request in the current message. Can be called multiple times after beginRequests() is
     * called.  endRequests() will be called once the request part of the message is complete.
     *
     * @see #beginRequests()
     * @see #endRequests()
     */
    public void writeRequest(OutboundMessage message) {
        message.write(getWriter());
    }

    /**
     * Write a response in the current message. Can be called multiple times after beginResponses()
     * is called.  endResponses() will be called once the response part of the message is complete.
     *
     * @see #beginResponses()
     * @see #endResponses()
     */
    public void writeResponse(OutboundMessage message) {
        message.write(getWriter());
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
            doWrite();
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // Initialization
    ///////////////////////////////////////////////////////////////////////////

} //class
