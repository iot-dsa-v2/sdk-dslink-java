package org.iot.dsa.node;

/**
 * Super class of anything found in the node tree.
 *
 * @author Aaron Hansen
 */
public interface DSIObject {

    /**
     * Return a copy if it makes sense, but return this otherwise.
     */
    public abstract DSIObject copy();

    public boolean isNull();

}
