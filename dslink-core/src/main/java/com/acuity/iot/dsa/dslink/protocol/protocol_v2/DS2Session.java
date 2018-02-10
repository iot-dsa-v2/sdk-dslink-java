package com.acuity.iot.dsa.dslink.protocol.protocol_v2;

import com.acuity.iot.dsa.dslink.DSSession;
import com.acuity.iot.dsa.dslink.protocol.protocol_v1.requester.DS1Requester;
import com.acuity.iot.dsa.dslink.protocol.protocol_v2.responder.DS2Responder;
import com.acuity.iot.dsa.dslink.transport.DSBinaryTransport;
import java.io.IOException;
import org.iot.dsa.dslink.DSIRequester;
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

    static final int END_MSG_THRESHOLD = 48000;
    static final String LAST_ACK_RECV = "Last Ack Recv";
    static final String LAST_ACK_SENT = "Last Ack Sent";

    static final int MAX_MSG_ID = 2147483647;
    static final int MAX_MSG_IVL = 45000;

    ///////////////////////////////////////////////////////////////////////////
    // Fields
    ///////////////////////////////////////////////////////////////////////////

    private DSInfo lastAckRecv = getInfo(LAST_ACK_RECV);
    private DSInfo lastAckSent = getInfo(LAST_ACK_SENT);
    private long lastMessageSent;
    private DS2MessageReader messageReader;
    private int nextAck = -1;
    private int nextMsg = 1;
    private boolean requestsNext = false;
    private DS1Requester requester;// = new DS1Requester(this);
    private DS2Responder responder = null;//new DS2Responder(this); //todo

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
        declareDefault(LAST_ACK_SENT, DSInt.NULL).setReadOnly(true);
    }

    @Override
    protected void doRecvMessage() throws IOException {
        if (messageReader == null) {
            messageReader = new DS2MessageReader();
        }
        messageReader.init(getTransport().getInput());
        int ack = messageReader.getAckId();
        if (ack > 0) {
            put(lastAckRecv, DSInt.valueOf(ack));
        }
        if (messageReader.isRequest()) {
            responder.handleRequest(messageReader);
        } else if (messageReader.isResponse()) {
            ;//requester.processResponse(messageReader);
        }
    }

    @Override
    protected void doSendMessage() {
        /*
        DSTransport transport = getTransport();
        long endTime = System.currentTimeMillis() + 2000;
        requestsNext = !requestsNext;
        transport.beginMessage();
        if (hasMessagesToSend()) {
            send(requestsNext, endTime);
            if ((System.currentTimeMillis() < endTime) && !shouldEndMessage()) {
                send(!requestsNext, endTime);
            }
        }
        transport.endMessage();
        lastMessageSent = System.currentTimeMillis();
        */
    }

    @Override
    public DS2LinkConnection getConnection() {
        return (DS2LinkConnection) super.getConnection();
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
     * We need to send an ack to the broker.
     */
    private synchronized void sendAck(int msg) {
        nextAck = msg;
        notifyOutgoing();
    }

    /**
     * Returns true if the current message size has crossed a message size threshold.
     */
    public boolean shouldEndMessage() {
        //TODO
        //return (messageWriter.length() + getTransport().messageSize()) > END_MSG_THRESHOLD;
        return getTransport().messageSize() > END_MSG_THRESHOLD;
    }

}
