package org.iot.dsa.node;

/**
 * These are the primitive types of DSA, which are different than the SDK primitives.
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

    DSValueType(String display) {
        this.display = display;
    }

    @Override
    public String toString() {
        return display;
    }

}
