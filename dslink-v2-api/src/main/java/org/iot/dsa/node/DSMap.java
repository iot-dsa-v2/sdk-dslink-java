package org.iot.dsa.node;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.iot.dsa.node.DSMap.Entry;

/**
 * String keyed collection of elements that preserves the order of addition.
 * <p>
 * This can be mounted in the node tree.  However, the parent node will not know when it has been
 * modified, so the modifier is responsible for calling DSNode.childChanged(DSInfo).
 * <p>
 * This is not thread safe.
 *
 * @author Aaron Hansen
 */
public class DSMap extends DSGroup implements Iterable<Entry> {

    // Constants
    // ---------

    // Fields
    // ------

    private Entry first;
    private Entry last;
    private Map<String, Entry> map = new HashMap<>();

    // Constructors
    // ------------

    public DSMap() {
    }

    // Public Methods
    // --------------

    @Override
    public DSMap clear() {
        first = null;
        last = null;
        map.clear();
        modified();
        return this;
    }

    public boolean contains(String key) {
        return getEntry(key) != null;
    }

    @Override
    public DSMap copy() {
        DSMap ret = new DSMap();
        Entry e = firstEntry();
        while (e != null) {
            ret.put(e.getKey(), e.getValue().copy());
            e = e.next();
        }
        return ret;
    }

    @Override
    public boolean equals(Object arg) {
        if (arg == null) {
            return false;
        }
        if (arg == this) {
            return true;
        }
        if (arg instanceof DSMap) {
            DSMap argMap = (DSMap) arg;
            int size = size();
            if (argMap.size() != size) {
                return false;
            }
            Entry myEntry = firstEntry();
            Entry argEntry = argMap.firstEntry();
            while (myEntry != null) {
                if (argEntry == null) {
                    return false;
                }
                if (!myEntry.equals(argEntry)) {
                    return false;
                }
                myEntry = myEntry.next();
                argEntry = argEntry.next();
            }
            if (argEntry != null) {
                return false;
            }
            return true;
        }
        return false;
    }

    @Override
    public DSElement first() {
        if (first != null) {
            return first.getValue();
        }
        return null;
    }

    public Entry firstEntry() {
        return first;
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

    public Entry getEntry(String key) {
        return map.get(key);
    }

    /**
     * Primitive getter.
     */
    public int getInt(String key) {
        return get(key).toInt();
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
        Entry e = firstEntry();
        while (e != null) {
            hashCode = 31 * hashCode + e.hashCode();
            e = e.next();
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

    @Override
    public Iterator<Entry> iterator() {
        return new EntryIterator();
    }

    @Override
    public DSElement last() {
        if (last != null) {
            return last.getValue();
        }
        return null;
    }

    public Entry lastEntry() {
        return last;
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
            DSElement curr = e.getValue();
            if (curr != val) {
                if (val.isGroup()) {
                    val.toGroup().setParent(this);
                }
                if (curr.isGroup()) {
                    curr.toGroup().setParent(null);
                }
                e.setValue(val);
            }
        } else {
            if (val.isGroup()) {
                val.toGroup().setParent(this);
            }
            e = new Entry(key, val);
            map.put(key, e);
            if (first == null) {
                first = e;
                last = e;
            } else {
                last.setNext(e);
                last = e;
            }
        }
        modified();
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
        if (toAdd != null) {
            Entry e = toAdd.firstEntry();
            while (e != null) {
                put(e.getKey(), e.getValue().copy());
                e = e.next();
            }
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
        if (size() == 0) {
            first = null;
            last = null;
        } else if (e == first) {
            first = first.next();
        } else {
            Entry prev = getPrev(key);
            prev.setNext(e.next());
            if (e == last) {
                last = prev;
            }
        }
        DSElement ret = e.getValue();
        if (ret.isGroup()) {
            ret.toGroup().setParent(null);
        }
        modified();
        return ret;
    }

    @Override
    public DSElement removeFirst() {
        Entry e = first;
        if (e != null) {
            return remove(e.getKey());
        }
        return null;
    }

    @Override
    public DSElement removeLast() {
        Entry e = last;
        if (e != null) {
            return remove(e.getKey());
        }
        return null;
    }

    @Override
    public int size() {
        return map.size();
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
        if (element.isMap()) {
            return element.toMap();
        }
        return new DSMap().put("val", element);
    }

    // Private Methods
    // ---------------

    private Entry getPrev(String key) {
        Entry prev = null;
        Entry entry = firstEntry();
        while (entry != null) {
            if (entry.getKey().equals(key)) {
                return prev;
            }
            prev = entry;
            entry = entry.next();
        }
        return prev;
    }

    // Inner Classes
    // -------------

    /**
     * Allows values to be accessed quickly by index in the list, rather than having to do a key
     * lookup in the map.
     */
    public static class Entry {

        String key;
        Entry next;
        DSElement val;

        Entry(String key, DSElement val) {
            this.key = key;
            this.val = val;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }
            if (obj instanceof Entry) {
                Entry e = (Entry) obj;
                if (!e.getKey().equals(key)) {
                    return false;
                }
                return e.getValue().equals(val);
            }
            return false;
        }

        public String getKey() {
            return key;
        }

        public DSElement getValue() {
            return val;
        }

        void setValue(DSElement val) {
            this.val = val;
        }

        @Override
        public int hashCode() {
            return key.hashCode() ^ val.hashCode();
        }

        public Entry next() {
            return next;
        }

        void setNext(Entry entry) {
            next = entry;
        }
    }

    private class EntryIterator implements Iterator<Entry> {

        private Entry last;
        private Entry next;

        EntryIterator() {
            next = firstEntry();
        }

        @Override
        public boolean hasNext() {
            return next != null;
        }

        @Override
        public Entry next() {
            last = next;
            next = next.next();
            return last;
        }

        @Override
        public void remove() {
            if (last == null) {
                throw new NullPointerException();
            }
            DSMap.this.remove(last.getKey());
        }

    }


}
