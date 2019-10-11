package org.iot.dsa.node;

import java.util.HashMap;
import java.util.Map;

/**
 * The primitive types of the SDK.
 *
 * @author Aaron Hansen
 */
public enum DSElementType implements DSIEnum, DSIValue {

    BOOLEAN,
    BYTES,
    DOUBLE,
    LIST,
    LONG,
    MAP,
    NULL,
    STRING;

    private DSString display;
    private static Map<String, DSElementType> nameMap;

    @Override
    public DSIObject copy() {
        return this;
    }

    @Override
    public DSList getEnums(DSList bucket) {
        if (bucket == null) {
            bucket = new DSList();
        }
        for (DSElementType e : values()) {
            bucket.add(e.toElement());
        }
        return bucket;
    }

    @Override
    public DSValueType getValueType() {
        return DSValueType.ENUM;
    }

    @Override
    public boolean isNull() {
        return false;
    }

    /**
     * Whether or not the given type string can be converted to an element type.
     */
    public static boolean isValid(String type) {
        if (type == null) {
            return false;
        }
        return nameMap.containsKey(type);
    }

    /**
     * Returns a DSString representation of the name() in lowercase.
     */
    @Override
    public DSElement toElement() {
        if (display == null) {
            display = DSString.valueOf(name().toLowerCase());
        }
        return display;
    }

    @Override
    public DSElementType valueOf(DSElement element) {
        return nameMap.get(element.toString());
    }

    public String toString() {
        return toElement().toString();
    }

    static {
        DSRegistry.registerDecoder(DSElementType.class, BOOLEAN);
        nameMap = new HashMap<>();
        for (DSElementType e : values()) {
            nameMap.put(e.name(), e);
            nameMap.put(e.toString(), e);
        }
    }

}
