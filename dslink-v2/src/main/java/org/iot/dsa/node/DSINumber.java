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
    public boolean isDouble();

    /**
     * Whether or not the object represents a double.
     */
    public boolean isFloat();

    /**
     * Whether or not the object represents an int.
     */
    public boolean isInt();

    /**
     * Whether or not the object represents a long.
     */
    public boolean isLong();

    /**
     * If not a double, will cast the underlying value.
     */
    public double toDouble();

    /**
     * If not a float, will cast the underlying value.
     */
    public float toFloat();

    /**
     * If not an int, will cast the underlying value.
     */
    public int toInt();

    /**
     * If not a long, will cast the underlying value.
     */
    public long toLong();

    /**
     * Returns the Java primitive wrapper.
     */
    public Number toNumber();

}
