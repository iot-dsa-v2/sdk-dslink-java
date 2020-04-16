package org.iot.dsa.node;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.iot.dsa.node.action.DSAction;
import org.iot.dsa.node.action.DSIAction;
import org.iot.dsa.node.action.DSISetAction;
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
public class DSInfo<T extends DSIObject> implements GroupListener {

    ///////////////////////////////////////////////////////////////////////////
    // Constants
    ///////////////////////////////////////////////////////////////////////////

    static final int ADMIN = 0;
    static final int PRIVATE = 1;
    static final int TRANSIENT = 2;
    static final int READONLY = 3;
    static final int DECLARED = 4;
    static final int DEFAULT_ON_COPY = 5;
    static final int LOCKED = 6;
    static final int IGNORE_EQUALS = 7;

    ///////////////////////////////////////////////////////////////////////////
    // Fields
    ///////////////////////////////////////////////////////////////////////////

    int flags = 0;
    DSMap metadata;
    String name;
    DSInfo<?> next;
    T object;
    DSNode parent;
    DSInfo<?> prev;

    ///////////////////////////////////////////////////////////////////////////
    // Constructors
    ///////////////////////////////////////////////////////////////////////////

    DSInfo() {
    }

    protected DSInfo(String name, T object) {
        this.name = name;
        setObject(object);
    }

    ///////////////////////////////////////////////////////////////////////////
    // Public Methods
    ///////////////////////////////////////////////////////////////////////////

    public void decodeState(DSElement state) {
        flags = state.toInt();
    }

    public DSElement encodeState() {
        return DSLong.valueOf(flags);
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
     * True if the arg is a DSInfo and it's name, metadata and objects all equals.
     */
    public boolean equivalent(Object arg) {
        if (arg == this) {
            return true;
        }
        if (arg instanceof DSInfo<?>) {
            DSInfo<?> argInfo = (DSInfo<?>) arg;
            if (flags != argInfo.flags) {
                return false;
            }
            if (!DSUtil.equal(name, argInfo.name)) {
                return false;
            }
            if (!DSUtil.equal(metadata, argInfo.metadata)) {
                return false;
            }
            if (isNode()) {
                if (!argInfo.isNode()) {
                    return false;
                }
                return getNode().equivalent(argInfo.getNode());
            } else {
                if (argInfo.isNode()) {
                    return false;
                }
                return DSUtil.equal(object, argInfo.object);
            }
        }
        return false;
    }

    /**
     * Returns the target object.
     */
    public T get() {
        return object;
    }

    public DSIAction getAction() {
        return (DSIAction) get();
    }

    public DSInfo<?> getChild(String name) {
        if (isNode()) {
            return getNode().getInfo(name);
        }
        return getParent().getVirtualAction(this, name);
    }

    public Iterator<String> getChildren() {
        List<String> bucket = new ArrayList<>();
        if (isNode()) {
            DSNode node = getNode();
            node.getVirtualActions(this, bucket);
            DSInfo<?> info = node.getFirstInfo();
            String name;
            while (info != null) {
                name = info.getName();
                switch (name.charAt(0)) {
                    case '$':
                    case '@':
                        break;
                    default:
                        bucket.add(name);
                }
                info = info.next();
            }
        } else {
            getParent().getVirtualActions(this, bucket);
        }
        return bucket.iterator();
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

    /**
     * Adds only the metadata stored here.
     */
    public void getMetadata(DSMap bucket) {
        if (metadata != null) {
            bucket.putAll(metadata);
        }
        if (isNode()) {
            DSNode node = getNode();
            DSInfo<?> info = node.getFirstInfo();
            while (info != null) {
                if (info.isValue()) {
                    char ch = info.getName().charAt(0);
                    if ((ch == '$') || (ch == '@')) {
                        bucket.put(info.getName(), info.getElement());
                    }
                }
                info = info.next;
            }
        }
    }

    /**
     * Use to configure metadata only.  Creates a DSMetadata wrapper, and possibly the metadata map.
     */
    public DSMetadata getMetadata() {
        if (metadata == null) {
            metadata = new DSMap();
            metadata.setParent(this);
        }
        return new DSMetadata(metadata);
    }

    public DSInfo<T> setMetadata(DSMap map) {
        map.setParent(this);
        metadata = map;
        return this;
    }

    public String getName() {
        return name;
    }

    DSInfo<T> setName(String arg) {
        this.name = arg;
        return this;
    }

    /**
     * A convenience that casts getObject().
     */
    public DSNode getNode() {
        return (DSNode) get();
    }

    public DSNode getParent() {
        return parent;
    }

    DSInfo<T> setParent(DSNode arg) {
        this.parent = arg;
        return this;
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
    public DSIValue getValue() {
        return (DSIValue) object;
    }

    /**
     * True if there is another info after this one.
     */
    public boolean hasNext() {
        return next != null;
    }

    /**
     * True if the value of the info is an instance of the given class.
     */
    public boolean is(Class<?> clazz) {
        if (object == null) {
            return false;
        }
        return clazz.isAssignableFrom(object.getClass());
    }

    public boolean isAction() {
        return object instanceof DSAction;
    }

    public boolean isAdmin() {
        return getFlag(ADMIN);
    }

    /**
     * False by default, set to true if you don't want the child to require admin level
     * permissions.
     */
    public DSInfo<T> setAdmin(boolean admin) {
        setFlag(ADMIN, admin);
        return this;
    }

    /**
     * True if the info represents a declared default.  If true, the info is not removable or
     * renamable.
     */
    public boolean isDeclared() {
        return getFlag(DECLARED);
    }

    DSInfo<T> setDeclared(boolean arg) {
        setFlag(DECLARED, arg);
        return this;
    }

    /**
     * Whether or not the current value, or the default value is copied.
     */
    public boolean isDefaultOnCopy() {
        return getFlag(DEFAULT_ON_COPY);
    }

    /**
     * False by default, set to true to reset the target to it's default when the parent node
     * is copied.
     */
    public DSInfo<T> setDefaultOnCopy(boolean defaultOnCopy) {
        setFlag(DEFAULT_ON_COPY, defaultOnCopy);
        return this;
    }

    /**
     * True if declared or locked.
     */
    public boolean isFrozen() {
        return isDeclared() || isLocked();
    }

    /**
     * False by default, set to true if you don't want equals objects to be applied and
     * events to be fired.
     */
    public boolean isIgnoreEquals() {
        return getFlag(IGNORE_EQUALS);
    }

    /**
     * False by default, set to true if you don't want equals objects to be applied and
     * events to be fired.
     */
    public DSInfo<T> setIgnoreEquals(boolean ignore) {
        setFlag(IGNORE_EQUALS, ignore);
        return this;
    }

    /**
     * True if the info cannot be removed or renamed.
     * Intended for non-default children, default children are implicitly locked.
     */
    public boolean isLocked() {
        return getFlag(LOCKED);
    }

    /**
     * False by default, set to true if the info cannot be removed or renamed.
     * Intended for non-default children, default children are implicitly locked.
     */
    public DSInfo<T> setLocked(boolean locked) {
        setFlag(LOCKED, locked);
        return this;
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
        if (isValue()) {
            return getValue().isNull();
        }
        return false;
    }

    /**
     * Whether or not an object is exposed outside of the process.
     */
    public boolean isPrivate() {
        return getFlag(PRIVATE);
    }

    /**
     * False by default, set to true if you don't want the child to be sent to clients.
     */
    public DSInfo<T> setPrivate(boolean hidden) {
        setFlag(PRIVATE, hidden);
        return this;
    }

    /**
     * Whether or not an object is a writable value, or an action that mutates anything.  If an
     * action is not read only, it will require write level permissions.
     */
    public boolean isReadOnly() {
        return getFlag(READONLY) || (object instanceof DSISetAction);
    }

    /**
     * False by default, set to true if the object is a writable value, or an action that
     * doesn't require write permission.
     */
    public DSInfo<T> setReadOnly(boolean readOnly) {
        setFlag(READONLY, readOnly);
        return this;
    }

    /**
     * Whether or not an object is persistent.
     */
    public boolean isTransient() {
        return getFlag(TRANSIENT);
    }

    /**
     * False by default, set to true if you don't want the child persisted.
     */
    public DSInfo<T> setTransient(boolean trans) {
        setFlag(TRANSIENT, trans);
        return this;
    }

    /**
     * Whether or not the object is a value.
     */
    public boolean isValue() {
        return object instanceof DSIValue;
    }

    /**
     * Fires a metadata changed event on the parent node.
     */
    @Override
    public void modified(DSGroup map) {
        if (isNode()) {
            getNode().fire(DSNode.METADATA_CHANGED_EVENT, null, metadata);
        }
        if (parent != null) {
            parent.fire(DSNode.METADATA_CHANGED_EVENT, this, metadata);
        }
    }

    /**
     * The next info in the parent node, or null.
     */
    public DSInfo<?> next() {
        return next;
    }

    /**
     * The next DSInfo in the parent whose object is of the given type, or null.
     */
    public <T extends DSIObject> DSInfo<T> next(Class<T> is) {
        DSInfo<?> cur = next;
        while (cur != null) {
            if (cur.is(is)) {
                return (DSInfo<T>) cur;
            }
            cur = cur.next();
        }
        return null;
    }

    /**
     * The next DSInfo in the parent that is an action, or null.
     */
    public DSInfo<DSAction> nextAction() {
        DSInfo<?> cur = next;
        while (cur != null) {
            if (cur.isAction()) {
                return (DSInfo<DSAction>) cur;
            }
            cur = cur.next();
        }
        return null;
    }

    ///////////////////////////////////////////////////////////////////////////
    // Package Methods
    ///////////////////////////////////////////////////////////////////////////

    /**
     * The next DSInfo in the parent that is a node, or null.
     */
    public DSInfo<DSNode> nextNode() {
        DSInfo<?> cur = next;
        while (cur != null) {
            if (cur.isNode()) {
                return (DSInfo<DSNode>) cur;
            }
            cur = cur.next();
        }
        return null;
    }

    /**
     * The next DSInfo in the parent that is a value, or null.
     */
    public DSInfo<DSIValue> nextValue() {
        DSInfo<?> cur = next;
        while (cur != null) {
            if (cur.isValue()) {
                return (DSInfo<DSIValue>) cur;
            }
            cur = cur.next();
        }
        return null;
    }

    @Override
    public String toString() {
        if (name != null) {
            return name;
        }
        return String.valueOf(object);
    }

    DSInfo<T> copy() {
        DSInfo<T> ret = new DSInfo<>();
        ret.copyState(this);
        if (isDefaultOnCopy()) {
            DSIObject val = getDefaultObject();
            if (val != null) {
                ret.setObject((T) val.copy());
                return ret;
            }
        }
        if (object != null) {
            ret.setObject((T) object.copy());
        }
        return ret;
    }

    void copy(DSInfo<?> info) {
        copyState(info);
        if (isDefaultOnCopy()) {
            return;
        }
        if (info.object != null) {
            setObject((T) info.object.copy());
        }
    }

    void copyState(DSInfo<?> info) {
        flags = info.flags;
        name = info.name;
        if (info.metadata != null) {
            metadata = info.metadata.copy();
        }
    }

    /**
     * If this is a proxy , this will return the original default instance.
     */
    T getDefaultObject() {
        return get();
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

    /**
     * Whether or not the info is for a dynamic action.
     */
    boolean isVirtual() {
        return false;
    }

    DSInfo<T> newProxy() {
        return new DSInfoProxy<>(this);
    }

    DSInfo<T> setFlag(int position, boolean on) {
        //modified?
        flags = DSUtil.setBit(flags, position, on);
        return this;
    }

    DSInfo<T> setObject(T arg) {
        if (isDeclared()) {
            if (!getDefaultObject().getClass().isInstance(arg)) {
                if (isValue() && (arg instanceof DSIValue)) {
                    DSIValue v = (DSIValue) arg;
                    this.object = (T) getValue().valueOf(v.toElement());
                    return this;
                }
                throw new IllegalArgumentException(arg + " not an instanceof the declared type " +
                                                           getDefaultObject().getClass().getName());
            }
        }
        this.object = arg;
        return this;
    }

}
