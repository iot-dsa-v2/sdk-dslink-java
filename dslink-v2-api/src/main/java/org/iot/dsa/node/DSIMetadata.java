package org.iot.dsa.node;

/**
 * Nodes and values can implement this to provide meta-data about themselves.
 *
 * @author Aaron Hansen
 */
public interface DSIMetadata {

    /**
     * The entity should add any metadata about itself to the given map.
     */
    void getMetadata(DSMap bucket);

}
