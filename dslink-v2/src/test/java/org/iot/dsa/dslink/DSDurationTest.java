package org.iot.dsa.dslink;

import java.util.Calendar;
import org.iot.dsa.time.DSDateTime;
import org.iot.dsa.time.DSDuration;
import org.iot.dsa.time.DSTime;
import org.testng.Assert;
import org.testng.annotations.Test;

public class DSDurationTest {

    @Test
    public void testAll() {
        DSDuration dur1 = DSDuration.valueOf("P1Y1M1DT1H1M1.1S");
        DSDuration dur2 = DSDuration.valueOf(false, 1, 1, 1, 1, 1, 1, 1);
        Assert.assertEquals(dur1.toString(), dur2.toString());
        Assert.assertEquals(dur1, dur2);
        DSDuration dur3 = dur2.valueOf(dur2.toElement());
        Assert.assertEquals(dur2, dur3);
        Assert.assertEquals(dur2.toString(), dur3.toString());
        Assert.assertEquals(dur1, dur3);
        long now = System.currentTimeMillis();
        long added = DSTime.addYears(1, now);
        added = DSTime.addMonths(1, added);
        added = DSTime.addDays(1, added);
        added = DSTime.addHours(1, added);
        added = DSTime.addMinutes(1, added);
        added = DSTime.addSeconds(1, added);
        added = DSTime.addMillis(1, added);
        Assert.assertEquals(added, dur1.apply(now));
        dur1 = DSDuration.valueOf("-P1Y1M1DT1H1M1.1S");
        dur2 = DSDuration.valueOf(true, 1, 1, 1, 1, 1, 1, 1);
        Assert.assertEquals(dur1.toString(), dur2.toString());
        Assert.assertEquals(dur1, dur2);
        dur3 = dur2.valueOf(dur2.toElement());
        Assert.assertEquals(dur2, dur3);
        Assert.assertEquals(dur2.toString(), dur3.toString());
        Assert.assertEquals(dur1, dur3);
        added = DSTime.addYears(-1, now);
        added = DSTime.addMonths(-1, added);
        added = DSTime.addDays(-1, added);
        added = DSTime.addHours(-1, added);
        added = DSTime.addMinutes(-1, added);
        added = DSTime.addSeconds(-1, added);
        added = DSTime.addMillis(-1, added);
        Assert.assertEquals(added, dur1.apply(now));
    }

    @Test
    public void testDays() {
        DSDuration dur1 = DSDuration.valueOf("P1D");
        DSDuration dur2 = DSDuration.valueOf(false, 0, 0, 1);
        Assert.assertEquals(dur1.toString(), dur2.toString());
        Assert.assertEquals(dur1, dur2);
        DSDuration dur3 = dur2.valueOf(dur2.toElement());
        Assert.assertEquals(dur2, dur3);
        Assert.assertEquals(dur2.toString(), dur3.toString());
        Assert.assertEquals(dur1, dur3);
        long now = System.currentTimeMillis();
        Assert.assertEquals(DSTime.addDays(1, now), dur1.apply(now));
        Calendar nowCal = DSTime.getCalendar(now);
        Assert.assertEquals(DSTime.addDays(1, nowCal), dur1.apply(DSTime.getCalendar(now)));
        DSDateTime nowDt = DSDateTime.valueOf(now);
        DSDateTime plusDt = DSDateTime.valueOf(DSTime.addDays(1, now));
        Assert.assertEquals(plusDt, dur1.apply(nowDt));
        dur1 = DSDuration.valueOf("-P1D");
        dur2 = DSDuration.valueOf(true, 0, 0, 1);
        Assert.assertEquals(dur1.toString(), dur2.toString());
        Assert.assertEquals(dur1, dur2);
        Assert.assertEquals(DSTime.addDays(-1, now), dur1.apply(now));
    }

    @Test
    public void testHMSm() {
        DSDuration dur1 = DSDuration.valueOf("PT1H1M1.1S");
        DSDuration dur2 = DSDuration.valueOf(false, 1, 1, 1, 1);
        Assert.assertEquals(dur1.toString(), dur2.toString());
        Assert.assertEquals(dur1, dur2);
        DSDuration dur3 = dur2.valueOf(dur2.toElement());
        Assert.assertEquals(dur2, dur3);
        Assert.assertEquals(dur2.toString(), dur3.toString());
        Assert.assertEquals(dur1, dur3);
        long now = System.currentTimeMillis();
        long added = DSTime.addHours(1, now);
        added = DSTime.addMinutes(1, added);
        added = DSTime.addSeconds(1, added);
        added = DSTime.addMillis(1, added);
        Assert.assertEquals(added, dur1.apply(now));
        dur1 = DSDuration.valueOf("-PT1H1M1.1S");
        dur2 = DSDuration.valueOf(true, 1, 1, 1, 1);
        Assert.assertEquals(dur1.toString(), dur2.toString());
        Assert.assertEquals(dur1, dur2);
        dur3 = dur2.valueOf(dur2.toElement());
        Assert.assertEquals(dur2, dur3);
        Assert.assertEquals(dur2.toString(), dur3.toString());
        Assert.assertEquals(dur1, dur3);
        added = DSTime.addHours(-1, now);
        added = DSTime.addMinutes(-1, added);
        added = DSTime.addSeconds(-1, added);
        added = DSTime.addMillis(-1, added);
        Assert.assertEquals(added, dur1.apply(now));
    }

    @Test
    public void testHours() {
        DSDuration dur1 = DSDuration.valueOf("PT1H");
        DSDuration dur2 = DSDuration.valueOf(false, 1, 0, 0, 0);
        Assert.assertEquals(dur1.toString(), dur2.toString());
        Assert.assertEquals(dur1, dur2);
        DSDuration dur3 = dur2.valueOf(dur2.toElement());
        Assert.assertEquals(dur2, dur3);
        Assert.assertEquals(dur2.toString(), dur3.toString());
        Assert.assertEquals(dur1, dur3);
        long now = System.currentTimeMillis();
        Assert.assertEquals(DSTime.addHours(1, now), dur1.apply(now));
        Calendar nowCal = DSTime.getCalendar(now);
        Assert.assertEquals(DSTime.addHours(1, nowCal), dur1.apply(DSTime.getCalendar(now)));
        DSDateTime nowDt = DSDateTime.valueOf(now);
        DSDateTime plusDt = DSDateTime.valueOf(DSTime.addHours(1, now));
        Assert.assertEquals(plusDt, dur1.apply(nowDt));
        dur1 = DSDuration.valueOf("-PT1H");
        dur2 = DSDuration.valueOf(true, 1, 0, 0, 0);
        Assert.assertEquals(dur1.toString(), dur2.toString());
        Assert.assertEquals(dur1, dur2);
        Assert.assertEquals(DSTime.addHours(-1, now), dur1.apply(now));
    }

    @Test
    public void testMillis() {
        DSDuration dur1 = DSDuration.valueOf("PT0.1S");
        DSDuration dur2 = DSDuration.valueOf(false, 0, 0, 0, 1);
        Assert.assertEquals(dur1.toString(), dur2.toString());
        Assert.assertEquals(dur1, dur2);
        DSDuration dur3 = dur2.valueOf(dur2.toElement());
        Assert.assertEquals(dur2, dur3);
        Assert.assertEquals(dur2.toString(), dur3.toString());
        Assert.assertEquals(dur1, dur3);
        long now = System.currentTimeMillis();
        Assert.assertEquals(DSTime.addMillis(1, now), dur1.apply(now));
        Calendar nowCal = DSTime.getCalendar(now);
        Assert.assertEquals(DSTime.addMillis(1, nowCal), dur1.apply(DSTime.getCalendar(now)));
        DSDateTime nowDt = DSDateTime.valueOf(now);
        DSDateTime plusDt = DSDateTime.valueOf(DSTime.addMillis(1, now));
        Assert.assertEquals(plusDt, dur1.apply(nowDt));
        dur1 = DSDuration.valueOf("-PT0.1S");
        dur2 = DSDuration.valueOf(true, 0, 0, 0, 1);
        Assert.assertEquals(dur1.toString(), dur2.toString());
        Assert.assertEquals(dur1, dur2);
        Assert.assertEquals(DSTime.addMillis(-1, now), dur1.apply(now));
    }

    @Test
    public void testMinutes() {
        DSDuration dur1 = DSDuration.valueOf("PT1M");
        DSDuration dur2 = DSDuration.valueOf(false, 0, 1, 0, 0);
        Assert.assertEquals(dur1.toString(), dur2.toString());
        Assert.assertEquals(dur1, dur2);
        DSDuration dur3 = dur2.valueOf(dur2.toElement());
        Assert.assertEquals(dur2, dur3);
        Assert.assertEquals(dur2.toString(), dur3.toString());
        Assert.assertEquals(dur1, dur3);
        long now = System.currentTimeMillis();
        Assert.assertEquals(DSTime.addMinutes(1, now), dur1.apply(now));
        Calendar nowCal = DSTime.getCalendar(now);
        Assert.assertEquals(DSTime.addMinutes(1, nowCal), dur1.apply(DSTime.getCalendar(now)));
        DSDateTime nowDt = DSDateTime.valueOf(now);
        DSDateTime plusDt = DSDateTime.valueOf(DSTime.addMinutes(1, now));
        Assert.assertEquals(plusDt, dur1.apply(nowDt));
        dur1 = DSDuration.valueOf("-PT1M");
        dur2 = DSDuration.valueOf(true, 0, 1, 0, 0);
        Assert.assertEquals(dur1.toString(), dur2.toString());
        Assert.assertEquals(dur1, dur2);
        Assert.assertEquals(DSTime.addMinutes(-1, now), dur1.apply(now));
    }

    @Test
    public void testMonths() {
        DSDuration dur1 = DSDuration.valueOf("P1M");
        DSDuration dur2 = DSDuration.valueOf(false, 0, 1, 0);
        Assert.assertEquals(dur1.toString(), dur2.toString());
        Assert.assertEquals(dur1, dur2);
        DSDuration dur3 = dur2.valueOf(dur2.toElement());
        Assert.assertEquals(dur2, dur3);
        Assert.assertEquals(dur2.toString(), dur3.toString());
        Assert.assertEquals(dur1, dur3);
        long now = System.currentTimeMillis();
        Assert.assertEquals(DSTime.addMonths(1, now), dur1.apply(now));
        Calendar nowCal = DSTime.getCalendar(now);
        Assert.assertEquals(DSTime.addMonths(1, nowCal), dur1.apply(DSTime.getCalendar(now)));
        DSDateTime nowDt = DSDateTime.valueOf(now);
        DSDateTime plusDt = DSDateTime.valueOf(DSTime.addMonths(1, now));
        Assert.assertEquals(plusDt, dur1.apply(nowDt));
        dur1 = DSDuration.valueOf("-P1M");
        dur2 = DSDuration.valueOf(true, 0, 1, 0);
        Assert.assertEquals(dur1.toString(), dur2.toString());
        Assert.assertEquals(dur1, dur2);
        Assert.assertEquals(DSTime.addMonths(-1, now), dur1.apply(now));
    }

    @Test
    public void testSeconds() {
        DSDuration dur1 = DSDuration.valueOf("PT1S");
        DSDuration dur2 = DSDuration.valueOf(false, 0, 0, 1, 0);
        Assert.assertEquals(dur1.toString(), dur2.toString());
        Assert.assertEquals(dur1, dur2);
        DSDuration dur3 = dur2.valueOf(dur2.toElement());
        Assert.assertEquals(dur2, dur3);
        Assert.assertEquals(dur2.toString(), dur3.toString());
        Assert.assertEquals(dur1, dur3);
        long now = System.currentTimeMillis();
        Assert.assertEquals(DSTime.addSeconds(1, now), dur1.apply(now));
        Calendar nowCal = DSTime.getCalendar(now);
        Assert.assertEquals(DSTime.addSeconds(1, nowCal), dur1.apply(DSTime.getCalendar(now)));
        DSDateTime nowDt = DSDateTime.valueOf(now);
        DSDateTime plusDt = DSDateTime.valueOf(DSTime.addSeconds(1, now));
        Assert.assertEquals(plusDt, dur1.apply(nowDt));
        dur1 = DSDuration.valueOf("-PT1S");
        dur2 = DSDuration.valueOf(true, 0, 0, 1, 0);
        Assert.assertEquals(dur1.toString(), dur2.toString());
        Assert.assertEquals(dur1, dur2);
        Assert.assertEquals(DSTime.addSeconds(-1, now), dur1.apply(now));
    }

    @Test
    public void testYMD() {
        DSDuration dur1 = DSDuration.valueOf("P1Y1M1D");
        DSDuration dur2 = DSDuration.valueOf(false, 1, 1, 1);
        Assert.assertEquals(dur1.toString(), dur2.toString());
        Assert.assertEquals(dur1, dur2);
        DSDuration dur3 = dur2.valueOf(dur2.toElement());
        Assert.assertEquals(dur2, dur3);
        Assert.assertEquals(dur2.toString(), dur3.toString());
        Assert.assertEquals(dur1, dur3);
        long now = System.currentTimeMillis();
        long added = DSTime.addYears(1, now);
        added = DSTime.addMonths(1, added);
        added = DSTime.addDays(1, added);
        Assert.assertEquals(added, dur1.apply(now));
        dur1 = DSDuration.valueOf("-P1Y1M1D");
        dur2 = DSDuration.valueOf(true, 1, 1, 1);
        Assert.assertEquals(dur1.toString(), dur2.toString());
        Assert.assertEquals(dur1, dur2);
        dur3 = dur2.valueOf(dur2.toElement());
        Assert.assertEquals(dur2, dur3);
        Assert.assertEquals(dur2.toString(), dur3.toString());
        Assert.assertEquals(dur1, dur3);
        added = DSTime.addYears(-1, now);
        added = DSTime.addMonths(-1, added);
        added = DSTime.addDays(-1, added);
        Assert.assertEquals(added, dur1.apply(now));
    }

    @Test
    public void testYears() {
        DSDuration dur1 = DSDuration.valueOf("P1Y");
        DSDuration dur2 = DSDuration.valueOf(false, 1, 0, 0);
        Assert.assertEquals(dur1.toString(), dur2.toString());
        Assert.assertEquals(dur1, dur2);
        DSDuration dur3 = dur2.valueOf(dur2.toElement());
        Assert.assertEquals(dur2, dur3);
        Assert.assertEquals(dur2.toString(), dur3.toString());
        Assert.assertEquals(dur1, dur3);
        long now = System.currentTimeMillis();
        Assert.assertEquals(DSTime.addYears(1, now), dur1.apply(now));
        Calendar nowCal = DSTime.getCalendar(now);
        Assert.assertEquals(DSTime.addYears(1, nowCal), dur1.apply(DSTime.getCalendar(now)));
        DSDateTime nowDt = DSDateTime.valueOf(now);
        DSDateTime plusDt = DSDateTime.valueOf(DSTime.addYears(1, now));
        Assert.assertEquals(plusDt, dur1.apply(nowDt));
        dur1 = DSDuration.valueOf("-P1Y");
        dur2 = DSDuration.valueOf(true, 1, 0, 0);
        Assert.assertEquals(dur1.toString(), dur2.toString());
        Assert.assertEquals(dur1, dur2);
        Assert.assertEquals(DSTime.addYears(-1, now), dur1.apply(now));
    }

}
