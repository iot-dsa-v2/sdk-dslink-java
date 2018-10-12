package org.iot.dsa.node;

/**
 * A Java int.
 *
 * @author Aaron Hansen
 */
public class DSInt extends DSValue implements Comparable<DSINumber>, DSIMetadata, DSINumber {

    // Constants
    // ---------

    public static final DSInt NULL = new DSInt(0);

    // Fields
    // ------

    private int value;

    // Constructors
    // ------------

    private DSInt(int val) {
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
        if (arg.isInt()) {
            return value - arg.toInt();
        } else if (arg.isDouble()) {
            return (int) (value - arg.toDouble());
        } else if (arg.isFloat()) {
            return (int) (value - arg.toFloat());
        }
        return (int) (value - arg.toLong());
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
    public void getMetadata(DSMap bucket) {
        bucket.put(DSMetadata.EDITOR, DSMetadata.NUM_EDITOR_INT);
    }

    @Override
    public DSValueType getValueType() {
        return DSValueType.NUMBER;
    }

    @Override
    public int hashCode() {
        return value;
    }

    @Override
    public boolean isDouble() {
        return false;
    }

    @Override
    public boolean isEqual(Object obj) {
        return equals(obj);
    }

    @Override
    public boolean isFloat() {
        return false;
    }

    @Override
    public boolean isInt() {
        return true;
    }

    @Override
    public boolean isLong() {
        return false;
    }

    @Override
    public boolean isNull() {
        return this == NULL;
    }

    @Override
    public double toDouble() {
        return value;
    }

    @Override
    public DSElement toElement() {
        if (isNull()) {
            return DSLong.NULL;
        }
        return DSLong.valueOf(value);
    }

    @Override
    public float toFloat() {
        return (float) value;
    }

    @Override
    public int toInt() {
        return (int) value;
    }

    @Override
    public long toLong() {
        return (long) value;
    }

    @Override
    public Number toNumber() {
        return value;
    }

    @Override
    public String toString() {
        if (isNull()) {
            return "null";
        }
        return String.valueOf(value);
    }

    @Override
    public DSInt valueOf(DSElement arg) {
        if ((arg == null) || arg.isNull()) {
            return NULL;
        }
        if (arg instanceof DSINumber) {
            return valueOf(arg.toInt());
        }
        if (arg instanceof DSString) {
            return valueOf(arg.toString());
        }
        throw new IllegalArgumentException("Can not decode int: " + arg);
    }

    /**
     * Attempts to reuse some common values before creating a new instance.
     */
    public static DSInt valueOf(int arg) {
        DSInt ret = IntCache.get(arg);
        if (ret == null) {
            ret = new DSInt(arg);
        }
        return ret;
    }

    /**
     * Checks for null, then uses Float.parseFloat()
     */
    public static DSInt valueOf(String arg) {
        if (arg == null) {
            return NULL;
        } else if (arg.length() == 0) {
            return NULL;
        } else if (arg.equalsIgnoreCase("null")) {
            return NULL;
        }
        return valueOf(Integer.parseInt(arg));
    }

    // Inner Classes
    // --------------

    private static class IntCache {

        private static final DSInt[] cache = new DSInt[256];

        static DSInt get(int arg) {
            int idx = arg + 128;
            if ((idx < 0) || (idx > 255)) {
                return null;
            }
            DSInt ret = cache[idx];
            if (ret != null) {
                return ret;
            }
            ret = new DSInt(arg);
            cache[idx] = ret;
            return ret;
        }

    }

    // Initialization
    // --------------

    static {
        DSRegistry.registerDecoder(DSInt.class, NULL);
    }

}
