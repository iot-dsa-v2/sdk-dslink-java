package org.iot.dsa.node;

/**
 * These are the primitive types in the DSA protocol.  Not all primitive types translate to the JSON
 * type system, unlike DSElementType which does.
 *
 * @author Aaron Hansen
 */
public enum DSValueType {

    BINARY("binary"),
    BOOL("bool"),
    DYNAMIC("dynamic"),
    ENUM("enum"),
    LIST("array"),
    MAP("map"),
    NUMBER("number"),
    STRING("string");

    private String display;

    private DSValueType(String display) {
        this.display = display;
    }

    public String toString() {
        return display;
    }

}
