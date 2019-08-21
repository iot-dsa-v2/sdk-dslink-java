package org.iot.dsa.dslink;

import java.util.Calendar;
import org.iot.dsa.time.DSDateTime;
import org.iot.dsa.time.DSDuration;
import org.iot.dsa.time.Time;
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
        long added = Time.addYears(1, now);
        added = Time.addMonths(1, added);
        added = Time.addDays(1, added);
        added = Time.addHours(1, added);
        added = Time.addMinutes(1, added);
        added = Time.addSeconds(1, added);
        added = Time.addMillis(1, added);
        Assert.assertEquals(added, dur1.apply(now));
        dur1 = DSDuration.valueOf("-P1Y1M1DT1H1M1.1S");
        dur2 = DSDuration.valueOf(true, 1, 1, 1, 1, 1, 1, 1);
        Assert.assertEquals(dur1.toString(), dur2.toString());
        Assert.assertEquals(dur1, dur2);
        dur3 = dur2.valueOf(dur2.toElement());
        Assert.assertEquals(dur2, dur3);
        Assert.assertEquals(dur2.toString(), dur3.toString());
        Assert.assertEquals(dur1, dur3);
        added = Time.addYears(-1, now);
        added = Time.addMonths(-1, added);
        added = Time.addDays(-1, added);
        added = Time.addHours(-1, added);
        added = Time.addMinutes(-1, added);
        added = Time.addSeconds(-1, added);
        added = Time.addMillis(-1, added);
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
        Assert.assertEquals(Time.addDays(1, now), dur1.apply(now));
        Calendar nowCal = Time.getCalendar(now);
        Assert.assertEquals(Time.addDays(1, nowCal), dur1.apply(Time.getCalendar(now)));
        DSDateTime nowDt = DSDateTime.valueOf(now);
        DSDateTime plusDt = DSDateTime.valueOf(Time.addDays(1, now));
        Assert.assertEquals(plusDt, dur1.apply(nowDt));
        dur1 = DSDuration.valueOf("-P1D");
        dur2 = DSDuration.valueOf(true, 0, 0, 1);
        Assert.assertEquals(dur1.toString(), dur2.toString());
        Assert.assertEquals(dur1, dur2);
        Assert.assertEquals(Time.addDays(-1, now), dur1.apply(now));
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
        long added = Time.addHours(1, now);
        added = Time.addMinutes(1, added);
        added = Time.addSeconds(1, added);
        added = Time.addMillis(1, added);
        Assert.assertEquals(added, dur1.apply(now));
        dur1 = DSDuration.valueOf("-PT1H1M1.1S");
        dur2 = DSDuration.valueOf(true, 1, 1, 1, 1);
        Assert.assertEquals(dur1.toString(), dur2.toString());
        Assert.assertEquals(dur1, dur2);
        dur3 = dur2.valueOf(dur2.toElement());
        Assert.assertEquals(dur2, dur3);
        Assert.assertEquals(dur2.toString(), dur3.toString());
        Assert.assertEquals(dur1, dur3);
        added = Time.addHours(-1, now);
        added = Time.addMinutes(-1, added);
        added = Time.addSeconds(-1, added);
        added = Time.addMillis(-1, added);
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
        Assert.assertEquals(Time.addHours(1, now), dur1.apply(now));
        Calendar nowCal = Time.getCalendar(now);
        Assert.assertEquals(Time.addHours(1, nowCal), dur1.apply(Time.getCalendar(now)));
        DSDateTime nowDt = DSDateTime.valueOf(now);
        DSDateTime plusDt = DSDateTime.valueOf(Time.addHours(1, now));
        Assert.assertEquals(plusDt, dur1.apply(nowDt));
        dur1 = DSDuration.valueOf("-PT1H");
        dur2 = DSDuration.valueOf(true, 1, 0, 0, 0);
        Assert.assertEquals(dur1.toString(), dur2.toString());
        Assert.assertEquals(dur1, dur2);
        Assert.assertEquals(Time.addHours(-1, now), dur1.apply(now));
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
        Assert.assertEquals(Time.addMillis(1, now), dur1.apply(now));
        Calendar nowCal = Time.getCalendar(now);
        Assert.assertEquals(Time.addMillis(1, nowCal), dur1.apply(Time.getCalendar(now)));
        DSDateTime nowDt = DSDateTime.valueOf(now);
        DSDateTime plusDt = DSDateTime.valueOf(Time.addMillis(1, now));
        Assert.assertEquals(plusDt, dur1.apply(nowDt));
        dur1 = DSDuration.valueOf("-PT0.1S");
        dur2 = DSDuration.valueOf(true, 0, 0, 0, 1);
        Assert.assertEquals(dur1.toString(), dur2.toString());
        Assert.assertEquals(dur1, dur2);
        Assert.assertEquals(Time.addMillis(-1, now), dur1.apply(now));
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
        Assert.assertEquals(Time.addMinutes(1, now), dur1.apply(now));
        Calendar nowCal = Time.getCalendar(now);
        Assert.assertEquals(Time.addMinutes(1, nowCal), dur1.apply(Time.getCalendar(now)));
        DSDateTime nowDt = DSDateTime.valueOf(now);
        DSDateTime plusDt = DSDateTime.valueOf(Time.addMinutes(1, now));
        Assert.assertEquals(plusDt, dur1.apply(nowDt));
        dur1 = DSDuration.valueOf("-PT1M");
        dur2 = DSDuration.valueOf(true, 0, 1, 0, 0);
        Assert.assertEquals(dur1.toString(), dur2.toString());
        Assert.assertEquals(dur1, dur2);
        Assert.assertEquals(Time.addMinutes(-1, now), dur1.apply(now));
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
        Assert.assertEquals(Time.addMonths(1, now), dur1.apply(now));
        Calendar nowCal = Time.getCalendar(now);
        Assert.assertEquals(Time.addMonths(1, nowCal), dur1.apply(Time.getCalendar(now)));
        DSDateTime nowDt = DSDateTime.valueOf(now);
        DSDateTime plusDt = DSDateTime.valueOf(Time.addMonths(1, now));
        Assert.assertEquals(plusDt, dur1.apply(nowDt));
        dur1 = DSDuration.valueOf("-P1M");
        dur2 = DSDuration.valueOf(true, 0, 1, 0);
        Assert.assertEquals(dur1.toString(), dur2.toString());
        Assert.assertEquals(dur1, dur2);
        Assert.assertEquals(Time.addMonths(-1, now), dur1.apply(now));
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
        Assert.assertEquals(Time.addSeconds(1, now), dur1.apply(now));
        Calendar nowCal = Time.getCalendar(now);
        Assert.assertEquals(Time.addSeconds(1, nowCal), dur1.apply(Time.getCalendar(now)));
        DSDateTime nowDt = DSDateTime.valueOf(now);
        DSDateTime plusDt = DSDateTime.valueOf(Time.addSeconds(1, now));
        Assert.assertEquals(plusDt, dur1.apply(nowDt));
        dur1 = DSDuration.valueOf("-PT1S");
        dur2 = DSDuration.valueOf(true, 0, 0, 1, 0);
        Assert.assertEquals(dur1.toString(), dur2.toString());
        Assert.assertEquals(dur1, dur2);
        Assert.assertEquals(Time.addSeconds(-1, now), dur1.apply(now));
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
        long added = Time.addYears(1, now);
        added = Time.addMonths(1, added);
        added = Time.addDays(1, added);
        Assert.assertEquals(added, dur1.apply(now));
        dur1 = DSDuration.valueOf("-P1Y1M1D");
        dur2 = DSDuration.valueOf(true, 1, 1, 1);
        Assert.assertEquals(dur1.toString(), dur2.toString());
        Assert.assertEquals(dur1, dur2);
        dur3 = dur2.valueOf(dur2.toElement());
        Assert.assertEquals(dur2, dur3);
        Assert.assertEquals(dur2.toString(), dur3.toString());
        Assert.assertEquals(dur1, dur3);
        added = Time.addYears(-1, now);
        added = Time.addMonths(-1, added);
        added = Time.addDays(-1, added);
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
        Assert.assertEquals(Time.addYears(1, now), dur1.apply(now));
        Calendar nowCal = Time.getCalendar(now);
        Assert.assertEquals(Time.addYears(1, nowCal), dur1.apply(Time.getCalendar(now)));
        DSDateTime nowDt = DSDateTime.valueOf(now);
        DSDateTime plusDt = DSDateTime.valueOf(Time.addYears(1, now));
        Assert.assertEquals(plusDt, dur1.apply(nowDt));
        dur1 = DSDuration.valueOf("-P1Y");
        dur2 = DSDuration.valueOf(true, 1, 0, 0);
        Assert.assertEquals(dur1.toString(), dur2.toString());
        Assert.assertEquals(dur1, dur2);
        Assert.assertEquals(Time.addYears(-1, now), dur1.apply(now));
    }

}
