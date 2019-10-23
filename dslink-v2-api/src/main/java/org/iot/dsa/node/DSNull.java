package org.iot.dsa.node;

/**
 * Try not to use, it is for decoding raw json.  Try to use strong typed null instances instead
 * (e.g. DBool.NULL).
 *
 * @author Aaron Hansen
 */
public class DSNull extends DSElement {

    // Constants
    // ---------

    public static DSNull NULL = new DSNull();

    // Fields
    // ------

    // Constructors
    // ------------

    private DSNull() {
    }

    // Public Methods
    // --------------

    /**
     * Returns this.
     */
    @Override
    public DSNull copy() {
        return this;
    }

    /**
     * True of the arg == this.
     */
    @Override
    public boolean equals(Object arg) {
        return arg == this;
    }

    @Override
    public DSElementType getElementType() {
        return DSElementType.NULL;
    }

    @Override
    public DSValueType getValueType() {
        return DSValueType.DYNAMIC;
    }

    /**
     * True
     */
    @Override
    public boolean isNull() {
        return true;
    }

    /**
     * Returns this.
     */
    @Override
    public DSNull toElement() {
        return this;
    }

    @Override
    public String toString() {
        return "null";
    }

    /**
     * Returns this.
     */
    @Override
    public DSNull valueOf(DSElement arg) {
        return this;
    }

    // Initialization
    // --------------

    static {
        DSRegistry.registerDecoder(DSNull.class, NULL);
    }

}
