package com.acuity.iot.dsa.dslink.protocol.responder;

import com.acuity.iot.dsa.dslink.protocol.DSSession;
import com.acuity.iot.dsa.dslink.protocol.message.MessageWriter;
import com.acuity.iot.dsa.dslink.protocol.message.OutboundMessage;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import org.iot.dsa.dslink.DSLink;
import org.iot.dsa.node.DSNode;

/**
 * Subscribe implementation for the responder.
 *
 * @author Aaron Hansen
 */
public class DSInboundSubscriptions extends DSNode implements OutboundMessage {

    ///////////////////////////////////////////////////////////////////////////
    // Class Fields
    ///////////////////////////////////////////////////////////////////////////

    private static final Integer ZERO = Integer.valueOf(0);

    ///////////////////////////////////////////////////////////////////////////
    // Instance Fields
    ///////////////////////////////////////////////////////////////////////////

    private boolean enqueued = false;
    private ConcurrentLinkedQueue<DSInboundSubscription> outbound =
            new ConcurrentLinkedQueue<DSInboundSubscription>();
    private Map<String, DSInboundSubscription> pathMap =
            new ConcurrentHashMap<String, DSInboundSubscription>();
    private DSResponder responder;
    private Map<Integer, DSInboundSubscription> sidMap =
            new ConcurrentHashMap<Integer, DSInboundSubscription>();
    private StringBuilder timestampBuffer = new StringBuilder();//used by the subs

    ///////////////////////////////////////////////////////////////////////////
    // Constructors
    ///////////////////////////////////////////////////////////////////////////

    public DSInboundSubscriptions(DSResponder responder) {
        this.responder = responder;
    }

    ///////////////////////////////////////////////////////////////////////////
    // Public Methods
    ///////////////////////////////////////////////////////////////////////////

    @Override
    public boolean canWrite(DSSession session) {
        if (outbound.size() == 1) {
            return outbound.peek().canWrite(session);
        }
        if (outbound.isEmpty()) {
            return false;
        }
        for (DSInboundSubscription sub : outbound) {
            if (sub.canWrite(session)) {
                return true;
            }
        }
        return false;
    }

    public DSResponder getResponder() {
        return responder;
    }

    /**
     * Create or update a subscription.
     */
    public DSInboundSubscription subscribe(Integer sid, String path, int qos) {
        trace(trace() ? "Subscribing " + path : null);
        DSInboundSubscription subscription = sidMap.get(sid);
        if (subscription == null) {
            subscription = makeSubscription(sid, path, qos);
            sidMap.put(sid, subscription);
            pathMap.put(path, subscription);
        } else if (!path.equals(subscription.getPath())) {
            unsubscribe(sid);
            return subscribe(sid, path, qos);
        } else {
            subscription.setQos(qos);
            //TODO refresh subscription, align w/v2
        }
        return subscription;
    }

    /**
     * Remove the subscription and call onClose.
     */
    public void unsubscribe(Integer sid) {
        DSInboundSubscription subscription = sidMap.remove(sid);
        if (subscription != null) {
            trace(trace() ? "Unsubscribing " + subscription.getPath() : null);
            pathMap.remove(subscription.getPath());
            try {
                subscription.onClose();
            } catch (Exception x) {
                debug(debug() ? subscription.toString() : null, x);
            }
        }
    }

    @Override
    public void write(DSSession session, MessageWriter writer) {
        writeBegin(writer);
        DSInboundSubscription sub;
        while (!responder.shouldEndMessage()) {
            sub = outbound.poll();
            if (sub == null) {
                break;
            }
            sub.write(session, writer, timestampBuffer);
            if (sub.isCloseAfterUpdate()) {
                unsubscribe(sub.getSubscriptionId());
            }
        }
        writeEnd(writer);
        synchronized (this) {
            if (outbound.isEmpty()) {
                enqueued = false;
                return;
            }
        }
        responder.sendResponse(this);
    }

    ///////////////////////////////////////////////////////////////////////////
    // Protected Methods
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Add to the outbound queue if not already enqueued.
     */
    protected void enqueue(DSInboundSubscription subscription) {
        synchronized (this) {
            outbound.add(subscription);
            if (enqueued) {
                return;
            }
            enqueued = true;
        }
        responder.sendResponse(this);
    }

    @Override
    protected String getLogName() {
        String pre = "";
        if (responder != null) {
            pre = responder.getLogName();
        }
        return pre + "." + getClass().getSimpleName();
    }

    /**
     * Returns a DSInboundSubscription for v1.
     *
     * @param sid  Subscription ID.
     * @param path Path being subscribed to.
     * @param qos  Quality of service.
     */
    protected DSInboundSubscription makeSubscription(Integer sid, String path, int qos) {
        return new DSInboundSubscription(this, sid, path, qos);
    }

    protected void onConnected() {
    }

    /**
     * Unsubscribes all.
     */
    protected void onDisconnected() {
        for (Integer i : sidMap.keySet()) {
            unsubscribe(i);
        }
    }

    /**
     * Override point for v2.
     */
    protected void writeBegin(MessageWriter writer) {
        writer.getWriter()
              .beginMap()
              .key("rid").value(ZERO)
              .key("updates").beginList();
    }

    /**
     * Override point for v2.
     */
    protected void writeEnd(MessageWriter writer) {
        writer.getWriter()
              .endList()
              .endMap();
    }

    ///////////////////////////////////////////////////////////////////////////
    // Package / Private Methods
    ///////////////////////////////////////////////////////////////////////////

    DSLink getLink() {
        return responder.getConnection().getLink();
    }

}
