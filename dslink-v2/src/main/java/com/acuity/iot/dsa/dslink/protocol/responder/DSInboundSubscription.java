package com.acuity.iot.dsa.dslink.protocol.responder;

import com.acuity.iot.dsa.dslink.protocol.DSSession;
import com.acuity.iot.dsa.dslink.protocol.message.DSTarget;
import com.acuity.iot.dsa.dslink.protocol.message.MessageWriter;
import org.iot.dsa.dslink.DSIResponder;
import org.iot.dsa.dslink.responder.InboundSubscribeRequest;
import org.iot.dsa.dslink.responder.SubscriptionCloseHandler;
import org.iot.dsa.io.DSIWriter;
import org.iot.dsa.node.DSIObject;
import org.iot.dsa.node.DSIStatus;
import org.iot.dsa.node.DSIValue;
import org.iot.dsa.node.DSInfo;
import org.iot.dsa.node.DSNode;
import org.iot.dsa.node.DSNull;
import org.iot.dsa.node.DSStatus;
import org.iot.dsa.node.DSString;
import org.iot.dsa.node.event.DSEvent;
import org.iot.dsa.node.event.DSISubscriber;
import org.iot.dsa.node.event.DSISubscription;
import org.iot.dsa.time.DSDateTime;

/**
 * Subscribe implementation for the responder.
 *
 * @author Aaron Hansen
 */
public class DSInboundSubscription extends DSInboundRequest
        implements DSISubscriber, InboundSubscribeRequest {

    ///////////////////////////////////////////////////////////////////////////
    // Instance Fields
    ///////////////////////////////////////////////////////////////////////////

    private int ackRequired = 0;
    private DSInfo<?> child;
    private boolean closeAfterUpdate = false;
    private SubscriptionCloseHandler closeHandler;
    private boolean enqueued = false;
    private DSInboundSubscriptions manager;
    private boolean open = true;
    private int qos;
    private Integer sid;
    private DSISubscription subscription;
    private Update updateHead;
    private Update updateTail;

    ///////////////////////////////////////////////////////////////////////////
    // Constructors
    ///////////////////////////////////////////////////////////////////////////

    protected DSInboundSubscription(DSInboundSubscriptions manager,
                                    Integer sid,
                                    String path,
                                    int qos) {
        this.manager = manager;
        this.sid = sid;
        setResponder(manager.getResponder());
        setSession(manager.getResponder().getSession());
        setPath(path);
        this.qos = qos;
        setLink(manager.getLink());
        init();
    }

    ///////////////////////////////////////////////////////////////////////////
    // Public Methods
    ///////////////////////////////////////////////////////////////////////////

    public boolean canWrite(DSSession session) {
        if (ackRequired > 0) {
            int last = session.getAckRcvd();
            if (last >= ackRequired) {
                return true;
            }
            //is the last ack is so far away that we have a rollover.
            return ((ackRequired - 10000000) > last);
        }
        return true;
    }

    @Override
    public void close() {
        //we never close locally in case the node is added back
        //manager.unsubscribe(sid);
    }

    public int getQos() {
        return qos;
    }

    public void setQos(Integer val) {
        synchronized (this) {
            if (val == 0) {
                updateHead = updateTail;
            }
            qos = val;
        }
    }

    protected DSInboundSubscription setQos(int qos) {
        this.qos = qos;
        return this;
    }

    /**
     * Unique subscription id for this path.
     */
    @Override
    public Integer getSubscriptionId() {
        return sid;
    }

    public void setSubscriptionId(Integer id) {
        sid = id;
    }

    /**
     * For v2 only.
     */
    public boolean isCloseAfterUpdate() {
        return closeAfterUpdate;
    }

    /**
     * For v2 only.
     */
    public DSInboundSubscription setCloseAfterUpdate(boolean closeAfterUpdate) {
        this.closeAfterUpdate = closeAfterUpdate;
        return this;
    }

    @Override
    public void onClosed(DSISubscription subscription) {
        this.subscription = null;
        close();
    }

    @Override
    public void onEvent(DSEvent event, DSNode node, DSInfo<?> child, DSIValue data) {
        DSDateTime dt = DSDateTime.now();
        DSStatus status = DSStatus.ok;
        switch (event.getEventId()) {
            case DSNode.CHILD_REMOVED:
            case DSNode.STOPPED:
                if (child == this.child) {
                    update(dt, DSNull.NULL, DSStatus.unknown);
                }
                break;
            case DSNode.VALUE_CHANGED:
                if (child == this.child) {
                    if (data == null) {
                        if ((child != null) && child.isValue()) {
                            data = child.getValue();
                        } else if (node instanceof DSIValue) {
                            data = (DSIValue) node;
                        } else {
                            data = DSNull.NULL;
                        }
                    }
                    if (data instanceof DSIStatus) {
                        status = ((DSIStatus) data).getStatus();
                    }
                    update(dt, data, status);
                }
                break;
            default:
                return;
        }
    }

    @Override
    public String toString() {
        return "Subscription (" + getSubscriptionId() + ") " + getPath();
    }

    ///////////////////////////////////////////////////////////////////////////
    // Protected Methods
    ///////////////////////////////////////////////////////////////////////////

    /**
     * The responder should call this whenever the value or status changes.
     */
    @Override
    public void update(DSDateTime timestamp, DSIValue value, DSStatus status) {
        if (!open) {
            return;
        }
        if (qos == 0) {
            synchronized (this) {
                if (updateHead == null) {
                    updateHead = new Update();
                }
                updateHead.set(timestamp, value, status);
                if (enqueued) {
                    return;
                }
                enqueued = true;
            }
        } else {
            Update update = new Update().set(timestamp, value, status);
            synchronized (this) {
                if (updateHead == null) {
                    updateHead = updateTail = update;
                } else {
                    updateTail.next = update;
                    updateTail = update;
                }
                if (enqueued) {
                    return;
                }
                enqueued = true;
            }
        }
        if (sid != 0) {
            manager.enqueue(this);
        }
    }

    /**
     * Remove an update from the queue.
     */
    protected synchronized Update dequeue() {
        if (updateHead == null) {
            return null;
        }
        Update ret = updateHead;
        if (updateHead == updateTail) {
            updateHead = updateTail = null;
        } else {
            updateHead = updateHead.next;
        }
        ret.next = null;
        return ret;
    }

    protected void init() {
        if (subscription != null) {
            subscription.close();
            subscription = null;
        }
        try {
            DSTarget path = new DSTarget(getPath(), getLink().getRootNode());
            if (path.isResponder()) {
                DSIResponder responder = (DSIResponder) path.getTarget();
                closeHandler = responder.onSubscribe(
                        new SubWrapper(path.getPath(), this));
            } else {
                DSIObject obj = path.getTarget();
                DSNode theNode;
                if (obj instanceof DSNode) {
                    theNode = (DSNode) obj;
                    if (obj instanceof DSIValue) {
                        child = null;
                        this.subscription = theNode.subscribe((event, node, child, data) -> {
                            if (child == null) {
                                DSInboundSubscription.this.onEvent(event, node, null, data);
                            }
                        });
                        onEvent(DSNode.VALUE_CHANGED_EVENT, theNode, null, null);
                    } else {
                        update(DSDateTime.now(), DSString.EMPTY, DSStatus.ok);
                    }
                } else {
                    DSInfo<?> info = path.getTargetInfo();
                    theNode = info.getParent();
                    child = info;
                    this.subscription = theNode.subscribe(DSInboundSubscription.this::onEvent);
                    onEvent(DSNode.VALUE_CHANGED_EVENT, theNode, info, info.getValue());
                }
            }
        } catch (Exception x) {
            update(DSDateTime.now(), DSNull.NULL, DSStatus.unknown);
        }
    }

    /**
     * Encodes one or more updates.
     *
     * @param writer Where to encode.
     * @param buf    For encoding timestamps.
     */
    protected void write(DSSession session, MessageWriter writer, StringBuilder buf) {
        if (qos > 0) {
            ackRequired = session.getMidSent();
        }
        Update update = dequeue();
        int count = 500;
        while (update != null) {
            write(update, writer, buf);
            if ((qos == 0) || session.shouldEndMessage()) {
                break;
            }
            if (--count >= 0) {
                update = dequeue();
            } else {
                update = null;
            }
        }
        synchronized (this) {
            if (updateHead == null) {
                if (qos == 0) {
                    //reuse instance
                    updateHead = update;
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
        out.key("ts").value(update.timestamp.toString());
        out.key("value").value(update.value.toElement());
        if ((update.status != null) && !update.status.isOk()) {
            out.key("status").value(update.status.toString());
        }
        out.endMap();
    }

    ///////////////////////////////////////////////////////////////////////////
    // Package / Private Methods
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Called no matter how closed.
     */
    void onClose() {
        synchronized (this) {
            if (!open) {
                return;
            }
            open = false;
        }
        try {
            if (subscription != null) {
                subscription.close();
                subscription = null;
            }
        } catch (Exception x) {
            manager.debug(manager.getPath(), x);
        }
        try {
            if (closeHandler != null) {
                closeHandler.onClose(getSubscriptionId());
                closeHandler = null;
            }
        } catch (Exception x) {
            manager.debug(manager.getPath(), x);
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // Inner Classes
    ///////////////////////////////////////////////////////////////////////////

    protected static class Update {

        Update next;
        public DSStatus status;
        public DSDateTime timestamp;
        public DSIValue value;

        Update set(DSDateTime timestamp, DSIValue value, DSStatus status) {
            this.timestamp = timestamp;
            this.value = value;
            this.status = status;
            return this;
        }
    }

}
