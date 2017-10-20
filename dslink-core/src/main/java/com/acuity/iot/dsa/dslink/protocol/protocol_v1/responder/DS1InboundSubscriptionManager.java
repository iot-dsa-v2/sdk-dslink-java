package com.acuity.iot.dsa.dslink.protocol.protocol_v1.responder;

import com.acuity.iot.dsa.dslink.protocol.message.OutboundMessage;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Logger;
import org.iot.dsa.dslink.DSIResponder;
import org.iot.dsa.io.DSIWriter;
import org.iot.dsa.logging.DSLogger;

/**
 * Subscribe implementation for the responder.
 *
 * @author Aaron Hansen
 */
class DS1InboundSubscriptionManager extends DSLogger implements OutboundMessage {

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
    private DS1Responder session;
    private Map<Integer, DS1InboundSubscription> sidMap =
            new ConcurrentHashMap<Integer, DS1InboundSubscription>();
    private StringBuilder timestampBuffer = new StringBuilder();

    ///////////////////////////////////////////////////////////////////////////
    // Constructors
    ///////////////////////////////////////////////////////////////////////////

    DS1InboundSubscriptionManager(DS1Responder session) {
        this.session = session;
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
        session.sendResponse(this);
    }

    @Override
    public Logger getLogger() {
        if (logger == null) {
            logger = Logger.getLogger(session.getConnection().getLink().getLinkName()
                                                 + ".responderSubscriptions");
        }
        return logger;
    }

    /**
     * Create or update a subscription.
     */
    void subscribe(DSIResponder responder, Integer sid, String path, int qos) {
        finest(finest() ? "Subscribing " + path : null);
        DS1InboundSubscription subscription = sidMap.get(sid);
        if (subscription == null) {
            subscription = new DS1InboundSubscription(this);
            subscription.setSubscriptionId(sid).setQos(qos).setPath(path);
            sidMap.put(sid, subscription);
            pathMap.put(path, subscription);
            subscription.setCloseHandler(responder.onSubscribe(subscription));
        } else {
            subscription.setQos(qos).setPath(path);
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
    public void write(DSIWriter out) {
        out.beginMap();
        out.key("rid").value(ZERO);
        out.key("updates").beginList();
        DS1InboundSubscription sub;
        while (!session.shouldEndMessage()) {
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
        session.sendResponse(this);
    }

    ///////////////////////////////////////////////////////////////////////////
    // Inner Classes
    ///////////////////////////////////////////////////////////////////////////

    ///////////////////////////////////////////////////////////////////////////
    // Initialization
    ///////////////////////////////////////////////////////////////////////////

}
