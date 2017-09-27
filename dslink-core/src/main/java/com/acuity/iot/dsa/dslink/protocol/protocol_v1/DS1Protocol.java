package com.acuity.iot.dsa.dslink.protocol.protocol_v1;

import static org.iot.dsa.io.DSIReader.Token.BEGIN_LIST;
import static org.iot.dsa.io.DSIReader.Token.BEGIN_MAP;
import static org.iot.dsa.io.DSIReader.Token.END_LIST;
import static org.iot.dsa.io.DSIReader.Token.END_MAP;
import static org.iot.dsa.io.DSIReader.Token.NULL;

import com.acuity.iot.dsa.dslink.DSProtocolException;
import com.acuity.iot.dsa.dslink.DSRequesterSession;
import com.acuity.iot.dsa.dslink.DSResponderSession;
import com.acuity.iot.dsa.dslink.protocol.protocol_v1.requester.DS1RequesterSession;
import com.acuity.iot.dsa.dslink.protocol.protocol_v1.responder.DS1ResponderSession;
import java.io.IOException;
import org.iot.dsa.io.DSIReader;
import org.iot.dsa.io.DSIReader.Token;
import org.iot.dsa.io.DSIWriter;
import org.iot.dsa.node.DSMap;

/**
 * Implements DSA 1.1.2
 *
 * @author Aaron Hansen
 */
public class DS1Protocol extends DSProtocol {

    ///////////////////////////////////////////////////////////////////////////
    // Constants
    ///////////////////////////////////////////////////////////////////////////

    static final int MAX_MSG_ID = 2147483647;
    static final int MAX_MSG_IVL = 45000;

    ///////////////////////////////////////////////////////////////////////////
    // Fields
    ///////////////////////////////////////////////////////////////////////////

    private int nextAck = -1;
    private int nextMsg = 1;
    private DS1RequesterSession requesterSession = new DS1RequesterSession(this);
    private DS1ResponderSession responderSession = new DS1ResponderSession(this);

    /////////////////////////////////////////////////////////////////
    // Constructors
    /////////////////////////////////////////////////////////////////

    /////////////////////////////////////////////////////////////////
    // Methods
    /////////////////////////////////////////////////////////////////

    @Override
    protected void beginMessage() {
        DSIWriter out = getWriter();
        out.beginMap();
        if (++nextMsg > MAX_MSG_ID) {
            nextMsg = 1;
        }
        out.key("msg").value(nextMsg);
        synchronized (this) {
            if (nextAck > 0) {
                out.key("ack").value(nextAck);
                nextAck = -1;
            }
        }
    }

    @Override
    protected void beginResponses() {
        getWriter().key("responses").beginList();
    }

    @Override
    protected void beginRequests() {
        getWriter().key("requests").beginList();
    }

    @Override
    public void close() {
        responderSession.close();
        super.close();
    }

    @Override
    protected void doRun() throws IOException {
        DSIReader reader = getReader();
        while (isOpen()) {
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
                    throw new IllegalStateException("Unexpected input: " + reader.last());
            }
        }
    }

    @Override
    protected void endMessage() {
        getWriter().endMap().flush();
    }

    @Override
    protected void endResponses() {
        getWriter().endList();
    }

    @Override
    protected void endRequests() {
        getWriter().endList();
    }

    @Override
    public DSRequesterSession getRequesterSession() {
        return requesterSession;
    }

    @Override
    public DSResponderSession getResponderSession() {
        return responderSession;
    }

    private boolean hasPingToSend() {
        return (System.currentTimeMillis() - getLastMessageSent()) > MAX_MSG_IVL;
    }

    /**
     * Override point, returns true if there are any pending acks or outbound messages queued up.
     */
    protected boolean hasSomethingToSend() {
        if (nextAck > 0) {
            return true;
        }
        if (hasPingToSend()) {
            return true;
        }
        return super.hasSomethingToSend();
    }

    private String printMemory() { //TODO temporary
        Runtime r = Runtime.getRuntime();
        long ttl = r.totalMemory();
        long free = r.freeMemory();
        return ((ttl - free) / 1024) + "KB";
    }

    /**
     * Decomposes and processes a complete envelope which can contain multiple requests and
     * responses.
     *
     * @param reader lastRun() will return BEGIN_MAP
     */
    protected void processEnvelope(DSIReader reader) {
        finest(finest() ? printMemory() : null);
        if (!requesterAllowed) {
            getConnection().setRequesterAllowed();
        }
        int msg = -1;
        Token next;
        switch (reader.next()) {
            case END_MAP:
                return;
            case KEY:
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
            } else if (key.equals("allowed")) {
                if (reader.next() != Token.BOOLEAN) {
                    throw new IllegalStateException("Allowed not a boolean");
                }
                if (reader.getBoolean()) {
                    config(config() ? "Requester allowed" : null);
                    getConnection().setRequesterAllowed();
                }
            } else if (key.equals("salt")) {
                if (reader.next() != Token.STRING) {
                    throw new IllegalStateException("Salt not a string");
                }
                config(config() ? "Next salt: " + reader.getString() : null);
                getConnection().updateSalt(reader.getString());
            }
            next = reader.next();
        } while (next != END_MAP);
        if (sendAck && (msg >= 0)) {
            sendAck(msg);
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
                finest(finest() ? "No request ID: " + req.toString() : null);
                throw new DSProtocolException("Response missing rid");
            }
            if (areRequests) {
                responderSession.processRequest(rid, req);
            } else {
                requesterSession.processResponse(rid, req);
            }

        }
        if (reader.last() != END_LIST) {
            throw new IllegalStateException("Unexpected input: " + reader.last());
        }
    }

    /**
     * We need to send an ack to the broker.
     */
    private synchronized void sendAck(int msg) {
        nextAck = msg;
        notifyOutgoing();
    }

    public boolean shouldEndMessage() {
        return getTransport().shouldEndMessage();
    }

    /////////////////////////////////////////////////////////////////
    // Inner Classes
    /////////////////////////////////////////////////////////////////

    /////////////////////////////////////////////////////////////////
    // Initialization
    /////////////////////////////////////////////////////////////////

}
