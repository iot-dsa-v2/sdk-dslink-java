package org.iot.dsa.dslink;

import org.iot.dsa.node.DSBool;
import org.iot.dsa.node.DSElement;
import org.iot.dsa.node.DSInfo;
import org.iot.dsa.node.DSInt;
import org.iot.dsa.node.DSNode;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author Aaron Hansen
 */
public class NodeTest {

    // Constants
    // ---------

    // Fields
    // ------

    // Constructors
    // ------------

    // Methods
    // -------

    @Test
    public void testOnChildChanged() throws Exception {
        DSNode root = new DSNode();
        MyNode child = new MyNode();
        root.add("child", child);
        root.start();
        Assert.assertTrue(child.isRunning());
        Assert.assertFalse(child.isStopped());
        Assert.assertTrue(child.lastChildChange == 0);
        long time = System.currentTimeMillis();
        child.setSecond(false);
        Assert.assertTrue(child.lastChildChange >= time);
        root.remove("child");
        Assert.assertFalse(child.isRunning());
        Assert.assertTrue(child.isStopped());
        Thread.sleep(10);
        time = System.currentTimeMillis();
        child.setSecond(true);
        Assert.assertFalse(child.lastChildChange >= time);
    }

    @Test
    public void theTest() {
        testMine(new MyNode());
        testMine(new MyNode()); //on purpose, making sure defaults hold
    }

    private void testMine(MyNode mine) {
        Assert.assertTrue(mine.get("first") != null);
        Assert.assertTrue(mine.get("second") != null);
        Assert.assertTrue(mine.get("first") != mine.get("second"));
        Assert.assertTrue(mine.get("first") == mine.getFirst());
        Assert.assertTrue(mine.get("second") == mine.getLast());
        Assert.assertTrue(mine.get("first") == DSInt.valueOf(1));
        mine.put("first", DSInt.valueOf(2));
        Assert.assertTrue(mine.get("first") == DSInt.valueOf(2));
        mine.remove("first");
        Assert.assertTrue(mine.get("first") == null);
        Assert.assertTrue(mine.childCount() == 1);
        try {
            mine.remove("second");
        } catch (Exception x) {
        }
        Assert.assertTrue(mine.childCount() == 1);
        mine.put("first", DSInt.valueOf(1));
        mine.put("first", DSInt.valueOf(2));
        Assert.assertTrue(mine.get("first").hashCode() == 2);
        mine.setSecond(true);
        Assert.assertTrue(mine.isSecond());
    }

    // Inner Classes
    // -------------

    public static class MyNode extends DSNode {

        long lastChildChange;
        private DSInfo<?> second = getInfo("second");

        public boolean isSecond() {
            return ((DSElement) second.get()).toBoolean();
        }

        public void setSecond(boolean arg) {
            put("second", DSBool.valueOf(arg));
        }

        @Override
        protected void declareDefaults() {
            add("first", DSInt.valueOf(1));
            declareDefault("second", DSBool.valueOf(true));
        }

        protected void onChildChanged(DSInfo<?> child) {
            lastChildChange = System.currentTimeMillis();
        }

    }


}
