package org.iot.dsa.node;

/**
 * These are the primitive types in the DSA protocol.  Not all primitive types translate to the JSON
 * type system, unlike DSElementType which does.
 *
 * @author Aaron Hansen
 */
public enum DSValueType {

    ANY,
    BINARY,
    BOOL,
    ENUM,
    LIST,
    MAP,
    NUMBER,
    STRING;

    private String display;

    public String toString() {
        if (display == null) {
            display = name().toLowerCase();
        }
        return display;
    }

}
