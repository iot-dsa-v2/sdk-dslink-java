package org.iot.dsa.node;

/**
 * A 64 bit integer (a Java long).
 *
 * @author Aaron Hansen
 */
public class DSLong extends DSElement implements Comparable<DSINumber>, DSINumber {

    // Constants
    // ---------

    public static final DSLong NULL = new DSLong(0);

    // Fields
    // ------

    private long value;

    // Constructors
    // ------------

    DSLong(long val) {
        value = val;
    }

    // Public Methods
    // --------------

    @Override
    public int compareTo(DSINumber arg) {
        if ((arg instanceof DSIValue) && (((DSIValue) arg).isNull())) {
            return (this == NULL) ? 0 : 1;
        }
        if (isNull()) {
            return -1;
        }
        if (arg.isLong()) {
            return (int) (value = arg.toLong());
        } else if (arg.isDouble()) {
            return (int) (value - arg.toDouble());
        } else if (arg.isFloat()) {
            return (int) (value - arg.toFloat());
        }
        return (int) (value - arg.toInt());
    }

    /**
     * True if the argument is a DSINumber and the values are equal or they are both isNull.
     */
    @Override
    public boolean equals(Object arg) {
        if (arg instanceof DSINumber) {
            DSINumber num = (DSINumber) arg;
            if ((arg instanceof DSIValue) && (((DSIValue) arg).isNull())) {
                return this == NULL;
            }
            if (num.isDouble()) {
                return num.toDouble() == value;
            } else if (num.isFloat()) {
                return num.toFloat() == value;
            } else if (num.isInt()) {
                return num.toInt() == value;
            } else if (num.isLong()) {
                return num.toLong() == value;
            }
        }
        return false;
    }

    @Override
    public DSElementType getElementType() {
        return DSElementType.LONG;
    }

    @Override
    public DSValueType getValueType() {
        return DSValueType.NUMBER;
    }

    @Override
    public int hashCode() {
        return (int) (value ^ (value >>> 32));
    }

    @Override
    public boolean isLong() {
        return true;
    }

    @Override
    public boolean isNull() {
        return this == NULL;
    }

    @Override
    public boolean isNumber() {
        return true;
    }

    @Override
    public boolean toBoolean() {
        return value != 0;
    }

    @Override
    public double toDouble() {
        return value;
    }

    @Override
    public float toFloat() {
        return value;
    }

    @Override
    public int toInt() {
        return (int) value;
    }

    @Override
    public long toLong() {
        return value;
    }

    @Override
    public Number toNumber() {
        return value;
    }

    @Override
    public String toString() {
        if (this == NULL) {
            return "null";
        }
        return String.valueOf(value);
    }

    /**
     * Returns this.
     */
    @Override
    public DSLong valueOf(DSElement arg) {
        if ((arg == null) || arg.isNull()) {
            return NULL;
        }
        if (arg instanceof DSINumber) {
            return valueOf(arg.toLong());
        }
        if (arg instanceof DSString) {
            return valueOf(arg.toString());
        }
        throw new IllegalArgumentException("Can not decode long: " + arg);
    }

    /**
     * Attempts to reuse some common values before creating a new instance.
     */
    public static DSLong valueOf(long arg) {
        DSLong ret = null;
        int i = (int) arg;
        if (arg == i) {
            ret = LongCache.get(i);
        }
        if (ret == null) {
            ret = new DSLong(arg);
        }
        return ret;
    }

    /**
     * Checks for null, then uses Float.parseFloat()
     */
    public static DSLong valueOf(String arg) {
        if (arg == null) {
            return NULL;
        } else if (arg.length() == 0) {
            return NULL;
        } else if (arg.equalsIgnoreCase("null")) {
            return NULL;
        }
        return valueOf(Long.parseLong(arg));
    }

    // Inner Classes
    // --------------

    private static class LongCache {

        private static final DSLong[] cache = new DSLong[256];

        static DSLong get(long arg) {
            long idx = arg + 128;
            if ((idx < 0) || (idx > 255)) {
                return null;
            }
            int i = (int) idx;
            DSLong ret = cache[i];
            if (ret != null) {
                return ret;
            }
            ret = new DSLong(arg);
            cache[i] = ret;
            return ret;
        }

    }

    // Initialization
    // --------------

    static {
        DSRegistry.registerDecoder(DSLong.class, NULL);
    }

}
