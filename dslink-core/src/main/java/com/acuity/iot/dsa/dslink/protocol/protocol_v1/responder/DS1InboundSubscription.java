package com.acuity.iot.dsa.dslink.protocol.protocol_v1.responder;

import com.acuity.iot.dsa.dslink.DSResponderSession;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.iot.dsa.dslink.responder.InboundSubscribeRequest;
import org.iot.dsa.dslink.responder.SubscriptionCloseHandler;
import org.iot.dsa.io.DSIWriter;
import org.iot.dsa.node.DSElement;
import org.iot.dsa.node.DSIValue;
import org.iot.dsa.node.DSQuality;
import org.iot.dsa.time.DSTime;

/**
 * Subscribe implementation for the responder.
 *
 * @author Aaron Hansen
 */
class DS1InboundSubscription extends DS1InboundRequest implements InboundSubscribeRequest {

    ///////////////////////////////////////////////////////////////////////////
    // Constants
    ///////////////////////////////////////////////////////////////////////////

    ///////////////////////////////////////////////////////////////////////////
    // Fields
    ///////////////////////////////////////////////////////////////////////////

    private SubscriptionCloseHandler closeHandler;
    private boolean enqueued = false;
    private DS1InboundSubscriptionManager manager;
    private boolean open = true;
    private Integer sid;
    private int qos = 0;
    private Update updateHead;
    private Update updateTail;

    ///////////////////////////////////////////////////////////////////////////
    // Constructors
    ///////////////////////////////////////////////////////////////////////////

    DS1InboundSubscription(DS1InboundSubscriptionManager manager) {
        this.manager = manager;
    }

    ///////////////////////////////////////////////////////////////////////////
    // Methods in alphabetical order
    ///////////////////////////////////////////////////////////////////////////

    @Override
    public void close() {
        //TODO update(System.currentTimeMillis(), DSElement.makeNull, 0); //need unknown status
        manager.unsubscribe(sid);
    }

    /**
     * Remove an update from the queue.
     */
    private synchronized Update dequeue() {
        if (updateHead == null) {
            return null;
        }
        Update ret = null;
        ret = updateHead;
        if (updateHead == updateTail) {
            updateHead = updateTail = null;
        } else {
            updateHead = updateHead.next;
        }
        ret.next = null;
        return ret;
    }

    @Override
    public Logger getLogger() {
        return manager.getLogger();
    }

    /**
     * Unique subscription id for this path.
     */
    @Override
    public Integer getSubscriptionId() {
        return sid;
    }

    public void onClose() {
        synchronized (this) {
            if (!open) {
                return;
            }
            open = false;
        }
        try {
            if (closeHandler != null) {
                closeHandler.onClose(getSubscriptionId());
            }
        } catch (Exception x) {
            getLogger().log(Level.WARNING, toString(), x);
        }
    }

    /**
     * The responder should call this whenever the value or status changes.
     */
    @Override
    public void update(long timestamp, DSIValue value, DSQuality quality) {
        if (!open) {
            return;
        }
        finest(finest() ? "Update " + getPath() + " to " + value : null);
        if (qos <= 1) {
            synchronized (this) {
                if (updateHead == null) {
                    updateHead = updateTail = new Update();
                }
                updateHead.set(timestamp, value, quality);
                if (enqueued) {
                    return;
                }
                enqueued = true;
            }
        } else {
            Update update = new Update().set(timestamp, value, quality);
            synchronized (this) {
                if (updateHead == null) {
                    updateHead = updateTail = update;
                } else {
                    updateTail.next = update;
                }
                if (enqueued) {
                    return;
                }
                enqueued = true;
            }
        }
        manager.enqueue(this);
    }

    DS1InboundSubscription setCloseHandler(SubscriptionCloseHandler closeHandler) {
        this.closeHandler = closeHandler;
        return this;
    }

    DS1InboundSubscription setQos(int qos) {
        this.qos = qos;
        return this;
    }

    DS1InboundSubscription setSubscriptionId(Integer sid) {
        this.sid = sid;
        return this;
    }

    @Override
    public String toString() {
        return "Subscription (" + getSubscriptionId() + ") " + getPath();
    }

    /**
     * Encodes as many updates as possible.
     *
     * @param out Where to encode.
     * @param buf For encoding timestamps.
     */
    void write(DSIWriter out, StringBuilder buf) {
        //Don't check open state - forcefully closing will send an update
        DSResponderSession session = getSession();
        Update update = dequeue();
        while (update != null) {
            out.beginMap();
            out.key("sid").value(getSubscriptionId());
            buf.setLength(0);
            DSTime.encode(update.timestamp, true, buf);
            out.key("ts").value(buf.toString());
            out.key("value").value(update.value.toElement());
            if ((update.quality != null) && !update.quality.isOk()) {
                out.key("quality").value(update.quality.toString());
            }
            out.endMap();
            if ((qos <= 1) || session.shouldEndMessage()) {
                break;
            }
        }
        synchronized (this) {
            if (updateHead == null) {
                if (qos <= 1) {
                    //reuse instance
                    updateHead = updateTail = update;
                }
                enqueued = false;
                return;
            }
        }
        manager.enqueue(this);
    }

    ///////////////////////////////////////////////////////////////////////////
    // Inner Classes
    ///////////////////////////////////////////////////////////////////////////

    private class Update {

        Update next;
        long timestamp;
        DSIValue value;
        DSQuality quality;

        Update set(long timestamp, DSIValue value, DSQuality quality) {
            this.timestamp = timestamp;
            this.value = value;
            this.quality = quality;
            return this;
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // Initialization
    ///////////////////////////////////////////////////////////////////////////

}
