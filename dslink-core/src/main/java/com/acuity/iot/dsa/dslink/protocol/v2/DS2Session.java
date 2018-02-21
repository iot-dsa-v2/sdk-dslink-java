package com.acuity.iot.dsa.dslink.protocol.v2;

import com.acuity.iot.dsa.dslink.DSSession;
import com.acuity.iot.dsa.dslink.protocol.message.OutboundMessage;
import com.acuity.iot.dsa.dslink.protocol.v1.requester.DS1Requester;
import com.acuity.iot.dsa.dslink.protocol.v2.responder.DS2Responder;
import com.acuity.iot.dsa.dslink.transport.DSBinaryTransport;
import com.acuity.iot.dsa.dslink.transport.DSTransport;
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
    // Constants
    ///////////////////////////////////////////////////////////////////////////

    static final int END_MSG_THRESHOLD = 48 * 1024;
    static final String LAST_ACK_RECV = "Last Ack Recv";
    static final int MAX_MSG_IVL = 45000;

    ///////////////////////////////////////////////////////////////////////////
    // Fields
    ///////////////////////////////////////////////////////////////////////////

    private boolean debugRecv = false;
    private StringBuilder debugRecvTranport = null;
    private StringBuilder debugRecvMessage = null;
    private boolean debugSend = false;
    private StringBuilder debugSendTranport = null;
    private StringBuilder debugSendMessage = null;
    private DSInfo lastAckRecv = getInfo(LAST_ACK_RECV);
    private long lastMessageSent;
    private DS2MessageReader messageReader;
    private DS2MessageWriter messageWriter;
    private boolean requestsNext = false;
    private DSIRequester requester = null;//new DS2Requester(this);
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
    // Methods
    /////////////////////////////////////////////////////////////////

    @Override
    protected void declareDefaults() {
        declareDefault(LAST_ACK_RECV, DSInt.NULL).setReadOnly(true);
    }

    @Override
    protected void doRecvMessage() {
        DSBinaryTransport transport = getTransport();
        DS2MessageReader reader = getMessageReader();
        boolean debug = debug();
        if (debug != debugRecv) {
            if (debug) {
                debugRecvMessage = new StringBuilder();
                debugRecvTranport = new StringBuilder();
                reader.setDebug(debugRecvMessage);
                transport.setDebugIn(debugRecvTranport);
            } else {
                debugRecvMessage = null;
                debugRecvTranport = null;
                reader.setDebug(null);
                transport.setDebugIn(null);
            }
            debugRecv = debug;
        }
        if (debug) {
            debugRecvMessage.setLength(0);
            debugRecvTranport.setLength(0);
            debugRecvTranport.append("Bytes read\n");
        }
        transport.beginRecvMessage();
        reader.init(transport.getInput());
        int ack = reader.getAckId();
        if (ack > 0) {
            put(lastAckRecv, DSInt.valueOf(ack));
        }
        if (reader.isRequest()) {
            responder.handleRequest(reader);
            setNextAck(reader.getRequestId());
        } else if (reader.isAck()) {
            put(lastAckRecv, DSInt.valueOf(DSBytes.readInt(reader.getBody(), false)));
        } else if (reader.isPing()) {
        } else if (reader.isResponse()) {
            //requester.handleResponse(reader);
            setNextAck(reader.getRequestId());
        }
        if (debug) {
            debug(debugRecvMessage);
            debug(debugRecvTranport);
            debugRecvMessage.setLength(0);
            debugRecvTranport.setLength(0);
        }
    }

    @Override
    protected void doSendMessage() {
        DSTransport transport = getTransport();
        boolean debug = debug();
        if (debug != debugSend) {
            if (debug) {
                debugSendMessage = new StringBuilder();
                debugSendTranport = new StringBuilder();
                getMessageWriter().setDebug(debugSendMessage);
                transport.setDebugOut(debugSendTranport);
            } else {
                debugSendMessage = null;
                debugSendTranport = null;
                getMessageWriter().setDebug(null);
                transport.setDebugOut(null);
            }
            debugSend = debug;
        }
        if (debug) {
            debugSendMessage.setLength(0);
            debugSendTranport.setLength(0);
            debugSendTranport.append("Bytes sent\n");
        }
        if (this.hasSomethingToSend()) {
            transport.beginSendMessage();
            boolean sent = send(requestsNext != requestsNext);  //alternate reqs and resps
            if (sent && debug) {
                debug(debugSendMessage);
                debug(debugSendTranport);
                debugSendMessage.setLength(0);
                debugSendTranport.setLength(0);
            }
            transport.endSendMessage();
        }
    }

    @Override
    public DS2LinkConnection getConnection() {
        return (DS2LinkConnection) super.getConnection();
    }

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

    @Override
    public DSIRequester getRequester() {
        return requester;
    }

    public DSBinaryTransport getTransport() {
        return (DSBinaryTransport) super.getTransport();
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
        messageReader = null;
        messageWriter = null;
        if (debugRecv) {
            debugRecvMessage = new StringBuilder();
            debugRecvTranport = new StringBuilder();
            getMessageReader().setDebug(debugRecvMessage);
            getTransport().setDebugIn(debugRecvTranport);
        }
        if (debugSend) {
            debugSendMessage = new StringBuilder();
            debugSendTranport = new StringBuilder();
            getMessageWriter().setDebug(debugSendMessage);
            getTransport().setDebugOut(debugSendTranport);
        }
        //requester.onConnect();
        responder.onConnect();
    }

    @Override
    public void onConnectFail() {
        super.onConnectFail();
        //requester.onConnectFail();
        responder.onConnectFail();
    }

    @Override
    public void onDisconnect() {
        super.onDisconnect();
        //requester.onDisconnect();
        responder.onDisconnect();
    }

    /**
     * Send messages from one of the queues.
     *
     * @param requests Determines which queue to use; True for outgoing requests, false for
     *                 responses.
     */
    private boolean send(boolean requests) {
        boolean hasSomething = false;
        if (requests) {
            hasSomething = hasOutgoingRequests();
        } else {
            hasSomething = hasOutgoingResponses();
        }
        OutboundMessage msg = null;
        if (hasSomething) {
            msg = requests ? dequeueOutgoingRequest() : dequeueOutgoingResponse();
        } else if (hasPingToSend()) {
            msg = new PingMessage(this);
        } else if (hasAckToSend()) {
            msg = new AckMessage(this);
        }
        if (msg != null) {
            lastMessageSent = System.currentTimeMillis();
            msg.write(getMessageWriter());
            return true;
        }
        return false;
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

}
