package com.acuity.iot.dsa.dslink.protocol.protocol_v1;

import static org.iot.dsa.io.DSIReader.Token.BEGIN_LIST;
import static org.iot.dsa.io.DSIReader.Token.BEGIN_MAP;
import static org.iot.dsa.io.DSIReader.Token.END_LIST;
import static org.iot.dsa.io.DSIReader.Token.END_MAP;
import static org.iot.dsa.io.DSIReader.Token.NULL;

import com.acuity.iot.dsa.dslink.DSProtocolException;
import com.acuity.iot.dsa.dslink.DSSession;
import com.acuity.iot.dsa.dslink.protocol.protocol_v1.requester.DS1Requester;
import com.acuity.iot.dsa.dslink.protocol.protocol_v1.responder.DS1Responder;
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

    static final String LAST_ACK_RECV = "Last Ack Recv";
    static final String LAST_ACK_SENT = "Last Ack Sent";

    static final int MAX_MSG_ID = 2147483647;
    static final int MAX_MSG_IVL = 45000;

    ///////////////////////////////////////////////////////////////////////////
    // Fields
    ///////////////////////////////////////////////////////////////////////////

    private DSInfo lastAckRecv = getInfo(LAST_ACK_RECV);
    private DSInfo lastAckSent = getInfo(LAST_ACK_SENT);
    private int nextAck = -1;
    private int nextMsg = 1;
    private DSIReader reader;
    private DS1Requester requester = new DS1Requester(this);
    private DS1Responder responder = new DS1Responder(this);
    private DSTransport transport;
    private DSIWriter writer;

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
                put(lastAckSent, DSInt.valueOf(nextAck));
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
    protected void declareDefaults() {
        declareDefault(LAST_ACK_RECV, DSInt.NULL).setReadOnly(true);
        declareDefault(LAST_ACK_SENT, DSInt.NULL).setReadOnly(true);
    }

    @Override
    protected void doRead() throws IOException {
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
    public DSIReader getReader() {
        return reader;
    }

    @Override
    public DSIRequester getRequester() {
        return requester;
    }

    @Override
    public DSTransport getTransport() {
        return transport;
    }

    @Override
    public DSIWriter getWriter() {
        return writer;
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
        transport = getConnection().getTransport();
        reader = getConnection().getReader();
        writer = getConnection().getWriter();
        requester.onConnect();
        responder.onConnect();
    }

    @Override
    public void onConnectFail() {
        requester.onConnectFail();
        responder.onConnectFail();
    }

    @Override
    public void onDisconnect() {
        super.onDisconnect();
        transport = null;
        reader = null;
        writer = null;
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
                responder.processRequest(rid, req);
            } else {
                requester.processResponse(rid, req);
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

}
