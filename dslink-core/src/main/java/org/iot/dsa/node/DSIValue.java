package org.iot.dsa.node;

/**
 * How data values get mapped into the node tree.
 *
 * <p>
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
 * <li>If mutable, you should implement DSIPublisher so nodes can know of changes.
 *
 * </ul>
 *
 * @author Aaron Hansen
 */
public interface DSIValue extends DSIObject {

    /**
     * The DSA type mapping.
     */
    public DSValueType getValueType();

    /**
     * Values should have an instance representing null.  This will allow null defaults in nodes,
     * but the null instance can be used to properly decode incoming values such as set requests.
     */
    public boolean isNull();

    /**
     * Deserialize a value from the configuration database.
     */
    public DSIValue restore(DSElement element);

    /**
     * Serialize the value for the configuration database.
     */
    public DSElement store();

    /**
     * The current value should convert itself to an element for DSA interop such as subscription
     * updates, and setting requests.  This is not for configuration database serialization.
     */
    public DSElement toElement();

    /**
     * This should convert an element transmitted over DSA, such as subscription updates or set
     * requests.  This is not for configuration database deserialization.
     */
    public DSIValue valueOf(DSElement element);

}
