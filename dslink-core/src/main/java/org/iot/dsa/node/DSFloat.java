package org.iot.dsa.node;

/**
 * A Java float.  Try not to use, floats can only be converted to doubles by converting them to
 * strings.
 *
 * @author Aaron Hansen
 */
public class DSFloat implements DSINumber, DSIValue {

    // Constants
    // ---------

    public static final DSFloat NULL = new DSFloat(0);

    // Fields
    // ------

    private float value;

    // Constructors
    // ------------

    private DSFloat(float val) {
        value = val;
    }

    // Public Methods
    // --------------

    public DSFloat copy() {
        return this;
    }

    @Override
    public DSFloat decode(DSElement arg) {
        if ((arg == null) || arg.isNull()) {
            return NULL;
        }
        if (arg instanceof DSINumber) {
            return valueOf(arg.toFloat());
        }
        if (arg instanceof DSString) {
            return valueOf(arg.toString());
        }
        throw new IllegalArgumentException("Cannot decode float: " + arg);
    }

    @Override
    public DSElement encode() {
        if (isNull()) {
            return DSDouble.NULL;
        }
        return DSDouble.valueOf(String.valueOf(value));
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
    public DSValueType getValueType() {
        return DSValueType.NUMBER;
    }

    @Override
    public int hashCode() {
        return Float.floatToIntBits(value);
    }

    @Override
    public boolean isDouble() {
        return false;
    }

    @Override
    public boolean isFloat() {
        return true;
    }

    @Override
    public boolean isInt() {
        return false;
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
        return Double.valueOf(value + "");
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

    /**
     * Attempts to reuse some common values before creating a new instance.
     */
    public static DSFloat valueOf(float arg) {
        DSFloat ret = FloatCache.get(arg);
        if (ret == null) {
            ret = new DSFloat(arg);
        }
        return ret;
    }

    /**
     * Checks for null, then uses Float.parseFloat()
     */
    public static DSFloat valueOf(String arg) {
        if (arg == null) {
            return NULL;
        } else if (arg.length() == 0) {
            return NULL;
        } else if (arg.equalsIgnoreCase("null")) {
            return NULL;
        }
        return valueOf(Float.parseFloat(arg));
    }

    // Inner Classes
    // --------------

    private static class FloatCache {

        private static final DSFloat[] cache = new DSFloat[256];

        static DSFloat get(float arg) {
            if ((arg % 1) != 0) {
                return null;
            }
            int idx = ((int) arg) + 128;
            if ((idx < 0) || (idx > 255)) {
                return null;
            }
            DSFloat ret = cache[idx];
            if (ret != null) {
                return ret;
            }
            ret = new DSFloat(arg);
            cache[idx] = ret;
            return ret;
        }

    }

    // Initialization
    // --------------

    static {
        DSRegistry.registerNull(DSFloat.class, NULL);
    }

}
