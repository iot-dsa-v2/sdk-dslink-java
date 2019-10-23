package org.iot.dsa.node;

/**
 * The primitives of the node model.  These map to JSON types without additional meta-data.
 *
 * @author Aaron Hansen
 */
public abstract class DSElement extends DSValue {

    /**
     * If an object is mutable (list or map) then this should clone it, immutable objects can simply
     * return themselves.
     */
    @Override
    public DSElement copy() {
        return this;
    }

    /**
     * For switch statements.
     */
    public abstract DSElementType getElementType();

    /**
     * The DSA value type mapping.
     */
    @Override
    public abstract DSValueType getValueType();

    /**
     * Whether or not the object represents a boolean.
     */
    public boolean isBoolean() {
        return false;
    }

    /**
     * Whether or not the object represents a byte array.
     */
    public boolean isBytes() {
        return false;
    }

    /**
     * Whether or not the object represents a double.
     */
    public boolean isDouble() {
        return false;
    }

    /**
     * Whether or not the object represents a float.
     */
    public boolean isFloat() {
        return false;
    }

    /**
     * Whether or not the object represents a list or map.
     */
    public boolean isGroup() {
        return false;
    }

    /**
     * Whether or not the object represents an int.
     */
    public boolean isInt() {
        return false;
    }

    /**
     * Whether or not the object represents a list.
     */
    public boolean isList() {
        return false;
    }

    /**
     * Whether or not the object represents a long.
     */
    public boolean isLong() {
        return false;
    }

    /**
     * Whether or not the object represents a amp.
     */
    public boolean isMap() {
        return false;
    }

    /**
     * Whether or not the object represents null.
     */
    @Override
    public boolean isNull() {
        return false;
    }

    /**
     * Whether or not the object represents a number.
     */
    public boolean isNumber() {
        return false;
    }

    /**
     * Whether or not the object represents a string.
     */
    public boolean isString() {
        return false;
    }

    /**
     * Creates an DSIObject representation of the primitive.
     */
    public static DSElement make(boolean arg) {
        return DSBool.valueOf(arg);
    }

    /**
     * Creates an DSIObject representation of the primitive.
     */
    public static DSElement make(byte[] arg) {
        return DSBytes.valueOf(arg);
    }

    /**
     * Creates an DSIObject representation of the primitive.
     */
    public static DSElement make(double arg) {
        return DSDouble.valueOf(arg);
    }

    /**
     * Creates an DSIObject representation of the primitive.
     */
    public static DSElement make(int arg) {
        return DSLong.valueOf(arg);
    }

    /**
     * Creates an DSIObject representation of the primitive.
     */
    public static DSElement make(long arg) {
        return DSLong.valueOf(arg);
    }

    /**
     * Creates an DSIObject representation of the primitive.
     */
    public static DSElement make(String arg) {
        if (arg == null) {
            return makeNull();
        }
        return DSString.valueOf(arg);
    }

    /**
     * Creates an DSIObject representation of null.
     */
    public static DSElement makeNull() {
        return DSNull.NULL;
    }

    /**
     * Attempts to return a boolean value.  Numerics will return false for 0 and true for anything
     * else.  Strings should return true for "true" or "1" and false for "false" or "0".  Anything
     * else will throws a ClassCastException.
     *
     * @throws ClassCastException If not convertible.
     */
    public boolean toBoolean() {
        throw new ClassCastException(getClass().getName() + " not boolean");
    }

    /**
     * Returns the raw byte array for DSBytes only, which should not be modified.
     *
     * @throws ClassCastException If not DSBytes.
     */
    public byte[] toBytes() {
        throw new ClassCastException(getClass().getName() + " not bytes");
    }

    /**
     * Attempts to return a double value.  Numerics of other types will cast the results. Booleans
     * will return 0 for false and 1 for true. Strings will attempt to parseRequest the numeric
     * which may result in a parseRequest exception.  Anything else will throw a
     * ClassCastException.
     *
     * @throws ClassCastException If not convertible.
     */
    public double toDouble() {
        throw new ClassCastException(getClass().getName() + " not double");
    }

    /**
     * Returns this.
     */
    @Override
    public DSElement toElement() {
        return this;
    }

    /**
     * Attempts to return a float value.  Numerics of other types will cast the results. Booleans
     * will return 0 for false and 1 for true. Strings will attempt to parseRequest the numeric
     * which may result in a parseRequest exception.  Anything else will throw a
     * ClassCastException.
     *
     * @throws ClassCastException If not convertible.
     */
    public float toFloat() {
        throw new ClassCastException(getClass().getName() + " not float");
    }

    /**
     * Lists and maps return themselves, everything else results in an exception.
     *
     * @throws ClassCastException If not convertible.
     */
    public DSGroup toGroup() {
        throw new ClassCastException(getClass().getName() + " not list");
    }

    /**
     * Attempts to return an int value.  Numerics of other types will cast the results. Booleans
     * will return 0 for false and 1 for true. Strings will attempt to parseRequest the numeric
     * which may result in a parseRequest exception.  Anything else will throw a
     * ClassCastException.
     *
     * @throws ClassCastException If not convertible.
     */
    public int toInt() {
        throw new ClassCastException(getClass().getName() + " not int");
    }

    /**
     * Lists return themselves, everything else results in an exception.
     *
     * @throws ClassCastException If not convertible.
     */
    public DSList toList() {
        throw new ClassCastException(getClass().getName() + " not list");
    }

    /**
     * Attempts to return a long value.  Numerics of other types will cast the results. Booleans
     * will return 0 for false and 1 for true. Strings will attempt to parseRequest the numeric
     * which may result in a parseRequest exception.  Anything else will throw a
     * ClassCastException.
     *
     * @throws ClassCastException If not convertible.
     */
    public long toLong() {
        throw new ClassCastException(getClass().getName() + " not long");
    }

    /**
     * Maps return themselves, everything else results in an exception.
     *
     * @throws ClassCastException If not convertible.
     */
    public DSMap toMap() {
        throw new ClassCastException(getClass().getName() + " not map");
    }

    /**
     * Elements must convert the argument to their type.
     */
    @Override
    public DSIValue valueOf(DSElement arg) {
        return arg;
    }

}
