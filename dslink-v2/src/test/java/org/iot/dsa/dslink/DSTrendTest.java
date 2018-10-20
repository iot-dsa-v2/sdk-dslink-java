package org.iot.dsa.dslink;

import java.util.ArrayList;
import java.util.Iterator;
import org.iot.dsa.node.DSLong;
import org.iot.dsa.node.DSStatus;
import org.iot.dsa.rollup.DSRollup;
import org.iot.dsa.rollup.RollupFunction;
import org.iot.dsa.table.DSDeltaTrend;
import org.iot.dsa.table.DSITrend;
import org.iot.dsa.table.DSIntervalTrend;
import org.iot.dsa.table.SimpleTrend;
import org.iot.dsa.time.DSDateTime;
import org.iot.dsa.time.DSDuration;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author Aaron Hansen
 */
public class DSTrendTest {

    @Test
    public void covTest() throws Exception {
        SimpleTrend trend = new SimpleTrend();
        trend.setValueType(DSLong.NULL);
        ArrayList<DSDateTime> list = timeSeries(DSDuration.valueOf("PT15M"), 12);
        for (DSDateTime dt : list) {
            trend.addRow(dt, DSLong.valueOf(1), DSStatus.ok);
        }
        Iterator<DSDateTime> it = list.iterator();
        DSDuration dur = DSDuration.valueOf("PT5M");
        DSIntervalTrend cur = new DSIntervalTrend(trend.trend(),
                                                  dur,
                                                  DSRollup.FIRST).setCov(true); //COV
        RollupFunction func = DSRollup.COUNT.getFunction();
        DSDateTime dt = it.next();
        while (cur.next()) {
            Assert.assertEquals(cur.getValue(), DSLong.valueOf(1));
            func.update(cur.getValue(), DSStatus.OK);
            Assert.assertEquals(cur.getTimestamp(), dt.timeInMillis());
            dt = dur.apply(dt);
        }
        //you'd think 36, but the last ts is :45, so missing :50 and :55
        Assert.assertEquals(func.getValue(), DSLong.valueOf(34));
    }

    @Test
    public void deltaTest() throws Exception {
        SimpleTrend trend = new SimpleTrend();
        trend.setValueType(DSLong.NULL);
        int val = 0;
        //13 ivls because of delta, just to make things nice and even
        ArrayList<DSDateTime> list = timeSeries(DSDuration.valueOf("PT15M"), 13);
        for (DSDateTime dt : list) {
            trend.addRow(dt, DSLong.valueOf(++val), DSStatus.ok);
        }
        Iterator<DSDateTime> it = list.iterator();
        DSIntervalTrend cur = new DSIntervalTrend(new DSDeltaTrend(trend.trend()), //DELTA
                                                  DSDuration.valueOf("PT1H"),
                                                  DSRollup.SUM);
        //row 1
        Assert.assertTrue(cur.next());
        Assert.assertEquals(cur.getValue(), DSLong.valueOf(4));
        Assert.assertEquals(cur.getTimestamp(), it.next().timeInMillis());
        it.next();
        it.next();
        it.next();
        //row 2
        Assert.assertTrue(cur.next());
        Assert.assertEquals(cur.getValue(), DSLong.valueOf(4));
        Assert.assertEquals(cur.getTimestamp(), it.next().timeInMillis());
        it.next();
        it.next();
        it.next();
        //row 3
        Assert.assertTrue(cur.next());
        Assert.assertEquals(cur.getValue(), DSLong.valueOf(4));
        Assert.assertEquals(cur.getTimestamp(), it.next().timeInMillis());
        //end
        Assert.assertFalse(cur.next());
    }

    @Test
    public void rollupTest() throws Exception {
        SimpleTrend trend = new SimpleTrend();
        trend.setValueType(DSLong.NULL);
        int val = 0;
        ArrayList<DSDateTime> list = timeSeries(DSDuration.valueOf("PT15M"), 12);
        for (DSDateTime dt : list) {
            trend.addRow(dt, DSLong.valueOf(++val), DSStatus.ok);
        }
        Iterator<DSDateTime> it = list.iterator();
        DSIntervalTrend cur = new DSIntervalTrend(trend.trend(),
                                                  DSDuration.valueOf("PT1H"),
                                                  DSRollup.COUNT);
        //row 1
        Assert.assertTrue(cur.next());
        Assert.assertEquals(cur.getValue(), DSLong.valueOf(4));
        Assert.assertEquals(cur.getTimestamp(), it.next().timeInMillis());
        it.next();
        it.next();
        it.next();
        //row 2
        Assert.assertTrue(cur.next());
        Assert.assertEquals(cur.getValue(), DSLong.valueOf(4));
        Assert.assertEquals(cur.getTimestamp(), it.next().timeInMillis());
        it.next();
        it.next();
        it.next();
        //row 3
        Assert.assertTrue(cur.next());
        Assert.assertEquals(cur.getValue(), DSLong.valueOf(4));
        Assert.assertEquals(cur.getTimestamp(), it.next().timeInMillis());
        //end
        Assert.assertFalse(cur.next());
    }

    @Test
    public void simpleTest() {
        SimpleTrend trend = new SimpleTrend();
        trend.setValueType(DSLong.NULL);
        int val = 0;
        ArrayList<DSDateTime> list = timeSeries(DSDuration.valueOf("PT15M"), 4);
        for (DSDateTime dt : list) {
            trend.addRow(dt, DSLong.valueOf(++val), DSStatus.ok);
        }
        Iterator<DSDateTime> it = list.iterator();
        DSITrend cur = trend.trend();
        //row 1
        Assert.assertTrue(cur.next());
        Assert.assertEquals(cur.getValue(), DSLong.valueOf(1));
        Assert.assertEquals(cur.getTimestamp(), it.next().timeInMillis());
        //row 2
        Assert.assertTrue(cur.next());
        Assert.assertEquals(cur.getValue(), DSLong.valueOf(2));
        Assert.assertEquals(cur.getTimestamp(), it.next().timeInMillis());
        //row 3
        Assert.assertTrue(cur.next());
        Assert.assertEquals(cur.getValue(), DSLong.valueOf(3));
        Assert.assertEquals(cur.getTimestamp(), it.next().timeInMillis());
        //row 4
        Assert.assertTrue(cur.next());
        Assert.assertEquals(cur.getValue(), DSLong.valueOf(4));
        Assert.assertEquals(cur.getTimestamp(), it.next().timeInMillis());
        //end
        Assert.assertFalse(cur.next());
        Assert.assertFalse(it.hasNext());
    }

    private ArrayList<DSDateTime> timeSeries(DSDuration interval, int intervals) {
        ArrayList<DSDateTime> ret = new ArrayList<DSDateTime>();
        DSDateTime dt = DSDateTime.valueOf(2018, 10, 19);
        for (int i = 0; i < intervals; i++) {
            ret.add(dt);
            dt = interval.apply(dt);
        }
        return ret;
    }

}
