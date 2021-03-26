package org.iot.dsa.node;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * String wrapper.
 *
 * @author Aaron Hansen
 */
public class DSString extends DSElement implements Comparable<Object> {

    // Constants
    // ---------

    /**
     * The string of length 0.
     */
    public static final DSString EMPTY = new DSString("");

    public static final DSString NULL = new DSString(null);

    /**
     * The standard UTF8 charset, can be used with string.getBytes(Charset).
     */
    public static final Charset UTF8 = StandardCharsets.UTF_8;

    // Fields
    // ------

    private String value;

    // Constructors
    // ------------

    static {
        DSRegistry.registerDecoder(DSString.class, NULL);
    }

    // Public Methods
    // --------------

    DSString(String val) {
        value = val;
    }

    @Override
    public int compareTo(Object arg) {
        if ((arg instanceof DSIValue) && (((DSIValue) arg).isNull())) {
            return (this == NULL) ? 0 : 1;
        }
        if (isNull()) {
            return -1;
        }
        return value.compareTo(arg.toString());
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof DSElement)) {
            if (o instanceof String) {
                return value.equals(o.toString());
            }
            return false;
        }
        DSElement obj = (DSElement) o;
        if (obj.getElementType() != DSElementType.STRING) {
            return false;
        }
        return value.equals(obj.toString());
    }

    /**
     * Wraps String.format
     */
    public static DSString format(String format, Object... args) {
        return valueOf(String.format(format, args));
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

    /**
     * True if not null and not empty.
     */
    public static boolean isNotEmpty(String str) {
        if (str == null) {
            return false;
        }
        return !str.isEmpty();
    }

    @Override
    public boolean isString() {
        return true;
    }

    @Override
    public boolean toBoolean() {
        if (value.equalsIgnoreCase("true")) {
            return true;
        } else if (value.equals("0")) {
            return false;
        } else if (value.equals("1")) {
            return true;
        } else if (value.equalsIgnoreCase("on")) {
            return true;
        }
        return false;
    }

    @Override
    public byte[] toBytes() {
        if (isNull()) {
            return null;
        }
        return DSBytes.decode(value);
    }

    @Override
    public double toDouble() {
        return Double.parseDouble(value);
    }

    @Override
    public float toFloat() {
        return Float.parseFloat(value);
    }

    @Override
    public int toInt() {
        return Integer.parseInt(value);
    }

    @Override
    public long toLong() {
        return Long.parseLong(value);
    }

    @Override
    public String toString() {
        return value;
    }

    @Override
    public DSString valueOf(DSElement arg) {
        if (arg.isString()) {
            return (DSString) arg;
        }
        return valueOf(arg.toString());
    }

    public static DSString valueOf(Object arg) {
        if (arg == null) {
            return NULL;
        }
        if (arg instanceof DSElement) {
            return NULL.valueOf((DSElement) arg);
        }
        String str = arg.toString();
        if (str.isEmpty()) {
            return EMPTY;
        }
        return new DSString(str);
    }

}
