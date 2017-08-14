package org.iot.dsa.dslink;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Arrays;
import javax.xml.bind.DatatypeConverter;
import org.iot.dsa.io.DSByteBuffer;
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
    // Fields
    ///////////////////////////////////////////////////////////////////////////

    ///////////////////////////////////////////////////////////////////////////
    // Constructors
    ///////////////////////////////////////////////////////////////////////////

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
        testF3(); //link->broker
    }

    public void testF0() throws Exception {
        DSKeys dsKeys = getLinkKeys();
        //dsId
        StringBuilder sbuf = new StringBuilder();
        sbuf.append("mylink-").append(dsKeys.encodePublicHashDsId());
        String dsId = sbuf.toString();
        Assert.assertTrue(dsId.equals("mylink-TTDXtL-U_NQ2sgFRU5w0HrZVib2D-O4CxXQrKk4hUsI"));
        byte[] dsIdBytes = dsId.getBytes("UTF-8");
        //client salt
        String saltHex = "c4ca4238a0b923820dcc509a6f75849bc81e728d9d4c2f636f067f89cc14862c";
        byte[] saltBytes = toBytesFromHex(saltHex);
        Assert.assertTrue(saltBytes.length == 32);
        //generate f0 message
        DSByteBuffer buf = new DSByteBuffer().open();
        buf.put(new byte[]{0, 0, 0, 0});
        buf.put(new byte[]{15, 0});
        buf.put((byte) 0xf0);
        buf.put(new byte[]{0, 0, 0, 0});
        buf.put(new byte[]{0, 0, 0, 0});
        //end of header
        buf.put(new byte[]{2, 0});
        byte[] bytes = new byte[4];
        writeShort(bytes, 0, dsIdBytes.length);
        buf.put(bytes, 0, 2);
        buf.put(dsIdBytes);
        buf.put(dsKeys.encodePublic());
        buf.put((byte) 0);
        buf.put(saltBytes);
        writeInt(bytes, 0, buf.length());
        buf.put(0, bytes, 0, 4);
        //the final message byte array
        bytes = buf.toByteArray();
        //what to test against
        String correctResult = "a70000000f00f00000000000000000020032006d796c696e6b2d54544458744c2d555f4e5132736746525535773048725a56696232442d4f3443785851724b6b34685573490415caf59c92efecb9253ea43912b419941fdb59a23d5d1289027128bf3d6ee4cb86fbe251b675a8d9bd991a65caa1bb23f8a8e0dd4eb0974f6b1eaa3436cec0e900c4ca4238a0b923820dcc509a6f75849bc81e728d9d4c2f636f067f89cc14862c"
                .toUpperCase();
        byte[] correctBytes = toBytesFromHex(correctResult);
        Assert.assertTrue(Arrays.equals(bytes, correctBytes));
    }

    public void testF1() throws Exception {
        DSKeys dsKeys = getBrokerKeys();
        //broker dsId
        StringBuilder sbuf = new StringBuilder();
        sbuf.append("broker-").append(dsKeys.encodePublicHashDsId());
        String dsId = sbuf.toString();
        Assert.assertTrue(dsId.equals("broker-g675gaSQogzMxjJFvL7HsCbyS8B0Ly2_Abhkw_-g4iI"));
        byte[] dsIdBytes = dsId.getBytes("UTF-8");
        //broker salt
        String saltHex = "eccbc87e4b5ce2fe28308fd9f2a7baf3a87ff679a2f3e71d9181a67b7542122c";
        byte[] saltBytes = toBytesFromHex(saltHex);
        Assert.assertTrue(saltBytes.length == 32);
        //generate f0 message
        DSByteBuffer buf = new DSByteBuffer().open();
        buf.put(new byte[]{0, 0, 0, 0});
        buf.put(new byte[]{15, 0});
        buf.put((byte) 0xf1);
        buf.put(new byte[]{0, 0, 0, 0});
        buf.put(new byte[]{0, 0, 0, 0});
        //end of header
        byte[] bytes = new byte[4];
        writeShort(bytes, 0, dsIdBytes.length);
        buf.put(bytes, 0, 2);
        buf.put(dsIdBytes);
        buf.put(dsKeys.encodePublic());
        buf.put(saltBytes);
        writeInt(bytes, 0, buf.length());
        buf.put(0, bytes, 0, 4);
        //the final message byte array
        bytes = buf.toByteArray();
        //what to test against
        String correctResult = "a40000000f00f10000000000000000320062726f6b65722d67363735676153516f677a4d786a4a46764c374873436279533842304c79325f4162686b775f2d6734694904f9e64edcec5ea0a645bd034e46ff209dd9fb21d8aba74a5531dc6dcbea28d696c6c9386d924ebc2f48092a1d6c8b2ca907005cca7e8d2a58783b8a765d8eb29deccbc87e4b5ce2fe28308fd9f2a7baf3a87ff679a2f3e71d9181a67b7542122c"
                .toUpperCase();
        byte[] correctBytes = toBytesFromHex(correctResult);
        Assert.assertTrue(Arrays.equals(bytes, correctBytes));
    }

    public void testF2() throws Exception {
        DSKeys brokerKeys = getBrokerKeys();
        byte[] pubKeyBytes = brokerKeys.encodePublic();
        Assert.assertTrue(pubKeyBytes.length == 65);
        DSKeys dsKeys = getLinkKeys();
        byte[] sharedSecret = dsKeys.generateSharedSecret(pubKeyBytes);
        //System.out.println("Mine: " + toHexFromBytes(sharedSecret));
        byte[] validSharedSecret = toBytesFromHex(
                "5f67b2cb3a0906afdcf5175ed9316762a8e18ce26053e8c51b760c489343d0d1");
        Assert.assertTrue(Arrays.equals(validSharedSecret, sharedSecret));
        String saltHex = "eccbc87e4b5ce2fe28308fd9f2a7baf3a87ff679a2f3e71d9181a67b7542122c";
        byte[] salt = toBytesFromHex(saltHex);
        byte[] bytes = DSKeys.generateHmacSHA256Signature(salt, validSharedSecret);
        String validAuthString = "f58c10e212a82bf327a020679c424fc63e852633a53253119df74114fac8b2ba";
        byte[] validAuthBytes = toBytesFromHex(validAuthString);
        Assert.assertTrue(Arrays.equals(bytes, validAuthBytes));
        DSByteBuffer buf = new DSByteBuffer().open();
        buf.put(new byte[]{0, 0, 0, 0});
        buf.put(new byte[]{15, 0});
        buf.put((byte) 0xf2);
        buf.put(new byte[]{0, 0, 0, 0});
        buf.put(new byte[]{0, 0, 0, 0});
        //end of header
        String clientToken = "sample_token_string";
        byte[] tokenBytes = clientToken.getBytes("UTF-8");
        byte[] lenBytes = new byte[4];
        writeShort(lenBytes, 0, tokenBytes.length);
        buf.put(lenBytes, 0, 2);
        buf.put(tokenBytes);
        buf.put((byte) 1);
        buf.put((byte) 1);
        buf.put((byte) 0);
        buf.put(bytes);
        writeInt(lenBytes, 0, buf.length());
        buf.put(0, lenBytes, 0, 4);
        bytes = buf.toByteArray();
        //what to test against
        String correctResult = "470000000f00f20000000000000000130073616d706c655f746f6b656e5f737472696e67010100f58c10e212a82bf327a020679c424fc63e852633a53253119df74114fac8b2ba"
                .toUpperCase();
        byte[] correctBytes = toBytesFromHex(correctResult);
        Assert.assertTrue(Arrays.equals(bytes, correctBytes));
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
        byte[] bytes = DSKeys.generateHmacSHA256Signature(salt, validSharedSecret);
        String validAuthString = "e709059f1ebb84cfb8c34d53fdba7fbf20b1fe3dff8c343050d2b5c7c62be85a";
        byte[] validAuthBytes = toBytesFromHex(validAuthString);
        Assert.assertTrue(Arrays.equals(bytes, validAuthBytes));
        DSByteBuffer buf = new DSByteBuffer().open();
        buf.put(new byte[]{0, 0, 0, 0});
        buf.put(new byte[]{15, 0});
        buf.put((byte) 0xf3);
        buf.put(new byte[]{0, 0, 0, 0});
        buf.put(new byte[]{0, 0, 0, 0});
        //end of header
        byte[] tmp = new byte[4];
        String clientSessionId = "sampe-session-001";
        writeShort(tmp, 0, clientSessionId.length());
        buf.put(tmp, 0, 2);
        buf.put(clientSessionId.getBytes("UTF-8"));

        String clientPath = "/downstream/mlink1";
        writeShort(tmp, 0, clientPath.length());
        buf.put(tmp, 0, 2);
        buf.put(clientPath.getBytes("UTF-8"));
        buf.put(bytes);
        writeInt(tmp, 0, buf.length());
        buf.put(0, tmp, 0, 4);
        tmp = buf.toByteArray();
        //what to test against
        String correctResult = "560000000f00f30000000000000000110073616d70652d73657373696f6e2d30303112002f646f776e73747265616d2f6d6c696e6b31e709059f1ebb84cfb8c34d53fdba7fbf20b1fe3dff8c343050d2b5c7c62be85a"
                .toUpperCase();
        byte[] correctBytes = toBytesFromHex(correctResult);
        System.out.println("Mine: " + toHexFromBytes(tmp));
        System.out.println("His:  " + toHexFromBytes(correctBytes));
        Assert.assertTrue(Arrays.equals(tmp, correctBytes));
    }

    /**
     * Read a 32-bit little endian integer value from the stream.
     */
    protected static int readInt(InputStream in) throws IOException {
        return (in.read() << 0) +
                (in.read() << 8) +
                (in.read() << 16) +
                (in.read() << 24);
    }

    /**
     * Read a 32-bit little endian integer value from the array.
     */
    protected static int readInt(byte[] buf, int off) {
        return (buf[off] << 0) +
                (buf[++off] << 8) +
                (buf[++off] << 16) +
                (buf[++off] << 24);
    }

    /**
     * Read a 16-bit little endian short value from the array.
     */
    protected static int readShort(byte[] buf, int off) {
        return (buf[off] << 0) +
                (buf[++off] << 8);
    }

    /**
     * Read a 32-bit little endian integer value from the stream.
     */
    protected static int readShort(InputStream in) throws IOException {
        return (in.read() << 0) +
                (in.read() << 8);
    }

    /**
     * Write a 32 bit little endian integer to the internal buffer.
     */
    protected static void writeInt(byte[] out, int off, int v) {
        out[off] = (byte) ((v >>> 0) & 0xFF);
        out[++off] = (byte) ((v >>> 8) & 0xFF);
        out[++off] = (byte) ((v >>> 16) & 0xFF);
        out[++off] = (byte) ((v >>> 24) & 0xFF);
    }

    /**
     * Write a 32 bit little endian integer to the internal buffer.
     */
    protected static void writeInt(OutputStream out, int v) throws IOException {
        out.write((v >>> 0) & 0xFF);
        out.write((v >>> 8) & 0xFF);
        out.write((v >>> 16) & 0xFF);
        out.write((v >>> 24) & 0xFF);
    }

    /**
     * Write a 16 bit little endian integer to the internal buffer.
     */
    protected static void writeShort(byte[] out, int off, int v) {
        out[off] = (byte) ((v >>> 0) & 0xFF);
        out[++off] = (byte) ((v >>> 8) & 0xFF);
    }

    /**
     * Write a 16 bit little endian integer to the internal buffer.
     */
    protected static void writeShort(OutputStream out, int v) throws IOException {
        out.write((v >>> 0) & 0xFF);
        out.write((v >>> 8) & 0xFF);
    }

    public static byte[] toBytesFromHex(String s) {
        return DatatypeConverter.parseHexBinary(s);
    }

    public static String toHexFromBytes(byte[] bytes) {
        return DatatypeConverter.printHexBinary(bytes);
    }

    ///////////////////////////////////////////////////////////////////////////
    // Inner Classes
    ///////////////////////////////////////////////////////////////////////////

    ///////////////////////////////////////////////////////////////////////////
    // Initialization
    ///////////////////////////////////////////////////////////////////////////

}
