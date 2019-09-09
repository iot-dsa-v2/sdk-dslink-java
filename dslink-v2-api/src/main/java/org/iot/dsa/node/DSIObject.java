package org.iot.dsa.node;

/**
 * Interface of anything found in the node tree.
 *
 * @author Aaron Hansen
 */
public interface DSIObject {

    /**
     * Return a copy if it makes sense, otherwise return this.
     */
    public DSIObject copy();

}
