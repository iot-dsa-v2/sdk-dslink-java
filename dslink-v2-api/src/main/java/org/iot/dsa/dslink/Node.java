package org.iot.dsa.dslink;

import org.iot.dsa.node.DSMap;

/**
 * Used in DSIResponder list responses.
 *
 * @author Aaron Hansen
 */
public interface Node {

    /**
     * Add all metadata for the object.  Does nothing by default.
     */
    default void getMetadata(DSMap bucket) {
    }

    /**
     * Whether or not the object requires admin level permission.  Returns false
     * by default.
     */
    default boolean isAdmin() {
        return false;
    }

}
