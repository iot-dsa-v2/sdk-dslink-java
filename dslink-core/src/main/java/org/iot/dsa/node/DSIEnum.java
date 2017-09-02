package org.iot.dsa.node;

/**
 * DSA Enum mapping.
 *
 * @author Aaron Hansen
 */
public interface DSIEnum {

    /**
     * Adds the range of possible values to the given bucket.
     *
     * @param bucket Also the return value, can be null, which will result in the creation of a new
     *               list.
     * @return The list argument, or if it was null, a new list.
     */
    public DSList getEnums(DSList bucket);

    /**
     * The string representation of the the enum value.
     */
    public String toString();

}
