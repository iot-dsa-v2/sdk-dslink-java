package org.iot.dsa.node;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * Indexed collection of elements.
 *
 * <p>
 *
 * This can be mounted in the node tree.  However, the parent node will not know when it has been
 * modified, so the modifier is responsible for calling DSNode.childChanged(DSInfo).
 *
 * <p>
 *
 * This is not thread safe.
 *
 * @author Aaron Hansen
 */
public class DSList extends DSGroup implements Iterable<DSElement> {

    // Constants
    // ---------

    // Fields
    // ------

    protected ArrayList list = new ArrayList();

    // Constructors
    // ------------

    // Public Methods
    // --------------

    /**
     * Adds the value and returns this.
     *
     * @param val Can be null, and can not be an already parented group.
     * @return this
     */
    public DSList add(DSElement val) {
        if (val == null) {
            return addNull();
        } else if (val.isGroup()) {
            val.toGroup().setParent(this);
        }
        list.add(val);
        return this;
    }

    /**
     * Appends the primitive and returns this.
     */
    public DSList add(boolean val) {
        list.add(DSBool.valueOf(val));
        return this;
    }

    /**
     * Appends the primitive and returns this.
     */
    public DSList add(double val) {
        list.add(DSDouble.valueOf(val));
        return this;
    }

    /**
     * Appends the primitive and returns this.
     */
    public DSList add(long val) {
        list.add(DSLong.valueOf(val));
        return this;
    }

    /**
     * Appends the primitive and returns this.
     */
    public DSList add(String val) {
        if (val == null) {
            return addNull();
        }
        list.add(DSString.valueOf(val));
        return this;
    }

    /**
     * Appends the primitive and returns this.
     */
    public DSList add(int val) {
        list.add(make(val));
        return this;
    }

    /**
     * Add all elements of the argument to this list and returns this.
     */
    public DSList addAll(DSList list) {
        for (DSElement e : list) {
            add(e);
        }
        return this;
    }

    /**
     * Appends a new list and returns it.  This is going to cause trouble, but the the primary usage
     * won't be to add an empty list.
     */
    public DSList addList() {
        DSList ret = new DSList();
        add(ret);
        return ret;
    }

    /**
     * Appends a new map and returns it.
     */
    public DSMap addMap() {
        DSMap ret = new DSMap();
        add(ret);
        return ret;
    }

    /**
     * Appends null and returns this.
     */
    public DSList addNull() {
        list.add(DSNull.NULL);
        return this;
    }

    @Override
    public DSList clear() {
        list.clear();
        return this;
    }

    public boolean contains(DSElement value) {
        return indexOf(value) >= 0;
    }

    @Override
    public DSList copy() {
        DSList ret = new DSList();
        for (int i = 0, len = list.size(); i < len; i++) {
            ret.add(get(i).copy());
        }
        return ret;
    }

    @Override
    public DSList decode(DSElement element) {
        return element.toList();
    }

    @Override
    public DSList encode() {
        return this;
    }

    @Override
    public DSElement get(int idx) {
        return (DSElement) list.get(idx);
    }

    @Override
    public DSElementType getElementType() {
        return DSElementType.LIST;
    }

    @Override
    public DSValueType getValueType() {
        return DSValueType.LIST;
    }

    /**
     * Returns true.
     */
    public boolean isList() {
        return true;
    }

    /**
     * Returns false.
     */
    public boolean isNull() {
        return false;
    }

    /**
     * Returns an iterator that does not implement remove.
     */
    public Iterator<DSElement> iterator() {
        return new MyIterator();
    }

    /**
     * Replaces a value and returns this.
     *
     * @param val Can be null.
     */
    public DSList put(int idx, DSElement val) {
        if (idx == list.size()) {
            add(val);
            return this;
        }
        if (val == null) {
            val = DSNull.NULL;
        }
        list.set(idx, val);
        return this;
    }

    /**
     * Primitive setter, returns this.
     */
    public DSList put(int idx, boolean val) {
        put(idx, DSBool.valueOf(val));
        return this;
    }

    /**
     * Primitive setter, returns this.
     */
    public DSList put(int idx, double val) {
        put(idx, DSDouble.valueOf(val));
        return this;
    }

    /**
     * Primitive setter, returns this.
     */
    public DSList put(int idx, int val) {
        put(idx, make(val));
        return this;
    }

    /**
     * Primitive setter, returns this.
     */
    public DSList put(int idx, long val) {
        put(idx, DSLong.valueOf(val));
        return this;
    }

    /**
     * Primitive setter, returns this.
     */
    public DSList put(int idx, String val) {
        put(idx, DSString.valueOf(val));
        return this;
    }

    @Override
    public DSElement remove(int idx) {
        DSElement ret = (DSElement) list.remove(idx);
        if (ret.isGroup()) {
            ret.toGroup().setParent(null);
        }
        return ret;
    }

    @Override
    public int size() {
        return list.size();
    }

    @Override
    public DSList toList() {
        return this;
    }

    public static DSList valueOf(DSElement... values) {
        DSList ret = new DSList();
        for (DSElement v : values) {
            ret.add(v);
        }
        return ret;
    }

    public static DSList valueOf(Double... values) {
        DSList ret = new DSList();
        for (Double v : values) {
            ret.add(v);
        }
        return ret;
    }

    public static DSList valueOf(Long... values) {
        DSList ret = new DSList();
        for (Long v : values) {
            ret.add(v);
        }
        return ret;
    }

    public static DSList valueOf(String... values) {
        DSList ret = new DSList();
        for (String v : values) {
            ret.add(v);
        }
        return ret;
    }

    // Inner Classes
    // -------------

    private class MyIterator implements Iterator<DSElement> {

        private int idx = 0;

        @Override
        public boolean hasNext() {
            return idx < size();
        }

        @Override
        public DSElement next() {
            return get(idx++);
        }

        /**
         * Not implements, throws an exception.
         */
        @Override
        public void remove() {
            throw new IllegalStateException("Not implemented");
        }
    }

    // Initialization
    // --------------

    static {
        DSRegistry.registerDecoder(DSList.class, new DSList());
    }


}
