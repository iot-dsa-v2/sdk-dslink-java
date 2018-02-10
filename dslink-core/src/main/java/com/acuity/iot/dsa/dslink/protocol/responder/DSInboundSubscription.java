package com.acuity.iot.dsa.dslink.protocol.responder;

import com.acuity.iot.dsa.dslink.protocol.message.MessageWriter;
import com.acuity.iot.dsa.dslink.protocol.message.RequestPath;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.iot.dsa.dslink.DSIResponder;
import org.iot.dsa.dslink.responder.InboundSubscribeRequest;
import org.iot.dsa.dslink.responder.SubscriptionCloseHandler;
import org.iot.dsa.io.DSIWriter;
import org.iot.dsa.node.DSIStatus;
import org.iot.dsa.node.DSIValue;
import org.iot.dsa.node.DSInfo;
import org.iot.dsa.node.DSNode;
import org.iot.dsa.node.DSStatus;
import org.iot.dsa.node.event.DSIEvent;
import org.iot.dsa.node.event.DSISubscriber;
import org.iot.dsa.node.event.DSTopic;
import org.iot.dsa.node.event.DSValueTopic.Event;
import org.iot.dsa.time.DSTime;

/**
 * Subscribe implementation for the responder.
 *
 * @author Aaron Hansen
 */
public class DSInboundSubscription extends DSInboundRequest
        implements DSISubscriber, InboundSubscribeRequest {

    ///////////////////////////////////////////////////////////////////////////
    // Fields
    ///////////////////////////////////////////////////////////////////////////

    private DSInfo child;
    private SubscriptionCloseHandler closeHandler;
    private boolean enqueued = false;
    private DSInboundSubscriptions manager;
    private DSNode node;
    private boolean open = true;
    private Integer sid;
    private int qos = 0;
    private Update updateHead;
    private Update updateTail;

    ///////////////////////////////////////////////////////////////////////////
    // Constructors
    ///////////////////////////////////////////////////////////////////////////

    protected DSInboundSubscription(DSInboundSubscriptions manager, Integer sid, String path, int qos) {
        this.manager = manager;
        this.sid = sid;
        setPath(path);
        this.qos = qos;
        setLink(manager.getLink());
        init();
    }

    ///////////////////////////////////////////////////////////////////////////
    // Methods in alphabetical order
    ///////////////////////////////////////////////////////////////////////////

    @Override
    public void close() {
        manager.unsubscribe(sid);
    }

    /**
     * Remove an update from the queue.
     */
    protected synchronized Update dequeue() {
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

    protected void init() {
        RequestPath path = new RequestPath(getPath(), getLink());
        if (path.isResponder()) {
            DSIResponder responder = (DSIResponder) path.getTarget();
            setPath(path.getPath());
            closeHandler = responder.onSubscribe(this);
        } else {
            DSInfo info = path.getInfo();
            if (info.isNode()) {
                node = info.getNode();
                node.subscribe(DSNode.VALUE_TOPIC, null, this);
                onEvent(DSNode.VALUE_TOPIC, Event.NODE_CHANGED, info.getNode(), null,
                        (Object[]) null);
            } else {
                node = path.getParent();
                child = info;
                node.subscribe(DSNode.VALUE_TOPIC, info, this);
                onEvent(DSNode.VALUE_TOPIC, Event.CHILD_CHANGED, node, info,
                        (Object[]) null);
            }
        }
    }

    /**
     * Called by DSSubcriptions no matter how closed.
     */
    void onClose() {
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
        try {
            if (node != null) {
                node.unsubscribe(DSNode.VALUE_TOPIC, child, this);
            }
        } catch (Exception x) {
            getLogger().log(Level.WARNING, toString(), x);
        }
    }

    @Override
    public void onEvent(DSTopic topic, DSIEvent event, DSNode node, DSInfo child,
                        Object... params) {
        DSIValue value;
        if (child != null) {
            value = child.getValue();
        } else {
            value = (DSIValue) node;
        }
        DSStatus quality = DSStatus.ok;
        if (value instanceof DSIStatus) {
            quality = ((DSIStatus) value).toStatus();
        }
        update(System.currentTimeMillis(), value, quality);
    }

    @Override
    public void onUnsubscribed(DSTopic topic, DSNode node, DSInfo info) {
        close();
    }

    /**
     * The responder should call this whenever the value or status changes.
     */
    @Override
    public void update(long timestamp, DSIValue value, DSStatus quality) {
        if (!open) {
            return;
        }
        finest(finest() ? "Update " + getPath() + " to " + value : null);
        if (qos == 0) {
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

    protected DSInboundSubscription setQos(int qos) {
        this.qos = qos;
        return this;
    }

    protected DSInboundSubscription setSubscriptionId(Integer sid) {
        this.sid = sid;
        return this;
    }

    @Override
    public String toString() {
        return "Subscription (" + getSubscriptionId() + ") " + getPath();
    }


    /**
     * Encodes one or more updates.
     *
     * @param writer Where to encode.
     * @param buf    For encoding timestamps.
     */
    protected void write(MessageWriter writer, StringBuilder buf) {
        DSResponder session = getResponder();
        Update update = dequeue();
        while (update != null) {
            write(update, writer, buf);
            if ((qos == 0) || session.shouldEndMessage()) {
                break;
            }
        }
        synchronized (this) {
            if (updateHead == null) {
                if (qos == 0) {
                    //reuse instance
                    updateHead = updateTail = update;
                }
                enqueued = false;
                return;
            }
        }
        manager.enqueue(this);
    }

    /**
     * Encode a single update.  This is implemented for v1 and will need to be overridden for
     * v2.
     *
     * @param update The udpate to write.
     * @param writer Where to write.
     * @param buf    For encoding timestamps.
     */
    protected void write(Update update, MessageWriter writer, StringBuilder buf) {
        DSIWriter out = writer.getWriter();
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
    }

    ///////////////////////////////////////////////////////////////////////////
    // Inner Classes
    ///////////////////////////////////////////////////////////////////////////

    protected class Update {

        Update next;
        public long timestamp;
        public DSIValue value;
        public DSStatus quality;

        Update set(long timestamp, DSIValue value, DSStatus quality) {
            this.timestamp = timestamp;
            this.value = value;
            this.quality = quality;
            return this;
        }
    }

}
