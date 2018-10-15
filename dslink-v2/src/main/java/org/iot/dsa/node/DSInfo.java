package org.iot.dsa.node;

import java.util.Iterator;
import org.iot.dsa.dslink.responder.ApiObject;
import org.iot.dsa.node.action.DSAbstractAction;
import org.iot.dsa.node.event.DSInfoTopic;
import org.iot.dsa.util.DSUtil;

/**
 * All node children have corresponding DSInfo instances. DSInfo serves two purposes:
 * <ul>
 * <li>It carries meta-data about the relationship between the parent node and the
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
public class DSInfo implements ApiObject, GroupListener {

    ///////////////////////////////////////////////////////////////////////////
    // Constants
    ///////////////////////////////////////////////////////////////////////////

    static final int ADMIN = 0;
    static final int HIDDEN = 1;
    static final int TRANSIENT = 2;
    static final int READONLY = 3;
    static final int PERMANENT = 4;
    static final int DEFAULT_ON_COPY = 5;

    ///////////////////////////////////////////////////////////////////////////
    // Fields
    ///////////////////////////////////////////////////////////////////////////

    int flags = 0;
    DSMap metadata;
    String name;
    DSInfo next;
    DSIObject object;
    DSNode parent;
    DSInfo prev;

    ///////////////////////////////////////////////////////////////////////////
    // Constructors
    ///////////////////////////////////////////////////////////////////////////

    DSInfo() {
    }

    protected DSInfo(String name, DSIObject object) {
        this.name = name;
        setObject(object);
    }

    ///////////////////////////////////////////////////////////////////////////
    // Methods
    ///////////////////////////////////////////////////////////////////////////

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
        if (object != null) {
            ret.setObject(object.copy());
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
     * True if this proxies a default and the metadata matches the default.
     */
    public boolean equalsDefaultMetadata() {
        return (metadata == null);
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

    @Override
    public DSAbstractAction getAction() {
        return (DSAbstractAction) getObject();
    }

    @Override
    public Iterator<ApiObject> getChildren() {
        return new ApiIterator(getNode().iterator());
    }

    /**
     * If this is a proxy , this will return the original default instance.
     */
    public DSIObject getDefaultObject() {
        return getObject();
    }

    /**
     * A convenience that casts the object.  Will call DSIValue.toElement on values.
     */
    public DSElement getElement() {
        DSIObject obj = getObject();
        if (obj instanceof DSElement) {
            return (DSElement) object;
        }
        return ((DSIValue) obj).toElement();
    }

    @Override
    public void getMetadata(DSMap bucket) {
        if (metadata != null) {
            bucket.putAll(metadata);
        }
    }

    /**
     * Use to configure metadata only.  Creates a DSMetadata wrapper, and possibly the metadata map.
     */
    public DSMetadata getMetadata() {
        if (metadata == null) {
            metadata = new DSMap();
        }
        return new DSMetadata(metadata);
    }

    public String getName() {
        return name;
    }

    /**
     * A convenience that casts getObject().
     */
    public DSNode getNode() {
        return (DSNode) getObject();
    }

    public DSIObject getObject() {
        return object;
    }

    public DSNode getParent() {
        return parent;
    }

    /**
     * Concatenates and encodes the path of the parent node and the name of this info.
     *
     * @param buf Can be null in which case a new one will be created.
     * @return The given buf, or the newly created one.
     */
    public StringBuilder getPath(StringBuilder buf) {
        return DSPath.append(DSPath.encodePath(parent, buf), name);
    }

    /**
     * A convenience that casts getObject().
     */
    @Override
    public DSIValue getValue() {
        return (DSIValue) object;
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
        return System.identityHashCode(this);
    }

    /**
     * True if the value of the info is an instance of the give class.
     */
    public boolean is(Class clazz) {
        if (object == null) {
            return false;
        }
        return clazz.isAssignableFrom(object.getClass());
    }

    @Override
    public boolean isAction() {
        return object instanceof DSAbstractAction;
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
        return object instanceof DSNode;
    }

    /**
     * True if the object is null.
     */
    public boolean isNull() {
        if (object == null) {
            return true;
        }
        return object.isNull();
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
        return object instanceof DSIValue;
    }

    public void modified(DSGroup map) {
        //TODO info modified
    }

    /**
     * The next info in the parent node.
     */
    public DSInfo next() {
        return next;
    }

    /**
     * The next DSInfo in the parent whose object is of the given type.
     */
    public DSInfo next(Class is) {
        DSInfo cur = next;
        while (cur != null) {
            if (cur.is(is)) {
                return cur;
            }
            cur = cur.next();
        }
        return cur;
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
     * False by default, set to true if you don't want the child to require admin level
     * permissions.
     */
    public DSInfo setAdmin(boolean admin) {
        setFlag(ADMIN, admin);
        return this;
    }

    /**
     * False by default, set to true if you don't want the child to be sent to clients.
     */
    public DSInfo setHidden(boolean hidden) {
        setFlag(HIDDEN, hidden);
        return this;
    }

    public DSInfo setMetadata(DSMap map) {
        map.setParent(this);
        metadata = map;
        return this;
    }

    /**
     * False by default, set to true if you don't want the child to be written by clients.
     */
    public DSInfo setReadOnly(boolean readOnly) {
        setFlag(READONLY, readOnly);
        return this;
    }

    /**
     * False by default, set to true if you don't want the child persisted.
     */
    public DSInfo setTransient(boolean trans) {
        setFlag(TRANSIENT, trans);
        return this;
    }

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
        if (info.object != null) {
            setObject(info.object.copy());
        }
    }

    boolean getFlag(int position) {
        return DSUtil.getBit(flags, position);
    }

    int getFlags() {
        return flags;
    }

    /**
     * Quick test for a proxy info.
     */
    boolean isProxy() {
        return false;
    }

    DSInfo setFlag(int position, boolean on) {
        fireInfoChanged();
        flags = DSUtil.setBit(flags, position, on);
        return this;
    }

    DSInfo setName(String arg) {
        this.name = arg;
        return this;
    }

    DSInfo setObject(DSIObject arg) {
        this.object = arg;
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
            parent.fire(DSInfoTopic.Event.METADATA_CHANGED, this);
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

}
