package com.acuity.iot.dsa.dslink.protocol.responder;

import com.acuity.iot.dsa.dslink.DSSession;
import com.acuity.iot.dsa.dslink.protocol.DSStream;
import com.acuity.iot.dsa.dslink.protocol.message.OutboundMessage;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;
import org.iot.dsa.dslink.DSLink;
import org.iot.dsa.dslink.DSLinkConnection;
import org.iot.dsa.node.DSNode;

/**
 * Abstract responder implementation.
 *
 * @author Aaron Hansen
 */
public abstract class DSResponder extends DSNode {

    ///////////////////////////////////////////////////////////////////////////
    // Constants
    ///////////////////////////////////////////////////////////////////////////

    ///////////////////////////////////////////////////////////////////////////
    // Fields
    ///////////////////////////////////////////////////////////////////////////

    private DSLinkConnection connection;
    private ConcurrentHashMap<Integer, DSStream> inboundRequests = new ConcurrentHashMap<Integer, DSStream>();
    private DSLink link;
    private Logger logger;
    private DSSession session;
    private DSResponder responder;
    //private DS2InboundSubscriptions subscriptions = new DSInboundSubscriptions(this);

    /////////////////////////////////////////////////////////////////
    // Methods - Constructors
    /////////////////////////////////////////////////////////////////

    public DSResponder(DSSession session) {
        this.session = session;
    }

    /////////////////////////////////////////////////////////////////
    // Methods - In alphabetical order by method name.
    /////////////////////////////////////////////////////////////////

    public DSLinkConnection getConnection() {
        if (connection == null) {
            connection = session.getConnection();
        }
        return connection;
    }

    public DSLink getLink() {
        if (link == null) {
            link = getConnection().getLink();
        }
        return link;
    }

    @Override
    public Logger getLogger() {
        if (logger == null) {
            logger = Logger.getLogger(
                    getConnection().getLink().getLinkName() + ".responder");
        }
        return logger;
    }

    public Map<Integer, DSStream> getRequests() {
        return inboundRequests;
    }

    public DSSession getSession() {
        return session;
    }

    public void onConnect() {
    }

    public void onConnectFail() {
    }

    public void onDisconnect() {
    }

    public DSStream putRequest(Integer rid, DSStream request) {
        return inboundRequests.put(rid, request);
    }
    public DSStream removeRequest(Integer rid) {
        return inboundRequests.remove(rid);
    }

    public boolean shouldEndMessage() {
        return session.shouldEndMessage();
    }

    public void sendResponse(OutboundMessage res) {
        session.enqueueOutgoingResponse(res);
    }

}
