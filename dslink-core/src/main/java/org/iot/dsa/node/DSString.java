package org.iot.dsa.node;

/**
 * String wrapper.
 *
 * @author Aaron Hansen
 */
public class DSString extends DSElement implements DSIValue {

    // Constants
    // ---------

    /**
     * The string of length 0.
     */
    public static final DSString EMPTY = new DSString("");
    public static final DSString NULL = new DSString(null);

    // Fields
    // ------

    private String value;

    // Constructors
    // ------------

    DSString(String val) {
        value = val;
    }

    // Public Methods
    // --------------

    /**
     * Returns this.
     */
    @Override
    public DSString copy() {
        return this;
    }

    @Override
    public DSString decode(DSElement arg) {
        return valueOf(arg.toString());
    }

    /**
     * Returns this.
     */
    @Override
    public DSString encode() {
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof DSElement)) {
            return false;
        }
        DSElement obj = (DSElement) o;
        if (obj.getElementType() != DSElementType.STRING) {
            return false;
        }
        return value.equals(obj.toString());
    }

    @Override
    public DSElementType getElementType() {
        return DSElementType.STRING;
    }

    @Override
    public DSValueType getValueType() {
        return DSValueType.STRING;
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

    @Override
    public boolean isString() {
        return true;
    }

    @Override
    public boolean toBoolean() {
        if (value.equalsIgnoreCase("true")) {
            return true;
        } else if (value.equalsIgnoreCase("false")) {
            return true;
        } else if (value.equals("0")) {
            return false;
        } else if (value.equals("1")) {
            return true;
        } else if (value.equalsIgnoreCase("on")) {
            return true;
        } else if (value.equalsIgnoreCase("off")) {
            return false;
        }
        return false;
    }

    @Override
    public String toString() {
        if (value == null) {
            return "null";
        }
        return value;
    }

    public static DSString valueOf(String arg) {
        if (arg == null) {
            return NULL;
        }
        if (arg.isEmpty()) {
            return EMPTY;
        }
        return new DSString(arg);
    }

    // Initialization
    // --------------

    static {
        DSRegistry.registerNull(DSString.class, NULL);
    }

}
