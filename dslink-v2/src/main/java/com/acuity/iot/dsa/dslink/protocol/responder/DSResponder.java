package com.acuity.iot.dsa.dslink.protocol.responder;

import com.acuity.iot.dsa.dslink.protocol.DSSession;
import com.acuity.iot.dsa.dslink.protocol.DSStream;
import com.acuity.iot.dsa.dslink.protocol.message.OutboundMessage;
import com.acuity.iot.dsa.dslink.transport.DSTransport;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
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
    // Instance Fields
    ///////////////////////////////////////////////////////////////////////////

    private DSLinkConnection connection;
    private ConcurrentHashMap<Integer, DSStream> inboundRequests = new ConcurrentHashMap<Integer, DSStream>();
    private DSLink link;
    private DSSession session;

    /////////////////////////////////////////////////////////////////
    // Constructors
    /////////////////////////////////////////////////////////////////

    public DSResponder() {
    }

    public DSResponder(DSSession session) {
        this.session = session;
    }

    /////////////////////////////////////////////////////////////////
    // Public Methods
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

    public Map<Integer, DSStream> getRequests() {
        return inboundRequests;
    }

    public DSSession getSession() {
        return session;
    }

    public DSTransport getTransport() {
        return getConnection().getTransport();
    }

    /**
     * V2 override point, default returns true.
     */
    public boolean isV1() {
        return true;
    }

    public void onConnected() {
        getSubscriptions().onConnected();
    }

    public void onDisconnected() {
        getSubscriptions().onDisconnected();
        Iterator<Entry<Integer, DSStream>> it = inboundRequests.entrySet().iterator();
        Map.Entry<Integer, DSStream> me;
        while (it.hasNext()) {
            me = it.next();
            try {
                me.getValue().onClose(me.getKey());
            } catch (Exception x) {
                error(getPath(), x);
            }
            it.remove();
        }
    }

    public DSStream removeRequest(Integer rid) {
        return inboundRequests.remove(rid);
    }

    public abstract void sendClose(int rid);

    public abstract void sendError(DSInboundRequest req, Throwable reason);

    public void sendResponse(OutboundMessage res) {
        session.enqueueOutgoingResponse(res);
    }

    public boolean shouldEndMessage() {
        return session.shouldEndMessage();
    }

    /////////////////////////////////////////////////////////////////
    // Protected Methods
    /////////////////////////////////////////////////////////////////

    @Override
    protected String getLogName() {
        return getLogName("responder");
    }

    protected abstract DSInboundSubscriptions getSubscriptions();

    protected DSStream putRequest(Integer rid, DSStream request) {
        return inboundRequests.put(rid, request);
    }

}
