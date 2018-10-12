package org.iot.dsa.dslink;

import org.iot.dsa.node.DSBool;
import org.iot.dsa.node.DSDouble;
import org.iot.dsa.node.DSStatus;
import org.iot.dsa.rollup.DSRollup;
import org.iot.dsa.rollup.RollupFunction;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Test that good status wins
 *
 * @author Aaron Hansen
 */
public class RollupTest {

    @Test
    public void testAnd() {
        Assert.assertEquals(DSRollup.valueOf("AND"), DSRollup.AND);
        Assert.assertEquals(DSRollup.valueFor("AND"), DSRollup.AND);
        Assert.assertEquals(DSRollup.valueFor("and"), DSRollup.AND);
        Assert.assertEquals(DSRollup.valueFor("And"), DSRollup.AND);
        DSRollup roll = DSRollup.valueFor("and");
        RollupFunction func = roll.getFunction();
        Assert.assertTrue(func.getCount() == 0);
        func.update(DSBool.TRUE, DSStatus.ok);
        Assert.assertTrue(func.getValue().toBoolean());
        func.update(DSBool.TRUE, DSStatus.ok);
        Assert.assertTrue(func.getValue().toBoolean());
        func.reset();
        func.update(DSBool.TRUE, DSStatus.ok);
        func.update(DSBool.FALSE, DSStatus.ok);
        Assert.assertFalse(func.getValue().toBoolean());
        func.reset();
        func.update(DSBool.FALSE, DSStatus.ok);
        Assert.assertFalse(func.getValue().toBoolean());
        func.update(DSBool.FALSE, DSStatus.ok);
        Assert.assertFalse(func.getValue().toBoolean());
        func.reset();
        Assert.assertTrue(func.getCount() == 0);
    }

    @Test
    public void testAvg() {
        Assert.assertEquals(DSRollup.valueOf("AVG"), DSRollup.AVG);
        Assert.assertEquals(DSRollup.valueFor("AVG"), DSRollup.AVG);
        Assert.assertEquals(DSRollup.valueFor("avg"), DSRollup.AVG);
        Assert.assertEquals(DSRollup.valueFor("Avg"), DSRollup.AVG);
        DSRollup roll = DSRollup.valueFor("avg");
        RollupFunction func = roll.getFunction();
        Assert.assertTrue(func.getCount() == 0);
        func.update(DSDouble.valueOf(2), DSStatus.ok);
        Assert.assertTrue(func.getValue().toDouble() == 2d);
        func.update(DSDouble.valueOf(4), DSStatus.ok);
        Assert.assertTrue(func.getValue().toDouble() == 3d);
        func.update(DSDouble.valueOf(4), DSStatus.fault);
        Assert.assertTrue(func.getValue().toDouble() == 3d);
        func.reset();
        Assert.assertTrue(func.getCount() == 0);
    }

    @Test
    public void testCount() {
        Assert.assertEquals(DSRollup.valueOf("COUNT"), DSRollup.COUNT);
        Assert.assertEquals(DSRollup.valueFor("COUNT"), DSRollup.COUNT);
        Assert.assertEquals(DSRollup.valueFor("count"), DSRollup.COUNT);
        Assert.assertEquals(DSRollup.valueFor("Count"), DSRollup.COUNT);
        DSRollup roll = DSRollup.valueFor("count");
        RollupFunction func = roll.getFunction();
        Assert.assertTrue(func.getCount() == 0);
        Assert.assertTrue(func.getValue().toDouble() == 0);
        func.update(DSDouble.valueOf(2), DSStatus.fault);
        Assert.assertTrue(func.getCount() == 1);
        Assert.assertTrue(func.getValue().toDouble() == 1);
        func.update(DSDouble.valueOf(2), DSStatus.fault);
        Assert.assertTrue(func.getCount() == 2);
        Assert.assertTrue(func.getValue().toDouble() == 2);
        func.update(DSDouble.valueOf(2), DSStatus.ok);
        Assert.assertTrue(func.getCount() == 1);
        Assert.assertTrue(func.getValue().toDouble() == 1);
        func.update(DSDouble.valueOf(2), DSStatus.fault);
        Assert.assertTrue(func.getCount() == 1);
        Assert.assertTrue(func.getValue().toDouble() == 1);
        func.reset();
        Assert.assertTrue(func.getCount() == 0);
        Assert.assertTrue(func.getValue().toDouble() == 0);
    }

    @Test
    public void testFirst() {
        Assert.assertEquals(DSRollup.valueOf("FIRST"), DSRollup.FIRST);
        Assert.assertEquals(DSRollup.valueFor("FIRST"), DSRollup.FIRST);
        Assert.assertEquals(DSRollup.valueFor("first"), DSRollup.FIRST);
        Assert.assertEquals(DSRollup.valueFor("First"), DSRollup.FIRST);
        DSRollup roll = DSRollup.valueFor("first");
        RollupFunction func = roll.getFunction();
        Assert.assertTrue(func.getCount() == 0);
        func.update(DSDouble.valueOf(2), DSStatus.fault);
        Assert.assertTrue(func.getValue().toDouble() == 2);
        func.update(DSDouble.valueOf(3), DSStatus.fault);
        Assert.assertTrue(func.getValue().toDouble() == 2);
        func.update(DSDouble.valueOf(4), DSStatus.ok);
        Assert.assertTrue(func.getValue().toDouble() == 4);
        func.reset();
        Assert.assertTrue(func.getCount() == 0);
    }

    @Test
    public void testLast() {
        Assert.assertEquals(DSRollup.valueOf("LAST"), DSRollup.LAST);
        Assert.assertEquals(DSRollup.valueFor("LAST"), DSRollup.LAST);
        Assert.assertEquals(DSRollup.valueFor("last"), DSRollup.LAST);
        Assert.assertEquals(DSRollup.valueFor("Last"), DSRollup.LAST);
        DSRollup roll = DSRollup.valueFor("last");
        RollupFunction func = roll.getFunction();
        Assert.assertTrue(func.getCount() == 0);
        func.update(DSDouble.valueOf(2), DSStatus.fault);
        Assert.assertTrue(func.getValue().toDouble() == 2);
        func.update(DSDouble.valueOf(3), DSStatus.fault);
        Assert.assertTrue(func.getValue().toDouble() == 3);
        func.update(DSDouble.valueOf(4), DSStatus.ok);
        Assert.assertTrue(func.getValue().toDouble() == 4);
        func.update(DSDouble.valueOf(3), DSStatus.fault);
        Assert.assertTrue(func.getValue().toDouble() == 4);
        func.reset();
        Assert.assertTrue(func.getCount() == 0);
    }

    @Test
    public void testMax() {
        Assert.assertEquals(DSRollup.valueOf("MAX"), DSRollup.MAX);
        Assert.assertEquals(DSRollup.valueFor("MAX"), DSRollup.MAX);
        Assert.assertEquals(DSRollup.valueFor("max"), DSRollup.MAX);
        Assert.assertEquals(DSRollup.valueFor("Max"), DSRollup.MAX);
        DSRollup roll = DSRollup.valueFor("Max");
        RollupFunction func = roll.getFunction();
        Assert.assertTrue(func.getCount() == 0);
        func.update(DSDouble.valueOf(2), DSStatus.fault);
        Assert.assertTrue(func.getValue().toDouble() == 2);
        func.update(DSDouble.valueOf(3), DSStatus.fault);
        Assert.assertTrue(func.getValue().toDouble() == 3);
        func.update(DSDouble.valueOf(1), DSStatus.ok);
        Assert.assertTrue(func.getValue().toDouble() == 1);
        func.update(DSDouble.valueOf(3), DSStatus.fault);
        Assert.assertTrue(func.getValue().toDouble() == 1);
        func.reset();
        Assert.assertTrue(func.getCount() == 0);
    }

    @Test
    public void testMedian() {
        Assert.assertEquals(DSRollup.valueOf("MEDIAN"), DSRollup.MEDIAN);
        Assert.assertEquals(DSRollup.valueFor("MEDIAN"), DSRollup.MEDIAN);
        Assert.assertEquals(DSRollup.valueFor("median"), DSRollup.MEDIAN);
        Assert.assertEquals(DSRollup.valueFor("Median"), DSRollup.MEDIAN);
        DSRollup roll = DSRollup.valueFor("Median");
        RollupFunction func = roll.getFunction();
        Assert.assertTrue(func.getCount() == 0);
        func.update(DSDouble.valueOf(2), DSStatus.fault);
        Assert.assertTrue(func.getValue().toDouble() == 2);
        func.update(DSDouble.valueOf(3), DSStatus.fault);
        Assert.assertTrue(func.getValue().toDouble() == 3);
        func.update(DSDouble.valueOf(2), DSStatus.fault);
        Assert.assertTrue(func.getValue().toDouble() == 2);
        func.update(DSDouble.valueOf(1), DSStatus.ok);
        Assert.assertTrue(func.getValue().toDouble() == 1);
        func.update(DSDouble.valueOf(5), DSStatus.ok);
        Assert.assertTrue(func.getValue().toDouble() == 5);
        func.update(DSDouble.valueOf(5), DSStatus.ok);
        Assert.assertTrue(func.getValue().toDouble() == 5);
        func.update(DSDouble.valueOf(10), DSStatus.ok);
        Assert.assertTrue(func.getValue().toDouble() == 5);
        func.update(DSDouble.valueOf(10), DSStatus.ok);
        Assert.assertTrue(func.getValue().toDouble() == 5);
        for (int i = 10; --i >= 0; ) {
            func.update(DSDouble.valueOf(7), DSStatus.fault);
        }
        Assert.assertTrue(func.getValue().toDouble() == 5);
        func.reset();
        Assert.assertTrue(func.getCount() == 0);
    }

    @Test
    public void testMin() {
        Assert.assertEquals(DSRollup.valueOf("MIN"), DSRollup.MIN);
        Assert.assertEquals(DSRollup.valueFor("MIN"), DSRollup.MIN);
        Assert.assertEquals(DSRollup.valueFor("min"), DSRollup.MIN);
        Assert.assertEquals(DSRollup.valueFor("Min"), DSRollup.MIN);
        DSRollup roll = DSRollup.valueFor("Min");
        RollupFunction func = roll.getFunction();
        Assert.assertTrue(func.getCount() == 0);
        func.update(DSDouble.valueOf(2), DSStatus.fault);
        Assert.assertTrue(func.getValue().toDouble() == 2);
        func.update(DSDouble.valueOf(3), DSStatus.fault);
        Assert.assertTrue(func.getValue().toDouble() == 2);
        func.update(DSDouble.valueOf(2), DSStatus.ok);
        Assert.assertTrue(func.getValue().toDouble() == 2);
        func.update(DSDouble.valueOf(1), DSStatus.fault);
        Assert.assertTrue(func.getValue().toDouble() == 2);
        func.reset();
        Assert.assertTrue(func.getCount() == 0);
    }

    @Test
    public void testMode() {
        Assert.assertEquals(DSRollup.valueOf("MODE"), DSRollup.MODE);
        Assert.assertEquals(DSRollup.valueFor("MODE"), DSRollup.MODE);
        Assert.assertEquals(DSRollup.valueFor("mode"), DSRollup.MODE);
        Assert.assertEquals(DSRollup.valueFor("Mode"), DSRollup.MODE);
        DSRollup roll = DSRollup.valueFor("Mode");
        RollupFunction func = roll.getFunction();
        for (int i = 9; --i >= 0; ) {
            func.update(DSDouble.valueOf(9), DSStatus.fault);
        }
        Assert.assertTrue(func.getValue().toDouble() == 9);
        for (int i = 10; --i >= 0; ) {
            func.update(DSDouble.valueOf(10), DSStatus.fault);
        }
        Assert.assertTrue(func.getValue().toDouble() == 10);
        for (int i = 3; --i >= 0; ) {
            func.update(DSDouble.valueOf(3), DSStatus.ok);
        }
        Assert.assertTrue(func.getValue().toDouble() == 3);
        for (int i = 10; --i >= 0; ) {
            func.update(DSDouble.valueOf(10), DSStatus.fault);
        }
        Assert.assertTrue(func.getValue().toDouble() == 3);
        func.reset();
        Assert.assertTrue(func.getCount() == 0);
    }

    @Test
    public void testOr() {
        Assert.assertEquals(DSRollup.valueOf("OR"), DSRollup.OR);
        Assert.assertEquals(DSRollup.valueFor("OR"), DSRollup.OR);
        Assert.assertEquals(DSRollup.valueFor("or"), DSRollup.OR);
        Assert.assertEquals(DSRollup.valueFor("Or"), DSRollup.OR);
        DSRollup roll = DSRollup.valueFor("Or");
        RollupFunction func = roll.getFunction();
        Assert.assertTrue(func.getCount() == 0);
        func.update(DSBool.FALSE, DSStatus.ok);
        Assert.assertFalse(func.getValue().toBoolean());
        func.update(DSBool.TRUE, DSStatus.ok);
        Assert.assertTrue(func.getValue().toBoolean());
        func.reset();
        func.update(DSBool.FALSE, DSStatus.ok);
        func.update(DSBool.FALSE, DSStatus.ok);
        Assert.assertFalse(func.getValue().toBoolean());
        func.reset();
        func.update(DSBool.FALSE, DSStatus.ok);
        Assert.assertFalse(func.getValue().toBoolean());
        func.update(DSBool.TRUE, DSStatus.fault);
        Assert.assertFalse(func.getValue().toBoolean());
        func.reset();
        Assert.assertTrue(func.getCount() == 0);
    }

    @Test
    public void testRange() {
        Assert.assertEquals(DSRollup.valueOf("RANGE"), DSRollup.RANGE);
        Assert.assertEquals(DSRollup.valueFor("RANGE"), DSRollup.RANGE);
        Assert.assertEquals(DSRollup.valueFor("range"), DSRollup.RANGE);
        Assert.assertEquals(DSRollup.valueFor("Range"), DSRollup.RANGE);
        DSRollup roll = DSRollup.valueFor("Range");
        RollupFunction func = roll.getFunction();
        Assert.assertTrue(func.getCount() == 0);
        func.update(DSDouble.valueOf(9), DSStatus.fault);
        Assert.assertTrue(func.getValue().toDouble() == 0);
        func.update(DSDouble.valueOf(11), DSStatus.fault);
        Assert.assertTrue(func.getValue().toDouble() == 2);
        func.update(DSDouble.valueOf(15), DSStatus.fault);
        Assert.assertTrue(func.getValue().toDouble() == 6);
        func.update(DSDouble.valueOf(-1), DSStatus.ok);
        Assert.assertTrue(func.getValue().toDouble() == 0);
        func.update(DSDouble.valueOf(1), DSStatus.ok);
        Assert.assertTrue(func.getValue().toDouble() == 2);
        func.update(DSDouble.valueOf(10), DSStatus.fault);
        Assert.assertTrue(func.getValue().toDouble() == 2);
        func.reset();
        Assert.assertTrue(func.getCount() == 0);
    }

    @Test
    public void testSum() {
        Assert.assertEquals(DSRollup.valueOf("SUM"), DSRollup.SUM);
        Assert.assertEquals(DSRollup.valueFor("SUM"), DSRollup.SUM);
        Assert.assertEquals(DSRollup.valueFor("Sum"), DSRollup.SUM);
        Assert.assertEquals(DSRollup.valueFor("sum"), DSRollup.SUM);
        DSRollup roll = DSRollup.valueFor("sum");
        RollupFunction func = roll.getFunction();
        Assert.assertTrue(func.getCount() == 0);
        func.update(DSDouble.valueOf(9), DSStatus.fault);
        Assert.assertTrue(func.getValue().toDouble() == 9);
        func.update(DSDouble.valueOf(11), DSStatus.fault);
        Assert.assertTrue(func.getValue().toDouble() == 20);
        func.update(DSDouble.valueOf(15), DSStatus.fault);
        Assert.assertTrue(func.getValue().toDouble() == 35);
        func.update(DSDouble.valueOf(-1), DSStatus.ok);
        Assert.assertTrue(func.getValue().toDouble() == -1);
        func.update(DSDouble.valueOf(1), DSStatus.ok);
        Assert.assertTrue(func.getValue().toDouble() == 0);
        func.update(DSDouble.valueOf(10), DSStatus.fault);
        Assert.assertTrue(func.getValue().toDouble() == 0);
        func.reset();
        Assert.assertTrue(func.getCount() == 0);
    }

    @Test
    public void testStatus() throws Exception {
        DSRollup roll = DSRollup.valueFor("sum");
        RollupFunction func = roll.getFunction();
        func.update(DSDouble.valueOf(0), DSStatus.fault);
        func.update(DSDouble.valueOf(0), DSStatus.down);
        Assert.assertTrue(func.getCount() == 2);
        Assert.assertTrue(func.getStatus().isAnyDown());
        Assert.assertTrue(func.getStatus().isAnyFault());
    }

    @Test
    public void testValid() throws Exception {
        DSRollup roll = DSRollup.valueFor("and");
        RollupFunction func = roll.getFunction();
        func.update(DSBool.TRUE, DSStatus.fault);
        Assert.assertTrue(func.getCount() == 1);
        func.update(DSBool.FALSE, DSStatus.fault);
        Assert.assertTrue(func.getCount() == 2);
        func.update(DSBool.TRUE, DSStatus.ok);
        Assert.assertTrue(func.getCount() == 1);
        func.update(DSBool.TRUE, DSStatus.ok);
        Assert.assertTrue(func.getCount() == 2);
        func.update(DSBool.FALSE, DSStatus.fault);
        Assert.assertTrue(func.getCount() == 2);
        Assert.assertTrue(func.getValue().toBoolean());
    }

}
