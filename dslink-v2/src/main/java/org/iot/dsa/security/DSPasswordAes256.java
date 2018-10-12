package org.iot.dsa.security;

import java.security.Key;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import org.iot.dsa.io.DSBase64;
import org.iot.dsa.node.DSElement;
import org.iot.dsa.node.DSIMetadata;
import org.iot.dsa.node.DSIStorable;
import org.iot.dsa.node.DSMap;
import org.iot.dsa.node.DSMetadata;
import org.iot.dsa.node.DSRegistry;
import org.iot.dsa.node.DSString;
import org.iot.dsa.node.DSValue;
import org.iot.dsa.node.DSValueType;
import org.iot.dsa.util.DSException;

/**
 * Do not casually use this, it requires the JCE unlimited strength jurisdiction policy files
 * wherever instances of this class are used.
 * <p>
 * This stores an encrypted password which can then be decrypted.
 *
 * @author Aaron Hansen
 */
public class DSPasswordAes256 extends DSValue implements DSIMetadata, DSIPassword, DSIStorable {

    // Constants
    // ---------

    private static Cipher cipher;
    private static Key key;
    public static final DSPasswordAes256 NULL = new DSPasswordAes256(DSString.NULL);

    // Fields
    // ------

    private DSString value; //base64 encoded

    // Constructors
    // ------------

    private DSPasswordAes256(DSString encrypted) {
        this.value = encrypted;
    }

    private DSPasswordAes256(String encrypted) {
        this(DSString.valueOf(encrypted));
    }

    // Public Methods
    // --------------

    /**
     * Returns the decrypted password.
     *
     * @throws DSException If there is a problem.
     */
    public String decode() {
        byte[] bytes = DSBase64.decode(value.toString());
        String ret = null;
        try {
            synchronized (cipher) {
                cipher.init(Cipher.DECRYPT_MODE, key);
                ret = new String(cipher.doFinal(bytes), DSString.UTF8);
            }
        } catch (Exception x) {
            DSException.throwRuntime(x);
        }
        return ret;
    }

    /**
     * Encrypts the given bytes.
     */
    public static String encode(byte[] arg) {
        byte[] bytes = null;
        try {
            synchronized (cipher) {
                cipher.init(Cipher.ENCRYPT_MODE, key);
                bytes = cipher.doFinal(arg);
            }
        } catch (Exception x) {
            DSException.throwRuntime(x);
        }
        return DSBase64.encodeUrl(bytes);
    }

    /**
     * Encrypts the given text.
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
     * Encrypts the string value of the given element and compares against the value stored in this
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
     * Encrypts the given string and compares against the value stored in this object.
     *
     * @param clearText If null, isNull, or is the empty string, this will on return true if this is
     *                  the NULL instance.
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
    public DSPasswordAes256 restore(DSElement element) {
        if (element.isNull()) {
            return NULL;
        }
        return new DSPasswordAes256(element.toString());
    }

    /**
     * Returns a string representing the url safe base64 encoding of the hash.
     */
    @Override
    public DSString toElement() {
        return value;
    }

    /**
     * The encrypted password, base64 encoded.
     */
    @Override
    public String toString() {
        return value.toString();
    }

    /**
     * Creates a encrypted password for the given clear text.
     *
     * @param arg The text to hash.
     * @return Returns the NULL instance if the arg is null, isNull() or the empty string.
     */
    @Override
    public DSPasswordAes256 valueOf(DSElement arg) {
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
     * Creates a encrypted password for the given clear text.
     *
     * @param arg The text to hash.
     * @return Returns the NULL instance if the arg is null or the empty string.
     */
    public static DSPasswordAes256 valueOf(String arg) {
        if (arg == null) {
            return NULL;
        } else if (arg.isEmpty()) {
            return NULL;
        }
        return new DSPasswordAes256(encode(arg));
    }

    // Initialization
    // --------------

    static {
        try {
            cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            byte[] nameBytes = DSPasswordAes256.class.getName().getBytes(DSString.UTF8);
            byte[] keyBytes = new byte[32];
            System.arraycopy(nameBytes, 0, keyBytes, 0, 32);
            key = new SecretKeySpec(keyBytes, "AES");
        } catch (Exception x) {
            Logger.getLogger("security").log(Level.SEVERE, "AES problem", x);
        }
        DSRegistry.registerDecoder(DSPasswordAes256.class, NULL);
    }

}
