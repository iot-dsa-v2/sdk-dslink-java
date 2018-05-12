package org.iot.dsa.node;

import java.util.Iterator;
import org.iot.dsa.dslink.responder.ApiObject;
import org.iot.dsa.node.action.DSAbstractAction;
import org.iot.dsa.node.action.DSAction;
import org.iot.dsa.node.event.DSInfoTopic;
import org.iot.dsa.util.DSUtil;

/**
 * All node children have corresponding DSInfo instances. This type serves two purposes:
 * <ul>
 * <li>It carries some meta-data about the relationship between the parent node and the
 * child.
 * <li>It tracks whether or not the child matches a declared default.
 * </ul>
 * <p>
 * Important things for developers to know about DSInfo are:
 * <ul>
 * <li>You can configure state such as transient, readonly and hidden.
 * <li>You can declare fields in the your Java class for default infos to avoid looking up
 * the child every time it is needed.  This is can be used to create fast getters and
 * setters.
 * </ul>
 *
 * @author Aaron Hansen
 */
public class DSInfo implements ApiObject {

    ///////////////////////////////////////////////////////////////////////////
    // Constants
    ///////////////////////////////////////////////////////////////////////////

    static final int ADMIN = 0;
    static final int DEFAULT_ON_COPY = 5;
    static final int HIDDEN = 1;
    static final int PERMANENT = 4;
    static final int READONLY = 3;
    static final int TRANSIENT = 2;

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

    protected DSInfo(String name, DSIObject value) {
        this.name = name;
        setObject(value);
    }

    ///////////////////////////////////////////////////////////////////////////
    // Methods
    ///////////////////////////////////////////////////////////////////////////

    /**
     * This is only called by DSNode.copy.  Therefore, this will already
     * be populated with the default value.
     */
    void copy(DSInfo info) {
        flags = info.flags;
        name = info.name;
        if (isDefaultOnCopy()) {
            return;
        }
        if (info.value != null) {
            setObject(info.value.copy());
        }
    }

    public DSInfo copy() {
        DSInfo ret = new DSInfo();
        ret.flags = flags;
        ret.name = name;
        if (isDefaultOnCopy()) {
            DSIObject val = getDefaultObject();
            if (val != null) {
                ret.setObject(val.copy());
                return ret;
            }
        }
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
            return isEqual((DSInfo) arg);
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
        if ((parent != null) && (parent.isRunning())) {
            parent.fire(DSInfoTopic.INSTANCE, DSInfoTopic.Event.METADATA_CHANGED, this);
        }
    }

    @Override
    public DSAbstractAction getAction() {
        return (DSAbstractAction) value;
    }

    @Override
    public Iterator<ApiObject> getChildren() {
        return new ApiIterator(getNode().iterator());
    }

    /**
     * If this represents a dynamic child, this just returns the current value.
     */
    public DSIObject getDefaultObject() {
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

    public String getName() {
        return name;
    }

    @Override
    public void getMetadata(DSMap bucket) {
        DSMetadata.getMetadata(this, bucket);
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

    @Override
    public int hashCode() {
        return System.identityHashCode(this);
    }

    /**
     * True if there is another info after this one.
     */
    public boolean hasNext() {
        return next != null;
    }

    @Override
    public boolean isAction() {
        return value instanceof DSAbstractAction;
    }

    @Override
    public boolean isAdmin() {
        return getFlag(ADMIN);
    }

    /**
     * Whether or not the current value, or the default value is copied.
     */
    public boolean isDefaultOnCopy() {
        return getFlag(DEFAULT_ON_COPY);
    }

    /**
     * Whether or not this info represents a declared default.
     */
    public boolean isDynamic() {
        return !getFlag(PERMANENT);
    }

    /**
     * True if the flags and target object are equal (not identical if the target is a node).  Two
     * nodes are considered equal if they have the same children, although they may be ordered
     * differently.
     */
    public boolean isEqual(DSInfo arg) {
        if (arg == this) {
            return true;
        } else if (arg == null) {
            return false;
        } else if (getFlags() != arg.getFlags()) {
            return false;
        } else if (!DSUtil.equal(arg.getName(), getName())) {
            return false;
        }
        if (isNode()) {
            return getNode().isEqual(arg.getObject());
        }
        return DSUtil.equal(getObject(), arg.getObject());
    }

    /**
     * Whether or not an object is visible to clients.
     */
    @Override
    public boolean isHidden() {
        return getFlag(HIDDEN);
    }

    /**
     * True if the flags and target object are identical.  Two nodes are identical if their children
     * are in the same order.
     */
    public boolean isIdentical(DSInfo arg) {
        if (arg == this) {
            return true;
        } else if (arg == null) {
            return false;
        } else if (getFlags() != arg.getFlags()) {
            return false;
        } else if (!DSUtil.equal(arg.getName(), getName())) {
            return false;
        }
        if (isNode()) {
            return getNode().isIdentical(arg.getObject());
        }
        return DSUtil.equal(getObject(), arg.getObject());
    }

    /**
     * Whether or not the object is a DSNode.
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

    public DSInfo setAdmin(boolean admin) {
        setFlag(ADMIN, admin);
        return this;
    }

    DSInfo setFlag(int position, boolean on) {
        fireInfoChanged();
        flags = DSUtil.setBit(flags, position, on);
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

}
