package org.iot.dsa.node;

import java.util.List;

/**
 * DSA Enum mapping.
 *
 * @author Aaron Hansen
 */
public interface DSIEnum {

    /**
     * Returns the range of enum values as an immutable list.
     */
    public List<String> getEnums();

    /**
     * The string representation of the the enum value.
     */
    public String toString();

}
