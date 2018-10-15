package org.iot.dsa.node;

import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import org.iot.dsa.dslink.DSIResponder;
import org.iot.dsa.dslink.responder.InboundSetRequest;
import org.iot.dsa.logging.DSLogger;
import org.iot.dsa.node.action.ActionInvocation;
import org.iot.dsa.node.action.ActionResult;
import org.iot.dsa.node.event.DSIEvent;
import org.iot.dsa.node.event.DSISubscriber;
import org.iot.dsa.node.event.DSInfoTopic;
import org.iot.dsa.node.event.DSTopic;
import org.iot.dsa.node.event.DSValueTopic;
import org.iot.dsa.util.DSException;
import org.iot.dsa.util.DSUtil;

/**
 * The organizational unit of the node tree.  Most links will bind their specific logic by
 * subclassing DSNode and utilizing the lifecycle callbacks.
 * <p>
 * To create a node subclass,  you should understand the following concepts:
 *
 * <ul>
 * <li>Constructors
 * <li>Defaults
 * <li>Node Lifecycle and Callbacks
 * <li>Subscriptions
 * <li>Values
 * <li>Actions
 * <li>DSInfo
 * </ul>
 *
 * <h3>Constructors</h3>
 * <p>
 * DSNode sub-classes must support the public no-arg constructor.  This is how they will be
 * instantiated when deserializing the configuration database.
 *
 * <h3>Defaults</h3>
 * <p>
 * Every subtype of DSNode has a private default instance, all other instances of any particular
 * type are copies of the default instance.  You should never perform application logic unless your
 * node is running (started or stable) because of this.
 * <p>
 * If a DSNode subtype needs to have specific child nodes or values (most will), it should override
 * the declareDefaults method.  The method should:
 *
 * <ul>
 * <li> Call super.declareDefaults();
 * <li> Call DSNode.declareDefault(String name, DSIObject child) for each non-removable child.  Do
 * not add dynamic children in declareDefaults, because if they are removed, they will be re-added
 * the next time the link is restarted.
 * </ul>
 * <p>
 * During node serialization (configuration database, not DSA interop), children that match their
 * declared default are omitted.  This has two benefits:
 *
 * <ul>
 * <li> Smaller node database means faster serialization / deserialization.
 * <li> Default values can be modified and all existing database will be automatically upgraded the
 * next time the updated class loaded.
 * </ul>
 *
 * <h3>Lifecycle</h3>
 * <p>
 * It is important to know the node lifecycle.  Your nodes should not execute any application logic
 * unless they are running (started or stable).
 *
 * <b>Stopped</b>
 * <p>
 * A node is instantiated in the stopped state.  If a node tree has been persisted, will be be fully
 * restored in the stopped state.  DSNode.onStopped will not be called, it is only called when nodes
 * transition from running to stopped.
 * <p>
 * When nodes are removed from a running parent node, they will be stopped.  DSNode.onStopped will
 * be called after all child nodes have been stopped.
 * <p>
 * When a link is stopped, an attempt to stop the tree will be made, but it cannot be guaranteed.
 *
 * <b>Started</b>
 * <p>
 * After the node tree is fully deserialized it will be started.  A node's onStart method will be
 * called after all of its child nodes have been started.  The only guarantee is that all child
 * nodes have been started.
 * <p>
 * Nodes will also started when they are added to an already running parent node.
 *
 * <b>Stable</b>
 * <p>
 * Stable is called after the entire tree has been started.  The first time the node tree is loaded,
 * there is a stable delay of 5 seconds.  This is configurable as <b>stableDelay</b> in
 * <i>slink.json</i>.
 * <p>
 * Nodes added to an already stable parent will have onStart and onStable called immediately.
 * <p>
 * When in doubt of whether to use onStarted or onStable, use onStable.
 *
 * <b>Other Callbacks</b>
 * <p>
 * When a node is stable, there are several other callbacks for various state changes.  All
 * callbacks begin with **on** such as onChildAdded().
 *
 * <h3>Subscriptions</h3>
 * <p>
 * Nodes should suspend, or minimize activity when nothing is interested in them.  For example, if
 * nothing is interested in a point, it is best to not poll the point on the foreign system.
 * <p>
 * To do this you use the following APIs:
 *
 * <ul>
 * <li>DSNode.onSubscribed - Called when the node transitions from unsubscribed to subscribed.  This
 * is not called for subsequent subscribers once in the subscribed state.
 * <li>DSNode.onUnsubscribed - Called when the node transitions from subscribed to unsubscribed. If
 * there are multiple subscribers, this is only called when the last one unsubscribes.
 * <li>DSNode.isSubscribed - Tells the caller whether or not the node is subscribed.
 * </ul>
 *
 * <h3>Values</h3>
 * <p>
 * Values mostly represent leaf members of the node tree.  There are two types of values:
 *
 * <ul>
 * <li>DSElement - These map to the JSON type system and represent leaf members of the node tree.
 * <li>DSIValue - These don't map to the JSON type system, and it is possible for nodes to implement
 * this interface. This allows for values to have children.
 * </ul>
 * <p>
 * Many values are singleton instances.  This is for efficiency, the same value instance (e.g.
 * DSBoolean.TRUE) can be stored in many nodes. Singleton values must be immutable.
 * <p>
 * Whenever possible, values should also have NULL instance.  Rather than storing a generic null,
 * this helps the system decode the proper type such as when a requester is attempting to set a
 * value.
 *
 * <h3>Actions</h3>
 * <p>
 * Add actions to your node to allow requester invocation using org.iot.dsa.node.action.DSAction.
 * <p>
 * Override DSNode.onInvoke to handle invocations.  The reason for this is complicated but it is
 * possible to subclass DSAction, just carefully read the javadoc if you do.  Be sure to call
 * super.onInvoke() when overriding that method.
 *
 * <h3>DSInfo</h3>
 * <p>
 * All node children have corresponding DSInfo instances.  This type serves two purposes:
 *
 * <ul>
 * <li>It carries some meta-data about the relationship between the parent node and the child.
 * <li>It tracks whether or not the child matches a declared default.
 * </ul>
 * <p>
 * Important things for developers to know about DSInfo are:
 *
 * <ul>
 * <li>You can configure state such as transient, readonly and hidden.
 * <li>You can declare fields in the your Java class for default info instances to avoid looking up
 * the child every time it is needed.  This is can be used to create fast getters and setters.
 * </ul>
 * <p>
 *
 * @author Aaron Hansen
 */
public class DSNode extends DSLogger implements DSIObject, Iterable<DSInfo> {

    ///////////////////////////////////////////////////////////////////////////
    // Class Fields
    ///////////////////////////////////////////////////////////////////////////

    //Prevents infinite loops when initializing default instances.
    static final DSNode defaultDefaultInstance = new DSNode();
    private static final int MAP_THRESHOLD = 7;

    public static final DSInfoTopic INFO_TOPIC = DSInfoTopic.INSTANCE;
    public static final DSValueTopic VALUE_TOPIC = DSValueTopic.INSTANCE;
    private static int STATE_STABLE = 2;
    private static int STATE_STARTED = 1;
    private static int STATE_STOPPED = 0;

    ///////////////////////////////////////////////////////////////////////////
    // Instance Fields
    ///////////////////////////////////////////////////////////////////////////

    private Map<String, DSInfo> childMap;
    private DSNode defaultInstance;
    private DSInfo firstChild;
    private DSInfo infoInParent;
    private DSInfo lastChild;
    private Object mutex = new Object();
    private String path;
    private int size = 0;
    private int state = 1; //See STATE_*
    private Subscription subscription;

    ///////////////////////////////////////////////////////////////////////////
    // Public Methods
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Adds the named child only if the name is not already in use.
     *
     * @param name   The name must not currently be in use.
     * @param object The object to add, nodes must not already be parented.
     * @return Info for the newly added child.
     * @throws IllegalArgumentException If the name is in use or the value is a node that is already
     *                                  parented.
     */
    public DSInfo add(String name, DSIObject object) {
        validateChild(object);
        DSNode argAsNode = null;
        boolean argIsNode = isNode(object);
        if (argIsNode) {
            argAsNode = toNode(object);
            argAsNode.validateParent(this);
        }
        if (argIsNode) {
            if (argAsNode.infoInParent != null) {
                throw new IllegalArgumentException("Already parented");
            }
        } else if (object instanceof DSGroup) {
            ((DSGroup) object).setParent(this);
        }
        DSInfo info = getInfo(name);
        if (info != null) {
            throw new IllegalArgumentException("Name already in use: " + name);
        }
        info = new DSInfo(name.intern(), object);
        add(info);
        if (isRunning()) {
            if (argIsNode) {
                argAsNode.start();
            }
        }
        if (isStable()) {
            if (argIsNode) {
                argAsNode.stable();
            }
        }
        return info;
    }

    /**
     * The number of children.
     */
    public int childCount() {
        dsInit();
        return size;
    }

    /**
     * Removes non-permanent children.
     *
     * @return this
     */
    public DSNode clear() {
        DSInfo info = getFirstInfo();
        while (info != null) {
            if (info.isDynamic()) {
                remove(info);
            }
            info = info.next();
        }
        return this;
    }

    /**
     * Whether or not this node has a child with the given name.
     */
    public boolean contains(String key) {
        return getInfo(key) != null;
    }

    /**
     * Returns a clone of this node and its subtree.
     */
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
        ret.dsInit();
        DSInfo myInfo = firstChild;
        DSInfo hisInfo;
        while (myInfo != null) {
            hisInfo = ret.getInfo(myInfo.getName());
            if (hisInfo == null) {
                ret.add(myInfo.copy());
            } else {
                hisInfo.copy(myInfo);
                if (hisInfo.isNode()) {
                    hisInfo.getNode().infoInParent = hisInfo;
                }
            }
            myInfo = myInfo.next();
        }
        //TODO match order
        return ret;
    }

    /**
     * Returns the child value with the given name, or null.
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
     * Ascends the tree looking for an ancestral node that is an instance of the parameter.
     *
     * @param clazz Can be a class, interface or super class.
     * @return Possibly null.
     * @see java.lang.Class#isAssignableFrom(Class)
     */
    public DSNode getAncestor(Class<?> clazz) {
        DSNode node = getParent();
        while (node != null) {
            if (clazz.isAssignableFrom(node.getClass())) {
                return node;
            }
            node = node.getParent();
        }
        return node;
    }

    /**
     * A convenience for (DSElement) get(name).
     */
    public DSElement getElement(String name) {
        return (DSElement) get(name);
    }

    /**
     * The first child, or null.
     */
    public DSIObject getFirst() {
        dsInit();
        DSInfo info = firstChild;
        if (info == null) {
            return null;
        }
        return info.getObject();
    }

    /**
     * The first child info, or null.
     */
    public DSInfo getFirstInfo() {
        dsInit();
        return firstChild;
    }

    public DSInfo getFirstInfo(Class type) {
        DSInfo info = getFirstInfo();
        if (info.is(type)) {
            return info;
        }
        return info.next(type);
    }

    /**
     * The info for the first child node, or null.
     */
    public DSInfo getFirstNodeInfo() {
        if (firstChild == null) {
            return null;
        }
        if (firstChild.isNode()) {
            return firstChild;
        }
        return firstChild.nextNode();
    }

    /**
     * DSInfo for this node in its parent, or null if un-parented.
     */
    public DSInfo getInfo() {
        return infoInParent;
    }

    /**
     * Returns the info for the child with the given name, or null.
     *
     * @return Possibly null.
     */
    public DSInfo getInfo(String name) {
        dsInit();
        if (firstChild == null) {
            return null;
        }
        synchronized (mutex) {
            if (size >= MAP_THRESHOLD) {
                if (childMap == null) {
                    childMap = new TreeMap<String, DSInfo>();
                    for (DSInfo info = firstChild; info != null; info = info.next()) {
                        childMap.put(info.getName(), info);
                    }
                }
                return childMap.get(name);
            }
        }
        DSInfo info = firstChild;
        while (info != null) {
            if (info.getName().equals(name)) {
                return info;
            }
            info = info.next();
        }
        return null;
    }

    /**
     * The last child, or null.
     */
    public DSIObject getLast() {
        DSInfo info = getLastInfo();
        if (info == null) {
            return null;
        }
        return info.getObject();
    }

    /**
     * The last child info, or null.
     */
    public DSInfo getLastInfo() {
        dsInit();
        return lastChild;
    }

    /**
     * Override point, add any meta data for the given info to the provided bucket.
     */
    public void getMetadata(DSInfo info, DSMap bucket) {
    }

    /**
     * Returns the name of this node in its parent, or null if un-parented.
     */
    public String getName() {
        if (infoInParent == null) {
            return null;
        }
        return infoInParent.getName();
    }

    /**
     * A convenience for (DSNode) get(name).
     */
    public DSNode getNode(String name) {
        return (DSNode) get(name);
    }

    /**
     * Returns the parent node, or null.
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

    /**
     * A convenience for (DSIValue) get(name).
     */
    public DSIValue getValue(String name) {
        return (DSIValue) get(name);
    }

    /**
     * True if the argument is a node with the same children, although their order can be
     * different.
     */
    @Override
    public boolean isEqual(Object arg) {
        if (arg == this) {
            return true;
        }
        if (!isNode(arg)) {
            return false;
        }
        DSNode argNode = toNode(arg);
        if (argNode.childCount() != childCount()) {
            return false;
        }
        DSInfo mine = getFirstInfo();
        while (mine != null) {
            if (!mine.isEqual(argNode.getInfo(mine.getName()))) {
                return false;
            }
            mine = mine.next();
        }
        return true;
    }

    /**
     * True if the argument is a node with the same children in the exact same order.
     */
    public boolean isIdentical(Object arg) {
        if (arg == this) {
            return true;
        }
        if (!isNode(arg)) {
            return false;
        }
        DSNode argNode = toNode(arg);
        if (argNode.childCount() != childCount()) {
            return false;
        }
        DSInfo mine = getFirstInfo();
        DSInfo argInfo = argNode.getFirstInfo();
        while (mine != null) {
            if (argInfo == null) {
                return false;
            }
            if (!mine.isIdentical(argInfo)) {
                return false;
            }
            mine = mine.next();
            argInfo = argInfo.next();
        }
        if (argInfo != null) {
            return false;
        }
        return true;
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
     * True after stable is called, children are stable before their parents.
     */
    public boolean isStable() {
        return DSUtil.getBit(state, STATE_STABLE);
    }

    /**
     * True after start is called, children are started before their parents.
     */
    public boolean isStarted() {
        return DSUtil.getBit(state, STATE_STARTED);
    }

    /**
     * True after stop is called, children are stopped before their parents.
     */
    public boolean isStopped() {
        return DSUtil.getBit(state, STATE_STOPPED);
    }

    /**
     * True if there are any subscribers.
     */
    public boolean isSubscribed() {
        return subscription != null;
    }

    /**
     * True if there are any subscriptions with the matching child and topic.
     *
     * @param child Can be null.
     * @param topic Can be null.
     */
    public boolean isSubscribed(DSInfo child, DSTopic topic) {
        Subscription sub = subscription;
        while (sub != null) {
            if (DSUtil.equal(topic, sub.topic)) {
                if (DSUtil.equal(child, sub.child)) {
                    return true;
                }
            }
            sub = sub.next;
        }
        return false;
    }

    /**
     * True if there any subscriptions for the given topic.
     */
    public boolean isSubscribed(DSTopic topic) {
        Subscription sub = subscription;
        while (sub != null) {
            if (sub.topic == topic) {
                return true;
            }
            sub = sub.next;
        }
        return false;
    }

    /**
     * True if this node or any nodes in the subtree have any subscriptions.
     *
     * @see #isSubscribed()
     */
    public boolean isTreeSubscribed() {
        if (isSubscribed()) {
            return true;
        }
        for (DSInfo info = getFirstNodeInfo(); info != null; info = info.nextNode()) {
            if (info.getNode().isTreeSubscribed()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns an info iterator of child DSNodes.
     */
    public Iterator<DSInfo> iterateNodes() {
        return new NodeIterator();
    }

    /**
     * Returns an info iterator of child values.
     */
    public Iterator<DSInfo> iterateValues() {
        return new ValueIterator();
    }

    /**
     * Returns an info iterator of all children.
     */
    public Iterator<DSInfo> iterator() {
        return new ChildIterator();
    }

    /**
     * Override point, called by the default implementation of DSAction.invoke.  You should call
     * super.onInvoke if you do not handle an incoming invocation.  However, do not call super if
     * you do.
     *
     * @param actionInfo Child info for the action, you can declare a field for the action info for
     *                   quick instance comparison.
     * @param invocation Details about the incoming invoke as well as the mechanism to send updates
     *                   over an open stream.
     * @return It is okay to return null if the action result type is void.
     * @throws IllegalStateException If the nothing handles an incoming invocation.
     * @see org.iot.dsa.node.action.DSAction#invoke(DSInfo, ActionInvocation)
     */
    public ActionResult onInvoke(DSInfo actionInfo, ActionInvocation invocation) {
        throw new IllegalStateException("onInvoke not overridden");
    }

    /**
     * Override point, called when a value being set.  The default implementation calls put(info,
     * value).  Throw an exception to report an error to the requester.
     *
     * @param info  The child being changed.
     * @param value The new value.
     * @see DSIResponder#onSet(InboundSetRequest)
     */
    public void onSet(DSInfo info, DSIValue value) {
        if (info != null) {
            put(info, value);
        }
    }

    /**
     * Override point, called only when a DSNode subclass implements DSIValue is being set.  This
     * will throw and IllegalStateException if not overridden. You should store the value of the
     * node as a child and call put in override implementation.  Throw an exception to
     * report an error to the requester.
     *
     * @param value The new value.
     * @see DSIResponder#onSet(InboundSetRequest)
     * @see DSValueNode
     */
    public void onSet(DSIValue value) {
        throw new IllegalStateException("DSNode.onSet(DSIValue) not overridden");
    }

    /**
     * Adds or replaces the named child.  If adding, add(String,DSIObject) will be called.
     *
     * @return The info for the child.
     */
    public DSInfo put(String name, DSIObject object) {
        DSInfo info = getInfo(name);
        if (info == null) {
            return add(name, object);
        }
        put(info, object);
        return info;
    }

    /**
     * A convenience for put(String, DSIObject)
     *
     * @return The info for the child.
     */
    public DSInfo put(String name, boolean arg) {
        return put(name, DSBool.valueOf(arg));
    }

    /**
     * A convenience for put(String, DSIObject)
     *
     * @return The info for the child.
     */
    public DSInfo put(String name, double arg) {
        return put(name, DSDouble.valueOf(arg));
    }

    /**
     * A convenience for put(String, DSIObject)
     *
     * @return The info for the child.
     */
    public DSInfo put(String name, float arg) {
        return put(name, DSFloat.valueOf(arg));
    }

    /**
     * A convenience for put(String, DSIObject)
     *
     * @return The info for the child.
     */
    public DSInfo put(String name, int arg) {
        return put(name, DSInt.valueOf(arg));
    }

    /**
     * A convenience for put(String, DSIObject)
     *
     * @return The info for the child.
     */
    public DSInfo put(String name, long arg) {
        return put(name, DSLong.valueOf(arg));
    }

    /**
     * A convenience for put(String, DSIObject)
     *
     * @return The info for the child.
     */
    public DSInfo put(String name, String arg) {
        return put(name, DSString.valueOf(arg));
    }

    /**
     * Replaces the child.
     *
     * @return This
     */
    public DSNode put(DSInfo info, DSIObject object) {
        if (info.getParent() != this) {
            throw new IllegalArgumentException("Info parented in another node");
        }
        validateChild(object);
        DSNode argAsNode = null;
        boolean argIsNode = isNode(object);
        if (argIsNode) {
            argAsNode = toNode(object);
            argAsNode.validateParent(this);
        }
        if (object instanceof DSGroup) {
            ((DSGroup) object).setParent(this);
        }
        DSIObject old = info.getObject();
        if (isNode(old)) {
            DSNode node = toNode(old);
            node.stop();
            node.infoInParent = null;
        } else if (old instanceof DSGroup) {
            ((DSGroup) object).setParent(null);
        }
        info.setObject(object);
        if (argIsNode) {
            argAsNode.infoInParent = info;
        }
        if (isRunning()) {
            if (argIsNode) {
                argAsNode.start();
            } else {
                try {
                    onChildChanged(info);
                } catch (Exception x) {
                    error(getPath(), x);
                }
                fire(VALUE_TOPIC, info);
            }
        }
        return this;
    }

    /**
     * Removes the child.
     *
     * @return This
     * @throws IllegalStateException If the info is permanent or not a child of this node.
     */
    public DSNode remove(DSInfo info) {
        if (!info.isDynamic()) {
            throw new IllegalStateException("Can not be removed");
        }
        if (info.getParent() != this) {
            throw new IllegalStateException("Not a child of this container");
        }
        synchronized (mutex) {
            if (childMap != null) {
                childMap.remove(info.getName());
                if (childMap.size() < MAP_THRESHOLD) {
                    childMap = null;
                }
            }
            if (size == 1) {
                firstChild = null;
                lastChild = null;
            } else if (info == firstChild) {
                firstChild = firstChild.next;
                firstChild.prev = null;
            } else if (info == lastChild) {
                lastChild = lastChild.prev;
                lastChild.next = null;
            } else {
                info.prev.next = info.next;
                info.next.prev = info.prev;
            }
            size--;
        }
        if (info.getObject() instanceof DSNode) {
            DSNode node = (DSNode) info.getObject();
            node.infoInParent = null;
            node.stop();
        }
        if (isRunning()) {
            try {
                onChildRemoved(info);
            } catch (Exception x) {
                error(getPath(), x);
            }
            fire(DSInfoTopic.Event.CHILD_REMOVED, info);
            Subscription sub = subscription;
            while (sub != null) {
                if (sub.shouldUnsubscribe(info)) {
                    unsubscribe(sub.topic, info, sub.subscriber);
                }
                sub = sub.next;
            }
        }
        return this;
    }

    /**
     * Remove the named child if it is contained.
     *
     * @return The removed info, or null.
     * @throws IllegalStateException If the info says its not removable.
     */
    public DSInfo remove(String name) {
        DSInfo info = getInfo(name);
        if (info == null) {
            return info;
        }
        remove(info);
        return info;
    }

    /**
     * Called after the entire subtree is started.  Will call onStable after the entire subtree is
     * stable.
     */
    public final void stable() {
        if (!isStarted()) {
            throw new IllegalStateException("Not started: " + getPath());
        }
        DSInfo info = getFirstInfo();
        while (info != null) {
            if (info.isNode()) {
                info.getNode().stable();
            }
            info = info.next();
        }
        state = DSUtil.setBit(state, STATE_STABLE, true);
        state = DSUtil.setBit(state, STATE_STARTED, false);
        try {
            onStable();
        } catch (Exception x) {
            error(getPath(), x);
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
        DSInfo info = getFirstInfo();
        while (info != null) {
            if (info.isNode()) {
                info.getNode().start();
            }
            info = info.next();
        }
        state = DSUtil.setBit(state, STATE_STOPPED, false);
        state = DSUtil.setBit(state, STATE_STARTED, true);
        try {
            onStarted();
        } catch (Exception x) {
            error(getPath(), x);
        }
    }

    /**
     * Sets the state to stopped.  Will call onStop before child nodes are stopped, and
     * onChildrenStopped after all child nodes are stopped.
     */
    public final void stop() {
        if (isStopped()) {
            return;
        }
        path = null;
        DSInfo info = getFirstInfo();
        while (info != null) {
            if (info.isNode()) {
                info.getNode().stop();
            }
            info = info.next();
        }
        Subscription sub = subscription;
        while (sub != null) {
            sub.unsubscribe();
            sub = sub.next;
        }
        subscription = null;
        state = DSUtil.setBit(state, STATE_STABLE, false);
        state = DSUtil.setBit(state, STATE_STOPPED, true);
        try {
            onStopped();
        } catch (Exception x) {
            debug(getPath(), x);
        }
    }


    /**
     * Subscribes to the topic on this node.
     *
     * @param topic      Can not be null.
     * @param subscriber Can not be null.
     */
    public void subscribe(DSTopic topic, DSISubscriber subscriber) {
        subscribe(topic, null, subscriber);
    }

    /**
     * Subscribes to the topic on this node, the child can be null if subscribing to the node
     * rather than a child.
     *
     * @param topic      Can not be null.
     * @param child      Can be null, and cannot be a child node.
     * @param subscriber Can not be null.
     */
    public void subscribe(DSTopic topic, DSInfo child, DSISubscriber subscriber) {
        if (child != null) {
            if (child.isNode()) {
                throw new IllegalArgumentException("Must subscribe nodes directly");
            }
        }
        if (subscriber == null) {
            throw new NullPointerException("Null subscriber");
        }
        if (topic == null) {
            throw new NullPointerException("Null topic");
        }
        boolean firstSubscription;
        int count = 0;
        synchronized (mutex) {
            Subscription sub = subscription;
            firstSubscription = (sub == null);
            Subscription prev = null;
            while (sub != null) {
                if (sub.equals(child, topic)) {
                    count++;
                    if (DSUtil.equal(subscriber, sub.subscriber)) {
                        return;
                    }
                }
                prev = sub;
                sub = sub.next;
            }
            sub = new Subscription(child, subscriber, topic);
            if (prev == null) {
                sub.next = subscription;
                subscription = sub;
            } else {
                prev.next = sub;
            }
        }
        try {
            onSubscribe(topic, child, subscriber);
        } catch (Exception x) {
            error(getParent(), x);
        }
        if (count == 0) {
            try {
                onSubscribed(topic, child);
            } catch (Exception x) {
                error(getParent(), x);
            }
        }
        if (firstSubscription) {
            try {
                onSubscribed();
            } catch (Exception x) {
                error(getParent(), x);
            }
        }
    }

    /**
     * Unsubscribes the tuple.
     *
     * @param topic      Can not be null.
     * @param child      Can be null.
     * @param subscriber Can not be null.
     */
    public void unsubscribe(DSTopic topic, DSInfo child, DSISubscriber subscriber) {
        int count = 0;
        Subscription removed = null;
        synchronized (mutex) {
            Subscription sub = subscription;
            Subscription prev = null;
            while (sub != null) {
                if (sub.equals(child, topic)) {
                    count++;
                    if (DSUtil.equal(subscriber, sub.subscriber)) {
                        count--;
                        removed = sub;
                        if (prev == null) {
                            subscription = subscription.next;
                        } else {
                            prev.next = sub.next;
                        }
                    }
                }
                prev = sub;
                sub = sub.next;
            }
        }
        if (removed != null) {
            try {
                onUnsubscribe(topic, child, subscriber);
            } catch (Exception x) {
                error(getParent(), x);
            }
            if (count == 0) {
                try {
                    onUnsubscribed(topic, child);
                } catch (Exception x) {
                    error(getParent(), x);
                }
            }
            if (subscription == null) {
                try {
                    onUnsubscribed();
                } catch (Exception x) {
                    error(getParent(), x);
                }
            }
            removed.unsubscribe();
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // Protected Methods
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Use this in the declareDefaults method to create a non-removable child.  This is only called
     * on the default instance.  Runtime instances clone the declared defaults found on the default
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
     * Use this in the declareDefaults method to create a non-removable child.  This is only called
     * on the default instance.  Runtime instances clone the declared defaults found on the default
     * instance.
     *
     * @return Info for the newly created child.
     * @see #declareDefaults()
     */
    protected DSInfo declareDefault(String name, DSIObject value, String description) {
        if (!isDefaultInstance()) {
            throw new IllegalStateException("Can only called on default instances");
        }
        DSInfo ret = add(name, value).setPermanent(true);
        ret.getMetadata().setDescription(description);
        return ret;
    }

    /**
     * The is only called once for each class.  It's purpose is to define the default children of
     * the node subtype.  Use the declareDefault method to add non-removable children that all
     * runtime instances should have.  Be sure to call super.declareDefaults().
     *
     * @see #declareDefault(String, DSIObject) To create non-removable children.
     */
    protected void declareDefaults() {
    }

    /**
     * Notifies subscribers of the event.
     *
     * @param event Must not be null.
     * @param child Can be null.
     */
    protected void fire(DSIEvent event, DSInfo child) {
        if (event == null) {
            throw new NullPointerException("Null event");
        }
        Subscription sub = subscription;
        while (sub != null) {
            if (sub.shouldFire(child, event.getTopic())) {
                try {
                    sub.subscriber.onEvent(this, child, event);
                } catch (Exception x) {
                    error(getPath(), x);
                }
            }
            sub = sub.next;
        }
    }

    /**
     * Delegates to an ancestral node.
     */
    @Override
    protected String getLogName() {
        DSNode parent = getParent();
        if (parent != null) {
            return parent.getLogName();
        }
        return super.getLogName();
    }

    /**
     * Convenience that appends the given string to the ancestral log name with an interleaving
     * period.
     */
    protected String getLogName(String name) {
        String s = null;
        DSNode parent = getParent();
        if (parent != null) {
            s = parent.getLogName();
        }
        if (s == null) {
            return name;
        }
        if (name.startsWith(".") || s.endsWith(".")) {
            return s + name;
        }
        return s + '.' + name;
    }

    /**
     * True if this is the default instance for the type.
     */
    protected final boolean isDefaultInstance() {
        return defaultInstance == defaultDefaultInstance;
    }

    /**
     * Convenience for instanceof DSNode.
     */
    protected static boolean isNode(Object obj) {
        return obj instanceof DSNode;
    }

    /**
     * A convenience for determining null.  First, == null is tested, otherwise if the parameter is
     * a DSIObject or a DSInfo, arg.isNull() is returned.
     *
     * @param obj Can be anything, but DSIObjects and DSInfos will be tested with their isNull
     *            method.
     * @return True if the parameter is null.
     */
    protected static boolean isNull(Object obj) {
        if (obj == null) {
            return true;
        }
        if (obj instanceof DSIObject) {
            return ((DSIObject) obj).isNull();
        } else if (obj instanceof DSInfo) {
            return ((DSInfo) obj).isNull();
        }
        return false;
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
     * Called once this node is stable, but before stable is called on children.
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
     * Called for every subscription.
     *
     * @param topic      Will not be null.
     * @param child      Can be null.
     * @param subscriber Will not be null.
     */
    protected void onSubscribe(DSTopic topic, DSInfo child, DSISubscriber subscriber) {
        //if qos needed, make it accessible via the subscriber implementing some other interface
    }

    /**
     * Called when this node transitions from having no subscriptions to having a subscription of
     * any kind.
     */
    protected void onSubscribed() {
    }

    /**
     * Called when the child and topic pair transitions from having no subscriptions to have a
     * subscription.
     *
     * @param child Can be null, which indicates node subscriptions.
     */
    protected void onSubscribed(DSTopic topic, DSInfo child) {
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
     * Called for every unsubscribe.
     *
     * @param topic      Will not be null.
     * @param child      Can be null.
     * @param subscriber Will not be null.
     */
    protected void onUnsubscribe(DSTopic topic, DSInfo child, DSISubscriber subscriber) {
    }

    /**
     * Called when this node transitions to having no subscriptions of any kind.
     */
    protected void onUnsubscribed() {
    }

    /**
     * Called when the child and topic pair transitions to having no subscriptions.
     *
     * @param topic Can not be null.
     * @param child Can be null.
     */
    protected void onUnsubscribed(DSTopic topic, DSInfo child) {
    }

    /**
     * A convenience that casts the argument to a node.
     */
    protected static DSNode toNode(Object obj) {
        return (DSNode) obj;
    }

    /**
     * Override point, throw a meaningful IllegalArgumentException if the child is not allowed
     */
    protected void validateChild(DSIObject obj) {
    }

    /**
     * Override point, throw a meaningful IllegalArgumentException if the parent is not allowed
     */
    protected void validateParent(DSNode node) {
    }

    ///////////////////////////////////////////////////////////////////////////
    // Package / Private Methods
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Adds the info to internal collections, sets parent related fields on child nodes.
     */
    void add(final DSInfo info) {
        dsInit();
        synchronized (mutex) {
            if (lastChild != null) {
                lastChild.next = info;
                info.prev = lastChild;
                lastChild = info;
            } else {
                firstChild = info;
                lastChild = info;
            }
            if (childMap != null) {
                childMap.put(info.getName(), info);
            }
            size++;
        }
        info.setParent(this);
        DSIObject val = info.getObject();
        if (val instanceof DSNode) {
            DSNode node = (DSNode) val;
            node.infoInParent = info;
        }
        if (isRunning()) {
            try {
                onChildAdded(info);
            } catch (Exception x) {
                error(getPath(), x);
            }
            fire(DSInfoTopic.Event.CHILD_ADDED, info);
        }
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
                    error(getPath(), x);
                    if (DSRegistry.getDefault(getClass()) == defaultDefaultInstance) {
                        DSRegistry.removeDefault(getClass());
                    }
                }
            }
            DSInfo info = defaultInstance.firstChild;
            while (info != null) {
                add(new DSInfoProxy(info));
                info = info.next();
            }
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // Inner Classes
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Iterates all children.
     */
    private class ChildIterator implements Iterator<DSInfo> {

        private DSInfo last;
        private DSInfo next;

        ChildIterator() {
            next = getFirstInfo();
        }

        public boolean hasNext() {
            return next != null;
        }

        public DSInfo next() {
            last = next;
            next = next.next;
            return last;
        }

        public void remove() {
            if (last == null) {
                throw new NullPointerException();
            }
            DSNode.this.remove(last);
        }
    } //ChildIterator

    /**
     * Iterates only node children. Does not support the remove operation.
     */
    private class NodeIterator implements Iterator<DSInfo> {

        private DSInfo last;
        private DSInfo next;

        NodeIterator() {
            if (firstChild.isNode()) {
                next = firstChild;
            } else {
                next = firstChild.nextNode();
            }
        }

        public boolean hasNext() {
            return next != null;
        }

        public DSInfo next() {
            last = next;
            next = next.nextNode();
            return last;
        }

        public void remove() {
            if (last == null) {
                throw new NullPointerException();
            }
            DSNode.this.remove(last);
        }
    } //NodeIterator

    private class Subscription {

        DSInfo child;
        Subscription next;
        DSISubscriber subscriber;
        DSTopic topic;

        Subscription(DSInfo child, DSISubscriber subscriber, DSTopic topic) {
            this.child = child;
            this.subscriber = subscriber;
            this.topic = topic;
        }

        boolean equals(DSInfo child, DSTopic topic) {
            if (!DSUtil.equal(topic, this.topic)) {
                return false;
            }
            return DSUtil.equal(child, this.child);
        }

        boolean shouldFire(DSInfo child, DSTopic topic) {
            if (!DSUtil.equal(topic, this.topic)) {
                return false;
            }
            if (topic == VALUE_TOPIC) {
                return DSUtil.equal(child, this.child);
            }
            return true;
        }

        boolean shouldUnsubscribe(DSInfo child) {
            if (topic == VALUE_TOPIC) {
                return DSUtil.equal(child, this.child);
            }
            return false;
        }

        void unsubscribe() {
            try {
                subscriber.onUnsubscribed(topic, DSNode.this, child);
            } catch (Exception x) {
                error(getPath(), x);
            }
        }

    } //Subscription

    /**
     * Iterates only value children.
     */
    private class ValueIterator implements Iterator<DSInfo> {

        private DSInfo last;
        private DSInfo next;

        ValueIterator() {
            if (firstChild.isValue()) {
                next = firstChild;
            } else {
                next = firstChild.nextValue();
            }
        }

        public boolean hasNext() {
            return next != null;
        }

        public DSInfo next() {
            last = next;
            next = next.nextValue();
            return last;
        }

        public void remove() {
            if (last == null) {
                throw new NullPointerException();
            }
            DSNode.this.remove(last);
        }
    } //ValueIterator

}
