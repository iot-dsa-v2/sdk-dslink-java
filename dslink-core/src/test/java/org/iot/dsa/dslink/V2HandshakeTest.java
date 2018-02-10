package org.iot.dsa.dslink;

import com.acuity.iot.dsa.dslink.io.DSByteBuffer;
import com.acuity.iot.dsa.dslink.protocol.protocol_v2.DS2MessageReader;
import com.acuity.iot.dsa.dslink.protocol.protocol_v2.DS2MessageWriter;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Arrays;
import javax.xml.bind.DatatypeConverter;
import org.iot.dsa.node.DSBytes;
import org.iot.dsa.security.DSKeys;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author Aaron Hansen
 */
public class V2HandshakeTest {

    ///////////////////////////////////////////////////////////////////////////
    // Constants
    ///////////////////////////////////////////////////////////////////////////

    private final static char[] HEXCHARS = "0123456789abcdef".toCharArray();

    ///////////////////////////////////////////////////////////////////////////
    // Methods
    ///////////////////////////////////////////////////////////////////////////

    private DSKeys getBrokerKeys() {
        //broker private key
        String privKeyHex = "82848ef9d9204097a98a8c393e06aac9cb9a1ba3cdabf772f4ca7e6899b9f277";
        byte[] privKeyBytes = toBytesFromHex(privKeyHex);
        PrivateKey privKey = DSKeys.decodePrivate(privKeyBytes);
        //broker public key
        String pubKeyHex = "04f9e64edcec5ea0a645bd034e46ff209dd9fb21d8aba74a5531dc6dcbea28d696c6c9386d924ebc2f48092a1d6c8b2ca907005cca7e8d2a58783b8a765d8eb29d";
        byte[] pubKeyBytes = toBytesFromHex(pubKeyHex);
        Assert.assertTrue(pubKeyBytes.length == 65);
        PublicKey pubKey = DSKeys.decodePublic(pubKeyBytes);
        return new DSKeys(new KeyPair(pubKey, privKey));
    }

    private DSKeys getLinkKeys() {
        //link private key
        String privKeyHex = "55e1bcad391b655f97fe3ba2f8e3031c9b5828b16793b7da538c2787c3a4dc59";
        byte[] privKeyBytes = toBytesFromHex(privKeyHex);
        PrivateKey privKey = DSKeys.decodePrivate(privKeyBytes);
        //link public key
        String pubKeyHex = "0415caf59c92efecb9253ea43912b419941fdb59a23d5d1289027128bf3d6ee4cb86fbe251b675a8d9bd991a65caa1bb23f8a8e0dd4eb0974f6b1eaa3436cec0e9";
        byte[] pubKeyBytes = toBytesFromHex(pubKeyHex);
        Assert.assertTrue(pubKeyBytes.length == 65);
        PublicKey pubKey = DSKeys.decodePublic(pubKeyBytes);
        return new DSKeys(new KeyPair(pubKey, privKey));
    }

    @Test
    public void test() throws Exception {
        testF0(); //link->broker
        testF1(); //broker->link
        testF2(); //link->broker
        testF3(); //broker->link
    }

    public void testF0() throws Exception {
        DSKeys dsKeys = getLinkKeys();
        //dsId
        StringBuilder sbuf = new StringBuilder();
        sbuf.append("mylink-").append(dsKeys.encodePublicHashDsId());
        String dsId = sbuf.toString();
        Assert.assertTrue(dsId.equals("mylink-TTDXtL-U_NQ2sgFRU5w0HrZVib2D-O4CxXQrKk4hUsI"));
        //client salt
        String saltHex = "c4ca4238a0b923820dcc509a6f75849bc81e728d9d4c2f636f067f89cc14862c";
        byte[] saltBytes = toBytesFromHex(saltHex);
        Assert.assertTrue(saltBytes.length == 32);
        //construct the message
        DS2MessageWriter writer = new DS2MessageWriter();
        writer.setMethod((byte) 0xf0);
        DSByteBuffer buffer = writer.getBody();
        buffer.put((byte) 2).put((byte) 0); //dsa version
        writer.writeString(dsId, buffer);
        buffer.put(dsKeys.encodePublic());
        buffer.put(saltBytes);
        byte[] bytes = writer.toByteArray();
        //what to test against
        String correctResult = "9e0000000700f0020032006d796c696e6b2d54544458744c2d555f4e5132736746525535773048725a56696232442d4f3443785851724b6b34685573490415caf59c92efecb9253ea43912b419941fdb59a23d5d1289027128bf3d6ee4cb86fbe251b675a8d9bd991a65caa1bb23f8a8e0dd4eb0974f6b1eaa3436cec0e9c4ca4238a0b923820dcc509a6f75849bc81e728d9d4c2f636f067f89cc14862c"
                .toUpperCase();
        byte[] correctBytes = toBytesFromHex(correctResult);
        Assert.assertArrayEquals(bytes, correctBytes);
    }

    public void testF1() throws Exception {
        DSKeys dsKeys = getBrokerKeys();
        //broker dsId
        StringBuilder sbuf = new StringBuilder();
        sbuf.append("broker-").append(dsKeys.encodePublicHashDsId());
        String dsId = sbuf.toString();
        Assert.assertTrue(dsId.equals("broker-g675gaSQogzMxjJFvL7HsCbyS8B0Ly2_Abhkw_-g4iI"));
        //broker salt
        String saltHex = "eccbc87e4b5ce2fe28308fd9f2a7baf3a87ff679a2f3e71d9181a67b7542122c";
        byte[] saltBytes = toBytesFromHex(saltHex);
        Assert.assertTrue(saltBytes.length == 32);
        //construct the message
        DS2MessageWriter writer = new DS2MessageWriter();
        writer.setMethod((byte) 0xf1);
        DSByteBuffer buffer = writer.getBody();
        //dsa version
        writer.writeString(dsId, buffer);
        byte[] publicKey = dsKeys.encodePublic();
        buffer.put(publicKey);
        buffer.put(saltBytes);
        int bodyLength = buffer.length();
        byte[] bytes = writer.toByteArray();
        //what to test against
        String correctResult = "9c0000000700f1320062726f6b65722d67363735676153516f677a4d786a4a46764c374873436279533842304c79325f4162686b775f2d6734694904f9e64edcec5ea0a645bd034e46ff209dd9fb21d8aba74a5531dc6dcbea28d696c6c9386d924ebc2f48092a1d6c8b2ca907005cca7e8d2a58783b8a765d8eb29deccbc87e4b5ce2fe28308fd9f2a7baf3a87ff679a2f3e71d9181a67b7542122c"
                .toUpperCase();
        byte[] correctBytes = toBytesFromHex(correctResult);
        Assert.assertArrayEquals(bytes, correctBytes);
        //validate reading
        DS2MessageReader reader = new DS2MessageReader();
        reader.init(new ByteArrayInputStream(bytes));
        InputStream in = reader.getBody();
        Assert.assertEquals(0xf1, reader.getMethod());
        Assert.assertEquals(bodyLength, reader.getBodyLength());
        String tmp = reader.readString(in);
        Assert.assertEquals(dsId, tmp);
        bytes = new byte[publicKey.length];
        in.read(bytes);
        Assert.assertArrayEquals(bytes, publicKey);
        bytes = new byte[saltBytes.length];
        in.read(bytes);
        Assert.assertArrayEquals(bytes, saltBytes);
    }

    public void testF2() throws Exception {
        DSKeys brokerKeys = getBrokerKeys();
        byte[] pubKeyBytes = brokerKeys.encodePublic();
        Assert.assertTrue(pubKeyBytes.length == 65);
        DSKeys dsKeys = getLinkKeys();
        byte[] sharedSecret = dsKeys.generateSharedSecret(pubKeyBytes);
        byte[] validSharedSecret = toBytesFromHex(
                "5f67b2cb3a0906afdcf5175ed9316762a8e18ce26053e8c51b760c489343d0d1");
        Assert.assertTrue(Arrays.equals(validSharedSecret, sharedSecret));
        String saltHex = "eccbc87e4b5ce2fe28308fd9f2a7baf3a87ff679a2f3e71d9181a67b7542122c";
        byte[] salt = toBytesFromHex(saltHex);
        byte[] auth = DSKeys.generateHmacSHA256Signature(salt, validSharedSecret);
        String validAuthString = "f58c10e212a82bf327a020679c424fc63e852633a53253119df74114fac8b2ba";
        byte[] validAuthBytes = toBytesFromHex(validAuthString);
        Assert.assertTrue(Arrays.equals(auth, validAuthBytes));
        //construct the message
        DS2MessageWriter writer = new DS2MessageWriter();
        writer.setMethod((byte) 0xf2);
        DSByteBuffer buffer = writer.getBody();
        writer.writeString("sample_token_string", buffer);
        buffer.put((byte) 0x01); //isResponder
        writer.writeString("", buffer); //blank server path
        buffer.put(auth);
        byte[] tmp = writer.toByteArray();
        //what to test against
        String correctResult = "3f0000000700f2130073616d706c655f746f6b656e5f737472696e67010000f58c10e212a82bf327a020679c424fc63e852633a53253119df74114fac8b2ba"
                .toUpperCase();
        byte[] correctBytes = toBytesFromHex(correctResult);
        Assert.assertArrayEquals(tmp, correctBytes);
    }

    public void testF3() throws Exception {
        DSKeys clientKeys = getLinkKeys();
        byte[] pubKeyBytes = clientKeys.encodePublic();
        Assert.assertTrue(pubKeyBytes.length == 65);
        DSKeys dsKeys = getBrokerKeys();
        byte[] sharedSecret = dsKeys.generateSharedSecret(pubKeyBytes);
        byte[] validSharedSecret = toBytesFromHex(
                "5f67b2cb3a0906afdcf5175ed9316762a8e18ce26053e8c51b760c489343d0d1");
        Assert.assertTrue(Arrays.equals(validSharedSecret, sharedSecret));
        String saltHex = "c4ca4238a0b923820dcc509a6f75849bc81e728d9d4c2f636f067f89cc14862c";
        byte[] salt = toBytesFromHex(saltHex);
        byte[] auth = DSKeys.generateHmacSHA256Signature(salt, validSharedSecret);
        String validAuthString = "e709059f1ebb84cfb8c34d53fdba7fbf20b1fe3dff8c343050d2b5c7c62be85a";
        byte[] validAuthBytes = toBytesFromHex(validAuthString);
        Assert.assertTrue(Arrays.equals(auth, validAuthBytes));
        //construct the message
        DS2MessageWriter writer = new DS2MessageWriter();
        writer.setMethod((byte) 0xf3);
        DSByteBuffer buffer = writer.getBody();
        buffer.put((byte) 1); // allow requester
        writer.writeString("/downstream/mlink1", buffer);
        buffer.put(auth);
        int bodyLength = writer.getBodyLength();
        byte[] tmp = writer.toByteArray();
        //what to test against
        String correctResult = "3c0000000700f30112002f646f776e73747265616d2f6d6c696e6b31e709059f1ebb84cfb8c34d53fdba7fbf20b1fe3dff8c343050d2b5c7c62be85a"
                .toUpperCase();
        byte[] correctBytes = toBytesFromHex(correctResult);
        Assert.assertTrue(Arrays.equals(tmp, correctBytes));
        //validate reading
        DS2MessageReader reader = new DS2MessageReader();
        reader.init(new ByteArrayInputStream(tmp));
        InputStream in = reader.getBody();
        Assert.assertEquals(0xf3, reader.getMethod());
        Assert.assertEquals(bodyLength, reader.getBodyLength());
        Assert.assertEquals(in.read(), 1); //allow requester
        Assert.assertEquals("/downstream/mlink1", reader.readString(in));
        tmp = new byte[auth.length];
        in.read(tmp);
        Assert.assertArrayEquals(tmp, auth);
    }

    public static byte[] toBytesFromHex(String s) {
        return DatatypeConverter.parseHexBinary(s);
    }

    public static String toHexFromBytes(byte[] bytes) {
        return DatatypeConverter.printHexBinary(bytes);
    }

}
