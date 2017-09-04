package org.iot.dsa.node;

/**
 * String wrapper.
 *
 * @author Aaron Hansen
 */
public class DSString extends DSElement {

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

    @Override
    public DSString valueOf(DSElement arg) {
        return valueOf(arg.toString());
    }

    public static DSString valueOf(Object arg) {
        if (arg == null) {
            return NULL;
        }
        String str = arg.toString();
        if (str.isEmpty()) {
            return EMPTY;
        }
        return new DSString(str);
    }

    // Initialization
    // --------------

    static {
        DSRegistry.registerDecoder(DSString.class, NULL);
    }

}
