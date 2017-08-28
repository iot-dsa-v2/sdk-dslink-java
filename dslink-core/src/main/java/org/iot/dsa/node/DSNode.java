package org.iot.dsa.node;

import static org.iot.dsa.node.DSIPublisher.Event.CHILD_ADDED;
import static org.iot.dsa.node.DSIPublisher.Event.CHILD_CHANGED;
import static org.iot.dsa.node.DSIPublisher.Event.CHILD_REMOVED;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import org.iot.dsa.dslink.responder.InboundSetRequest;
import org.iot.dsa.logging.DSLogger;
import org.iot.dsa.logging.DSLogging;
import org.iot.dsa.node.action.ActionInvocation;
import org.iot.dsa.node.action.ActionResult;
import org.iot.dsa.util.DSException;
import org.iot.dsa.util.DSUtil;

/**
 * A container of values.
 *
 * <p>
 *
 * Containers manage the lifecycle of the node tree. They transition from stopped, to starting to
 * stable.  When a node is added to an already stable tree, it will still go through the same
 * transition.  When removed, a node will transition to stopped.  There are onXXX callbacks for each
 * of these transitions.
 *
 * <p>
 *
 * To receive notification of changes to a container, implement DSISubscriber and subscribe to it.
 * Events will not be published if the container is in the stopped state. The subscription state
 * will be used for features such as poll on demand.
 *
 * <p>
 *
 * Containers do not notify parent containers of any events.  However, if a container subtype
 * implements this interface, parent containers will treat all events emitted as a child changed
 * event.  This is the mechanism used by mutable DSIValues so that parent containers can publish
 * child changed events.
 *
 * @author Aaron Hansen
 */
public class DSNode extends DSLogger implements DSIObject, Iterable<DSInfo> {

    ///////////////////////////////////////////////////////////////////////////
    // Constants
    ///////////////////////////////////////////////////////////////////////////

    //Prevents infinite loops when initializing default instances.
    static final DSNode defaultDefaultInstance = new DSNode();

    private static int STATE_STOPPED = 0;
    private static int STATE_STARTED = 1;
    private static int STATE_STABLE = 2;

    ///////////////////////////////////////////////////////////////////////////
    // Fields
    ///////////////////////////////////////////////////////////////////////////

    ArrayList<DSInfo> children;
    private ConcurrentHashMap<String, DSInfo> childMap;
    private DSNode defaultInstance;
    private DSInfo infoInParent;
    private String path;
    protected DSISubscriber subscriber;
    private int state = 1; //See STATE_*

    ///////////////////////////////////////////////////////////////////////////
    // Constructors
    ///////////////////////////////////////////////////////////////////////////

    ///////////////////////////////////////////////////////////////////////////
    // Methods
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Adds the info to internal collections, sets parent related fields on child containers.
     */
    void add(final DSInfo info) {
        synchronized (children) {
            children.add(info);
        }
        info.setParent(this);
        childMap.put(info.getName(), info);
        DSIObject val = info.getObject();
        if (val instanceof DSNode) {
            DSNode container = (DSNode) val;
            container.infoInParent = info;
        }
        if (isRunning()) {
            try {
                onChildAdded(info);
            } catch (Exception x) {
                severe(getPath(), x);
            }
            if (subscriber != null) {
                try {
                    subscriber.onEvent(this, info, CHILD_ADDED);
                } catch (Exception x) {
                    severe(getPath(), x);
                }
            }
        }
    }

    /**
     * Adds the named child only if the name is not already in use.
     *
     * @param name   The name must not currently be in use.
     * @param object The object to add, containers must not already be parented.
     * @return Info for the newly added child.
     * @throws IllegalArgumentException If the name is in use or the value is a container that is
     *                                  already parented.
     */
    public DSInfo add(String name, DSIObject object) {
        dsInit();
        if (object instanceof DSNode) {
            DSNode container = (DSNode) object;
            if (container.infoInParent != null) {
                throw new IllegalArgumentException("Already parented");
            }
        }
        DSInfo info;
        if (childMap == null) {
            childMap = new ConcurrentHashMap<String, DSInfo>();
            children = new ArrayList<DSInfo>();
        } else {
            info = childMap.get(name);
            if (info != null) {
                throw new IllegalArgumentException("Name already in use: " + name);
            }
        }
        info = new DSInfo(name, object);
        add(info);
        if (isRunning()) {
            if (object instanceof DSNode) {
                ((DSNode) object).start();
            }
            info.subscribe();
        }
        if (isStable()) {
            if (object instanceof DSNode) {
                ((DSNode) object).stable();
            }
        }
        return info;
    }

    /**
     * The number of children.
     */
    public int childCount() {
        dsInit();
        if (childMap == null) {
            return 0;
        }
        return childMap.size();
    }

    /**
     * Removes non-permanent children.
     *
     * @return this
     */
    public DSNode clear() {
        for (int i = childCount(); --i >= 0; ) {
            if (!getInfo(i).isPermanent()) {
                remove(i);
            }
        }
        return this;
    }

    /**
     * Whether or not this container has a child with the given name.
     */
    public boolean contains(String key) {
        if (childMap == null) {
            return false;
        }
        return childMap.get(key) != null;
    }

    @Override
    public DSNode copy() {
        dsInit();
        DSNode ret = null;
        try {
            ret = getClass().newInstance();
            ret.defaultInstance = defaultInstance;
        } catch (Exception x) {
            DSException.throwRuntime(x);
        }
        if (children != null) {
            ArrayList<DSInfo> tmp = new ArrayList<DSInfo>();
            synchronized (children) {
                tmp.addAll(children);
            }
            ret.children = new ArrayList<DSInfo>();
            ret.childMap = new ConcurrentHashMap<String, DSInfo>();
            for (int i = 0, len = tmp.size(); i < len; i++) {
                ret.add(tmp.get(i).copy());
            }
        }
        return ret;
    }

    /**
     * This creates a child that can not be removed, and can only be called on the default
     * instance.
     *
     * @return Info for the newly created child.
     * @see #declareDefaults()
     */
    protected DSInfo declareDefault(String name, DSIObject value) {
        if (!isDefaultInstance()) {
            throw new IllegalStateException("Can only called on default instances");
        }
        return add(name, value).setPermanent(true);
    }

    /**
     * The is only called once for each class.  It's purpose is to define the default children of
     * the container type.  Use the addDefault method to create un-removable children which would
     * then be safe to create getters and setters for.
     *
     * @see #declareDefault(String, DSIObject) To create unremovable children.
     */
    protected void declareDefaults() {
    }

    /**
     * Loads the default instance and initialises this state to use the default as much as
     * possible.
     */
    void dsInit() {
        if (defaultInstance == null) {
            defaultInstance = DSRegistry.getDefault(getClass());
            if (defaultInstance == defaultDefaultInstance) {
                //currently initializing
                return;
            }
            if (defaultInstance == null) {
                DSRegistry.registerDefault(getClass(), defaultDefaultInstance);
                try {
                    defaultInstance = getClass().newInstance();
                    defaultInstance.defaultInstance = defaultDefaultInstance;
                    DSRegistry.registerDefault(getClass(), defaultInstance);
                    defaultInstance.declareDefaults();
                } catch (Exception x) {
                    if (DSRegistry.getDefault(getClass()) == defaultDefaultInstance) {
                        DSRegistry.removeDefault(getClass());
                    }
                    DSException.throwRuntime(x);
                }
            }
            int len = defaultInstance.childCount();
            if (len > 0) {
                childMap = new ConcurrentHashMap<String, DSInfo>();
                children = new ArrayList<DSInfo>(len);
                DSInfo info;
                for (int i = 0; i < len; i++) {
                    info = new DSInfoProxy(defaultInstance.getInfo(i));
                    info.setParent(this);
                    childMap.put(info.getName(), info);
                    children.add(info);
                }
            }
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof DSNode) {
            DSNode him = (DSNode) obj;
            if (him.childCount() != childCount()) {
                return false;
            }
            if (children != null) {
                DSInfo his;
                DSInfo mine;
                ArrayList<DSInfo> tmp = new ArrayList<DSInfo>();
                synchronized (children) {
                    tmp.addAll(children);
                }
                for (int i = tmp.size(); --i >= 0; ) {
                    mine = tmp.get(i);
                    his = him.getInfo(mine.getName());
                    if (his == null) {
                        return false;
                    }
                    if (!DSUtil.equal(mine.getObject(), his.getObject())) {
                        return false;
                    }
                }
            }
            return true;
        }
        return false;
    }

    /**
     * Returns the child value at the given index.
     *
     * @throws IndexOutOfBoundsException if invalid index.
     */
    public DSIObject get(int index) {
        return getInfo(index).getObject();
    }

    /**
     * Returns the child value with the given myName, or null.
     *
     * @return Possibly null.
     */
    public DSIObject get(String name) {
        DSInfo info = getInfo(name);
        if (info != null) {
            return info.getObject();
        }
        return null;
    }

    /**
     * Returns the child value for the given info.
     *
     * @param info Must be an info of this container.
     * @return The desired child.
     * @throws IllegalArgumentException If info is not a child of this container.
     */
    public DSIObject get(DSInfo info) {
        if (info.getParent() != this) {
            throw new IllegalArgumentException("Not a child of this container: " + getPath());
        }
        return info.getObject();
    }

    /**
     * DSInfo for this container in its parent, or null if un-parented.
     */
    public DSInfo getInfo() {
        return infoInParent;
    }

    /**
     * Returns the child info at the given index.
     */
    public DSInfo getInfo(int index) {
        dsInit();
        synchronized (children) {
            if (children == null) {
                throw new IndexOutOfBoundsException(Integer.toString(index));
            }
            return children.get(index);
        }
    }

    /**
     * Returns the child info with the given myName, or null.
     *
     * @return Possibly null.
     */
    public DSInfo getInfo(String name) {
        dsInit();
        if (childMap == null) {
            return null;
        }
        return childMap.get(name);
    }

    /**
     * Override point, add any meta data for the given info to the provided bucket.
     */
    public void getMetadata(DSInfo info, DSMap bucket) {
    }

    /**
     * Returns the name in the parent container, or null if un-parented.
     */
    public String getName() {
        if (infoInParent == null) {
            return null;
        }
        return infoInParent.getName();
    }

    /**
     * A convenience for (DSNode) get(idx).
     */
    public DSNode getNode(int idx) {
        return (DSNode) get(idx);
    }

    /**
     * A convenience for (DSNode) get(name).
     */
    public DSNode getNode(String name) {
        return (DSNode) get(name);
    }

    /**
     * Returns the parent container, or null.
     */
    public DSNode getParent() {
        if (infoInParent == null) {
            return null;
        }
        return infoInParent.getParent();
    }

    /**
     * The DSA path, properly encoded.
     */
    public String getPath() {
        if (path == null) {
            path = DSPath.encodePath(this);
        }
        return path;
    }

    @Override
    public int hashCode() {
        int hc = 0;
        if (childMap != null) {
            hc = childMap.hashCode();
        }
        return hc;
    }

    /**
     * Called by the info when it detects a change.  Will propagate the event asynchronously.
     */
    void infoChanged(final DSInfo info) {
        if (isStopped()) {
            return;
        }
        try {
            onInfoChanged(info);
        } catch (Exception x) {
            severe(getPath(), x);
        }
        if (subscriber != null) {
            try {
                subscriber.onEvent(this, info, DSIPublisher.Event.INFO_CHANGED);
            } catch (Exception x) {
                severe(getPath(), x);
            }
        }
    }

    /**
     * Whether or not this container has a child with the given name.
     */
    public int indexOf(String key) {
        DSInfo info = getInfo(key);
        if (info == null) {
            return -1;
        }
        synchronized (children) {
            for (int i = children.size(); --i >= 0; ) {
                if (children.get(i) == info) {
                    return i;
                }
            }
        }
        throw new IllegalStateException("Key is known, but the index is not, very weird.");
    }

    /**
     * True if this is default instance for the type.
     */
    protected final boolean isDefaultInstance() {
        return defaultInstance == defaultDefaultInstance;
    }

    /**
     * Returns false.
     */
    @Override
    public boolean isNull() {
        return false;
    }

    /**
     * A convenience for !isStopped().
     */
    public boolean isRunning() {
        return !isStopped();
    }

    /**
     * True once stable is called, will be true before children.
     */
    public boolean isStable() {
        return DSUtil.getBit(state, STATE_STABLE);
    }

    /**
     * True after start is called but before stable.
     */
    public boolean isStarted() {
        return DSUtil.getBit(state, STATE_STARTED);
    }

    /**
     * True once stop is called, will be true before children.
     */
    public boolean isStopped() {
        return DSUtil.getBit(state, STATE_STOPPED);
    }

    /**
     * True if there are any subscribers.
     */
    public boolean isSubscribed() {
        return subscriber != null;
    }

    /**
     * Returns an info iterator that DOES NOT implement the optional remove method.
     */
    public Iterator<DSInfo> iterator() {
        return new ChildIterator();
    }

    /**
     * Called when the given child is added and in the stable state.
     */
    protected void onChildAdded(DSInfo info) {
    }

    /**
     * Called when the given child is changed and in the stable state.
     */
    protected void onChildChanged(DSInfo info) {
    }

    /**
     * Called when the given child is removed and in the stable state.
     */
    protected void onChildRemoved(DSInfo info) {
    }

    /**
     * Called when the given info is modified and in the stable state.
     */
    protected void onInfoChanged(DSInfo info) {
    }

    /**
     * Override point, called by the default implementation of DSAction.invoke.
     *
     * @param actionInfo Child info for the action, you can declare a field for the action info for
     *                   quick instance comparison.
     * @param invocation Details about the incoming invoke as well as the mechanism to send updates
     *                   over an open stream.
     * @return It is okay to return null if the action result type is void.
     * @throws IllegalStateException If not overridden.
     * @see org.iot.dsa.node.action.DSAction#invoke(DSInfo, ActionInvocation)
     */
    public ActionResult onInvoke(DSInfo actionInfo, ActionInvocation invocation) {
        throw new IllegalStateException("onInvoke not overridden");
    }

    /**
     * Override point, called when a value child is being set by the responder.  The default
     * implementation calls put(info, value).  If you throw an exception, an error will be
     * reported to the requester.
     *
     * @param info The child being changed.
     * @param value The new value.
     * @see org.iot.dsa.dslink.DSResponder#onSet(InboundSetRequest)
     */
    public void onSet(DSInfo info, DSIValue value) {
        put(info, value);
    }

    /**
     * Called when this container transitions from unsubscribed to subscribed, but is not called for
     * subsequent subscribers.
     */
    protected void onSubscribed() {
    }

    /**
     * Called once this container is stable, but before stable is called on children.
     */
    protected void onStable() {
    }

    /**
     * Called once this node and its entire subtree is started.
     */
    protected void onStarted() {
    }

    /**
     * Called once this node and its entire subtree is stopped.
     */
    protected void onStopped() {
    }

    /**
     * Only called when the last subscriber is unsubscribed.
     */
    protected void onUnsubscribed() {
    }

    /**
     * Add or replace the named child.
     *
     * @return this
     */
    public DSNode put(String name, DSIObject object) {
        DSInfo info = getInfo(name);
        if (info == null) {
            add(name, object);
            return this;
        }
        return put(info, object);
    }

    /**
     * Replace the child.
     *
     * @return this
     */
    public DSNode put(DSInfo info, DSIObject object) {
        if (info.getParent() != this) {
            throw new IllegalArgumentException("Info parented in another container");
        }
        DSIObject old = info.getObject();
        if (old instanceof DSNode) {
            DSNode container = (DSNode) old;
            container.infoInParent = null;
            container.stop();
        }
        info.unsubscribe();
        info.setObject(object);
        if (object instanceof DSNode) {
            DSNode container = (DSNode) object;
            container.infoInParent = info;
        }
        if (isRunning()) {
            if (object instanceof DSNode) {
                ((DSNode) object).start();
            }
            info.subscribe();
            childChanged(info);
            if (isStable()) {
                if (object instanceof DSNode) {
                    ((DSNode) object).stable();
                }
            }
        }
        return this;
    }
    
    /**
     * TEMPORARY
     * Call this to indicate that a child has changed
     */
    public void childChanged(DSInfo info) {
    	try {
            onChildChanged(info);
        } catch (Exception x) {
            severe(getPath(), x);
        }
        if (subscriber != null) {
            try {
                subscriber.onEvent(this, info, CHILD_CHANGED);
            } catch (Exception x) {
                severe(getPath(), x);
            }
        }
    }

    /**
     * Remove the child a the given index.
     *
     * @return The removed value.
     * @throws IllegalStateException If the info says its not removable.
     */

    public DSInfo remove(int index) {
        final DSInfo info = getInfo(index);
        if (info.isPermanent()) {
            throw new IllegalStateException("Can not be removed");
        }
        synchronized (children) {
            children.remove(index);
        }
        childMap.remove(info.getName());
        if (info.getObject() instanceof DSNode) {
            DSNode container = (DSNode) info.getObject();
            container.infoInParent = null;
            container.stop();
        }
        info.unsubscribe();
        if (isRunning()) {
            try {
                onChildRemoved(info);
            } catch (Exception x) {
                severe(getPath(), x);
            }
            if (subscriber != null) {
                try {
                    subscriber.onEvent(this, info, CHILD_REMOVED);
                } catch (Exception x) {
                    severe(getPath(), x);
                }
            }
        }
        return info;
    }

    /**
     * Remove the named child if it is contained.
     *
     * @return The removed info, or null.
     * @throws IllegalStateException If the info says its not removable.
     */
    public DSInfo remove(String name) {
        int idx = indexOf(name);
        if (idx < 0) {
            return null;
        }
        return remove(idx);
    }

    /**
     * There can be multiple subscribers but will only call onSubscribe for the first subscriber
     * that transitions the container from unsubscribed to subscribed.
     */
    public void subscribe(DSISubscriber arg) {
        boolean first = false;
        synchronized (this) {
            if (subscriber == null) {
                subscriber = arg;
                first = true;
            } else if (subscriber instanceof SubscriberAdapter) {
                ((SubscriberAdapter) subscriber).subscribe(arg);
            } else {
                SubscriberAdapter adapter = new SubscriberAdapter(subscriber);
                subscriber = adapter;
                adapter.subscribe(arg);
            }
        }
        if (first) {
            try {
                onSubscribed();
            } catch (Exception x) {
                severe(getPath(), x);
            }
        }
    }

    /** TODO later
     * Reorder the child to be first.
     public void reorderToFirst(DSInfo info) {
     if (info.getParent() != null) {
     throw new IllegalStateException("Info is not a child: " + getPath());
     }
     synchronized (children) {
     if (children.remove(info)) {
     children.add(0,info);
     }
     }
     }
     */

    /** TODO later
     * Reorder the child to be last.
     public void reorderToLast(DSInfo info) {
     if (info.getParent() != null) {
     throw new IllegalStateException("Info is not a child: " + getPath());
     }
     synchronized (children) {
     if (children.remove(info)) {
     children.add(info);
     }
     }
     }
     */

    /**
     * Called after the entire subtree is started.  Will call onStable after the entire subtree is
     * stable.
     */
    public final void stable() {
        if (!isStarted()) {
            throw new IllegalStateException("Not starting: " + getPath());
        }
        state = DSUtil.setBit(state, STATE_STABLE, true);
        state = DSUtil.setBit(state, STATE_STARTED, false);
        DSIObject obj;
        for (int i = 0, len = childCount(); i < len; i++) {
            obj = get(i);
            if (obj instanceof DSNode) {
                ((DSNode) obj).stable();
            }
        }
        try {
            onStable();
        } catch (Exception x) {
            DSLogging.getDefaultLogger().log(Level.FINE, getPath(), x);
        }
    }

    /**
     * Sets the state to starting.  Calls onStarted once the entire subtree is started.
     */
    public final void start() {
        if (isRunning()) {
            throw new IllegalStateException("Already running: " + getPath());
        }
        path = null;
        state = DSUtil.setBit(state, STATE_STOPPED, false);
        state = DSUtil.setBit(state, STATE_STARTED, true);
        DSIObject obj;
        DSInfo info;
        for (int i = 0, len = childCount(); i < len; i++) {
            info = getInfo(i);
            obj = info.getObject();
            if (obj instanceof DSNode) {
                ((DSNode) obj).start();
            }
            info.subscribe();
        }
        try {
            onStarted();
        } catch (Exception x) {
            DSLogging.getDefaultLogger().log(Level.FINE, getPath(), x);
        }
    }

    /**
     * Sets the state to stopped.  Will call onStop before child containers are stopped, and
     * onChildrenStopped after all child containers are stopped.
     */
    public final void stop() {
        if (isStopped()) {
            throw new IllegalStateException("Not stable: " + getPath());
        }
        path = null;
        state = DSUtil.setBit(state, STATE_STABLE, false);
        state = DSUtil.setBit(state, STATE_STOPPED, true);
        DSIObject obj;
        DSInfo info;
        for (int i = 0, len = childCount(); i < len; i++) {
            info = getInfo(i);
            info.unsubscribe();
            obj = info.getObject();
            if (obj instanceof DSNode) {
                ((DSNode) obj).stop();
            }
        }
        try {
            onStopped();
        } catch (Exception x) {
            DSLogging.getDefaultLogger().log(Level.FINE, getPath(), x);
        }
    }

    /**
     * There can be multiple subscribers but this will only call onUnsubscribed for the removal of
     * the last subscriber.
     */
    public void unsubscribe(DSISubscriber arg) {
        synchronized (this) {
            if (arg == null) {
                return;
            } else if (subscriber instanceof SubscriberAdapter) {
                SubscriberAdapter adapter = (SubscriberAdapter) subscriber;
                adapter.unsubscribe(arg);
                if (adapter.isEmpty()) {
                    subscriber = null;
                }
            } else if (subscriber == arg) {
                subscriber = null;
            }
        }
        if (subscriber == null) {
            try {
                onUnsubscribed();
            } catch (Exception x) {
                severe(getPath(), x);
            }
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // Inner Classes
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Does not support the remove operation.
     */
    private class ChildIterator implements Iterator<DSInfo> {

        private DSInfo next;
        private int nextIndex = -1;

        ChildIterator() {
            next();
        }

        public boolean hasNext() {
            return next != null;
        }

        public DSInfo next() {
            DSInfo ret = next;
            next = null;
            try {
                if (++nextIndex < childCount()) {
                    next = getInfo(nextIndex);
                }
            } catch (Exception ignorable) {
                fine(getPath(), ignorable);
            }
            return ret;
        }

        public void remove() {
            throw new UnsupportedOperationException("Remove not supported");
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // Initialization
    ///////////////////////////////////////////////////////////////////////////

}
