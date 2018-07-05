package org.iot.dsa.node;


/**
 * Represents a boolean value.
 *
 * @author Aaron Hansen
 */
public class DSBool extends DSElement implements DSIBoolean {

    // Constants
    // ---------

    public static final DSBool TRUE = new DSBool(true);
    public static final DSBool FALSE = new DSBool(false);
    public static final DSBool NULL = new DSBool(false);

    // Fields
    // ------

    private boolean value;

    // Constructors
    // ------------

    private DSBool(boolean val) {
        value = val;
    }

    // Public Methods
    // --------------

    @Override
    public boolean equals(Object arg) {
        if (arg instanceof DSBool) {
            return arg == this;
        }
        return false;
    }


    @Override
    public boolean isBoolean() {
        return true;
    }

    @Override
    public boolean isNull() {
        return this == NULL;
    }

    @Override
    public DSElementType getElementType() {
        return DSElementType.BOOLEAN;
    }

    @Override
    public DSValueType getValueType() {
        return DSValueType.BOOL;
    }

    @Override
    public int hashCode() {
        if (value) {
            return Boolean.TRUE.hashCode();
        }
        return Boolean.FALSE.hashCode();
    }

    @Override
    public boolean toBoolean() {
        return value;
    }

    /**
     * 0 or 1.
     */
    @Override
    public double toDouble() {
        if (value) {
            return 1;
        }
        return 0;
    }

    /**
     * 0 or 1.
     */
    @Override
    public float toFloat() {
        if (value) {
            return 1;
        }
        return 0;
    }

    /**
     * 0 or 1.
     */
    @Override
    public int toInt() {
        if (value) {
            return 1;
        }
        return 0;
    }

    /**
     * 0 or 1.
     */
    @Override
    public long toLong() {
        if (value) {
            return 1;
        }
        return 0;
    }

    @Override
    public String toString() {
        if (isNull()) {
            return "null";
        }
        return String.valueOf(value);
    }

    /**
     * Will return either TRUE or FALSE.
     */
    public static DSBool valueOf(boolean arg) {
        if (arg) {
            return TRUE;
        }
        return FALSE;
    }

    @Override
    public DSBool valueOf(DSElement arg) {
        if ((arg == null) || arg.isNull()) {
            return NULL;
        }
        if (arg instanceof DSBool) {
            return (DSBool) arg;
        }
        if (arg instanceof DSString) {
            return valueOf(arg.toString());
        }
        throw new IllegalArgumentException("Cannot decoding boolean: " + arg);
    }

    /**
     * Will return NULL, TRUE or FALSE.
     *
     * @throws IllegalArgumentException If the string cannot be decoded.
     */
    public static DSBool valueOf(String arg) {
        if (arg == null) {
            return NULL;
        }
        if (arg.length() == 0) {
            return NULL;
        }
        if (arg.equalsIgnoreCase("null")) {
            return NULL;
        }
        if (arg.equalsIgnoreCase("true")) {
            return TRUE;
        }
        if (arg.equalsIgnoreCase("false")) {
            return FALSE;
        }
        if (arg.equals("1")) {
            return TRUE;
        }
        if (arg.equals("0")) {
            return FALSE;
        }
        throw new IllegalArgumentException("Cannot decode boolean: " + arg);
    }

    // Initialization
    // --------------

    static {
        DSRegistry.registerDecoder(DSBool.class, NULL);
    }

}
