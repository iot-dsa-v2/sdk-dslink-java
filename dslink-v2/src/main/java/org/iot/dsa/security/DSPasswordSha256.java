package org.iot.dsa.security;

import java.security.MessageDigest;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.acuity.iot.dsa.dslink.io.DSBase64;
import org.iot.dsa.node.DSElement;
import org.iot.dsa.node.DSIMetadata;
import org.iot.dsa.node.DSIStorable;
import org.iot.dsa.node.DSMap;
import org.iot.dsa.node.DSMetadata;
import org.iot.dsa.node.DSRegistry;
import org.iot.dsa.node.DSString;
import org.iot.dsa.node.DSValue;
import org.iot.dsa.node.DSValueType;

/**
 * Requires the JCE unlimited strength jurisdiction policy files wherever instances
 * of this class are used.
 * <p>
 * Stores and verifies passwords using a SHA-256 hash of the text (the clear text is not
 * accessible).
 * <p>
 * DSPasswordSha1 (SHA-1) is not considered secure against well-funded opponents, but it doesn't
 * require the JCE unlimited strength jurisdiction policy files.
 *
 * @author Aaron Hansen
 */
public class DSPasswordSha256 extends DSValue implements DSIMetadata, DSIPassword, DSIStorable {

    // Constants
    // ---------

    public static final DSPasswordSha256 NULL = new DSPasswordSha256(DSString.NULL);

    private static MessageDigest digest;

    // Fields
    // ------

    private DSString value;

    // Constructors
    // ------------

    private DSPasswordSha256(DSString hashBase64) {
        this.value = hashBase64;
    }

    private DSPasswordSha256(String hashBase64) {
        this(DSString.valueOf(hashBase64));
    }

    // Public Methods
    // --------------

    /**
     * SHA-256 hash of the bytes encoded as url safe base 64..
     */
    public static String encode(byte[] arg) {
        return DSBase64.encodeUrl(hash(arg));
    }

    /**
     * SHA-256 hash of the UTF-8 bytes, encoded as url safe base 64..
     */
    public static String encode(String arg) {
        return encode(arg.getBytes(DSString.UTF8));
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof DSPasswordAes256) {
            return value.equals(obj.toString());
        }
        return false;
    }

    @Override
    public void getMetadata(DSMap bucket) {
        bucket.put(DSMetadata.EDITOR, DSMetadata.STR_EDITOR_PASSWORD);
    }

    @Override
    public DSValueType getValueType() {
        return DSValueType.STRING;
    }

    /**
     * SHA-256 hash of the bytes.
     */
    static byte[] hash(byte[] arg) {
        byte[] hash = null;
        synchronized (digest) {
            hash = digest.digest(arg);
            digest.reset();
        }
        return hash;
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

    @Override
    public boolean isEqual(Object obj) {
        return equals(obj);
    }

    @Override
    public boolean isNull() {
        return this == NULL;
    }

    /**
     * Hashes the string value of the given element and compares against the hash stored in this
     * object.
     *
     * @param clearText If null, isNull, or is the empty string, this will on return true if this is
     *                  the NULL instance.
     */
    public boolean isValid(DSElement clearText) {
        if (clearText == null) {
            return isNull();
        }
        if (clearText.isNull()) {
            return isNull();
        }
        return isValid(clearText.toString());
    }

    /**
     * Hashes the given string and compares against the value stored in this object.
     *
     * @param clearText If null, or the empty string, this will on return true if this is the NULL
     *                  instance.
     */
    public boolean isValid(String clearText) {
        if ((clearText == null) || clearText.isEmpty()) {
            return isNull();
        }
        return value.toString().equals(encode(clearText));
    }

    @Override
    public DSString store() {
        return toElement();
    }

    @Override
    public DSPasswordSha256 restore(DSElement element) {
        if (element.isNull()) {
            return NULL;
        }
        return new DSPasswordSha256(element.toString());
    }

    /**
     * Returns a string representing the url safe base64 encoding of the hash.
     */
    @Override
    public DSString toElement() {
        return value;
    }

    @Override
    public String toString() {
        return value.toString();
    }

    /**
     * Creates a digest password for the given clear text.
     *
     * @param arg The text to hash.
     * @return Returns the NULL instance if the arg is null, isNull() or the empty string.
     */
    @Override
    public DSPasswordSha256 valueOf(DSElement arg) {
        if ((arg == null) || arg.isNull()) {
            return NULL;
        }
        String s = arg.toString();
        if (s.isEmpty()) {
            return NULL;
        }
        return valueOf(s);
    }

    /**
     * Creates a digest password for the given clear text.
     *
     * @param arg The text to hash.
     * @return Returns the NULL instance if the arg is null or the empty string.
     */
    public static DSPasswordSha256 valueOf(String arg) {
        if (arg == null) {
            return NULL;
        } else if (arg.isEmpty()) {
            return NULL;
        }
        return new DSPasswordSha256(encode(arg));
    }

    // Initialization
    // --------------

    static {
        try {
            digest = MessageDigest.getInstance("SHA-256");
        } catch (Exception x) {
            Logger.getLogger("security").log(Level.SEVERE, "SHA-256 unknown", x);
        }
        DSRegistry.registerDecoder(DSPasswordSha256.class, NULL);
    }

}
