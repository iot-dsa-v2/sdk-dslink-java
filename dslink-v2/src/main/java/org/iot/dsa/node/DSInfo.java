package org.iot.dsa.node;

import java.util.ArrayList;
import java.util.Iterator;
import org.iot.dsa.dslink.responder.ApiObject;
import org.iot.dsa.node.action.DSAction;
import org.iot.dsa.node.event.DSNodeTopic;
import org.iot.dsa.util.DSUtil;

/**
 * All objects in the node tree have corresponding DSInfo instances. DSInfo provides information
 * about the object at a specific path.
 * <p>
 * Important things developers should know about DSInfo are:
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
    static final int PRIVATE = 1;
    static final int TRANSIENT = 2;
    static final int READONLY = 3;
    static final int DECLARED = 4;
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

    public DSInfo(String name, DSIObject object) {
        this.name = name;
        setObject(object);
    }

    ///////////////////////////////////////////////////////////////////////////
    // Public Methods
    ///////////////////////////////////////////////////////////////////////////

    public DSInfo copy() {
        DSInfo ret = new DSInfo();
        ret.copyState(this);
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

    /**
     * Returns the target object.
     */
    public DSIObject get() {
        return object;
    }

    @Override
    public DSAction getAction() {
        return (DSAction) get();
    }

    @Override
    public Iterator<ApiObject> getChildren() {
        ArrayList<ApiObject> ret = new ArrayList<>();
        ArrayList<String> actions = new ArrayList<>();
        DSInfo info;
        DSNode node;
        if (isNode()) {
            node = getNode();
            node.getDynamicActions(this, actions);
        } else {
            node = getParent();
            node.getDynamicActions(this, actions);
        }
        for (String s : actions) {
            info = node.getDynamicAction(this, s);
            if (info != null) {
                ret.add(info);
            }
        }
        if (isNode()) {
            info = node.getFirstInfo();
            while (info != null) {
                ret.add(info);
                info = info.next();
            }
        }
        return ret.iterator();
    }

    /**
     * If this is a proxy , this will return the original default instance.
     */
    public DSIObject getDefaultObject() {
        return get();
    }

    /**
     * A convenience that casts the object.  Will call DSIValue.toElement on values.
     */
    public DSElement getElement() {
        DSIObject obj = get();
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
        return (DSNode) get();
    }

    /**
     * User get() instead.
     *
     * @deprecated 18-11-14
     */
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
        return object instanceof DSAction;
    }

    @Override
    public boolean isAdmin() {
        return getFlag(ADMIN);
    }

    /**
     * True if the info represents a declared default.  If true, the info is not removable or
     * renamable.
     */
    public boolean isDeclared() {
        return getFlag(DECLARED);
    }

    /**
     * Whether or not the current value, or the default value is copied.
     */
    public boolean isDefaultOnCopy() {
        return getFlag(DEFAULT_ON_COPY);
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
            return getNode().isEqual(arg.get());
        }
        return DSUtil.equal(get(), arg.get());
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
            return getNode().isIdentical(arg.get());
        }
        return DSUtil.equal(get(), arg.get());
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
     * Whether or not an object is exposed outside of the process.
     */
    @Override
    public boolean isPrivate() {
        return getFlag(PRIVATE);
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

    /**
     * Fires a metadata changed event.
     */
    public void modified(DSGroup map) {
        fireInfoChanged();
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
     * False by default, set to true to reset the target to it's default when the encapsulated node
     * is copied.
     */
    public DSInfo setDefaultOnCopy(boolean defaultOnCopy) {
        setFlag(DEFAULT_ON_COPY, defaultOnCopy);
        return this;
    }

    public DSInfo setMetadata(DSMap map) {
        map.setParent(this);
        metadata = map;
        return this;
    }

    /**
     * False by default, set to true if you don't want the child to be sent to clients.
     */
    public DSInfo setPrivate(boolean hidden) {
        setFlag(PRIVATE, hidden);
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

    ///////////////////////////////////////////////////////////////////////////
    // Package Methods
    ///////////////////////////////////////////////////////////////////////////

    /**
     * This is only called by DSNode.copy.  Therefore, this will already
     * be populated with the default value.
     */
    void copy(DSInfo info) {
        copyState(info);
        if (isDefaultOnCopy()) {
            return;
        }
        if (info.object != null) {
            setObject(info.object.copy());
        }
    }

    /**
     * This is only called by DSNode.copy.  Therefore, this will already
     * be populated with the default value.
     */
    void copyState(DSInfo info) {
        flags = info.flags;
        name = info.name;
        if (info.metadata != null) {
            metadata = info.metadata.copy();
        }
    }

    boolean getFlag(int position) {
        return DSUtil.getBit(flags, position);
    }

    int getFlags() {
        return flags;
    }

    /**
     * Whether or not the info is for a dynamic action.
     */
    boolean isDynamic() {
        return false;
    }

    /**
     * Quick test for a proxy info.
     */
    boolean isProxy() {
        return false;
    }

    /**
     * Whether or not this info represents a declared default.
     */
    boolean isRemovable() {
        return !getFlag(DECLARED);
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

    DSInfo setDeclared(boolean arg) {
        setFlag(DECLARED, arg);
        return this;
    }

    ///////////////////////////////////////////////////////////////////////////
    // Private Methods
    ///////////////////////////////////////////////////////////////////////////

    private void fireInfoChanged() {
        if ((parent != null) && (parent.isRunning())) {
            parent.fire(DSNodeTopic.METADATA_CHANGED, this);
        }
    }

}
