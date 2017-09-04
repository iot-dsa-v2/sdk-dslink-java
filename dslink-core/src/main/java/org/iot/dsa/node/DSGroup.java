package org.iot.dsa.node;

import org.iot.dsa.io.json.JsonAppender;

/**
 * An index accessible collection of primitives.
 *
 * @author Aaron Hansen
 */
public abstract class DSGroup extends DSElement {

    // Fields
    // --------------

    private Object parent;

    // Public Methods
    // --------------

    /**
     * Removes all items.
     *
     * @return This
     */
    public abstract DSGroup clear();

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof DSGroup)) {
            return false;
        }
        DSGroup arg = (DSGroup) o;
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

    @Override
    public int hashCode() {
        int hashCode = 1;
        for (int i = size(); --i >= 0; ) {
            hashCode = 31 * hashCode + get(i).hashCode();
        }
        return hashCode;
    }

    /**
     * Returns the item at index 0.
     *
     * @return Null if empty.
     */
    public DSElement first() {
        if (isEmpty()) {
            return null;
        }
        return get(0);
    }

    /**
     * Returns the value at the given index.
     */
    public abstract DSElement get(int idx);

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
     * Returns true when childCount() == 0.
     */
    public boolean isEmpty() {
        return size() == 0;
    }

    @Override
    public boolean isGroup() {
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
     * Returns the item at the highest index.
     *
     * @return Null if empty.
     */
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
     * Removes the value at the given index and returns it.
     *
     * @return The value removed.
     */
    public abstract DSElement remove(int idx);

    /**
     * Remove and return the item at index 0.
     *
     * @return The value removed.
     */
    public DSElement removeFirst() {
        return remove(0);
    }

    /**
     * Remove and return the item at the highest index.
     *
     * @return The value removed.
     */
    public DSElement removeLast() {
        return remove(size() - 1);
    }

    /**
     * Sets the parent and returns this for un-parented groups, otherwise throws an
     * IllegalStateException.
     *
     * @param arg The new parent.
     * @return This
     * @throws IllegalStateException If already parented.
     */
    DSGroup setParent(Object arg) {
        if (arg == null) {
            this.parent = null;
            return this;
        }
        if (this.parent != null) {
            throw new IllegalStateException("Already parented");
        }
        this.parent = arg;
        return this;
    }

    @Override
    public DSGroup toGroup() {
        return this;
    }

    /**
     * The number of items is the group.
     */
    public abstract int size();

    /**
     * Json encodes the graph, be careful.
     */
    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder();
        new JsonAppender(buf).value(this).close();
        return buf.toString();
    }


}
