package org.iot.dsa.node;

import java.util.Arrays;
import org.iot.dsa.io.DSBase64;

/**
 * Byte array that gets encoded as a base64 string.
 *
 * @author Aaron Hansen
 */
public class DSBytes extends DSValue {

    // Constants
    // ---------

    public static final DSBytes NULL = new DSBytes(new byte[0]);
    private static final String PREFIX = "\u001Bbytes:";

    // Fields
    // ------

    private byte[] value;

    // Constructors
    // ------------

    private DSBytes(byte[] val) {
        value = val;
    }

    // Public Methods
    // --------------

    /**
     * True if the argument is a DSINumber and the values are equal or they are both isNull.
     */
    @Override
    public boolean equals(Object arg) {
        if (arg == this) {
            return true;
        }
        if (arg instanceof DSBytes) {
            DSBytes other = (DSBytes) arg;
            return Arrays.equals(value, other.value);
        }
        return false;
    }

    /**
     * The raw bytes, do not modify.
     */
    public byte[] getBytes() {
        return value;
    }

    @Override
    public DSValueType getValueType() {
        return DSValueType.BINARY;
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

    @Override
    public boolean isNull() {
        return this == NULL;
    }

    /**
     * The number of bytes in the array.
     */
    public int length() {
        if (value == null) {
            return 0;
        }
        return value.length;
    }

    @Override
    public DSString toElement() {
        if (isNull()) {
            return DSString.NULL;
        }
        return DSString.valueOf(toString());
    }

    @Override
    public String toString() {
        if (isNull()) {
            return "null";
        }
        return PREFIX + DSBase64.encodeUrl(value);
    }

    @Override
    public DSBytes valueOf(DSElement arg) {
        if ((arg == null) || arg.isNull()) {
            return NULL;
        }
        if (arg instanceof DSString) {
            return valueOf(arg.toString());
        }
        throw new IllegalArgumentException("Cannot decode: " + arg);
    }

    /**
     * Decodes a base64 encoded byte array.
     */
    public static DSBytes valueOf(String arg) {
        if (arg == null) {
            return NULL;
        } else if (arg.length() == 0) {
            return NULL;
        } else if (arg.equalsIgnoreCase("null")) {
            return NULL;
        }
        if (arg.startsWith(PREFIX)) {
            arg = arg.substring(PREFIX.length());
        }
        return new DSBytes(DSBase64.decode(arg));
    }

    // Initialization
    // --------------

    static {
        DSRegistry.registerDecoder(DSBytes.class, NULL);
    }

}
