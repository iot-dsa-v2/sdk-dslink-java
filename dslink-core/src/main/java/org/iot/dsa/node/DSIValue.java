package org.iot.dsa.node;

/**
 * How data values are represented in the node tree.
 *
 * <p>
 *
 * Beyond the interface methods, custom implementations should:
 *
 * <p>
 *
 * <ul>
 *
 * <li>Have a NULL instance if possible.
 *
 * <li>Try to maintain singleton instances when possible, especially for common instances.
 *
 * <li>Instead of constructors, use static valueOf methods to ensure singleton values such as NULL
 * are used.
 *
 * <li>Register an instance for decoding with DSRegistry.registerDecoder(YourValue.class, instance)
 * in a static initializer. If you have a NULL instance, use that.
 *
 * <li>If mutable (avoid if at all possible), implement DSIPublisher so nodes know of changes.
 *
 * <li>If a DSNode subclass is implementing DSIValue, it's onSet(DSIValue) must also be overridden.
 *
 * </ul>
 *
 * @author Aaron Hansen
 * @see DSNode#onSet(DSIValue)
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
