package org.iot.dsa.dslink;

import java.security.KeyPair;
import java.security.MessageDigest;
import java.security.Signature;
import org.iot.dsa.io.DSBase64;
import org.iot.dsa.security.DSKeys;
import org.iot.dsa.util.DSException;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author Aaron Hansen
 */
public class DSKeysTest {

    ///////////////////////////////////////////////////////////////////////////
    // Constants
    ///////////////////////////////////////////////////////////////////////////

    private static byte[] TEST_MSG;

    ///////////////////////////////////////////////////////////////////////////
    // Fields
    ///////////////////////////////////////////////////////////////////////////

    ///////////////////////////////////////////////////////////////////////////
    // Constructors
    ///////////////////////////////////////////////////////////////////////////

    ///////////////////////////////////////////////////////////////////////////
    // Methods
    ///////////////////////////////////////////////////////////////////////////

    @Test
    public void documentedTestCase() throws Exception {
        String keyFile =
                "BEACGownMzthVjNFT7Ry-RPX395kPSoUqhQ_H_vz0dZzs5RYoVJKA16XZhdYd__ksJP0DOlwQXAvoDjSMWAhkg4"
                        + " " + "M6S41GAL0gH0I97Hhy7A2-icf8dHnxXPmYIRwem03HE";
        DSKeys keys = DSKeys.decodeKeys(keyFile);
        String test = keys.encodeKeys();
        DSKeys keys2 = DSKeys.decodeKeys(test);
        Assert.assertTrue(keys.getPublicKey().equals(keys2.getPublicKey()));
        Assert.assertTrue(keys.getPrivateKey().equals(keys2.getPrivateKey()));
        String tempKey =
                "BCVrEhPXmozrKAextseekQauwrRz3lz2sj56td9j09Oajar0RoVR5Uo95AVuuws1vVEbDzhOUu7freU0BXD759U";
        byte[] salt = "0000".getBytes("UTF-8");
        byte[] secret = keys.generateSharedSecret(tempKey);
        byte[] bytes = new byte[salt.length + secret.length];
        System.arraycopy(salt, 0, bytes, 0, salt.length);
        System.arraycopy(secret, 0, bytes, salt.length, secret.length);
        MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
        messageDigest.update(bytes);
        bytes = messageDigest.digest();
        String auth = DSBase64.encodeUrl(bytes);
        Assert.assertTrue(auth.equals("V2P1nwhoENIi7SqkNBuRFcoc8daWd_iWYYDh_0Z01rs"));
    }

    @Test
    public void testAll() throws Exception {
        DSKeys keys = new DSKeys(DSKeys.newKeyPair());
        String signature = keys.sign(TEST_MSG, 0, TEST_MSG.length);
        Assert.assertTrue(keys.verify(TEST_MSG, 0, TEST_MSG.length, signature));
        String encoded = keys.encodeKeys();
        keys = DSKeys.decodeKeys(encoded);
        Assert.assertTrue(keys.verify(TEST_MSG, 0, TEST_MSG.length, signature));
    }

    @Test
    public void testPublicHash() {
        DSKeys keys = new DSKeys(DSKeys.newKeyPair());
        Assert.assertTrue(keys.encodePublicHashDsId().length() == 43);
    }

    @Test
    public void testSerialization() throws Exception {
        DSKeys first = new DSKeys(DSKeys.newKeyPair());
        String encoded = first.encodeKeys();
        DSKeys second = DSKeys.decodeKeys(encoded);
        Assert.assertEquals(first.getPublicKey(), second.getPublicKey());
        Assert.assertEquals(first.getPrivateKey(), second.getPrivateKey());
        first = new DSKeys(DSKeys.newKeyPair());
        Assert.assertNotEquals(first.getPublicKey(), second.getPublicKey());
        Assert.assertNotEquals(first.getPrivateKey(), second.getPrivateKey());
    }

    @Test
    public void testSignerAndVerifier() throws Exception {
        DSKeys keys = new DSKeys(DSKeys.newKeyPair());
        DSKeys.Signer signer = keys.newSigner();
        String signature = signer.update(TEST_MSG).getSignatureBase64();
        DSKeys.Verifier verifier = keys.newVerifier();
        Assert.assertTrue(verifier.update(TEST_MSG).validate(signature));
        //Now test that reset works on each.
        signer.reset();
        signature = signer.update(TEST_MSG).getSignatureBase64();
        verifier.reset();
        Assert.assertTrue(verifier.update(TEST_MSG).validate(signature));
    }

    @Test
    public void testVerification() throws Exception {
        KeyPair keyPair = DSKeys.newKeyPair();
        Signature signer = DSKeys.newSignature();
        signer.initSign(keyPair.getPrivate());
        signer.update(TEST_MSG);
        byte[] signature = signer.sign();
        signer.initVerify(keyPair.getPublic());
        signer.update(TEST_MSG);
        Assert.assertTrue(signer.verify(signature));
    }

    ///////////////////////////////////////////////////////////////////////////
    // Inner Classes
    ///////////////////////////////////////////////////////////////////////////

    ///////////////////////////////////////////////////////////////////////////
    // Initialization
    ///////////////////////////////////////////////////////////////////////////

    static {
        try {
            TEST_MSG = "The quick brown fox jumps over the lazy dog.".getBytes("UTF-8");
        } catch (Exception x) {
            DSException.throwRuntime(x);
        }
    }

}
