package org.iot.dsa.node;

/**
 * Nodes and values can implement this to provide meta-data about themselves.
 *
 * @author Aaron Hansen
 */
public interface DSIMetadata {

    /**
     * The entity should add any metadata about itself to the given map.  DSIMetadata instances will
     * first populate the bucket, then parent nodes will be given a chance the modify the bucket via
     * DSNode.getMetadata(DSInfo,DSMap).
     */
    public void getMetadata(DSMap bucket);

}
