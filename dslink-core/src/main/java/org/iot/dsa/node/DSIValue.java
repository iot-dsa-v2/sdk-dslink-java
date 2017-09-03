package org.iot.dsa.node;

/**
 * Atomic values of the node tree.
 *
 * <ul>
 *
 * <li>Try to make immutable instances when possible.
 *
 * <li>Try to maintain singleton instances when possible.
 *
 * <li>All value types should have an instance representing null.  With this, the link can report
 * the type of a value node to clients even when they are null.  The null instance should be
 * registered with DSRegistry and that instance will be used to decode a serialized node database.
 *
 * <li>Use a static valueOf method(s) rather than a constructor(s), it helps manage singletons.
 *
 * <li>If mutable, you must implement DSIPublisher so clients can know of changes.
 *
 * </ul>
 *
 * @author Aaron Hansen
 */
public interface DSIValue extends DSIObject {

    /**
     * Instances must decode new instances from an element type.  The null instance
     * from will most often be used for decoding.
     */
    public DSIValue decode(DSElement element);

    /**
     * Instances must convert themselves into an element type.
     */
    public DSElement encode();

    /**
     * The DSA type mapping.
     */
    public DSValueType getValueType();

    /**
     * All values must have an instance representing null.
     */
    public boolean isNull();

}
