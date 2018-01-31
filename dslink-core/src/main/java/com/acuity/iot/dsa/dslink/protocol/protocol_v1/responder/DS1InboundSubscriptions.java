package com.acuity.iot.dsa.dslink.protocol.protocol_v1.responder;

import com.acuity.iot.dsa.dslink.protocol.message.MessageWriter;
import com.acuity.iot.dsa.dslink.protocol.message.OutboundMessage;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Logger;
import org.iot.dsa.dslink.DSLink;
import org.iot.dsa.io.DSIWriter;
import org.iot.dsa.logging.DSLogger;

/**
 * Subscribe implementation for the responder.
 *
 * @author Aaron Hansen
 */
class DS1InboundSubscriptions extends DSLogger implements OutboundMessage {

    ///////////////////////////////////////////////////////////////////////////
    // Constants
    ///////////////////////////////////////////////////////////////////////////

    private static final Integer ZERO = Integer.valueOf(0);

    ///////////////////////////////////////////////////////////////////////////
    // Fields
    ///////////////////////////////////////////////////////////////////////////

    private boolean enqueued = false;
    private Logger logger;
    private ConcurrentLinkedQueue<DS1InboundSubscription> outbound =
            new ConcurrentLinkedQueue<DS1InboundSubscription>();
    private Map<String, DS1InboundSubscription> pathMap =
            new ConcurrentHashMap<String, DS1InboundSubscription>();
    private DS1Responder responder;
    private Map<Integer, DS1InboundSubscription> sidMap =
            new ConcurrentHashMap<Integer, DS1InboundSubscription>();
    private StringBuilder timestampBuffer = new StringBuilder();

    ///////////////////////////////////////////////////////////////////////////
    // Constructors
    ///////////////////////////////////////////////////////////////////////////

    DS1InboundSubscriptions(DS1Responder responder) {
        this.responder = responder;
    }

    ///////////////////////////////////////////////////////////////////////////
    // Methods in alphabetical order
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Unsubscribes all.
     */
    void close() {
        for (Integer i : sidMap.keySet()) {
            unsubscribe(i);
        }
    }

    /**
     * Add to the outbound queue if not already enqueued.
     */
    void enqueue(DS1InboundSubscription subscription) {
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

    @Override
    public Logger getLogger() {
        if (logger == null) {
            logger = Logger.getLogger(responder.getConnection().getLink().getLinkName()
                                              + ".responderSubscriptions");
        }
        return logger;
    }

    /**
     * Create or update a subscription.
     */
    void subscribe(Integer sid, String path, int qos) {
        finest(finest() ? "Subscribing " + path : null);
        DS1InboundSubscription subscription = sidMap.get(sid);
        if (subscription == null) {
            subscription = new DS1InboundSubscription(this, sid, path, qos);
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
    void unsubscribe(Integer sid) {
        DS1InboundSubscription subscription = sidMap.remove(sid);
        if (subscription != null) {
            finest(finest() ? "Unsubscribing " + subscription.getPath() : null);
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
        DSIWriter out = writer.getWriter();
        out.beginMap();
        out.key("rid").value(ZERO);
        out.key("updates").beginList();
        DS1InboundSubscription sub;
        while (!responder.shouldEndMessage()) {
            sub = outbound.poll();
            if (sub == null) {
                break;
            }
            sub.write(out, timestampBuffer);
        }
        out.endList();
        out.endMap();
        timestampBuffer.setLength(0);
        synchronized (this) {
            if (outbound.isEmpty()) {
                enqueued = false;
                return;
            }
        }
        responder.sendResponse(this);
    }

}
