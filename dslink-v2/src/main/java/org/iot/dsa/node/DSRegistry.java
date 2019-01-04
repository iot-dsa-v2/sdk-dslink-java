package org.iot.dsa.node;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Static type related meta-data.
 *
 * @author Aaron Hansen
 */
public class DSRegistry {

    // Fields
    // ------

    private static ConcurrentHashMap<Class, DSIValue> decoderMap =
            new ConcurrentHashMap<Class, DSIValue>();
    private static ConcurrentHashMap<Class, DSNode> defaultMap =
            new ConcurrentHashMap<Class, DSNode>();

    // Public Methods
    // --------------

    /**
     * The instance to use for decoding.
     *
     * @param clazz The class of a value type.
     */
    public static DSIValue getDecoder(Class clazz) {
        return decoderMap.get(clazz);
    }

    static DSNode getDefault(Class clazz) {
        return defaultMap.get(clazz);
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
     * DSIValues must provide an instance for decoding.
     *
     * @param clazz    The type the instance is for.
     * @param instance An instance to use for decoding.
     */
    public static void registerDecoder(Class clazz, DSIValue instance) {
        decoderMap.put(clazz, instance);
    }

    /**
     * De-registers a default instance.  This is used when creating a temporary default instance to
     * prevent infinite loops, when create default instances.
     */
    static DSNode removeDefault(Class clazz) {
        return defaultMap.remove(clazz);
    }

}
