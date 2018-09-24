package com.acuity.iot.dsa.dslink.protocol.v1;

import static org.iot.dsa.io.DSIReader.Token.BEGIN_LIST;
import static org.iot.dsa.io.DSIReader.Token.BEGIN_MAP;
import static org.iot.dsa.io.DSIReader.Token.END_LIST;
import static org.iot.dsa.io.DSIReader.Token.END_MAP;
import static org.iot.dsa.io.DSIReader.Token.NULL;

import com.acuity.iot.dsa.dslink.protocol.DSProtocolException;
import com.acuity.iot.dsa.dslink.protocol.DSSession;
import com.acuity.iot.dsa.dslink.protocol.message.MessageWriter;
import com.acuity.iot.dsa.dslink.protocol.message.OutboundMessage;
import com.acuity.iot.dsa.dslink.protocol.responder.DSResponder;
import com.acuity.iot.dsa.dslink.protocol.v1.requester.DS1Requester;
import com.acuity.iot.dsa.dslink.protocol.v1.responder.DS1Responder;
import java.io.IOException;
import org.iot.dsa.dslink.DSIRequester;
import org.iot.dsa.io.DSIReader;
import org.iot.dsa.io.DSIReader.Token;
import org.iot.dsa.io.DSIWriter;
import org.iot.dsa.node.DSMap;

/**
 * Implements DSA 1.1.2
 *
 * @author Aaron Hansen
 */
public class DS1Session extends DSSession {

    ///////////////////////////////////////////////////////////////////////////
    // Class Fields
    ///////////////////////////////////////////////////////////////////////////

    static final int END_MSG_THRESHOLD = 30000;
    static final int MAX_MSG_IVL = 45000;

    ///////////////////////////////////////////////////////////////////////////
    // Instance Fields
    ///////////////////////////////////////////////////////////////////////////

    private long lastMessageSent;
    private MessageWriter messageWriter;
    private DS1Requester requester = new DS1Requester(this);
    private boolean requestsBegun = false;
    private boolean requestsNext = false;
    private DS1Responder responder = new DS1Responder(this);
    private boolean responsesBegun = false;

    /////////////////////////////////////////////////////////////////
    // Constructors
    /////////////////////////////////////////////////////////////////

    public DS1Session() {
    }

    public DS1Session(DS1LinkConnection connection) {
        super(connection);
    }

    /////////////////////////////////////////////////////////////////
    // Public Methods
    /////////////////////////////////////////////////////////////////

    @Override
    public DS1LinkConnection getConnection() {
        return (DS1LinkConnection) super.getConnection();
    }

    public DSIReader getReader() {
        return getConnection().getReader();
    }

    @Override
    public DSIRequester getRequester() {
        return requester;
    }

    @Override
    public DSResponder getResponder() {
        return responder;
    }

    @Override
    public boolean shouldEndMessage() {
        return (getWriter().length() + getTransport().messageSize()) > END_MSG_THRESHOLD;
    }

    /////////////////////////////////////////////////////////////////
    // Protected Methods
    /////////////////////////////////////////////////////////////////

    @Override
    protected void doRecvMessage() throws IOException {
        DSIReader reader = getReader();
        getTransport().beginRecvMessage();
        switch (reader.next()) {
            case BEGIN_MAP:
                processEnvelope(reader);
                getTransport().endRecvMessage();
                break;
            case END_MAP:
            case END_LIST:
            case ROOT:
                break;
            case END_INPUT:
                throw new IOException("Connection remotely closed");
            default:
                throw new IOException("Unexpected input: " + reader.last());
        }
    }

    @Override
    protected void doSendMessage() {
        try {
            beginMessage();
            if (!waitingForAcks()) {
                requestsNext = !requestsNext;
                send(requestsNext);
                if (!shouldEndMessage()) {
                    send(!requestsNext);
                }
                lastMessageSent = System.currentTimeMillis();
                if (requestsBegun || responsesBegun) {
                    setAckRequired();
                }
            }
            endMessage();
        } finally {
            requestsBegun = false;
            responsesBegun = false;
        }
    }

    @Override
    protected boolean hasPingToSend() {
        return (System.currentTimeMillis() - lastMessageSent) > MAX_MSG_IVL;
    }

    @Override
    protected void onConnected() {
        super.onConnected();
        setRequesterAllowed(true); //Rick says Dart broker doesn't send this
        requester.onConnected();
        responder.onConnected();
    }

    @Override
    protected void onDisconnected() {
        super.onDisconnected();
        requester.onDisconnected();
        responder.onDisconnected();
        messageWriter = null;
    }

    @Override
    protected void onStarted() {
        super.onStarted();
        put(REQUESTER, requester);
        put(RESPONDER, responder);
    }

    /////////////////////////////////////////////////////////////////
    // Package / Private Methods
    /////////////////////////////////////////////////////////////////

    /**
     * Prepare a new message.  After this is called, beginRequests and/beginResponses maybe called.
     * When the message is complete, endMessage will be called.
     *
     * @see #beginRequests()
     * @see #beginResponses()
     * @see #endMessage()
     */
    private void beginMessage() {
        getWriter().reset();
        getTransport().beginSendMessage();
        DSIWriter out = getWriter();
        out.beginMap();
        out.key("msg").value(getNextMessageId());
        int nextAck = getAckToSend();
        if (nextAck > 0) {
            out.key("ack").value(nextAck);
        }
    }

    /**
     * Prepare for the request portion of the message.  This will be followed by one or more calls
     * to writeRequest().  When there are no more responses, endRequests() will be called.
     *
     * @see #writeResponse(OutboundMessage)
     * @see #endResponses()
     */
    private void beginRequests() {
        requestsBegun = true;
        getWriter().key("requests").beginList();
    }

    /**
     * Prepare for the response portion of the message.  This will be followed by one or more calls
     * to writeResponse().  When there are no more responses, endResponses() will be called.
     *
     * @see #writeResponse(OutboundMessage)
     * @see #endResponses()
     */
    private void beginResponses() {
        responsesBegun = true;
        getWriter().key("responses").beginList();
    }

    /**
     * Complete the message.
     */
    private void endMessage() {
        getWriter().endMap().flush();
        getTransport().endSendMessage();
    }

    /**
     * Complete the outgoing requests part of the message.
     *
     * @see #beginRequests()
     * @see #writeRequest(OutboundMessage)
     */
    private void endRequests() {
        getWriter().endList();
    }

    /**
     * Complete the outgoing responses part of the message.
     *
     * @see #beginResponses()
     * @see #writeResponse(OutboundMessage)
     */
    private void endResponses() {
        getWriter().endList();
    }

    private MessageWriter getMessageWriter() {
        if (messageWriter == null) {
            messageWriter = new MyMessageWriter(getConnection().getWriter());
        }
        return messageWriter;
    }

    private DSIWriter getWriter() {
        return getMessageWriter().getWriter();
    }

    /**
     * Decomposes and processes a complete envelope which can contain multiple requests and
     * responses.
     *
     * @param reader last() must return BEGIN_MAP
     */
    private void processEnvelope(DSIReader reader) {
        switch (reader.next()) {
            case END_MAP:
                return;
            case STRING:
                break;
            default:
                throw new IllegalStateException("Poorly formatted request");
        }
        int msg = -1;
        Token next;
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
                int ack = (int) reader.getLong();
                setAckRcvd(ack);
            } else if (key.equals("allowed")) {
                if (reader.next() != Token.BOOLEAN) {
                    throw new IllegalStateException("Allowed not a boolean");
                }
                debug(debug() ? "Requester allowed" : null);
                setRequesterAllowed(reader.getBoolean());
            } else if (key.equals("salt")) {
                reader.next();
                String s = reader.getElement().toString();
                debug(debug() ? "Next salt: " + s : null);
                getConnection().updateSalt(s);
            }
            next = reader.next();
        } while (next != END_MAP);
        if (sendAck && (msg >= 0)) {
            setAckToSend(msg);
        }
    }

    /**
     * Processes a list of requests or responses.
     *
     * @param areRequests True for requests, false for responses.
     */
    private void processMessages(DSIReader reader, boolean areRequests) {
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
                requester.handleResponse(rid, req);
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
     */
    private void send(boolean requests) {
        int count;
        if (requests) {
            count = numOutgoingRequests();
        } else {
            count = numOutgoingResponses();
        }
        if (count == 0) {
            return;
        }
        OutboundMessage msg = requests ? dequeueOutgoingRequest() : dequeueOutgoingResponse();
        while (msg != null) {
            if (!msg.canWrite(this)) {
                if (requests) {
                    requeueOutgoingRequest(msg);
                } else {
                    requeueOutgoingResponse(msg);
                }
            } else {
                if (requests) {
                    writeRequest(msg);
                } else {
                    writeResponse(msg);
                }
            }
            if (--count == 0) {
                msg = null;
            } else if (shouldEndMessage()) {
                msg = null;
            } else {
                msg = requests ? dequeueOutgoingRequest() : dequeueOutgoingResponse();
            }
        }
        if (requests) {
            if (requestsBegun) {
                endRequests();
            }
        } else {
            if (responsesBegun) {
                endResponses();
            }
        }
    }

    /**
     * Write a request in the current message. Can be called multiple times after beginRequests() is
     * called.  endRequests() will be called once the request part of the message is complete.
     *
     * @see #beginRequests()
     * @see #endRequests()
     */
    private void writeRequest(OutboundMessage message) {
        if (!requestsBegun) {
            beginRequests();
        }
        message.write(this, getMessageWriter());
    }

    /**
     * Write a response in the current message. Can be called multiple times after beginResponses()
     * is called.  endResponses() will be called once the response part of the message is complete.
     *
     * @see #beginResponses()
     * @see #endResponses()
     */
    private void writeResponse(OutboundMessage message) {
        if (!responsesBegun) {
            beginResponses();
        }
        message.write(this, getMessageWriter());
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
