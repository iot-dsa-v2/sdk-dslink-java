package org.iot.dsa.node;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * Indexed collection of elements.
 * <p>
 * <p>
 * <p>
 * This can be mounted in the node tree.  However, the parent node will not know when it has been
 * modified, so the modifier is responsible for calling DSNode.childChanged(DSInfo).
 * <p>
 * <p>
 * <p>
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
        modified();
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
            add(e.copy());
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
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof DSList)) {
            return false;
        }
        DSList arg = (DSList) o;
        if (arg.getElementType() != getElementType()) {
            return false;
        }
        int size = size();
        if (size != arg.size()) {
            return false;
        }
        for (int i = size; --i >= 0; ) {
            if (!get(i).equals(arg.get(i))) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns the item at index 0.
     *
     * @return Null if empty.
     */
    @Override
    public DSElement first() {
        if (isEmpty()) {
            return null;
        }
        return get(0);
    }

    public DSElement get(int idx) {
        return (DSElement) list.get(idx);
    }

    /**
     * Optional getter.
     */
    public boolean get(int idx, boolean def) {
        if (idx >= size()) {
            return def;
        }
        DSElement ret = get(idx);
        if ((ret == null) || ret.isNull()) {
            return def;
        }
        try {
            return ret.toBoolean();
        } catch (Exception x) {
        }
        return def;
    }

    /**
     * Optional getter.
     */
    public double get(int idx, double def) {
        if (idx >= size()) {
            return def;
        }
        DSElement ret = get(idx);
        if ((ret == null) || ret.isNull()) {
            return def;
        }
        try {
            return ret.toDouble();
        } catch (Exception x) {
        }
        return def;
    }

    /**
     * Optional getter.
     */
    public int get(int idx, int def) {
        if (idx >= size()) {
            return def;
        }
        DSElement ret = get(idx);
        if ((ret == null) || ret.isNull()) {
            return def;
        }
        try {
            return ret.toInt();
        } catch (Exception x) {
        }
        return def;
    }

    /**
     * Optional getter.
     */
    public long get(int idx, long def) {
        if (idx >= size()) {
            return def;
        }
        DSElement ret = get(idx);
        if ((ret == null) || ret.isNull()) {
            return def;
        }
        try {
            return ret.toLong();
        } catch (Exception x) {
        }
        return def;
    }

    /**
     * Optional getter.
     */
    public String get(int idx, String def) {
        if (idx >= size()) {
            return def;
        }
        DSElement ret = get(idx);
        if ((ret == null) || ret.isNull()) {
            return def;
        }
        return ret.toString();
    }

    /**
     * Primitive getter.
     */
    public boolean getBoolean(int idx) {
        return get(idx).toBoolean();
    }

    /**
     * Primitive getter.
     */
    public double getDouble(int idx) {
        return get(idx).toDouble();
    }

    @Override
    public DSElementType getElementType() {
        return DSElementType.LIST;
    }

    /**
     * Primitive getter.
     */
    public double getFloat(int idx) {
        return get(idx).toFloat();
    }

    /**
     * Primitive getter.
     */
    public int getInt(int idx) {
        return get(idx).toInt();
    }

    /**
     * Primitive getter.
     */
    public DSList getList(int idx) {
        return get(idx).toList();
    }

    /**
     * Primitive getter.
     */
    public long getLong(int idx) {
        return get(idx).toLong();
    }

    /**
     * Primitive getter.
     */
    public DSMap getMap(int idx) {
        return get(idx).toMap();
    }

    /**
     * Primitive getter.
     */
    public String getString(int idx) {
        return get(idx).toString();
    }

    @Override
    public DSValueType getValueType() {
        return DSValueType.LIST;
    }

    @Override
    public int hashCode() {
        int hashCode = 1;
        for (int i = size(); --i >= 0; ) {
            hashCode = 31 * hashCode + get(i).hashCode();
        }
        return hashCode;
    }

    /**
     * Scans the collection and returns the first index that equal the arg.
     *
     * @return -1 if not found.
     */
    public int indexOf(DSElement obj) {
        boolean isNull = ((obj == null) || obj.isNull());
        DSElement tmp;
        int len = size();
        for (int i = 0; i < len; i++) {
            tmp = get(i);
            if (obj == tmp) {
                return i;
            }
            if (isNull && tmp.isNull()) {
                return i;
            }
            if (obj.equals(tmp)) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Scans the collection and returns the first index that equal the arg.
     *
     * @return -1 if not found.
     */
    public int indexOf(String obj) {
        boolean isNull = (obj == null);
        DSElement tmp;
        int len = size();
        for (int i = 0; i < len; i++) {
            tmp = get(i);
            if (isNull && tmp.isNull()) {
                return i;
            }
            if (tmp.isString()) {
                if (tmp.toString().equals(obj)) {
                    return i;
                }
            }
        }
        return -1;
    }

    /**
     * Returns true.
     */
    @Override
    public boolean isList() {
        return true;
    }

    /**
     * Whether or not the object at the given index is null.  Will return true if the index is out
     * of bounds.
     */
    public boolean isNull(int idx) {
        if (idx >= size()) {
            return true;
        }
        if (idx < 0) {
            return true;
        }
        return get(idx).isNull();
    }

    /**
     * Returns false.
     */
    @Override
    public boolean isNull() {
        return false;
    }

    /**
     * Returns an iterator that does not implement remove.
     */
    @Override
    public Iterator<DSElement> iterator() {
        return new MyIterator();
    }

    /**
     * Returns the item at the highest index.
     *
     * @return Null if empty.
     */
    @Override
    public DSElement last() {
        if (isEmpty()) {
            return null;
        }
        return get(size() - 1);
    }

    /**
     * Scans the collection and returns the first index that equal the arg.
     *
     * @return -1 if not found.
     */
    public int lastIndexOf(DSElement obj) {
        boolean isNull = ((obj == null) || obj.isNull());
        DSElement tmp;
        int len = size();
        for (int i = len; --i >= 0; ) {
            tmp = get(i);
            if (obj == tmp) {
                return i;
            }
            if (isNull && tmp.isNull()) {
                return i;
            }
            if (obj.equals(tmp)) {
                return i;
            }
        }
        return -1;
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
        modified();
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

    /**
     * Returns true if the element is removed.
     */
    public boolean remove(DSElement value) {
        int idx = indexOf(value);
        if (idx >= 0) {
            remove(idx);
            return true;
        }
        return false;
    }

    /**
     * Returns the element that was removed from the given index.
     */
    public DSElement remove(int idx) {
        DSElement ret = (DSElement) list.remove(idx);
        if (ret.isGroup()) {
            ret.toGroup().setParent(null);
        }
        modified();
        return ret;
    }

    @Override
    public DSElement removeFirst() {
        return remove(0);
    }

    @Override
    public DSElement removeLast() {
        return remove(size() - 1);
    }

    @Override
    public int size() {
        return list.size();
    }

    @Override
    public DSList toElement() {
        return this;
    }

    @Override
    public DSList toList() {
        return this;
    }

    @Override
    public DSList valueOf(DSElement element) {
        if (element.isList()) {
            return element.toList();
        }
        return new DSList().add(element);
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
         * Not implemented, throws an exception.
         */
        @Override
        public void remove() {
            throw new IllegalStateException("Not implemented");
        }
    }

}
