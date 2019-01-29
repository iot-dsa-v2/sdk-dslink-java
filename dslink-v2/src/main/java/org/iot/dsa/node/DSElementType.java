package org.iot.dsa.node;

/**
 * Thre primitives of this SDK.
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

    private String display;

    public String toString() {
        if (display == null) {
            display = name().toLowerCase();
        }
        return display;
    }

}
