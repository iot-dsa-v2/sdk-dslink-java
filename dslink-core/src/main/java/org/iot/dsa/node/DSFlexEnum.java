package org.iot.dsa.node;

import org.iot.dsa.logging.DSLogger;

/**
 * An enum where the range and value are mutable, primarily intended for defining action
 * parameters.
 *
 * @author Aaron Hansen
 */
public class DSFlexEnum extends DSLogger implements DSIEnum, DSIMetadata, DSIValue {

    // Constants
    // ---------

    public static final DSFlexEnum NULL = new DSFlexEnum("null", null); //TODO

    // Fields
    // ------

    private String value;
    private DSList values;

    // Constructors
    // ------------

    private DSFlexEnum() {
    }

    private DSFlexEnum(String value, DSList values) {
        this.value = value;
        this.values = values;
    }

    // Public Methods
    // --------------

    @Override
    public DSFlexEnum copy() {
        DSFlexEnum ret = new DSFlexEnum();
        ret.value = value;
        ret.values = values;
        return ret;
    }

    @Override
    public DSFlexEnum decode(DSElement arg) {
        if ((arg == null) || arg.isNull()) {
            return NULL;
        }
        if (arg instanceof DSMap) {
            DSFlexEnum ret = new DSFlexEnum();
            DSMap map = (DSMap) arg;
            ret.value = map.getString("value");
            ret.values = map.getList("values");
            return ret;
        }
        return valueOf(arg.toString());
    }

    @Override
    public DSElement encode() {
        if (isNull()) {
            return DSString.NULL;
        }
        DSMap ret = new DSMap();
        ret.put("value", value);
        ret.put("values", values);
        return ret;
    }

    /**
     * True if the argument is a DSFlexEnum and the values are equal or they are both isNull.
     */
    @Override
    public boolean equals(Object arg) {
        if (arg == this) {
            return true;
        }
        if (arg instanceof DSFlexEnum) {
            DSFlexEnum fe = (DSFlexEnum) arg;
            if (!value.equals(fe.value)) {
                return false;
            }
            if (!values.equals(fe.values)) {
                return false;
            }
            return true;
        }
        return false;
    }

    @Override
    public DSList getEnums(DSList bucket) {
        if (bucket == null) {
            bucket = new DSList();
        }
        bucket.addAll(values);
        return bucket;
    }

    @Override
    public void getMetadata(DSMap bucket) {
        bucket.put(DSMetadata.ENUM_RANGE, values.copy());
    }

    @Override
    public DSValueType getValueType() {
        return DSValueType.ENUM;
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

    @Override
    public boolean isNull() {
        return this == null;
    }

    @Override
    public String toString() {
        return value;
    }

    /**
     * Creates a enum representing the given value and range.
     *
     * @param value Must be in the given range.
     * @param range The all possible values in the range, must include the value param.
     */
    public static DSFlexEnum valueOf(String value, DSList range) {
        if (!range.contains(DSString.valueOf(value))) {
            throw new IllegalArgumentException("Not in range: " + value);
        }
        return new DSFlexEnum(value, range.copy());
    }

    /**
     * Creates a new enum for the given value using the range of values from this instance.
     *
     * @param value Must be a member of the range in this enum.  If null, the NULL instance will be
     *              returned.
     */
    public DSFlexEnum valueOf(String value) {
        if (value == null) {
            return NULL;
        }
        if (!values.contains(DSString.valueOf(value))) {
            throw new IllegalArgumentException("Not in range: " + value);
        }
        return new DSFlexEnum(value, values);
    }

    // Initialization
    // --------------

    static {
        DSRegistry.registerDecoder(DSFlexEnum.class, NULL);
    }

}
