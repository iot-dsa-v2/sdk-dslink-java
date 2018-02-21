package com.acuity.iot.dsa.dslink.protocol.v1;

import static org.iot.dsa.io.DSIReader.Token.BEGIN_LIST;
import static org.iot.dsa.io.DSIReader.Token.BEGIN_MAP;
import static org.iot.dsa.io.DSIReader.Token.END_LIST;
import static org.iot.dsa.io.DSIReader.Token.END_MAP;
import static org.iot.dsa.io.DSIReader.Token.NULL;

import com.acuity.iot.dsa.dslink.DSProtocolException;
import com.acuity.iot.dsa.dslink.DSSession;
import com.acuity.iot.dsa.dslink.protocol.message.MessageWriter;
import com.acuity.iot.dsa.dslink.protocol.message.OutboundMessage;
import com.acuity.iot.dsa.dslink.protocol.v1.requester.DS1Requester;
import com.acuity.iot.dsa.dslink.protocol.v1.responder.DS1Responder;
import com.acuity.iot.dsa.dslink.transport.DSTransport;
import java.io.IOException;
import org.iot.dsa.dslink.DSIRequester;
import org.iot.dsa.io.DSIReader;
import org.iot.dsa.io.DSIReader.Token;
import org.iot.dsa.io.DSIWriter;
import org.iot.dsa.node.DSInfo;
import org.iot.dsa.node.DSInt;
import org.iot.dsa.node.DSMap;

/**
 * Implements DSA 1.1.2
 *
 * @author Aaron Hansen
 */
public class DS1Session extends DSSession {

    ///////////////////////////////////////////////////////////////////////////
    // Constants
    ///////////////////////////////////////////////////////////////////////////

    static final int END_MSG_THRESHOLD = 48000;
    static final String LAST_ACK_RECV = "Last Ack Recv";
    static final String LAST_ACK_SENT = "Last Ack Sent";

    static final int MAX_MSG_IVL = 45000;

    ///////////////////////////////////////////////////////////////////////////
    // Fields
    ///////////////////////////////////////////////////////////////////////////

    private DSInfo lastAckRecv = getInfo(LAST_ACK_RECV);
    private DSInfo lastAckSent = getInfo(LAST_ACK_SENT);
    private long lastMessageSent;
    private MessageWriter messageWriter;
    private boolean requestsNext = false;
    private DS1Requester requester = new DS1Requester(this);
    private DS1Responder responder = new DS1Responder(this);

    /////////////////////////////////////////////////////////////////
    // Constructors
    /////////////////////////////////////////////////////////////////

    public DS1Session() {
    }

    public DS1Session(DS1LinkConnection connection) {
        super(connection);
    }

    /////////////////////////////////////////////////////////////////
    // Methods
    /////////////////////////////////////////////////////////////////

    /**
     * Prepare a new message.  After this is called, beginRequests and/beginResponses maybe called.
     * When the message is complete, endMessage will be called.
     *
     * @see #beginRequests()
     * @see #beginResponses()
     * @see #endMessage()
     */
    protected void beginMessage() {
        DSIWriter out = getWriter();
        out.beginMap();
        out.key("msg").value(getNextMessageId());
        int nextAck = getNextAck();
        if (nextAck > 0) {
            out.key("ack").value(nextAck);
            put(lastAckSent, DSInt.valueOf(nextAck));
        }
    }

    /**
     * Prepare for the response portion of the message.  This will be followed by one or more calls
     * to writeResponse().  When there are no more responses, endResponses() will be called.
     *
     * @see #writeResponse(OutboundMessage)
     * @see #endResponses()
     */
    protected void beginResponses() {
        getWriter().key("responses").beginList();
    }

    /**
     * Prepare for the request portion of the message.  This will be followed by one or more calls
     * to writeRequest().  When there are no more responses, endRequests() will be called.
     *
     * @see #writeResponse(OutboundMessage)
     * @see #endResponses()
     */
    protected void beginRequests() {
        getWriter().key("requests").beginList();
    }

    @Override
    protected void declareDefaults() {
        declareDefault(LAST_ACK_RECV, DSInt.NULL).setReadOnly(true);
        declareDefault(LAST_ACK_SENT, DSInt.NULL).setReadOnly(true);
    }

    @Override
    protected void doRecvMessage() throws IOException {
        DSIReader reader = getReader();
        switch (reader.next()) {
            case BEGIN_MAP:
                processEnvelope(reader);
                break;
            case END_MAP:
            case END_LIST:
            case ROOT:
                break;
            case END_INPUT:
                return;
            default:
                throw new IOException("Unexpected input: " + reader.last());
        }
    }

    @Override
    protected void doSendMessage() {
        DSTransport transport = getTransport();
        long endTime = System.currentTimeMillis() + 2000;
        DSIWriter writer = getWriter();
        writer.reset();
        requestsNext = !requestsNext;
        transport.beginSendMessage();
        beginMessage();
        if (this.hasSomethingToSend()) {
            send(requestsNext, endTime);
            if ((System.currentTimeMillis() < endTime) && !shouldEndMessage()) {
                send(!requestsNext, endTime);
            }
        }
        endMessage();
        transport.endSendMessage();
        lastMessageSent = System.currentTimeMillis();
    }

    /**
     * Complete the message.
     */
    protected void endMessage() {
        getWriter().endMap().flush();
    }

    /**
     * Complete the outgoing responses part of the message.
     *
     * @see #beginResponses()
     * @see #writeResponse(OutboundMessage)
     */
    protected void endResponses() {
        getWriter().endList();
    }

    /**
     * Complete the outgoing requests part of the message.
     *
     * @see #beginRequests()
     * @see #writeRequest(OutboundMessage)
     */
    protected void endRequests() {
        getWriter().endList();
    }

    @Override
    public DS1LinkConnection getConnection() {
        return (DS1LinkConnection) super.getConnection();
    }

    private MessageWriter getMessageWriter() {
        if (messageWriter == null) {
            messageWriter = new MyMessageWriter(getConnection().getWriter());
        }
        return messageWriter;
    }

    public DSIReader getReader() {
        return getConnection().getReader();
    }

    @Override
    public DSIRequester getRequester() {
        return requester;
    }

    private DSIWriter getWriter() {
        return getMessageWriter().getWriter();
    }

    private boolean hasPingToSend() {
        return (System.currentTimeMillis() - lastMessageSent) > MAX_MSG_IVL;
    }

    /**
     * Override point, returns true if there are any pending acks or outbound messages queued up.
     */
    protected boolean hasSomethingToSend() {
        if (hasPingToSend()) {
            return true;
        }
        return super.hasSomethingToSend();
    }

    /*
    @Override
    protected void onStable() {
        put("Requester Session", requesterSession);
        put("Responder Session", responderSession);
    }
    */

    @Override
    public void onConnect() {
        super.onConnect();
        requester.onConnect();
        responder.onConnect();
    }

    @Override
    public void onConnectFail() {
        super.onConnectFail();
        requester.onConnectFail();
        responder.onConnectFail();
    }

    @Override
    public void onDisconnect() {
        super.onDisconnect();
        requester.onDisconnect();
        responder.onDisconnect();
    }

    /**
     * Decomposes and processes a complete envelope which can contain multiple requests and
     * responses.
     *
     * @param reader lastRun() will return BEGIN_MAP
     */
    protected void processEnvelope(DSIReader reader) {
        if (!requesterAllowed) {
            getConnection().setRequesterAllowed();
        }
        int msg = -1;
        Token next;
        switch (reader.next()) {
            case END_MAP:
                return;
            case STRING:
                break;
            default:
                throw new IllegalStateException("Poorly formatted request");
        }
        boolean sendAck = false;
        do {
            String key = reader.getString();
            if (key.equals("requests")) {
                sendAck = true;
                processMessages(reader, true);
            } else if (key.equals("responses")) {
                sendAck = true;
                processMessages(reader, false);
            } else if (key.equals("msg")) {
                reader.next();
                msg = (int) reader.getLong();
            } else if (key.equals("ack")) {
                reader.next();
                put(lastAckRecv, DSInt.valueOf((int) reader.getLong()));
            } else if (key.equals("allowed")) {
                if (reader.next() != Token.BOOLEAN) {
                    throw new IllegalStateException("Allowed not a boolean");
                }
                if (reader.getBoolean()) {
                    fine(fine() ? "Requester allowed" : null);
                    getConnection().setRequesterAllowed();
                }
            } else if (key.equals("salt")) {
                if (reader.next() != Token.STRING) {
                    throw new IllegalStateException("Salt not a string");
                }
                fine(fine() ? "Next salt: " + reader.getString() : null);
                getConnection().updateSalt(reader.getString());
            }
            next = reader.next();
        } while (next != END_MAP);
        if (sendAck && (msg >= 0)) {
            setNextAck(msg);
        }
    }

    /**
     * Processes a list of requests or responses.
     *
     * @param areRequests True for requests, false for responses.
     */
    protected void processMessages(DSIReader reader, boolean areRequests) {
        Token next = reader.next();
        if (next == NULL) {
            return;
        }
        if (next != BEGIN_LIST) {
            throw new IllegalStateException("Requests should be a list");
        }
        DSMap req;
        int rid;
        while (reader.next() == BEGIN_MAP) {
            req = reader.getMap();
            rid = req.get("rid", -1);
            if (rid < 0) {
                trace(trace() ? "No request ID: " + req.toString() : null);
                throw new DSProtocolException("Response missing rid");
            }
            if (areRequests) {
                responder.handleRequest(rid, req);
            } else {
                requester.processResponse(rid, req);
            }

        }
        if (reader.last() != END_LIST) {
            throw new IllegalStateException("Unexpected input: " + reader.last());
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
            if (!hasOutgoingRequests()) {
                return;
            }
            beginRequests();
        } else {
            if (!hasOutgoingResponses()) {
                return;
            }
            beginResponses();
        }
        OutboundMessage msg = requests ? dequeueOutgoingRequest() : dequeueOutgoingResponse();
        while ((msg != null) && (System.currentTimeMillis() < endTime)) {
            if (requests) {
                writeRequest(msg);
            } else {
                writeResponse(msg);
            }
            if (!shouldEndMessage()) {
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
     * Returns true if the current message size has crossed a message size threshold.
     */
    public boolean shouldEndMessage() {
        return (getWriter().length() + getTransport().messageSize()) > END_MSG_THRESHOLD;
    }

    /**
     * Write a request in the current message. Can be called multiple times after beginRequests() is
     * called.  endRequests() will be called once the request part of the message is complete.
     *
     * @see #beginRequests()
     * @see #endRequests()
     */
    public void writeRequest(OutboundMessage message) {
        message.write(getMessageWriter());
    }

    /**
     * Write a response in the current message. Can be called multiple times after beginResponses()
     * is called.  endResponses() will be called once the response part of the message is complete.
     *
     * @see #beginResponses()
     * @see #endResponses()
     */
    public void writeResponse(OutboundMessage message) {
        message.write(getMessageWriter());
    }

    /////////////////////////////////////////////////////////////////
    // Inner Classes
    /////////////////////////////////////////////////////////////////

    private class MyMessageWriter implements MessageWriter {

        DSIWriter writer;

        MyMessageWriter(DSIWriter writer) {
            this.writer = writer;
        }

        @Override
        public DSIWriter getWriter() {
            return writer;
        }
    }

}
