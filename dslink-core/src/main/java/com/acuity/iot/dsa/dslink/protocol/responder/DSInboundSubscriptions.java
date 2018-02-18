package com.acuity.iot.dsa.dslink.protocol.responder;

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
    // Constants
    ///////////////////////////////////////////////////////////////////////////

    private static final Integer ZERO = Integer.valueOf(0);

    ///////////////////////////////////////////////////////////////////////////
    // Fields
    ///////////////////////////////////////////////////////////////////////////

    private boolean enqueued = false;
    private ConcurrentLinkedQueue<DSInboundSubscription> outbound =
            new ConcurrentLinkedQueue<DSInboundSubscription>();
    private Map<String, DSInboundSubscription> pathMap =
            new ConcurrentHashMap<String, DSInboundSubscription>();
    private DSResponder responder;
    private Map<Integer, DSInboundSubscription> sidMap =
            new ConcurrentHashMap<Integer, DSInboundSubscription>();
    private StringBuilder timestampBuffer = new StringBuilder();

    ///////////////////////////////////////////////////////////////////////////
    // Constructors
    ///////////////////////////////////////////////////////////////////////////

    public DSInboundSubscriptions(DSResponder responder) {
        this.responder = responder;
    }

    ///////////////////////////////////////////////////////////////////////////
    // Methods in alphabetical order
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Unsubscribes all.
     */
    public void close() {
        for (Integer i : sidMap.keySet()) {
            unsubscribe(i);
        }
    }

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

    DSLink getLink() {
        return responder.getConnection().getLink();
    }

    /**
     * This returns a DSInboundSubscription for v1, this will be overridden for v2.
     *
     * @param sid  Subscription ID.
     * @param path Path being subscribed to.
     * @param qos  Qualityf of service.
     */
    protected DSInboundSubscription makeSubscription(Integer sid, String path, int qos) {
        return new DSInboundSubscription(this, sid, path, qos);
    }

    /**
     * Create or update a subscription.
     */
    public void subscribe(Integer sid, String path, int qos) {
        trace(trace() ? "Subscribing " + path : null);
        DSInboundSubscription subscription = sidMap.get(sid);
        if (subscription == null) {
            subscription = makeSubscription(sid, path, qos);
            sidMap.put(sid, subscription);
            pathMap.put(path, subscription);
        } else if (!path.equals(subscription.getPath())) {
            unsubscribe(sid);
            subscribe(sid, path, qos);
        } else {
            subscription.setQos(qos);
            //TODO refresh subscription, align w/v2
        }
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
                warn(warn() ? subscription.toString() : null, x);
            }
        }
    }

    @Override
    public void write(MessageWriter writer) {
        writeBegin(writer);
        DSInboundSubscription sub;
        while (!responder.shouldEndMessage()) {
            sub = outbound.poll();
            if (sub == null) {
                break;
            }
            sub.write(writer, timestampBuffer);
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

}
