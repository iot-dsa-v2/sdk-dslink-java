package org.iot.dsa.dslink;

import java.util.Calendar;
import java.util.TimeZone;
import org.iot.dsa.time.Time;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Tests for org.dsa.iot.dslink.util.Time
 */
public class TimeTest {

    @Test
    public void testAdding() {
        Calendar cal = make(2016, 3, 6, 8, 55, 30);
        long origTs = cal.getTimeInMillis();
        //--- Seconds ---
        //positive
        Time.addSeconds(15, cal);
        validateEqual(cal, make(2016, 3, 6, 8, 55, 45));
        //positive + cross boundary
        cal.setTimeInMillis(origTs); //2016,3,6,8,55,30
        Time.addSeconds(60, cal);
        validateEqual(cal, make(2016, 3, 6, 8, 56, 30));
        //negative
        cal.setTimeInMillis(origTs);
        Time.addSeconds(-15, cal);
        validateEqual(cal, make(2016, 3, 6, 8, 55, 15));
        //positive + cross boundary
        cal.setTimeInMillis(origTs);
        Time.addSeconds(-60, cal);
        validateEqual(cal, make(2016, 3, 6, 8, 54, 30));
        //--- Minutes ---
        //positive
        cal.setTimeInMillis(origTs); //2016,3,6,8,55,30
        Time.addMinutes(2, cal);
        validateEqual(cal, make(2016, 3, 6, 8, 57, 30));
        //positive + cross boundary
        cal.setTimeInMillis(origTs);
        Time.addMinutes(10, cal);
        validateEqual(cal, make(2016, 3, 6, 9, 5, 30));
        //negative
        cal.setTimeInMillis(origTs);
        Time.addMinutes(-15, cal);
        validateEqual(cal, make(2016, 3, 6, 8, 40, 30));
        //positive + cross boundary
        cal.setTimeInMillis(origTs);
        Time.addMinutes(-60, cal);
        validateEqual(cal, make(2016, 3, 6, 7, 55, 30));
        //--- Days ---
        //positive
        cal.setTimeInMillis(origTs); //2016,3,6,8,55,30
        Time.addDays(2, cal);
        validateEqual(cal, make(2016, 3, 8, 8, 55, 30));
        //positive + cross boundary
        cal.setTimeInMillis(origTs);
        Time.addDays(30, cal);
        validateEqual(cal, make(2016, 4, 6, 8, 55, 30));
        //negative
        cal.setTimeInMillis(origTs);
        Time.addDays(-2, cal);
        validateEqual(cal, make(2016, 3, 4, 8, 55, 30));
        //positive + cross boundary
        cal.setTimeInMillis(origTs);
        Time.addDays(-31, cal);
        validateEqual(cal, make(2016, 2, 6, 8, 55, 30));
        //--- Weeks ---
        //positive
        cal.setTimeInMillis(origTs); //2016,3,6,8,55,30
        Time.addWeeks(2, cal);
        validateEqual(cal, make(2016, 3, 20, 8, 55, 30));
        //positive + cross boundary
        cal.setTimeInMillis(origTs);
        Time.addWeeks(4, cal);
        validateEqual(cal, make(2016, 4, 4, 8, 55, 30));
        //negative
        cal.setTimeInMillis(origTs);
        Time.addWeeks(-1, cal);
        validateEqual(cal, make(2016, 2, 30, 8, 55, 30));
        //positive + cross boundary
        cal.setTimeInMillis(origTs);
        Time.addWeeks(-4, cal);
        validateEqual(cal, make(2016, 2, 9, 8, 55, 30));
        //--- Months ---
        //positive
        cal.setTimeInMillis(origTs); //2016,3,6,8,55,30
        Time.addMonths(1, cal);
        validateEqual(cal, make(2016, 4, 6, 8, 55, 30));
        //positive + cross boundary
        cal.setTimeInMillis(origTs);
        Time.addMonths(12, cal);
        validateEqual(cal, make(2017, 3, 6, 8, 55, 30));
        //negative
        cal.setTimeInMillis(origTs);
        Time.addMonths(-1, cal);
        validateEqual(cal, make(2016, 2, 6, 8, 55, 30));
        //positive + cross boundary
        cal.setTimeInMillis(origTs);
        Time.addMonths(-12, cal);
        validateEqual(cal, make(2015, 3, 6, 8, 55, 30));
        //--- Years ---
        //positive
        cal.setTimeInMillis(origTs); //2016,3,6,8,55,30
        Time.addYears(1, cal);
        validateEqual(cal, make(2017, 3, 6, 8, 55, 30));
        //positive + cross boundary
        cal.setTimeInMillis(origTs);
        Time.addYears(10, cal);
        validateEqual(cal, make(2026, 3, 6, 8, 55, 30));
        //negative
        cal.setTimeInMillis(origTs);
        Time.addYears(-1, cal);
        validateEqual(cal, make(2015, 3, 6, 8, 55, 30));
        //positive + cross boundary
        cal.setTimeInMillis(origTs);
        Time.addYears(-10, cal);
        validateEqual(cal, make(2006, 3, 6, 8, 55, 30));
    }

    @Test
    public void testAlignment() {
        Calendar cal = make(2016, 3, 6, 8, 55, 30);
        long origTs = cal.getTimeInMillis();
        //--- Seconds ---
        Time.alignSecond(cal);
        validateEqual(cal, make(2016, 3, 6, 8, 55, 30));
        //common case
        cal.setTimeInMillis(origTs);
        Time.alignSeconds(20, cal);
        validateEqual(cal, make(2016, 3, 6, 8, 55, 20));
        //align to self
        cal.setTimeInMillis(origTs);
        Time.alignSeconds(10, cal);
        validateEqual(cal, make(2016, 3, 6, 8, 55, 30));
        //odd case
        cal.setTimeInMillis(origTs);
        Time.alignSeconds(7, cal);
        validateEqual(cal, make(2016, 3, 6, 8, 55, 28));
        //--- Minutes ---
        cal.setTimeInMillis(origTs);
        Time.alignMinute(cal);
        validateEqual(cal, make(2016, 3, 6, 8, 55, 0));
        //common case
        cal.setTimeInMillis(origTs);
        Time.alignMinutes(15, cal);
        validateEqual(cal, make(2016, 3, 6, 8, 45, 0));
        //align to self
        cal.setTimeInMillis(origTs);
        Time.alignMinutes(5, cal);
        validateEqual(cal, make(2016, 3, 6, 8, 55, 0));
        //odd case
        cal.setTimeInMillis(origTs);
        Time.alignMinutes(7, cal);
        validateEqual(cal, make(2016, 3, 6, 8, 49, 0));
        //--- Hours ---
        cal.setTimeInMillis(origTs);
        Time.alignHour(cal);
        validateEqual(cal, make(2016, 3, 6, 8, 0, 0));
        //common case
        cal.setTimeInMillis(origTs);
        Time.alignHours(6, cal);
        validateEqual(cal, make(2016, 3, 6, 6, 0, 0));
        //align to self
        cal.setTimeInMillis(origTs);
        Time.alignHours(2, cal);
        validateEqual(cal, make(2016, 3, 6, 8, 0, 0));
        //odd case
        cal.setTimeInMillis(origTs);
        Time.alignHours(3, cal);
        validateEqual(cal, make(2016, 3, 6, 6, 0, 0));
        //beginning of day
        cal.setTimeInMillis(origTs);
        Time.alignHours(12, cal);
        validateEqual(cal, make(2016, 3, 6, 0, 0, 0));
        //--- Days ---
        cal.setTimeInMillis(origTs);
        Time.alignDay(cal);
        validateEqual(cal, make(2016, 3, 6, 0, 0, 0));
        //common case
        cal.setTimeInMillis(origTs);
        Time.alignDays(1, cal);
        validateEqual(cal, make(2016, 3, 6, 0, 0, 0));
        //align to self
        cal.setTimeInMillis(origTs);
        Time.alignDays(6, cal);
        validateEqual(cal, make(2016, 3, 6, 0, 0, 0));
        //odd case
        cal.setTimeInMillis(origTs);
        Time.alignDays(5, cal);
        validateEqual(cal, make(2016, 3, 5, 0, 0, 0));
        //--- Weeks ---
        cal.setTimeInMillis(origTs);
        Time.alignWeek(cal);
        validateEqual(cal, make(2016, 3, 3, 0, 0, 0));
        //--- Months ---
        cal.setTimeInMillis(origTs);
        Time.alignMonth(cal);
        validateEqual(cal, make(2016, 3, 1, 0, 0, 0));
        //--- Years ---
        cal.setTimeInMillis(origTs);
        Time.alignYear(cal);
        validateEqual(cal, make(2016, 0, 1, 0, 0, 0));
    }

    @Test
    public void testDecoding() {
        TimeZone timeZone = TimeZone.getTimeZone("America/Los_Angeles");
        Calendar correctTime = make(2016, 0, 1, 0, 0, 0);
        correctTime.setTimeZone(timeZone);
        Calendar cal = Calendar.getInstance();
        cal.setTimeZone(timeZone);
        String encoded = "2016-01-01T00:00:00.000";
        cal = Time.decode(encoded, cal);
        validateEqual(cal, correctTime);
        encoded = "2016-01-01T00:00:00.000-08:00";
        cal = Time.decode(encoded, cal);
        validateEqual(cal, correctTime);
        encoded = "2016-01-01T00:00:00.987654-08:00";
        long millis = Time.decode(encoded);
        Assert.assertTrue(millis % 1000 == 987);
        try {
            Time.decode("2016_01-01T00:00:00.000-08:00", null);
            throw new IllegalStateException();
        } catch (IllegalArgumentException ignored) {
        }
        try {
            Time.decode("2016-1-01T00:00:00.000-08:00", null);
            throw new IllegalStateException();
        } catch (IllegalArgumentException ignored) {
        }
        try {
            Time.decode("2016_01-01T0:00:00.000-08:00", null);
            throw new IllegalStateException();
        } catch (IllegalArgumentException ignored) {
        }
    }

    @Test
    public void testEncoding() {
        TimeZone timeZone = TimeZone.getTimeZone("America/Los_Angeles");
        Calendar cal = make(2016, 0, 1, 0, 0, 0);
        cal.setTimeZone(timeZone);
        String encoded = Time.encode(cal, false, null).toString();
        validateEqual(encoded, "2016-01-01T00:00:00.000");
        encoded = Time.encode(cal, true, null).toString();
        validateEqual(encoded, "2016-01-01T00:00:00.000-08:00");
    }

    /**
     * Convenience for constructing calendars.
     */
    private Calendar make(
            int year,
            int month,
            int day,
            int hour,
            int minute,
            int second) {
        Calendar ret = Calendar.getInstance();
        ret.set(year, month, day, hour, minute, second);
        return Time.alignSecond(ret);
    }

    /**
     * Throws an IllegalStateException if the two calendars are not equal.
     */
    private void validateEqual(Calendar c1, Calendar c2) {
        if (c1.getTimeInMillis() != c2.getTimeInMillis()) {
            System.out.print(Time.encode(c1.getTimeInMillis(), true));
            System.out.print(" != ");
            System.out.println(Time.encode(c2.getTimeInMillis(), true));
            throw new IllegalStateException();
        }
    }

    /**
     * Throws an IllegalStateException if the two Strings are not equal.
     */
    private void validateEqual(String s1, String s2) {
        if (!s1.equals(s2)) {
            System.out.print(s1);
            System.out.print(" != ");
            System.out.println(s1);
            throw new IllegalStateException();
        }
    }

}
