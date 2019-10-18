package org.iot.dsa.node;

import java.util.HashMap;
import java.util.Map;

/**
 * The primitive types of the SDK.
 *
 * @author Aaron Hansen
 */
public enum DSElementType {

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

    /**
     * Returns the DSString representation of the type name.
     */
    public DSString toElement() {
        if (display == null) {
            display = DSString.valueOf(name().toLowerCase());
        }
        return display;
    }

    @Override
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

    static {
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
