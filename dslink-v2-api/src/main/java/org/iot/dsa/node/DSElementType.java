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

    private static Map<String, DSElementType> nameMap;
    private DSString display;

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
     * Returns a DSString representation of the name() in lowercase.
     */
    @Override
    public DSElement toElement() {
        if (display == null) {
            display = DSString.valueOf(name().toLowerCase());
        }
        return display;
    }

    public String toString() {
        return toElement().toString();
    }

    /**
     * Unlike Enum.valueOf, this will handle the lowercase display.
     */
    public static DSElementType valueFor(String type) {
        DSElementType ret = nameMap.get(type);
        return ret == null ? NULL : ret;
    }

    @Override
    public DSElementType valueOf(DSElement element) {
        DSElementType ret = nameMap.get(element.toString());
        return ret == null ? NULL : ret;
    }

    static {
        DSRegistry.registerDecoder(DSElementType.class, BOOLEAN);
        nameMap = new HashMap<>();
        for (DSElementType e : values()) {
            nameMap.put(e.name(), e);
            nameMap.put(e.toString(), e);
        }
        //DSA types
        nameMap.put("bool", BOOLEAN);
        nameMap.put("number", DOUBLE);
        nameMap.put("array", LIST);
        nameMap.put("binary", BYTES);
        nameMap.put("dynamic", NULL);
    }
}
