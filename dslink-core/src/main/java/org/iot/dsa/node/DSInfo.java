package org.iot.dsa.node;

import java.util.Iterator;
import org.iot.dsa.dslink.responder.ApiObject;
import org.iot.dsa.node.action.DSAction;
import org.iot.dsa.util.DSUtil;

/**
 * All node children have corresponding DSInfo instances. This type serves two purposes:
 *
 * <p>
 *
 * <ul>
 *
 * <li>It carries some meta-data about the relationship between the parent node and the child.
 *
 * <li>It tracks whether or not the child matches a declared default.
 *
 * </ul>
 *
 * <p>
 *
 * Important things for developers to know about DSInfo are:
 *
 * <p>
 *
 * <ul>
 *
 * <li>You can configure state such as transient, readonly and hidden.
 *
 * <li>You can declare fields in the your Java class for default infos to avoid looking up the child
 * every time it is needed.  This is can be used to create fast getters and setters.
 *
 * </ul>
 *
 * <p>
 *
 * @author Aaron Hansen
 */
public class DSInfo implements ApiObject, DSISubscriber {

    ///////////////////////////////////////////////////////////////////////////
    // Constants
    ///////////////////////////////////////////////////////////////////////////

    //Config vs admin vs operator
    //
    static final int CONFIG = 0;
    static final int HIDDEN = 1;
    static final int TRANSIENT = 2;
    static final int READONLY = 3;
    static final int PERMANENT = 4;
    //static final int PERMANENTLY_SUBSCRIBED = 5;
    //static final int NO_ADD_REMOVE_CHILDREN = 5;
    //static final int NO_MODIFY_FLAGS = 5;
    //static final int NO_AUDIT      = 5;

    ///////////////////////////////////////////////////////////////////////////
    // Fields
    ///////////////////////////////////////////////////////////////////////////

    int flags = 0;
    String name;
    DSInfo next;
    DSNode parent;
    DSInfo prev;
    DSIObject value;

    ///////////////////////////////////////////////////////////////////////////
    // Constructors
    ///////////////////////////////////////////////////////////////////////////

    DSInfo() {
    }

    DSInfo(String name, DSIObject value) {
        this.name = name;
        setObject(value);
    }

    ///////////////////////////////////////////////////////////////////////////
    // Methods
    ///////////////////////////////////////////////////////////////////////////

    public DSInfo copy() {
        DSInfo ret = new DSInfo();
        ret.flags = flags;
        ret.name = name;
        if (value != null) {
            ret.setObject(value.copy());
        }
        return ret;
    }

    public void decodeState(DSElement state) {
        flags = state.toInt();
    }

    public DSElement encodeState() {
        return DSLong.valueOf(flags);
    }

    @Override
    public boolean equals(Object arg) {
        if (arg instanceof DSInfo) {
            return equivalent((DSInfo) arg);
        }
        return false;
    }

    /**
     * True if this proxies a default and the state and value match the default.
     */
    public boolean equalsDefault() {
        return false;
    }

    /**
     * True if the state matches the default state.
     */
    public boolean equalsDefaultState() {
        return flags == 0;
    }

    /**
     * True if this proxies a default and the value type matches the default.
     */
    public boolean equalsDefaultType() {
        return false;
    }

    /**
     * True if this proxies a default and the value matches the default.
     */
    public boolean equalsDefaultValue() {
        return false;
    }

    private boolean equivalent(DSInfo arg) {
        if (getFlags() != arg.getFlags()) {
            return false;
        } else if (!DSUtil.equal(arg.getName(), getName())) {
            return false;
        }
        return DSUtil.equal(getObject(), arg.getObject());
    }

    private void fireInfoChanged() {
        if (parent != null) {
            parent.infoChanged(this);
        }
    }

    @Override
    public DSAction getAction() {
        return (DSAction) value;
    }

    @Override
    public Iterator<ApiObject> getChildren() {
        return new ApiIterator(getNode().iterator());
    }

    /**
     * If this represents a dynamic child, this just returns the current value.
     */
    public DSIObject getDefaultObject() { //TODO Maybe dynamic should return null.
        if (value == null) {
            return null;
        }
        return value;
    }

    /**
     * A convenience that casts getObject().
     */
    public DSElement getElement() {
        return (DSElement) value;
    }

    boolean getFlag(int position) {
        return DSUtil.getBit(flags, position);
    }

    int getFlags() {
        return flags;
    }

    @Override
    public void getMetadata(DSMap bucket) {
        if (value instanceof DSIMetadata) {
            ((DSIMetadata) value).getMetadata(bucket);
        }
        if (value instanceof DSNode) {
            ((DSNode) value).getMetadata(this, bucket);
        }
    }

    public String getName() {
        return name;
    }

    /**
     * A convenience that casts getObject().
     */
    public DSNode getNode() {
        return (DSNode) value;
    }

    public DSIObject getObject() {
        return value;
    }

    public DSNode getParent() {
        return parent;
    }

    /**
     * A convenience that casts getObject().
     */
    @Override
    public DSIValue getValue() {
        return (DSIValue) value;
    }

    @Override
    public boolean hasChildren() {
        if (isNode()) {
            return getNode().childCount() > 0;
        }
        return false;
    }

    /**
     * True if there is another info after this one.
     */
    public boolean hasNext() {
        return next != null;
    }

    @Override
    public int hashCode() {
        int hc = flags;
        hc ^= getName().hashCode();
        Object obj = getObject();
        hc ^= (obj == null) ? 0 : obj.hashCode();
        return hc;
    }

    @Override
    public boolean isAction() {
        return value instanceof DSAction;
    }

    @Override
    public boolean isConfig() {
        return getFlag(CONFIG);
    }

    /**
     * Whether or not this info represents a declared default.
     */
    public boolean isDynamic() {
        return !getFlag(PERMANENT);
    }

    /**
     * Whether or not an object is visible to clients.
     */
    @Override
    public boolean isHidden() {
        return getFlag(HIDDEN);
    }

    /**
     * Whether or not the object is a DSNode.
     * @return
     */
    public boolean isNode() {
        return value instanceof DSNode;
    }

    /**
     * Quick test for a proxy info.
     */
    boolean isProxy() {
        return false;
    }

    /**
     * Whether or not an object can be written by a client.
     */
    public boolean isReadOnly() {
        return getFlag(READONLY);
    }

    /**
     * Whether or not an object is persistent.
     */
    public boolean isTransient() {
        return getFlag(TRANSIENT);
    }

    @Override
    public boolean isValue() {
        return value instanceof DSIValue;
    }

    /**
     * The next info in the parent node.
     */
    public DSInfo next() {
        return next;
    }

    /**
     * The next DSInfo in the parent that is an action, or null.
     */
    public DSInfo nextAction() {
        DSInfo cur = next;
        while (cur != null) {
            if (cur.isAction()) {
                return cur;
            }
            cur = cur.next();
        }
        return cur;
    }

    /**
     * The next DSInfo in the parent that is a node, or null.
     */
    public DSInfo nextNode() {
        DSInfo cur = next;
        while (cur != null) {
            if (cur.isNode()) {
                return cur;
            }
            cur = cur.next();
        }
        return cur;
    }

    /**
     * The next DSInfo in the parent that is a value, or null.
     */
    public DSInfo nextValue() {
        DSInfo cur = next;
        while (cur != null) {
            if (cur.isValue()) {
                return cur;
            }
            cur = cur.next();
        }
        return cur;
    }

    /**
     * Routes events from child DSIPublishers to onChildChanged in the parent.
     */
    @Override
    public void onEvent(DSIObject publisher, DSInfo child, DSIPublisher.Event event) {
        if (parent != null) {
            parent.onChildChanged(this);
        }
    }

    DSInfo setFlag(int position, boolean on) {
        fireInfoChanged();
        flags = DSUtil.setBit(flags, position, on);
        return this;
    }

    public DSInfo setConfig(boolean config) {
        setFlag(CONFIG, config);
        return this;
    }

    public DSInfo setHidden(boolean hidden) {
        setFlag(HIDDEN, hidden);
        return this;
    }

    DSInfo setName(String arg) {
        this.name = arg;
        return this;
    }

    DSInfo setObject(DSIObject arg) {
        this.value = arg;
        return this;
    }

    DSInfo setParent(DSNode arg) {
        this.parent = arg;
        return this;
    }

    DSInfo setPermanent(boolean arg) {
        setFlag(PERMANENT, arg);
        return this;
    }

    public DSInfo setReadOnly(boolean readOnly) {
        setFlag(READONLY, readOnly);
        return this;
    }

    public DSInfo setTransient(boolean trans) {
        setFlag(TRANSIENT, trans);
        return this;
    }

    void subscribe() {
        if (value instanceof DSIPublisher) {
            ((DSIPublisher) value).subscribe(this);
        }
    }

    void unsubscribe() {
        if (value instanceof DSIPublisher) {
            ((DSIPublisher) value).unsubscribe(this);
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // Inner Classes
    ///////////////////////////////////////////////////////////////////////////

    class ApiIterator implements Iterator<ApiObject> {

        Iterator<DSInfo> iterator;

        ApiIterator(Iterator<DSInfo> iterator) {
            this.iterator = iterator;
        }

        public boolean hasNext() {
            return iterator.hasNext();
        }

        public ApiObject next() {
            return iterator.next();
        }

        public void remove() {
            iterator.remove();
        }

    }

    ///////////////////////////////////////////////////////////////////////////
    // Initialization
    ///////////////////////////////////////////////////////////////////////////

} //class
