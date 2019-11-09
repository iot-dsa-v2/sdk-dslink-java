package org.iot.dsa.node;

/**
 * DSA Enum mapping.
 *
 * @author Aaron Hansen
 */
public interface DSIEnum extends DSIMetadata {

    /**
     * Adds the range of possible values to the given bucket.
     *
     * @param bucket Also the return value, can be null, which will result in the creation of a new
     *               list.
     * @return The list argument, or if it was null, a new list.
     */
    DSList getEnums(DSList bucket);

    /**
     * Adds the enum range to the bucket.
     */
    @Override
    default void getMetadata(DSMap bucket) {
        bucket.put(DSMetadata.ENUM_RANGE, getEnums(new DSList()));
    }

    /**
     * The string representation of the the enum value.
     */
    @Override
    String toString();

}
