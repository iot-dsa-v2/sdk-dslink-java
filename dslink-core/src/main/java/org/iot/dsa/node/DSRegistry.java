package org.iot.dsa.node;


import java.util.concurrent.ConcurrentHashMap;

/**
 * Static type related meta-data.
 *
 * @author Aaron Hansen
 */
public class DSRegistry {

    // Constants
    // ---------

    // Fields
    // ------

    private static ConcurrentHashMap<Class, DSNode> defaultMap =
            new ConcurrentHashMap<Class, DSNode>();
    private static ConcurrentHashMap<Class, DSIValue> nullMap =
            new ConcurrentHashMap<Class, DSIValue>();

    // Constructors
    // ------------

    // Public Methods
    // --------------

    static DSNode getDefault(Class clazz) {
        return defaultMap.get(clazz);
    }

    /**
     * The null instance for the give DSIValue.
     *
     * @param clazz The class of a synthetic type.
     */
    public static DSIValue getNull(Class clazz) {
        return nullMap.get(clazz);
    }

    /**
     * DSIValue subclasses must register a decoder for their specific class.  Sub and super classes
     * will not be used.
     *
     * @param clazz    The container type for the default instance being registered.
     * @param instance The default instance for the given class.
     */
    static void registerDefault(Class clazz, DSNode instance) {
        defaultMap.put(clazz, instance);
    }

    /**
     * DSSynthetics must provide the null instance for their specific class type.
     *
     * @param clazz    The type the instance is for.
     * @param instance The null instance, will often be used for decoding.
     */
    public static void registerNull(Class clazz, DSIValue instance) {
        nullMap.put(clazz, instance);
    }

    /**
     * De-registers a default instance.  This is used when creating a temporary default instance to
     * prevent infinite loops, when create default instances.
     */
    static DSNode removeDefault(Class clazz) {
        return defaultMap.remove(clazz);
    }

    // Inner Classes
    // -------------

}
