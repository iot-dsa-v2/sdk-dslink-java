package com.acuity.iot.dsa.dslink.protocol.v2;

import com.acuity.iot.dsa.dslink.protocol.DSSession;
import com.acuity.iot.dsa.dslink.protocol.message.OutboundMessage;
import com.acuity.iot.dsa.dslink.protocol.responder.DSResponder;
import com.acuity.iot.dsa.dslink.protocol.v2.requester.DS2Requester;
import com.acuity.iot.dsa.dslink.protocol.v2.responder.DS2Responder;
import java.util.HashMap;
import java.util.Map;
import org.iot.dsa.dslink.DSIRequester;
import org.iot.dsa.dslink.DSITransport;

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
    static final int MAX_MSG_IVL = 45000;

    ///////////////////////////////////////////////////////////////////////////
    // Instance Fields
    ///////////////////////////////////////////////////////////////////////////

    private long lastMessageSent;
    private DS2MessageReader messageReader;
    private DS2MessageWriter messageWriter;
    private Map<Integer, MultipartReader> multiparts = new HashMap<>();
    private DS2Requester requester = new DS2Requester(this);
    private boolean requestsNext = false;
    private DS2Responder responder = new DS2Responder(this);

    /////////////////////////////////////////////////////////////////
    // Constructors
    /////////////////////////////////////////////////////////////////

    public DS2Session() {
    }

    /////////////////////////////////////////////////////////////////
    // Public Methods
    /////////////////////////////////////////////////////////////////

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
        return getMessageWriter().getBodyLength() > END_MSG_THRESHOLD;
    }

    /////////////////////////////////////////////////////////////////
    // Protected Methods
    /////////////////////////////////////////////////////////////////

    @Override
    protected void doRecvMessage() {
        DSITransport transport = getTransport();
        DS2MessageReader reader = getMessageReader();
        transport.beginRecvMessage();
        reader.init(transport.getBinaryInput());
        int ack = reader.getAckId();
        setMidRcvd(reader.getRequestId());
        if (ack > 0) {
            setAckRcvd(ack);
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
        } else if (reader.isResponse()) {
            requester.handleResponse(reader);
            setAckToSend(reader.getRequestId());
        } else if (reader.isAck()) {
            ;
        } else if (reader.isPing()) {
            ;
        } else {
            error("Unknown method: " + reader.getMethod());
        }
        transport.endRecvMessage();
    }

    @Override
    protected void doSendMessage() {
        send(requestsNext = !requestsNext);  //alternate reqs and resps
    }

    @Override
    protected boolean hasPingToSend() {
        return (System.currentTimeMillis() - lastMessageSent) > MAX_MSG_IVL;
    }

    @Override
    protected void onConnected() {
        super.onConnected();
        setRequesterAllowed(true); //TODO - currently used for testing
        messageReader = null;
        messageWriter = null;
        requester.onConnected();
        responder.onConnected();
    }

    @Override
    protected void onDisconnected() {
        super.onDisconnected();
        requester.onDisconnected();
        responder.onDisconnected();
        messageReader = null;
        messageWriter = null;
        multiparts.clear();
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

    /**
     * Send messages from one of the queues.
     *
     * @param requests Determines which queue to use; True for outgoing requests, false for
     *                 responses.
     */
    private void send(boolean requests) {
        int count = 0;
        if (!waitingForAcks()) {
            if (requests) {
                count = numOutgoingRequests();
            } else {
                count = numOutgoingResponses();
            }
        }
        OutboundMessage msg = null;
        if (count > 0) {
            while (msg == null) {
                if (--count < 0) {
                    break;
                }
                msg = requests ? dequeueOutgoingRequest() : dequeueOutgoingResponse();
                if (!msg.canWrite(this)) {
                    if (requests) {
                        requeueOutgoingRequest(msg);
                    } else {
                        requeueOutgoingResponse(msg);
                    }
                    msg = null;
                }
            }
            setAckRequired();
        } else if (hasPingToSend()) {
            msg = new PingMessage(this);
        } else if (hasAckToSend()) {
            msg = new AckMessage(this);
        }
        if (msg != null) {
            DSITransport transport = getTransport();
            transport.beginSendMessage();
            msg.write(this, getMessageWriter());
            transport.endSendMessage();
            lastMessageSent = System.currentTimeMillis();
        }
    }

}
