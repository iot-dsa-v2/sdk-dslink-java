package org.iot.dsa.node;

import java.util.ArrayList;

/**
 * Indexed collection of DSObjs.  This is not thread safe.
 * <p>
 * Can not be added to the node tree.
 *
 * @author Aaron Hansen
 */
public class DSList extends DSGroup {

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
     * Appends a new list and returns it.  This is going to cause trouble, but the
     * the primary usage won't be to add an empty list.
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
    public DSGroup clear() {
        list.clear();
        return this;
    }

    @Override
    public DSElement copy() {
        DSList ret = new DSList();
        for (int i = 0, len = list.size(); i < len; i++) {
            ret.add(get(i).copy());
        }
        return ret;
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


}//DSList
