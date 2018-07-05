package org.iot.dsa.node;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.iot.dsa.util.DSUtil;

/**
 * String keyed collection of elements that preserves the order of addition.  Keys and values can be
 * accessed via index.
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
public class DSMap extends DSGroup {

    // Constants
    // ---------

    // Fields
    // ------

    /**
     * For preserving order.
     */
    protected List<Entry> keys = new ArrayList<Entry>();
    protected Map<String, Entry> map = new HashMap<String, Entry>();

    // Constructors
    // ------------

    public DSMap() {
    }

    // Public Methods
    // --------------

    @Override
    public DSMap clear() {
        keys.clear();
        map.clear();
        return this;
    }

    public boolean contains(String key) {
        return indexOf(key) >= 0;
    }

    @Override
    public DSMap copy() {
        DSMap ret = new DSMap();
        for (int i = 0, len = size(); i < len; i++) {
            ret.put(getKey(i), get(i).copy());
        }
        return ret;
    }

    @Override
    public boolean equals(Object arg) {
        if (!(arg instanceof DSMap)) {
            return false;
        }
        DSMap argMap = (DSMap) arg;
        int size = size();
        if (argMap.size() != size) {
            return false;
        }
        Entry mine = null;
        for (int i = size; --i >= 0; ) {
            mine = getEntry(i);
            if (!DSUtil.equal(mine.getValue(), argMap.get(mine.getKey()))) {
                return false;
            }
        }
        return true;
    }

    @Override
    public DSElement get(int idx) {
        return keys.get(idx).getValue();
    }

    /**
     * Returns the value for the given key.
     *
     * @return Possibly null.
     */
    public DSElement get(String key) {
        Entry e = map.get(key);
        if (e == null) {
            return null;
        }
        return e.getValue();
    }

    /**
     * Optional getter, returns the provided default if the value mapped to the key is null or not
     * convertible.
     */
    public boolean get(String key, boolean def) {
        DSElement ret = get(key);
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
     * Optional getter, returns the provided default if the value mapped to the key is null.
     */
    public double get(String key, double def) {
        DSElement ret = get(key);
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
     * Optional getter, returns the provided default if the value mapped to the key is null or not
     * convertible.
     */
    public int get(String key, int def) {
        DSElement ret = get(key);
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
     * Optional getter, returns the provided default if the value mapped to the key is null or not
     * convertible.
     */
    public long get(String key, long def) {
        DSElement ret = get(key);
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
     * Optional getter, returns the provided default if the value mapped to the key is null.
     */
    public String get(String key, String def) {
        DSElement ret = get(key);
        if ((ret == null) || ret.isNull()) {
            return def;
        }
        return ret.toString();
    }

    /**
     * Primitive getter.
     */
    public boolean getBoolean(String key) {
        return get(key).toBoolean();
    }

    /**
     * Primitive getter.
     */
    public double getDouble(String key) {
        return get(key).toDouble();
    }

    @Override
    public DSElementType getElementType() {
        return DSElementType.MAP;
    }

    public Entry getEntry(int index) {
        return keys.get(index);
    }

    public Entry getEntry(String key) {
        return map.get(key);
    }

    /**
     * Return the list, or null.
     *
     * @return Possibly null.
     */
    public DSList getList(String key) {
        DSElement obj = get(key);
        if (obj == null) {
            return null;
        }
        return obj.toList();
    }

    /**
     * Primitive getter.
     */
    public long getLong(String key) {
        return get(key).toLong();
    }

    /**
     * Primitive getter.
     */
    public int getInt(String key) {
        return get(key).toInt();
    }

    /**
     * Returns the key at the given index.
     */
    public String getKey(int idx) {
        return keys.get(idx).getKey();
    }

    /**
     * Returns the map value for the given key, or null.
     *
     * @return Possibly null.
     */
    public DSMap getMap(String key) {
        DSElement o = get(key);
        if (o == null) {
            return null;
        }
        return o.toMap();
    }

    /**
     * Returns the String value for the given key, or null.
     *
     * @return Possibly null.
     */
    public String getString(String key) {
        DSElement o = get(key);
        if (o == null) {
            return null;
        }
        return o.toString();
    }

    @Override
    public DSValueType getValueType() {
        return DSValueType.MAP;
    }

    @Override
    public int hashCode() {
        int hashCode = 1;
        for (int i = size(); --i >= 0; ) {
            hashCode = 31 * hashCode + getKey(i).hashCode();
            hashCode = 31 * hashCode + get(i).hashCode();
        }
        return hashCode;
    }

    /**
     * Returns true.
     */
    @Override
    public boolean isMap() {
        return true;
    }

    /**
     * Returns false.
     */
    @Override
    public boolean isNull() {
        return false;
    }

    /**
     * Returns true if the key isn't in the map, or it's value is null.
     */
    public boolean isNull(String key) {
        DSElement o = get(key);
        if (o == null) {
            return true;
        }
        return o.getElementType() == DSElementType.NULL;
    }

    /**
     * Index of the given key, or -1.
     */
    public int indexOf(String key) {
        if (map.get(key) == null) {
            return -1;
        }
        List<Entry> l = keys;
        for (int i = 0, len = l.size(); i < len; i++) {
            if (key.equals(l.get(i).getKey())) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Adds or replaces the value for the given key and returns this.
     *
     * @param key Must not be null.
     * @param val Can be null, and can not be an already parented group.
     * @return this
     */
    public DSMap put(String key, DSElement val) {
        if (val == null) {
            val = DSNull.NULL;
        }
        Entry e = map.get(key);
        if (e != null) {
            //lets not err if putting same key/val pair.
            if (e.getValue() != val) {
                if (val.isGroup()) {
                    val.toGroup().setParent(this);
                }
                e.setValue(val);
            }
        } else {
            if (val.isGroup()) {
                val.toGroup().setParent(this);
            }
            e = new Entry(key, val);
            map.put(key, e);
            keys.add(e);
        }
        return this;
    }

    /**
     * Primitive setter, returns this.
     */
    public DSMap put(String key, boolean val) {
        put(key, DSBool.valueOf(val));
        return this;
    }

    /**
     * Primitive setter, returns this.
     */
    public DSMap put(String key, double val) {
        put(key, DSDouble.valueOf(val));
        return this;
    }

    /**
     * Primitive setter, returns this.
     */
    public DSMap put(String key, int val) {
        put(key, make(val));
        return this;
    }

    /**
     * Primitive setter, returns this.
     */
    public DSMap put(String key, long val) {
        put(key, DSLong.valueOf(val));
        return this;
    }

    /**
     * Primitive setter, returns this.
     */
    public DSMap put(String key, String val) {
        if (val == null) {
            put(key, DSNull.NULL);
        } else {
            put(key, DSString.valueOf(val));
        }
        return this;
    }

    /**
     * Puts a String representing the stack trace into the map.
     */
    public DSMap put(String key, Throwable val) {
        StringWriter str = new StringWriter();
        PrintWriter out = new PrintWriter(str);
        val.printStackTrace(out);
        out.flush();
        put(key, str.toString());
        try {
            out.close();
        } catch (Exception x) {
        }
        try {
            str.close();
        } catch (Exception x) {
        }
        return this;
    }

    /**
     * Adds / overwrites entries in this map with those from the given.
     *
     * @return This
     */
    public DSMap putAll(DSMap toAdd) {
        Entry e;
        for (int i = 0, len = toAdd.size(); i < len; i++) {
            e = toAdd.getEntry(i);
            put(e.getKey(), e.getValue().copy());
        }
        return this;
    }

    /**
     * Puts a new list for given key and returns it.
     */
    public DSList putList(String key) {
        DSList ret = new DSList();
        put(key, ret);
        return ret;
    }

    /**
     * Puts a new map for given key and returns it.
     */
    public DSMap putMap(String key) {
        DSMap ret = new DSMap();
        put(key, ret);
        return ret;
    }

    /**
     * Puts a null value for given key and returns this.
     */
    public DSMap putNull(String key) {
        return put(key, DSNull.NULL);
    }

    @Override
    public DSElement remove(int idx) {
        Entry e = keys.remove(idx);
        map.remove(e.getKey());
        DSElement ret = e.getValue();
        if (ret.isGroup()) {
            ret.toGroup().setParent(null);
        }
        return ret;
    }

    /**
     * Removes the key-value pair and returns the removed value.
     *
     * @return Possibly null.
     */
    public DSElement remove(String key) {
        Entry e = map.remove(key);
        if (e == null) {
            return null;
        }
        keys.remove(e);
        DSElement ret = e.getValue();
        if (ret.isGroup()) {
            ret.toGroup().setParent(null);
        }
        return ret;
    }

    @Override
    public int size() {
        return keys.size();
    }

    @Override
    public DSMap toElement() {
        return this;
    }

    @Override
    public DSMap toMap() {
        return this;
    }

    @Override
    public DSMap valueOf(DSElement element) {
        return element.toMap();
    }

    // Inner Classes
    // -------------

    /**
     * Allows values to be accessed quickly by index in the list, rather than having to do a key
     * lookup in the map.
     */
    public static class Entry {

        String key;
        DSElement val;

        Entry(String key, DSElement val) {
            this.key = key;
            this.val = val;
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof Entry)) {
                return false;
            }
            Entry e = (Entry) obj;
            return e.getKey().equals(key);
        }

        public String getKey() {
            return key;
        }

        public DSElement getValue() {
            return val;
        }

        @Override
        public int hashCode() {
            return key.hashCode();
        }

        void setValue(DSElement val) {
            this.val = val;
        }
    }

}
