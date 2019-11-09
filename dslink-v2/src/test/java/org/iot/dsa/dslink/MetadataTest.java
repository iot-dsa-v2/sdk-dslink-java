package org.iot.dsa.dslink;

import org.iot.dsa.io.NodeDecoder;
import org.iot.dsa.io.NodeEncoder;
import org.iot.dsa.node.DSFlexEnum;
import org.iot.dsa.node.DSInfo;
import org.iot.dsa.node.DSList;
import org.iot.dsa.node.DSLong;
import org.iot.dsa.node.DSMap;
import org.iot.dsa.node.DSMetadata;
import org.iot.dsa.node.DSNode;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author Aaron Hansen
 */
public class MetadataTest {

    // Fields
    // ------

    private static DSMetadata metadata = new DSMetadata();

    // Methods
    // -------

    @Test
    public void testNode() {
        DSNode node = new DSNode();
        DSInfo<?> info = node.put("enum",
                                  DSFlexEnum.valueOf("abc", DSList.valueOf("abc", "def")));
        DSMap meta = DSMetadata.getMetadata(info, null);
        metadata.setMap(meta);
        Assert.assertEquals(2, metadata.getEnumRange().size());
        Assert.assertTrue(metadata.getEnumRange().get(0).equals("abc"));
        Assert.assertTrue(metadata.getEnumRange().get(1).equals("def"));
        meta.clear();
        node = new MyNode();
        info = node.put("enum",
                        DSFlexEnum.valueOf("abc", DSList.valueOf("abc", "def")));
        DSMetadata.getMetadata(info, meta);
        Assert.assertEquals(3, metadata.getEnumRange().size());
        Assert.assertTrue(metadata.getEnumRange().get(0).equals("abc"));
        Assert.assertTrue(metadata.getEnumRange().get(1).equals("def"));
        Assert.assertTrue(metadata.getEnumRange().get(2).equals("ghi"));
    }

    @Test
    public void testProxy() {
        MyNode node = new MyNode();
        Assert.assertTrue(node.getInfo("foobar").getMetadata().getUnit().equals("ms"));
        node = (MyNode) NodeDecoder.decode(NodeEncoder.encode(node));
        Assert.assertTrue(node.getInfo("foobar").getMetadata().getUnit().equals("ms"));
        node.getInfo("foobar").getMetadata().setUnit("sec");
        Assert.assertTrue(node.getInfo("foobar").getMetadata().getUnit().equals("sec"));
        node = (MyNode) NodeDecoder.decode(NodeEncoder.encode(node));
        Assert.assertTrue(node.getInfo("foobar").getMetadata().getUnit().equals("sec"));
    }

    // Inner Classes
    // -------------

    public static class MyNode extends DSNode {

        @Override
        public void getMetadata(DSInfo<?> info, DSMap bucket) {
            DSList list = metadata.getEnumRange();
            list.add("ghi");
        }

        @Override
        protected void declareDefaults() {
            declareDefault("foobar", DSLong.valueOf(5)).getMetadata().setUnit("ms");
        }
    }


}
