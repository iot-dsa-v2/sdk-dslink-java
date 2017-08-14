package org.iot.dsa.dslink;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import org.iot.dsa.io.NodeDecoder;
import org.iot.dsa.io.NodeEncoder;
import org.iot.dsa.io.json.JsonReader;
import org.iot.dsa.io.json.JsonWriter;
import org.iot.dsa.node.DSFloat;
import org.iot.dsa.node.DSInt;
import org.iot.dsa.node.DSNode;
import org.iot.dsa.node.DSString;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author Aaron Hansen
 */
public class SerializationTest {

    // Constants
    // ---------

    // Fields
    // ------

    // Constructors
    // ------------

    // Methods
    // -------

    private void debug(DSNode node) throws Exception {
        File file = new File("test.json");
        FileOutputStream fos = new FileOutputStream(file);
        fos.write(encode(node));
        fos.close();
    }

    private DSNode decode(byte[] bytes) throws Exception {
        ByteArrayInputStream bin = new ByteArrayInputStream(bytes);
        JsonReader reader = new JsonReader(bin, "UTF-8");
        DSNode ret = NodeDecoder.decode(reader);
        reader.close();
        return ret;
    }

    private byte[] encode(DSNode node) throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        JsonWriter writer = new JsonWriter(bos);
        NodeEncoder.encode(writer, node);
        writer.close();
        return bos.toByteArray();
    }

    @Test
    public void theTest() throws Exception {
        DSNode orig = new MyNode();
        DSNode decoded = decode(encode(orig));
        Assert.assertTrue(orig.equals(decoded));
        orig.put("int", DSInt.valueOf(2));
        decoded = decode(encode(orig));
        Assert.assertTrue(orig.equals(decoded));
        orig.add("real", DSFloat.valueOf(2.2f));
        decoded = decode(encode(orig));
        Assert.assertTrue(orig.equals(decoded));
        Assert.assertTrue(orig.get("real").equals(DSFloat.valueOf(2.2f)));
        orig.getInfo("int").setReadOnly(true);
        decoded = decode(encode(orig));
        Assert.assertTrue(orig.equals(decoded));
        DSNode tmp = new MyNode();
        tmp.add("foo", DSString.valueOf("bar"));
        orig.add("anotherNode", tmp);
        debug(orig);
        decoded = decode(encode(orig));
        Assert.assertTrue(orig.equals(decoded));
    }

    // Inner Classes
    // -------------

    public static class MyNode extends DSNode {

        @Override
        public void declareDefaults() {
            declareDefault("int", DSInt.valueOf(1));
            declareDefault("node", new DSNode());
        }
    }
}
