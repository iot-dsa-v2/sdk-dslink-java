package org.iot.dsa.dslink;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import org.iot.dsa.io.NodeDecoder;
import org.iot.dsa.io.NodeEncoder;
import org.iot.dsa.io.json.JsonReader;
import org.iot.dsa.io.json.JsonWriter;
import org.iot.dsa.node.*;
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
        Assert.assertTrue(orig.isEqual(decoded));
        orig.put("int", DSInt.valueOf(2));
        decoded = decode(encode(orig));
        Assert.assertTrue(orig.isEqual(decoded));
        orig.add("real", DSFloat.valueOf(2.2f));
        decoded = decode(encode(orig));
        Assert.assertTrue(orig.isEqual(decoded));
        Assert.assertTrue(orig.get("real").equals(DSFloat.valueOf(2.2f)));
        orig.getInfo("int").setReadOnly(true);
        decoded = decode(encode(orig));
        Assert.assertTrue(orig.isEqual(decoded));
        DSNode tmp = new MyNode();
        tmp.add("foo", DSString.valueOf("bar"));
        orig.add("anotherNode", tmp);
        debug(orig);
        decoded = decode(encode(orig));
        Assert.assertTrue(orig.isEqual(decoded));
    }

    /**
     * Testing an "Already parented" bug that cropped up.
     */
    @Test
    public void testMultipleGroups() throws Exception {
        DSNode node = new MyNode();
        DSList list = new DSList();
        list.add("a").add("b").add("c");
        node.put("first", list);
        list = new DSList();
        list.add("d").add("e").add("f");
        node.put("second", list);
        node = decode(encode(node));
        list = (DSList) node.get("first");
        Assert.assertTrue("a".equals(list.getString(0)));
        Assert.assertTrue("b".equals(list.getString(1)));
        Assert.assertTrue("c".equals(list.getString(2)));
        list = (DSList) node.get("second");
        Assert.assertTrue("d".equals(list.getString(0)));
        Assert.assertTrue("e".equals(list.getString(1)));
        Assert.assertTrue("f".equals(list.getString(2)));
    }

    @Test
    public void differentTypeThanDefault() throws Exception {
        DSNode node = new MyNode();
        node.put("tmp", DSElement.make(10));
        node.put("string", DSLong.valueOf(6));
        node = decode(encode(node));
        Assert.assertEquals(node.get("string"), DSLong.valueOf(6));
        Assert.assertEquals(node.get("tmp"), DSElement.make(10));
    }

    // Inner Classes
    // -------------

    public static class MyNode extends DSNode {

        @Override
        public void declareDefaults() {
            declareDefault("int", DSInt.valueOf(1));
            declareDefault("node", new DSNode());
            declareDefault("string", DSString.EMPTY);
        }
    }
}
