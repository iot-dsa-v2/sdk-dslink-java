package org.iot.dsa.node;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import org.iot.dsa.dslink.DSIResponder;
import org.iot.dsa.dslink.responder.InboundSetRequest;
import org.iot.dsa.logging.DSLogger;
import org.iot.dsa.node.action.ActionInvocation;
import org.iot.dsa.node.action.ActionResult;
import org.iot.dsa.node.action.DSAction;
import org.iot.dsa.node.action.DSISetAction;
import org.iot.dsa.node.action.DeleteAction;
import org.iot.dsa.node.action.DuplicateAction;
import org.iot.dsa.node.action.RenameAction;
import org.iot.dsa.node.event.DSEvent;
import org.iot.dsa.node.event.DSEventFilter;
import org.iot.dsa.node.event.DSISubscriber;
import org.iot.dsa.node.event.DSISubscription;
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

    /**
     * Event ID.
     */
    public static final String CHILD_ADDED = "CHILD_ADDED";

    /**
     * Singleton instance, fired whenever a child is added. There will be a child info, but
     * not data.
     */
    public static final DSEvent CHILD_ADDED_EVENT = new DSEvent(CHILD_ADDED);

    /**
     * Event ID.
     */
    public static final String CHILD_REMOVED = "CHILD_REMOVED";

    public static final DSEvent CHILD_REMOVED_EVENT = new DSEvent(CHILD_REMOVED);

    /**
     * Event ID.
     */
    public static final String CHILD_RENAMED = "CHILD_RENAMED";

    /**
     * Singleton instance, fired whenever a child is renamed. There will be a child info, and
     * the data will be a DSString representing the old name.
     */
    public static final DSEvent CHILD_RENAMED_EVENT = new DSEvent(CHILD_RENAMED);

    /**
     * Event ID.
     */
    public static final String METADATA_CHANGED = "METADATA_CHANGED";

    /**
     * Singleton instance, fired whenever metadata changes. There may be a child info, but no
     * data accompanying this event.
     */
    public static final DSEvent METADATA_CHANGED_EVENT = new DSEvent(METADATA_CHANGED);

    /**
     * Event ID.
     */
    public static final String VALUE_CHANGED = "VALUE_CHANGED";

    /**
     * Singleton instance, fired whenever a child value changes, as well as when nodes that
     * implement DSIValue change.  There may be a child info, the data will be the element value.
     */
    public static final DSEvent VALUE_CHANGED_EVENT = new DSEvent(VALUE_CHANGED);

    //Prevents infinite loops when initializing default instances.
    static final DSNode defaultDefaultInstance = new DSNode();
    private static final int MAP_THRESHOLD = 5;
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
        if ((name == null) || name.isEmpty()) {
            throw new NullPointerException("Illegal name: " + name);
        }
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
     * Override point, used to determine whether or not to expose the duplicate action.  The
     * default implementation returns false if the child is permanent.
     */
    public boolean canDuplicate(DSInfo child) {
        return !child.isPermanent();
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
            if (!info.isPermanent()) {
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
     * True if the arg is a DSNode with equivalent children in the same order.
     * DSNode.equivalent will be used on child nodes, equals will be used on everything else.
     *
     * @see DSInfo#equivalent(Object)
     */
    public boolean equivalent(Object arg) {
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
        DSInfo myInfo = getFirstInfo();
        DSInfo argInfo = argNode.getFirstInfo();
        while (myInfo != null) {
            if (!myInfo.equivalent(argInfo)) {
                return false;
            }
            myInfo = myInfo.next();
            argInfo = argInfo.next();
        }
        return true;
    }

    /**
     * Returns the child value with the given name, or null.
     *
     * @return Possibly null.
     */
    public DSIObject get(String name) {
        DSInfo info = getInfo(name);
        if (info != null) {
            return info.get();
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
     * A convenience for getValue(name).toElement().
     */
    public DSElement getElement(String name) {
        return getValue(name).toElement();
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
        return info.get();
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
     * Returns the info for the child or dynamic action with the given name, or null.
     *
     * @return Possibly null.
     */
    public DSInfo getInfo(String name) {
        dsInit();
        if (firstChild == null) {
            if (getInfo() == null) {
                return null;
            } else {
                return getVirtualAction(getInfo(), name);
            }
        }
        synchronized (mutex) {
            if (size >= MAP_THRESHOLD) {
                if (childMap == null) {
                    childMap = new TreeMap<>();
                    for (DSInfo info = firstChild; info != null; info = info.next()) {
                        childMap.put(info.getName(), info);
                    }
                }
                DSInfo ret = childMap.get(name);
                if (ret != null) {
                    return ret;
                } else {
                    if (getInfo() == null) {
                        return null;
                    } else {
                        return getVirtualAction(getInfo(), name);
                    }
                }
            }
        }
        DSInfo info = firstChild;
        while (info != null) {
            if (info.getName().equals(name)) {
                return info;
            }
            info = info.next();
        }
        if (getInfo() == null) {
            return null;
        } else {
            return getVirtualAction(getInfo(), name);
        }
    }

    /**
     * The last child, or null.
     */
    public DSIObject getLast() {
        DSInfo info = getLastInfo();
        if (info == null) {
            return null;
        }
        return info.get();
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
     * Ascends the tree to the root node.  Does not cache the result.
     */
    public DSNode getRootNode() {
        DSNode ret = this;
        DSNode tmp = getParent();
        while (tmp != null) {
            ret = tmp;
            tmp = tmp.getParent();
        }
        return ret;
    }

    /**
     * A convenience for (DSIValue) get(name).
     */
    public DSIValue getValue(String name) {
        return (DSIValue) get(name);
    }

    /**
     * Should return an info for the dynamic action on the given target.  Overrides should
     * call super to for the default edit actions.  The override is free to modify what the
     * default implementation returns.
     *
     * @param target Could be the info for this node, or the info of a non-node value child.
     * @param name   The name of the action.
     * @return DSInfo for the desired action.
     * @see #virtualInfo(String, DSAction)
     */
    public DSInfo getVirtualAction(DSInfo target, String name) {
        DSInfo info = null;
        if (target.is(DSISetAction.class)) {
            DSISetAction sa = (DSISetAction) target.get();
            if (name.equals(sa.getSetActionName())) {
                if (target.getFlag(DSInfo.READONLY)) {
                    throw new IllegalStateException("Value is readonly: " + name);
                }
                return virtualInfo(sa.getSetActionName(), sa.getSetAction());
            }
        }
        if (!target.isPermanent()) {
            switch (name) {
                case DeleteAction.DELETE:
                    info = virtualInfo(name, DeleteAction.INSTANCE);
                    break;
                case RenameAction.RENAME:
                    info = virtualInfo(name, RenameAction.INSTANCE);
                    break;
            }
        }
        if (info == null) {
            if (name.equals(DuplicateAction.DUPLICATE) && canDuplicate(target)) {
                info = virtualInfo(name, DuplicateAction.INSTANCE);
            }
        }
        if (info != null) {
            info.getMetadata().setActionGroup(DSAction.EDIT_GROUP, null);
        }
        return info;
    }

    /**
     * Adds all action names for the given target to the bucket.  Overrides should call super
     * for the default edit actions (such as delete, rename, and duplicate).  The override
     * is free to modify the default implementation returns.
     *
     * @param target Could be the info for this node, or the info of a non-node value child.
     * @param bucket Where to add action names.  Actions for this node must have unique names among
     *               all children of the node.
     */
    public void getVirtualActions(DSInfo target, Collection<String> bucket) {
        if (target.isNode()) {
            if (target.getParent() == this) {
                target.getNode().getVirtualActions(target, bucket);
                return;
            } else if (target.getNode() != this) {
                throw new IllegalArgumentException("DSInfo target is from another node.");
            } else if (target.getParent() == null) {
                return; //no edit actions on root node
            }
        } else if (target.is(DSISetAction.class)) {
            if (!target.getFlag(DSInfo.READONLY)) {
                bucket.add(((DSISetAction) target.get()).getSetActionName());
            }
        }
        if (!target.isPermanent()) {
            bucket.add(DeleteAction.DELETE);
            bucket.add(RenameAction.RENAME);
            bucket.add(DuplicateAction.DUPLICATE);
        }
        if (canDuplicate(target)) {
            bucket.add(DuplicateAction.DUPLICATE);
        }
    }

    /**
     * Override point.  It is safe to use the calling thread for long lived operations.  By
     * default, this routes the request to the action's invoke method.
     *
     * @param action  Info for the action being invoked.
     * @param target  Info for the target (parent) of the action.  Could be this node,
     *                or the info of a child that is a non-node value.
     * @param request Details about the incoming invoke as well as the mechanism to send updates
     *                over an open stream.
     * @return It is okay to return null if the action result type is void.
     * @throws IllegalStateException If the nothing handles an incoming request.
     * @see DSAction#invoke(DSInfo, ActionInvocation)
     */
    public ActionResult invoke(DSInfo action, DSInfo target, ActionInvocation request) {
        trace(trace() ? String
                .format("action=%s, target=%s, params=%s", action, target, request.getParameters())
                      : null);
        return action.getAction().invoke(target, request);
    }

    /**
     * True if this is the default instance for the type.
     */
    public final boolean isDefaultInstance() {
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
     * Replaces the existing child.
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
        DSIObject old = info.get();
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
                if (info.isValue()) {
                    fire(VALUE_CHANGED_EVENT, info, info.getElement());
                } else {
                    fire(VALUE_CHANGED_EVENT, info, null);
                }
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
        if (info.isDeclared()) {
            throw new IllegalStateException("Can not be removed");
        }
        if (info.isVirtual()) {
            return this;
        }
        if (info.getParent() != this) {
            throw new IllegalStateException("Not a child of this container");
        }
        synchronized (mutex) {
            if (childMap != null) {
                childMap.remove(info.getName());
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
        if (info.isNode()) {
            DSNode node = info.getNode();
            if (isRunning()) {
                notifyRemoved(node);
            }
            node.infoInParent = null;
            node.stop();
        }
        if (isRunning()) {
            try {
                onChildRemoved(info);
            } catch (Exception x) {
                error(getPath(), x);
            }
            fire(CHILD_REMOVED_EVENT, info, null);
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
     * Rename the child in place rather than removing and re-adding with the new name.
     */
    public void rename(DSInfo info, String newName) {
        String old;
        synchronized (mutex) {
            if (info.getParent() != this) {
                throw new IllegalArgumentException("Info not parented by this node");
            }
            if (info.isDeclared()) {
                throw new IllegalArgumentException("Cannot rename declared children");
            }
            if (newName == null) {
                throw new IllegalArgumentException("Missing new name");
            }
            if (newName.isEmpty()) {
                throw new IllegalArgumentException("New name is empty");
            }
            if (newName.equals(info.getName())) {
                return;
            }
            if (contains(newName)) {
                throw new IllegalArgumentException("New name already in use: " + newName);
            }
            old = info.getName();
            info.setName(newName);
            childMap = null;
            if (info.isNode()) {
                try {
                    info.getNode().onRenamed(old);
                } catch (Exception x) {
                    error(old, x);
                }
            }
            try {
                onRenamed(info, old);
            } catch (Exception x) {
                error(old, x);
            }
        }
        fire(CHILD_RENAMED_EVENT, info, DSString.valueOf(old));
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
        setLogger(null);
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
        synchronized (mutex) {
            Subscription sub = subscription;
            while (sub != null) {
                sub.close();
                sub = sub.next;
            }
            subscription = null;
        }
        state = DSUtil.setBit(state, STATE_STABLE, false);
        state = DSUtil.setBit(state, STATE_STOPPED, true);
        try {
            onStopped();
        } catch (Exception x) {
            debug(getPath(), x);
        }
    }

    /**
     * This is a convenience that creates a filter for the given event and or child.  Only non-null
     * events and children are filtered.
     *
     * @param subscriber Required.
     * @param event      Optional.
     * @param child      Optional.
     */
    public DSISubscription subscribe(DSISubscriber subscriber, DSEvent event, DSInfo child) {
        return subscribe(new DSEventFilter(subscriber, event, child));
    }

    ///////////////////////////////////////////////////////////////////////////
    // Protected Methods
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Only creates a subscription if not already subscribed.
     *
     * @param subscriber Required.
     */
    public DSISubscription subscribe(DSISubscriber subscriber) {
        if (subscriber == null) {
            throw new NullPointerException("Null subscriber");
        }
        boolean firstSubscription = subscription == null;
        Subscription sub = null;
        synchronized (mutex) {
            sub = subscription;
            while (sub != null) {
                if (sub == subscriber) {
                    return sub;
                }
                sub = sub.next;
            }
            sub = new Subscription(subscriber);
            sub.next = subscription;
            subscription = sub;
        }
        try {
            onSubscribe(sub);
        } catch (Exception x) {
            error(getParent(), x);
        }
        if (firstSubscription) {
            try {
                onSubscribed();
            } catch (Exception x) {
                error(getParent(), x);
            }
        }
        return sub;
    }

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
        return add(name, value).setDeclared(true);
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
        DSInfo ret = add(name, value).setDeclared(true);
        ret.getMetadata().setDescription(description);
        return ret;
    }

    /**
     * This only called once for each class.  It's purpose is to define the default children of
     * the node subtype.  Use the declareDefault method to add permanent children that all
     * runtime instances will have.  Be sure to call super.declareDefaults().
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
     * @param data  Can be null.
     */
    protected void fire(DSEvent event, DSInfo child, DSIValue data) {
        if (event == null) {
            throw new NullPointerException("Null event");
        }
        trace(trace() ? String
                .format("event=%s, child=%s, data=%s", event.getEventId(), child, data) : null);
        Subscription sub = subscription;
        while (sub != null) {
            try {
                sub.getSubscriber().onEvent(event, this, child, data);
            } catch (Exception x) {
                error(getPath(), x);
            }
            sub = sub.next;
        }
    }

    /**
     * Delegates to an ancestral node.
     */
    @Override
    protected String getLogName() {
        if (getParent() != null) {
            return "dsa" + getPath()
                    .replace('.', '_')
                    .replace(' ', '_')
                    .replace('/', '.');
        }
        return "dsa";
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
     * Called when the given child is added and this is running.
     */
    protected void onChildAdded(DSInfo info) {
    }

    /**
     * Called when the given child is changed and this is running.
     */
    protected void onChildChanged(DSInfo info) {
    }

    /**
     * Called when the given child is removed and this is running.  This subtree will be notified
     * via onRemoved before this is call.
     *
     * @param info The reference to this node as the parent will have already been cleared.
     */
    protected void onChildRemoved(DSInfo info) {
    }

    /**
     * Called when the given info is modified and this is running.
     */
    protected void onInfoChanged(DSInfo info) {
    }

    /**
     * Called when the node or one of its ancestors is being removed from the tree.
     * Called on children first, and called before onStopped.
     */
    protected void onRemoved() {
    }

    /**
     * Called when this node is renamed in it's parent.
     */
    protected void onRenamed(String oldName) {
    }

    /**
     * Called when a child is renamed.
     */
    protected void onRenamed(DSInfo child, String oldName) {
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
     */
    protected void onSubscribe(Subscription subscription) {
    }

    /**
     * Called when this node transitions from having no subscriptions to having a subscription of
     * any kind.
     */
    protected void onSubscribed() {
    }

    /**
     * Called for every unsubscribe.
     */
    protected void onUnsubscribe(Subscription subscription) {
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
     * Called when this node transitions to having no subscriptions of any kind.
     */
    protected void onUnsubscribed() {
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

    /**
     * Use to create DSInfos when overriding getVirtualAction(s).
     */
    protected DSInfo virtualInfo(String name, DSAction target) {
        return new VirtualInfo(name, target).setParent(this);
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
        DSIObject val = info.get();
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
            fire(CHILD_ADDED_EVENT, info, null);
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
    // Private Methods
    ///////////////////////////////////////////////////////////////////////////

    private void notifyRemoved(DSNode node) {
        DSInfo info = node.getFirstNodeInfo();
        while (info != null) {
            notifyRemoved(info.getNode());
            info = info.nextNode();
        }
        try {
            node.onRemoved();
        } catch (Exception x) {
            error(getPath(), x);
        }
    }

    private void remove(Subscription toRemove) {
        Subscription removed = null;
        synchronized (mutex) {
            Subscription sub = subscription;
            Subscription prev = null;
            while (sub != null) {
                if (sub == toRemove) {
                    removed = sub;
                    if (prev == null) {
                        subscription = subscription.next;
                    } else {
                        prev.next = sub.next;
                    }
                }
                prev = sub;
                sub = sub.next;
            }
        }
        if (removed != null) {
            try {
                onUnsubscribe(removed);
            } catch (Exception x) {
                error(getParent(), x);
            }
            if (subscription == null) {
                try {
                    onUnsubscribed();
                } catch (Exception x) {
                    error(getParent(), x);
                }
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

    /**
     * Encapsulates the details of a subscription.
     */
    private class Subscription implements DSISubscription {

        Subscription next;
        boolean open = true;
        DSISubscriber subscriber;

        Subscription(DSISubscriber subscriber) {
            this.subscriber = subscriber;
        }

        @Override
        public void close() {
            synchronized (this) {
                if (!open) {
                    return;
                }
                open = false;
            }
            remove(this);
            try {
                subscriber.onClosed(this);
            } catch (Exception x) {
                error(getPath(), x);
            }
        }

        @Override
        public DSNode getNode() {
            return DSNode.this;
        }

        @Override
        public DSISubscriber getSubscriber() {
            return subscriber;
        }

        @Override
        public boolean isOpen() {
            return open;
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
