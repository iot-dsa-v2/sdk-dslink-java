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
    public DSIObject copy();

    /**
     * Equals implementation that doesn't require hashCodes to equal, primarily intended
     * so for comparing nodes.
     */
    public default boolean isEqual(Object obj) {
        return equals(obj);
    }

    public boolean isNull();

}
