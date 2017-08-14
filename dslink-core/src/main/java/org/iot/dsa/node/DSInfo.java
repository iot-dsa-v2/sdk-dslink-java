package org.iot.dsa.node;

import java.util.Iterator;
import org.iot.dsa.dslink.responder.ApiObject;
import org.iot.dsa.node.action.DSAction;
import org.iot.dsa.util.DSUtil;

/**
 * Metadata about a child in it's parent container.
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
    //static final int NO_AUDIT      = 5; //future

    ///////////////////////////////////////////////////////////////////////////
    // Fields
    ///////////////////////////////////////////////////////////////////////////

    int flags = 0;
    String name;
    DSNode parent;
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

    public boolean equivalent(DSInfo arg) {
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
        return new ApiIterator(((DSNode) value).iterator());
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

    boolean getFlag(int position) {
        return DSUtil.getBit(flags, position);
    }

    int getFlags() {
        return flags;
    }

    @Override
    public void getMetadata(DSMap bucket) {
        if (value instanceof DSNode) {
            ((DSNode) value).getMetadata(this, bucket);
        }
    }

    public String getName() {
        return name;
    }

    public DSIObject getObject() {
        return value;
    }

    public DSNode getParent() {
        return parent;
    }

    @Override
    public DSIValue getValue() {
        return (DSIValue) value;
    }

    @Override
    public boolean hasChildren() {
        if (value instanceof DSNode) {
            return ((DSNode) value).childCount() > 0;
        }
        return false;
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

    /**
     * Whether or not configuration permission is required to see the object.
     */
    public boolean isConfig() {
        return getFlag(CONFIG);
    }

    /**
     * True if this proxies a default and the state and value match the default.
     */
    public boolean isDefault() {
        return false;
    }

    /**
     * True if the state matches the default state.
     */
    public boolean isDefaultState() {
        return flags == 0;
    }

    /**
     * True if this proxies a default and the value type matches the default.
     */
    public boolean isDefaultType() {
        return false;
    }

    /**
     * True if this proxies a default and the value matches the default.
     */
    public boolean isDefaultValue() {
        return false;
    }

    /**
     * Whether or not an object is visible to clients.
     */
    public boolean isHidden() {
        return getFlag(HIDDEN);
    }

    /**
     * Whether or not an object can be removed.
     */
    public boolean isPermanent() {
        return getFlag(PERMANENT);
    }

    @Override
    public boolean isValue() {
        return value instanceof DSIValue;
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

    public DSInfo setReadOnly(boolean readOnly) {
        setFlag(READONLY, readOnly);
        return this;
    }

    public DSInfo setTransient(boolean trans) {
        setFlag(TRANSIENT, trans);
        return this;
    }

    /**
     * Change the value and return this.
     */
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
