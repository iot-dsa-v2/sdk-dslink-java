package org.iot.dsa.node;

/**
 * Indicates something that is/has a numeric value.
 *
 * @author Aaron Hansen
 */
public interface DSINumber {

    /**
     * Whether or not the object represents a double.
     */
    boolean isDouble();

    /**
     * Whether or not the object represents a double.
     */
    boolean isFloat();

    /**
     * Whether or not the object represents an int.
     */
    boolean isInt();

    /**
     * Whether or not the object represents a long.
     */
    boolean isLong();

    /**
     * If not a double, will cast the underlying value.
     */
    double toDouble();

    /**
     * If not a float, will cast the underlying value.
     */
    float toFloat();

    /**
     * If not an int, will cast the underlying value.
     */
    int toInt();

    /**
     * If not a long, will cast the underlying value.
     */
    long toLong();

    /**
     * Returns the Java primitive wrapper.
     */
    Number toNumber();

}
