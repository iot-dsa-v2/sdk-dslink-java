package org.iot.dsa.node;

/**
 * A 64 bit floating point (Java double).
 *
 * @author Aaron Hansen
 */
public class DSDouble extends DSElement implements DSINumber {

    // Constants
    // ---------

    public static final DSDouble NULL = new DSDouble(Double.NaN);

    // Fields
    // ------

    private double value;

    // Constructors
    // ------------

    DSDouble(double val) {
        value = val;
    }

    // Public Methods
    // --------------

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
        return DSElementType.DOUBLE;
    }

    @Override
    public DSValueType getValueType() {
        return DSValueType.NUMBER;
    }

    @Override
    public int hashCode() {
        long v = Double.doubleToLongBits(value);
        return (int) (v ^ (v >>> 32));
    }

    @Override
    public boolean isDouble() {
        return true;
    }

    /**
     * @see Double#isInfinite()
     */
    public boolean isInfinite() {
        return Double.isInfinite(value);
    }

    /**
     * @see Double#isNaN()
     */
    public boolean isNaN() {
        return Double.isNaN(value);
    }

    @Override
    public boolean isNumber() {
        return true;
    }

    @Override
    public boolean isNull() {
        return this == NULL;
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
        return Float.parseFloat(String.valueOf(value));
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
    public static DSDouble valueOf(double arg) {
        DSDouble ret = null;
        int i = (int) arg;
        if (arg == i) {
            ret = DblCache.get(i);
        }
        if (ret == null) {
            ret = new DSDouble(arg);
        }
        return ret;
    }

    @Override
    public DSDouble valueOf(DSElement arg) {
        if ((arg == null) || arg.isNull()) {
            return NULL;
        }
        if (arg == this) {
            return this;
        }
        if (arg instanceof DSINumber) {
            if (arg instanceof DSDouble) {
                return (DSDouble) arg;
            }
            return valueOf(arg.toDouble());
        }
        if (arg instanceof DSString) {
            return valueOf(arg.toString());
        }
        throw new IllegalArgumentException("Cannot decoding double: " + arg);
    }

    /**
     * Checks for null, then uses Double.parseDouble()
     */
    public static DSDouble valueOf(String arg) {
        if (arg == null) {
            return NULL;
        } else if (arg.length() == 0) {
            return NULL;
        } else if (arg.equalsIgnoreCase("null")) {
            return NULL;
        }
        return valueOf(Double.parseDouble(arg));
    }

    // Inner Classes
    // --------------

    private static class DblCache {

        private static final DSDouble[] cache = new DSDouble[256];

        static DSDouble get(float arg) {
            if ((arg % 1) != 0) {
                return null;
            }
            int idx = ((int) arg) + 128;
            if ((idx < 0) || (idx > 255)) {
                return null;
            }
            DSDouble ret = cache[idx];
            if (ret != null) {
                return ret;
            }
            ret = new DSDouble(arg);
            cache[idx] = ret;
            return ret;
        }

    }

    // Initialization
    // --------------

    static {
        DSRegistry.registerDecoder(DSDouble.class, NULL);
    }

}//DSDouble
