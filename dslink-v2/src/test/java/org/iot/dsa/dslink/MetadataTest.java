package org.iot.dsa.dslink;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
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
public class MetadataTest {

    // Fields
    // ------

    private static DSMetadata metadata = new DSMetadata();

    // Methods
    // -------

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
        DSNode node = new DSNode();
        DSInfo info = node.put("enum", DSFlexEnum.valueOf("abc", DSList.valueOf("abc", "def")));
        DSMap meta = DSMetadata.getMetadata(info, null);
        metadata.setMap(meta);
        Assert.assertEquals(2, metadata.getEnumRange().size());
        Assert.assertTrue(metadata.getEnumRange().get(0).equals("abc"));
        Assert.assertTrue(metadata.getEnumRange().get(1).equals("def"));
        meta.clear();
        node = new MyNode();
        info = node.put("enum", DSFlexEnum.valueOf("abc", DSList.valueOf("abc", "def")));
        DSMetadata.getMetadata(info, meta);
        Assert.assertEquals(3, metadata.getEnumRange().size());
        Assert.assertTrue(metadata.getEnumRange().get(0).equals("abc"));
        Assert.assertTrue(metadata.getEnumRange().get(1).equals("def"));
        Assert.assertTrue(metadata.getEnumRange().get(2).equals("ghi"));
    }

    // Inner Classes
    // -------------

    public static class MyNode extends DSNode {

        @Override
        public void getMetadata(DSInfo info, DSMap bucket) {
            DSList list = metadata.getEnumRange();
            list.add("ghi");
        }
    }


}
