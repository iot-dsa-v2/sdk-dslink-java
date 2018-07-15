package com.acuity.iot.dsa.dslink.protocol.v2;

import com.acuity.iot.dsa.dslink.protocol.DSSession;
import com.acuity.iot.dsa.dslink.protocol.message.OutboundMessage;
import com.acuity.iot.dsa.dslink.protocol.responder.DSResponder;
import com.acuity.iot.dsa.dslink.protocol.v2.requester.DS2Requester;
import com.acuity.iot.dsa.dslink.protocol.v2.responder.DS2Responder;
import com.acuity.iot.dsa.dslink.transport.DSBinaryTransport;
import com.acuity.iot.dsa.dslink.transport.DSTransport;
import java.util.HashMap;
import java.util.Map;
import org.iot.dsa.dslink.DSIRequester;
import org.iot.dsa.node.DSBytes;
import org.iot.dsa.node.DSInfo;
import org.iot.dsa.node.DSInt;

/**
 * Implements DSA 1.1.2
 *
 * @author Aaron Hansen
 */
public class DS2Session extends DSSession implements MessageConstants {

    ///////////////////////////////////////////////////////////////////////////
    // Class Fields
    ///////////////////////////////////////////////////////////////////////////

    static final int END_MSG_THRESHOLD = 48 * 1024;
    static final String LAST_ACK_RCVD = "Last Ack Rcvd";
    static final int MAX_MSG_IVL = 45000;

    ///////////////////////////////////////////////////////////////////////////
    // Instance Fields
    ///////////////////////////////////////////////////////////////////////////

    private DSInfo lastAckRcvd = getInfo(LAST_ACK_RCVD);
    private long lastMessageSent;
    private DS2MessageReader messageReader;
    private DS2MessageWriter messageWriter;
    private Map<Integer, MultipartReader> multiparts = new HashMap<Integer, MultipartReader>();
    private DS2Requester requester = new DS2Requester(this);
    private boolean requestsNext = false;
    private DS2Responder responder = new DS2Responder(this);

    /////////////////////////////////////////////////////////////////
    // Constructors
    /////////////////////////////////////////////////////////////////

    public DS2Session() {
    }

    public DS2Session(DS2LinkConnection connection) {
        super(connection);
    }

    /////////////////////////////////////////////////////////////////
    // Public Methods
    /////////////////////////////////////////////////////////////////

    @Override
    public DS2LinkConnection getConnection() {
        return (DS2LinkConnection) super.getConnection();
    }

    @Override
    public DSIRequester getRequester() {
        return requester;
    }

    @Override
    public DSResponder getResponder() {
        return responder;
    }

    public DSBinaryTransport getTransport() {
        return (DSBinaryTransport) super.getTransport();
    }

    @Override
    public void onConnect() {
        super.onConnect();
        messageReader = null;
        messageWriter = null;
        requester.onConnect();
        responder.onConnect();
    }

    @Override
    public void onConnectFail() {
        super.onConnectFail();
        messageReader = null;
        messageWriter = null;
        multiparts.clear();
        requester.onConnectFail();
        responder.onConnectFail();
    }

    @Override
    public void onDisconnect() {
        super.onDisconnect();
        messageReader = null;
        messageWriter = null;
        multiparts.clear();
        requester.onDisconnect();
        responder.onDisconnect();
    }

    /**
     * Returns true if the current message size has crossed a message size threshold.
     */
    public boolean shouldEndMessage() {
        if (getMessageWriter().getBodyLength() > END_MSG_THRESHOLD) {
            return true;
        }
        return (System.currentTimeMillis() - lastMessageSent) > 1000;
    }

    /////////////////////////////////////////////////////////////////
    // Protected Methods
    /////////////////////////////////////////////////////////////////

    @Override
    protected void declareDefaults() {
        declareDefault(LAST_ACK_RCVD, DSInt.NULL).setReadOnly(true);
    }

    @Override
    protected void doRecvMessage() {
        DSBinaryTransport transport = getTransport();
        DS2MessageReader reader = getMessageReader();
        transport.beginRecvMessage();
        reader.init(transport.getInput());
        int ack = reader.getAckId();
        if (ack > 0) {
            setAckRcvd(ack);
            put(lastAckRcvd, DSInt.valueOf(ack));
        }
        if (reader.isMultipart()) {
            MultipartReader multi = multiparts.get(reader.getRequestId());
            if (multi == null) {
                multi = new MultipartReader(reader);
                multiparts.put(reader.getRequestId(), multi);
                return;
            } else if (multi.update(reader)) {
                return;
            }
            multiparts.remove(reader.getRequestId());
            reader = multi.makeReader();
        }
        if (reader.isRequest()) {
            responder.handleRequest(reader);
            setAckToSend(reader.getRequestId());
        } else if (reader.isAck()) {
            put(lastAckRcvd, DSInt.valueOf(DSBytes.readInt(reader.getBody(), false)));
        } else if (reader.isPing()) {
            ;
        } else if (reader.isResponse()) {
            requester.handleResponse(reader);
            setAckToSend(reader.getRequestId());
        } else {
            error("Unknown method: " + reader.getMethod());
        }
        transport.endRecvMessage();
    }

    @Override
    protected void doSendMessage() {
        send(requestsNext = !requestsNext);  //alternate reqs and resps
    }

    /*
    @Override
    protected void onStable() {
        put("Requester Session", requesterSession);
        put("Responder Session", responderSession);
    }
    */

    /**
     * Override point, returns true if there are any pending acks or outbound messages queued up.
     */
    protected boolean hasSomethingToSend() {
        if (hasPingToSend()) {
            return true;
        }
        return super.hasSomethingToSend();
    }

    /////////////////////////////////////////////////////////////////
    // Package / Private Methods
    /////////////////////////////////////////////////////////////////

    private DS2MessageReader getMessageReader() {
        if (messageReader == null) {
            messageReader = new DS2MessageReader();
        }
        return messageReader;
    }

    private DS2MessageWriter getMessageWriter() {
        if (messageWriter == null) {
            messageWriter = new DS2MessageWriter();
        }
        return messageWriter;
    }

    private boolean hasPingToSend() {
        return (System.currentTimeMillis() - lastMessageSent) > MAX_MSG_IVL;
    }

    /**
     * Send messages from one of the queues.
     *
     * @param requests Determines which queue to use; True for outgoing requests, false for
     *                 responses.
     */
    private void send(boolean requests) {
        boolean hasSomething = false;
        if (!waitingForAcks()) {
            if (requests) {
                hasSomething = hasOutgoingRequests();
            } else {
                hasSomething = hasOutgoingResponses();
            }
        }
        OutboundMessage msg = null;
        if (hasSomething) {
            if (requests) {
                msg = dequeueOutgoingRequest();
            } else {
                msg = dequeueOutgoingResponse();
            }
        } else if (hasPingToSend()) {
            msg = new PingMessage(this);
        } else if (hasAckToSend()) {
            msg = new AckMessage(this);
        }
        if (msg != null) {
            DSTransport transport = getTransport();
            transport.beginSendMessage();
            msg.write(this, getMessageWriter());
            transport.endSendMessage();
            lastMessageSent = System.currentTimeMillis();
        }
    }

}
