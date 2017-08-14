package org.iot.dsa.security;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.security.AlgorithmParameters;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.security.spec.ECGenParameterSpec;
import java.security.spec.ECParameterSpec;
import java.security.spec.ECPoint;
import java.security.spec.ECPrivateKeySpec;
import java.security.spec.ECPublicKeySpec;
import java.util.Arrays;
import javax.crypto.KeyAgreement;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.iot.dsa.io.DSBase64;
import org.iot.dsa.util.DSException;

/**
 * DSA public private key pair.
 *
 * @author Aaron Hansen
 */
public class DSKeys {

    ///////////////////////////////////////////////////////////////////////////
    // Constants
    ///////////////////////////////////////////////////////////////////////////

    ///////////////////////////////////////////////////////////////////////////
    // Fields
    ///////////////////////////////////////////////////////////////////////////

    private KeyPair keyPair;

    ///////////////////////////////////////////////////////////////////////////
    // Constructors
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Creates a DSKeys object by decoding existing keys from the given file.  If the file does not
     * exist, new keys will be created and stored in that file.
     *
     * @param keyFile The file to decodeKeys existing keys, or to store newly generated keys.
     * @throws DSException Wrapping underlying IOExceptions.
     */
    public DSKeys(File keyFile) {
        if (!keyFile.exists()) {
            keyPair = newKeyPair();
            store(keyFile);
        } else {
            DSKeys tmp = restore(keyFile);
            keyPair = tmp.keyPair;
        }
    }

    /**
     * Creates a DSKeys object for an existing key pair.
     *
     * @param keyPair Must represent the EC curve secp256r1.
     */
    public DSKeys(KeyPair keyPair) {
        this.keyPair = keyPair;
    }

    ///////////////////////////////////////////////////////////////////////////
    // Methods
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Decodes an instance that was encoded with encodeKeys().
     *
     * @param arg String produced by encodeKeys().
     */
    public static DSKeys decodeKeys(String arg) {
        String[] keys = arg.split(" ");
        ECPublicKey publicKey = decodePublic(DSBase64.decode(keys[0]));
        ECPrivateKey privateKey = decodePrivate(DSBase64.decode(keys[1]));
        return new DSKeys(new KeyPair(publicKey, privateKey));
    }

    /**
     * Decodes the X9.63 encoding of a public key.
     */
    public static ECPrivateKey decodePrivate(byte[] bytes) {
        try {
            BigInteger s = new BigInteger(bytes);
            ECPrivateKeySpec spec = new ECPrivateKeySpec(s, getParameters());
            KeyFactory fac = KeyFactory.getInstance("EC");
            return (ECPrivateKey) fac.generatePrivate(spec);
        } catch (Exception x) {
            DSException.throwRuntime(x);
        }
        return null;
    }

    /**
     * Decodes the X9.63 encoding of a public key.
     */
    public static ECPublicKey decodePublic(byte[] bytes) {
        try {
            if (bytes[0] != 0x04) {
                throw new IllegalArgumentException("Invalid public key");
            }
            BigInteger x = new BigInteger(Arrays.copyOfRange(bytes, 1, 33));
            BigInteger y = new BigInteger(Arrays.copyOfRange(bytes, 33, 65));
            ECPoint point = new ECPoint(x, y);
            ECPublicKeySpec spec = new ECPublicKeySpec(point, getParameters());
            KeyFactory fac = KeyFactory.getInstance("EC");
            return (ECPublicKey) fac.generatePublic(spec);
        } catch (Exception x) {
            DSException.throwRuntime(x);
        }
        return null;
    }

    public static byte[] generateHmacSHA256Signature(byte[] data, byte[] secretKeyBytes) {
        byte[] ret = null;
        try {
            SecretKeySpec secretKey = new SecretKeySpec(secretKeyBytes, "HmacSHA256");
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(secretKey);
            ret = mac.doFinal(data);
        } catch (Exception x) {
            DSException.throwRuntime(x);
        }
        return ret;
    }

    /**
     * Uses the given public key to generate an ECDH shared secret.
     *
     * @param publicKeyBytes Public key of the other party.
     */
    public byte[] generateSharedSecret(byte[] publicKeyBytes) {
        try {
            ECPublicKey pubKey = decodePublic(publicKeyBytes);
            KeyAgreement keyAgreement = KeyAgreement.getInstance("ECDH");
            keyAgreement.init(getPrivateKey());
            keyAgreement.doPhase(pubKey, true);
            return keyAgreement.generateSecret();
        } catch (Exception x) {
            DSException.throwRuntime(x);
        }
        return null;
    }

    /**
     * Uses the given public key to generate an ECDH shared secret.
     *
     * @param base64key Base64 encoded public key of the other party.
     */
    public byte[] generateSharedSecret(String base64key) {
        return generateSharedSecret(DSBase64.decode(base64key));
    }

    /**
     * Encodes the key pair which can be then decoded with decodeKeys().
     */
    public String encodeKeys() {
        StringBuilder builder = new StringBuilder();
        builder.append(DSBase64.encodeUrl(encodePublic()));
        builder.append(" ");
        builder.append(DSBase64.encodeUrl(toUnsignedByteArray(getPrivateKey().getS())));
        return builder.toString();
    }

    /**
     * Base64 encoding (no padding and url safe) of the SHA256 hash of the public key. This is used
     * to generate the DSID of a link.
     *
     * @throws DSException wrapping any security related exceptions.
     */
    public String encodePublicHashDsId() {
        String ret = null;
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            ret = DSBase64.encodeUrl(md.digest(encodePublic()));
        } catch (Exception x) {
            DSException.throwRuntime(x);
        }
        return ret;
    }

    /**
     * X9.63 encoding of the public key.
     */
    public byte[] encodePublic() {
        ECPublicKey publicKey = getPublicKey();
        byte[] x = toUnsignedByteArray(publicKey.getW().getAffineX());
        byte[] y = toUnsignedByteArray(publicKey.getW().getAffineY());
        byte[] ret = new byte[x.length + y.length + 1]; //32 + 32 + 1
        ret[0] = 0x04;
        System.arraycopy(x, 0, ret, 1, x.length);
        System.arraycopy(y, 0, ret, x.length + 1, y.length);
        return ret;
    }

    /**
     * The AlgorithmParameterSpec for the predefined elliptic curve secp256r1.
     */
    public static ECParameterSpec getParameters() {
        try {
            AlgorithmParameters params = AlgorithmParameters.getInstance("EC");
            params.init(new ECGenParameterSpec("secp256r1"));
            return params.getParameterSpec(ECParameterSpec.class);
        } catch (Exception x) {
            DSException.throwRuntime(x);
        }
        return null;
    }

    /**
     * The public and private keys.
     */
    public KeyPair getKeys() {
        return keyPair;
    }

    /**
     * A convenience that casts the private key.
     */
    public ECPrivateKey getPrivateKey() {
        return (ECPrivateKey) keyPair.getPrivate();
    }

    /**
     * A convenience that casts the public key.
     */
    public ECPublicKey getPublicKey() {
        return (ECPublicKey) keyPair.getPublic();
    }

    /**
     * Creates a key pair for the predefined elliptic curve secp256r1.
     *
     * @throws DSException Wrapping underlying security exceptions.
     */
    public static KeyPair newKeyPair() {
        try {
            KeyPairGenerator gen = KeyPairGenerator.getInstance("EC");
            gen.initialize(new ECGenParameterSpec("secp256r1"));
            return gen.generateKeyPair();
        } catch (Exception x) {
            DSException.throwRuntime(x);
        }
        return null;
    }

    /**
     * Returns a SHA256withECDSA signature.
     *
     * @throws DSException Wrapping underlying security exceptions.
     */
    public static Signature newSignature() {
        Signature ret = null;
        try {
            ret = Signature.getInstance("SHA256withECDSA");
        } catch (Exception x) {
            DSException.throwRuntime(x);
        }
        return ret;
    }

    /**
     * Creates a signer for this private key.
     */
    public Signer newSigner() {
        return new Signer(keyPair.getPrivate());
    }

    /**
     * Creates a verifier for this public key.
     */
    public Verifier newVerifier() {
        return new Verifier(keyPair.getPublic());
    }

    /**
     * A convenience that creates a signer and signs the given bytes.
     *
     * @return DSBase64 encoding of the signature.
     */
    public String sign(byte[] buf, int off, int len) {
        Signer signer = newSigner();
        signer.update(buf, off, len);
        return signer.getSignatureBase64();
    }

    /**
     * Decodes a key pair that was encoded by the store method.
     *
     * @param file File containing the serialized keys.
     * @throws DSException Wrapping underlying IOExceptions.
     */
    public static DSKeys restore(File file) {
        DSKeys ret = null;
        FileInputStream in = null;
        try {
            in = new FileInputStream(file);
            ret = restore(in);
        } catch (Exception x) {
            DSException.throwRuntime(x);
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (Exception ignore) {
            }
        }
        return ret;
    }

    /**
     * Decodes a key pair that was encoded by the store method, does not onClose the given stream.
     *
     * @param in Stream containing the serialized keys, will not be closed.
     * @throws DSException Wrapping underlying IOExceptions.
     */
    public static DSKeys restore(InputStream in) {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            byte[] buf = new byte[512];
            int len = in.read(buf);
            while (len > 0) {
                out.write(buf, 0, len);
                len = in.read(buf);
            }
            out.close();
            String encoded = new String(out.toByteArray(), "UTF-8");
            return decodeKeys(encoded);
        } catch (Exception x) {
            DSException.throwRuntime(x);
        }
        return null;
    }

    /**
     * Write the bytes from the string encoding to the given file.
     *
     * @param file Will be created or overwritten.
     * @throws DSException Wrapping underlying IOExceptions.
     */
    public void store(File file) {
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(file);
            store(out);
        } catch (Exception x) {
            DSException.throwRuntime(x);
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (Exception ignore) {
            }
        }
    }

    /**
     * Writes the bytes from the string encoding to the given stream, does not onClose the stream.
     *
     * @param out Where write the serialized keys, will not be closed.
     * @throws DSException Wrapping underlying IOExceptions.
     */
    public void store(OutputStream out) {
        try {
            byte[] bytes = encodeKeys().getBytes("UTF-8");
            out.write(bytes);
            out.flush();
        } catch (Exception x) {
            DSException.throwRuntime(x);
        }
    }

    /**
     * Will drop the first byte if it is zero and the length of the encoding is 33 bytes.
     */
    private static byte[] toUnsignedByteArray(BigInteger arg) {
        byte[] bytes = arg.toByteArray();
        if ((bytes[0] == 0) && (bytes.length == 33)) {
            byte[] tmp = new byte[32];
            System.arraycopy(bytes, 1, tmp, 0, 32);
            bytes = tmp;
        }
        return bytes;
    }

    /**
     * A convenience that creates a verifier and validates the signature for the given bytes.
     */
    public boolean verify(byte[] buf, int off, int len, String signature) {
        Verifier verifier = newVerifier();
        verifier.update(buf, off, len);
        return verifier.validate(signature);
    }

    ///////////////////////////////////////////////////////////////////////////
    // Inner Classes
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Signs bytes.  Call the update one or more times.  Call getSignature when finished.  This
     * object is not thread safe, but can be reused by calling reset to being a new signature.
     */
    public class Signer {

        private PrivateKey privateKey;
        private Signature signature;

        public Signer(PrivateKey privateKey) {
            this.privateKey = privateKey;
            signature = newSignature();
            reset();
        }

        /**
         * The signature for all bytes passed to the update method.
         */
        public byte[] getSignature() {
            byte[] ret = null;
            try {
                ret = signature.sign();
            } catch (Exception x) {
                DSException.throwRuntime(x);
            }
            return ret;
        }

        /**
         * The base 64 encoding of the signature for all bytes passed to the update method.
         */
        public String getSignatureBase64() {
            return DSBase64.encodeUrl(getSignature());
        }

        /**
         * Call to begin a new signature.
         */
        public Signer reset() {
            try {
                signature.initSign(getKeys().getPrivate());
            } catch (Exception x) {
                DSException.throwRuntime(x);
            }
            return this;
        }

        /**
         * Update the signature with the given bytes.
         *
         * @param buf The source of the bytes.
         */
        public Signer update(byte[] buf) {
            return update(buf, 0, buf.length);
        }

        /**
         * Update the signature with the given bytes.
         *
         * @param buf The source of the bytes.
         * @param off The offset of the first byte.
         * @param len The number of bytes to sign.
         */
        public Signer update(byte[] buf, int off, int len) {
            try {
                signature.update(buf, off, len);
            } catch (Exception x) {
                DSException.throwRuntime(x);
            }
            return this;
        }

    }

    /**
     * Verifies signatures.  Call update one or more times with the bytes of the message. Call
     * verify with a signature for the message to validate. This object is not thread safe, but can
     * be reused by calling reset to begin a new signature.
     */
    public static class Verifier {

        public Verifier(PublicKey publicKey) {
            this.publicKey = publicKey;
            this.signature = newSignature();
            reset();
        }

        /**
         * Call to begin a new signature.
         */
        public Verifier reset() {
            try {
                signature.initVerify(publicKey);
            } catch (Exception x) {
                DSException.throwRuntime(x);
            }
            return this;
        }

        /**
         * Update the signature with the given bytes.
         *
         * @param buf The source of the bytes.
         */
        public Verifier update(byte[] buf) {
            return update(buf, 0, buf.length);
        }

        /**
         * Update the signature with the given bytes.
         *
         * @param buf The source of the bytes.
         * @param off The offset of the first byte.
         * @param len The number of bytes to sign.
         */
        public Verifier update(byte[] buf, int off, int len) {
            try {
                signature.update(buf, off, len);
            } catch (Exception x) {
                DSException.throwRuntime(x);
            }
            return this;
        }

        /**
         * Returns true if the given signature is valid for the bytes passed to update.
         */
        public boolean validate(byte[] signature) {
            boolean ret = false;
            try {
                ret = this.signature.verify(signature);
            } catch (Exception x) {
                DSException.throwRuntime(x);
            }
            return ret;
        }

        /**
         * Returns true if the given base 64 encoded signature is valid for the bytes passed to
         * update.
         */
        public boolean validate(String base64signature) {
            return validate(DSBase64.decode(base64signature));
        }

        private PublicKey publicKey;
        private Signature signature;
    }

    ///////////////////////////////////////////////////////////////////////////
    // Initialization
    ///////////////////////////////////////////////////////////////////////////

} //class
