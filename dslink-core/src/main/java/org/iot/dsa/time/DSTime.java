package org.iot.dsa.time;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.SimpleTimeZone;
import java.util.TimeZone;

/**
 * Misc time utility functions.
 *
 * @author Aaron Hansen
 */
public class DSTime {

    ///////////////////////////////////////////////////////////////////////////
    // Constants
    ///////////////////////////////////////////////////////////////////////////

    public static final long NANOS_IN_MS = 1000000;
    public static final long NANOS_IN_SEC = 1000 * NANOS_IN_MS;

    public static final int MILLIS_SECOND = 1000;
    public static final int MILLIS_FIVE_SECONDS = 5000;
    public static final int MILLIS_TEN_SECONDS = 10000;
    public static final int MILLIS_FIFTEEN_SECONDS = 15000;
    public static final int MILLIS_THIRTY_SECONDS = 30000;
    public static final int MILLIS_MINUTE = 60000;
    public static final int MILLIS_FIVE_MINUTES = 300000;
    public static final int MILLIS_TEN_MINUTES = 600000;
    public static final int MILLIS_FIFTEEN_MINUTES = 900000;
    public static final int MILLIS_TWENTY_MINUTES = 1200000;
    public static final int MILLIS_THIRTY_MINUTES = 1800000;
    public static final int MILLIS_HOUR = 3600000;
    public static final int MILLIS_TWO_HOURS = 7200000;
    public static final int MILLIS_THREE_HOURS = 10800000;
    public static final int MILLIS_FOUR_HOURS = 14400000;
    public static final int MILLIS_SIX_HOURS = 21600000;
    public static final int MILLIS_TWELVE_HOURS = 43200000;
    public static final int MILLIS_DAY = 86400000;
    public static final int MILLIS_WEEK = 604800000;
    public static final long MILLIS_MONTH = 2592000000l;
    public static final long MILLIS_QUARTER = MILLIS_MONTH * 3;
    public static final long MILLIS_YEAR = MILLIS_MONTH * 12;

    ///////////////////////////////////////////////////////////////////////////
    // Fields
    ///////////////////////////////////////////////////////////////////////////

    private static Calendar calendarCache1;
    private static Calendar calendarCache2;
    private static final Map<String, TimeZone> timezones = new HashMap<String, TimeZone>();

    ///////////////////////////////////////////////////////////////////////////
    // Constructors
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Do not allow instantiation.
     */
    private DSTime() {
    }

    ///////////////////////////////////////////////////////////////////////////
    // Methods
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Adds or subtracts the corresponding time field, does not perform any alignment.
     *
     * @param count     The quantity to change, can be negative.
     * @param timestamp The calendar to modify.
     * @return The timestamp parameter.
     */
    public static Calendar addDays(int count, Calendar timestamp) {
        timestamp.add(Calendar.DATE, count);
        return timestamp;
    }

    /**
     * Adds or subtracts the corresponding time field, does not perform any alignment.
     *
     * @param count     The quantity to change, can be negative.
     * @param timestamp The time to modify.
     * @return The adjusted time.
     */
    public static long addDays(int count, long timestamp) {
        Calendar cal = getCalendar(timestamp);
        addDays(count, cal);
        timestamp = cal.getTimeInMillis();
        recycle(cal);
        return timestamp;
    }

    /**
     * Adds or subtracts the corresponding time field, does not perform any alignment.
     *
     * @param count     The quantity to change, can be negative.
     * @param timestamp The calendar to modify.
     * @return The timestamp parameter.
     */
    public static Calendar addHours(int count, Calendar timestamp) {
        timestamp.add(Calendar.HOUR_OF_DAY, count);
        return timestamp;
    }

    /**
     * Adds or subtracts the corresponding time field, does not perform any alignment.
     *
     * @param count     The quantity to change, can be negative.
     * @param timestamp The time to modify.
     * @return The adjusted time.
     */
    public static long addHours(int count, long timestamp) {
        Calendar cal = getCalendar(timestamp);
        addHours(count, cal);
        timestamp = cal.getTimeInMillis();
        recycle(cal);
        return timestamp;
    }

    /**
     * Adds or subtracts the corresponding time field, does not perform any alignment.
     *
     * @param count     The quantity to change, can be negative.
     * @param timestamp The calendar to modify.
     * @return The timestamp parameter.
     */
    public static Calendar addMinutes(int count, Calendar timestamp) {
        timestamp.add(Calendar.MINUTE, count);
        return timestamp;
    }

    /**
     * Adds or subtracts the corresponding time field, does not perform any alignment.
     *
     * @param count     The quantity to change, can be negative.
     * @param timestamp The time to modify.
     * @return The adjusted time.
     */
    public static long addMinutes(int count, long timestamp) {
        Calendar cal = getCalendar(timestamp);
        addMinutes(count, cal);
        timestamp = cal.getTimeInMillis();
        recycle(cal);
        return timestamp;
    }

    /**
     * Adds or subtracts the corresponding time field, does not perform any alignment.
     *
     * @param count     The quantity to change, can be negative.
     * @param timestamp The calendar to modify.
     * @return The timestamp parameter.
     */
    public static Calendar addMonths(int count, Calendar timestamp) {
        timestamp.add(Calendar.MONTH, count);
        return timestamp;
    }

    /**
     * Adds or subtracts the corresponding time field, does not perform any alignment.
     *
     * @param count     The quantity to change, can be negative.
     * @param timestamp The time to modify.
     * @return The adjusted time.
     */
    public static long addMonths(int count, long timestamp) {
        Calendar cal = getCalendar(timestamp);
        addMonths(count, cal);
        timestamp = cal.getTimeInMillis();
        recycle(cal);
        return timestamp;
    }

    /**
     * Adds or subtracts the corresponding time field, does not perform any alignment.
     *
     * @param count     The quantity to change, can be negative.
     * @param timestamp The calendar to modify.
     * @return The timestamp parameter.
     */
    public static Calendar addSeconds(int count, Calendar timestamp) {
        timestamp.add(Calendar.SECOND, count);
        return timestamp;
    }

    /**
     * Adds or subtracts the corresponding time field, does not perform any alignment.
     *
     * @param count     The quantity to change, can be negative.
     * @param timestamp The time to modify.
     * @return The adjusted time.
     */
    public static long addSeconds(int count, long timestamp) {
        Calendar cal = getCalendar(timestamp);
        addSeconds(count, cal);
        timestamp = cal.getTimeInMillis();
        recycle(cal);
        return timestamp;
    }

    /**
     * Adds or subtracts the corresponding time field, does not perform any alignment.
     *
     * @param count     The quantity to change, can be negative.
     * @param timestamp The calendar to modify.
     * @return The timestamp parameter.
     */
    public static Calendar addWeeks(int count, Calendar timestamp) {
        return addDays(count * 7, timestamp);
    }

    /**
     * Adds or subtracts the corresponding time field, does not perform any alignment.
     *
     * @param count     The quantity to change, can be negative.
     * @param timestamp The time to modify.
     * @return The adjusted time.
     */
    public static long addWeeks(int count, long timestamp) {
        return addDays(count * 7, timestamp);
    }

    /**
     * Adds or subtracts the corresponding time field, does not perform any alignment.
     *
     * @param count     The quantity to change, can be negative.
     * @param timestamp The calendar to modify.
     * @return The timestamp parameter.
     */
    public static Calendar addYears(int count, Calendar timestamp) {
        timestamp.add(Calendar.YEAR, count);
        return timestamp;
    }

    /**
     * Adds or subtracts the corresponding time field, does not perform any alignment.
     *
     * @param count     The quantity to change, can be negative.
     * @param timestamp The time to modify.
     * @return The adjusted time.
     */
    public static long addYears(int count, long timestamp) {
        Calendar cal = getCalendar(timestamp);
        addYears(count, cal);
        timestamp = cal.getTimeInMillis();
        recycle(cal);
        return timestamp;
    }

    /**
     * Aligns the time fields to the start of the day.
     *
     * @param timestamp The calendar to align.
     * @return The parameter.
     */
    public static Calendar alignDay(Calendar timestamp) {
        timestamp.set(Calendar.HOUR_OF_DAY, 0);
        timestamp.set(Calendar.MINUTE, 0);
        timestamp.set(Calendar.SECOND, 0);
        timestamp.set(Calendar.MILLISECOND, 0);
        return timestamp;
    }

    /**
     * Aligns the time fields to the start of given interval.
     *
     * @param interval  The number of days in the interval to align to.
     * @param timestamp The calendar to align.
     * @return The calendar parameter, aligned.
     */
    public static Calendar alignDays(int interval, Calendar timestamp) {
        int value = timestamp.get(Calendar.DATE);
        value = value - (value % interval);
        timestamp.set(Calendar.DATE, value);
        return alignDay(timestamp);
    }

    /**
     * Aligns the time fields to the start of the hour.
     *
     * @param timestamp The calendar to align.
     * @return The parameter.
     */
    public static Calendar alignHour(Calendar timestamp) {
        timestamp.set(Calendar.MINUTE, 0);
        timestamp.set(Calendar.SECOND, 0);
        timestamp.set(Calendar.MILLISECOND, 0);
        return timestamp;
    }

    /**
     * Aligns the time fields to the start of given interval.
     *
     * @param interval  The number of hours in the interval to align to.
     * @param timestamp The calendar to align.
     * @return The calendar parameter, aligned.
     */
    public static Calendar alignHours(int interval, Calendar timestamp) {
        int value = timestamp.get(Calendar.HOUR_OF_DAY);
        value = value - (value % interval);
        timestamp.set(Calendar.HOUR_OF_DAY, value);
        return alignHour(timestamp);
    }

    /**
     * Aligns the time fields to the start of the minute.
     *
     * @param timestamp The calendar to align.
     * @return The parameter.
     */
    public static Calendar alignMinute(Calendar timestamp) {
        timestamp.set(Calendar.SECOND, 0);
        timestamp.set(Calendar.MILLISECOND, 0);
        return timestamp;
    }

    /**
     * Aligns the time fields to the start of given interval.
     *
     * @param interval  The number of minutes in the interval to align to.
     * @param timestamp The calendar to align.
     * @return The calendar parameter, aligned.
     */
    public static Calendar alignMinutes(int interval, Calendar timestamp) {
        int value = timestamp.get(Calendar.MINUTE);
        value = value - (value % interval);
        timestamp.set(Calendar.MINUTE, value);
        return alignMinute(timestamp);
    }

    /**
     * Aligns the time fields to the start of the month.
     *
     * @param timestamp The calendar to align.
     * @return The parameter.
     */
    public static Calendar alignMonth(Calendar timestamp) {
        timestamp.set(Calendar.DAY_OF_MONTH, 1);
        return alignDay(timestamp);
    }

    /**
     * Aligns the time fields to the start of the second.
     *
     * @param timestamp The calendar to align.
     * @return The parameter.
     */
    public static Calendar alignSecond(Calendar timestamp) {
        timestamp.set(Calendar.MILLISECOND, 0);
        return timestamp;
    }

    /**
     * Aligns the time fields to the start of given interval.
     *
     * @param interval  The number of seconds in the interval to align to.
     * @param timestamp The calendar to align.
     * @return The calendar parameter, aligned.
     */
    public static Calendar alignSeconds(int interval, Calendar timestamp) {
        int value = timestamp.get(Calendar.SECOND);
        value = value - (value % interval);
        timestamp.set(Calendar.SECOND, value);
        return alignSecond(timestamp);
    }

    /**
     * Aligns the time fields to the start of the week.
     *
     * @param timestamp The calendar to align.
     * @return The parameter.
     */
    public static Calendar alignWeek(Calendar timestamp) {
        timestamp = alignDay(timestamp);
        int dayOfWeek = timestamp.get(Calendar.DAY_OF_WEEK);
        int offset = 1 - dayOfWeek;
        if (offset == 0) {
            return timestamp;
        }
        return addDays(offset, timestamp);
    }

    /**
     * Aligns the time fields to the start of the year.
     *
     * @param timestamp The calendar to align.
     * @return The parameter.
     */
    public static Calendar alignYear(Calendar timestamp) {
        timestamp.set(Calendar.MONTH, 0);
        return alignMonth(timestamp);
    }

    /**
     * Converts the characters into an int.
     */
    private static int convertDigits(char tens, char ones) {
        return (toDigit(tens) * 10) + toDigit(ones);
    }

    /**
     * Converts the characters into an int.
     */
    private static int convertDigits(char thousands, char hundreds, char tens, char ones) {
        return toDigit(thousands) * 1000 +
                toDigit(hundreds) * 100 +
                toDigit(tens) * 10 +
                toDigit(ones);
    }

    /**
     * This is a convenience that uses reuses and recycles a calendar instance to get the time in
     * millis.
     */
    public static long decode(String timestamp) {
        Calendar cal = getCalendar();
        decode(timestamp, cal);
        long millis = cal.getTimeInMillis();
        recycle(cal);
        return millis;
    }

    /**
     * Converts a DSA encoded timestamp into a Java Calendar.  DSA encoding is based on ISO 8601 but
     * allows for an unspecified timezone.
     *
     * @param timestamp The encoded timestamp.
     * @param calendar  The instance to decodeKeys into and returnt, may be null.  If the timestamp
     *                  does not specify a timezone, the zone in this instance will be used.
     */
    public static Calendar decode(String timestamp, Calendar calendar) {
        if (calendar == null) {
            calendar = getCalendar();
        }
        try {
            char[] chars = timestamp.toCharArray();
            int idx = 0;
            int year = convertDigits(chars[idx++], chars[idx++], chars[idx++], chars[idx++]);
            validateChar(chars[idx++], '-');
            int month = convertDigits(chars[idx++], chars[idx++]) - 1;
            validateChar(chars[idx++], '-');
            int day = convertDigits(chars[idx++], chars[idx++]);
            validateChar(chars[idx++], 'T');
            int hour = convertDigits(chars[idx++], chars[idx++]);
            validateChar(chars[idx++], ':');
            int minute = convertDigits(chars[idx++], chars[idx++]);
            validateChar(chars[idx++], ':');
            int second = convertDigits(chars[idx++], chars[idx++]);
            int millis = 0;
            if ((chars.length > idx) && (chars[idx] == '.')) {
                idx++;
                millis = convertDigits('0', chars[idx++], chars[idx++], chars[idx++]);
            }
            //more than 3 millis digits is possible
            while ((chars.length > idx)
                    && (chars[idx] != 'Z')
                    && (chars[idx] != '+')
                    && (chars[idx] != '-')) {
                idx++;
            }
            // timezone offset sign
            if (idx < chars.length) {
                char sign = chars[idx++];
                if (sign == 'Z') {
                    calendar.setTimeZone(TimeZone.getTimeZone("GMT"));
                } else {
                    int tzOff;
                    if (sign != '+' && sign != '-') {
                        throw new Exception();
                    }
                    int hrOff = convertDigits(chars[idx++], chars[idx++]);
                    int minOff = 0;
                    //minutes are optional in 8601
                    if (idx < chars.length) { //minutes optional
                        validateChar(chars[idx++], ':');
                        minOff = convertDigits(chars[idx++], chars[idx++]);
                    }
                    tzOff = (hrOff * MILLIS_HOUR) + (minOff * MILLIS_MINUTE);
                    if (sign == '-') {
                        tzOff *= -1;
                    }
                    TimeZone timezone = calendar.getTimeZone();
                    int localOffset = timezone.getOffset(calendar.getTimeInMillis());
                    if (localOffset != tzOff) {
                        String timeZoneName = "Offset" + tzOff;
                        synchronized (timezones) {
                            timezone = timezones.get(timeZoneName);
                            if (timezone == null) {
                                timezone = new SimpleTimeZone(tzOff, timeZoneName);
                                timezones.put(timeZoneName, timezone);
                            }
                        }
                        calendar.setTimeZone(timezone);
                    }
                }
            }
            calendar.set(year, month, day, hour, minute, second);
            calendar.set(Calendar.MILLISECOND, millis);
        } catch (Exception x) {
            throw new IllegalArgumentException("Invalid timestamp: " + timestamp);
        }
        return calendar;
    }

    /**
     * Converts a Java Calendar into a DSA encoded timestamp.  DSA encoding is based on ISO 8601 but
     * allows the timezone offset to not be specified.
     *
     * @param timestamp      What to encode.
     * @param encodeTzOffset Whether or not to encode the timezone offset.
     * @return The buffer containing the encoding.
     */
    public static StringBuilder encode(long timestamp, boolean encodeTzOffset) {
        Calendar cal = getCalendar(timestamp);
        StringBuilder buf = encode(cal, encodeTzOffset, new StringBuilder());
        recycle(cal);
        return buf;
    }

    /**
     * Converts a Java Calendar into a DSA encoded timestamp.  DSA encoding is based on ISO 8601 but
     * allows the timezone offset to not be specified.
     *
     * @param timestamp      What to encode.
     * @param encodeTzOffset Whether or not to encode the timezone offset.
     * @param buf            The buffer to append the encoded timestamp and return value, can be
     *                       null.
     * @return The buffer containing the encoding.
     */
    public static StringBuilder encode(long timestamp, boolean encodeTzOffset, StringBuilder buf) {
        Calendar cal = getCalendar(timestamp);
        buf = encode(cal, encodeTzOffset, buf);
        recycle(cal);
        return buf;
    }

    /**
     * Converts a Java Calendar into a DSA encoded timestamp.  DSA encoding is based on ISO 8601 but
     * the timezone offset is optional.
     *
     * @param calendar       The calendar representing the timestamp to encode.
     * @param encodeTzOffset Whether or not to encode the timezone offset.
     * @param buf            The buffer to append the encoded timestamp and return value, can be
     *                       null.
     * @return The buf argument, or if that was null, a new StringBuilder.
     */
    public static StringBuilder encode(Calendar calendar,
                                       boolean encodeTzOffset,
                                       StringBuilder buf) {
        if (buf == null) {
            buf = new StringBuilder();
        }
        long millis = calendar.getTimeInMillis();
        int tmp = calendar.get(Calendar.YEAR);
        buf.append(tmp).append('-');
        //month
        tmp = calendar.get(Calendar.MONTH) + 1;
        if (tmp < 10) {
            buf.append('0');
        }
        buf.append(tmp).append('-');
        //date
        tmp = calendar.get(Calendar.DAY_OF_MONTH);
        if (tmp < 10) {
            buf.append('0');
        }
        buf.append(tmp).append('T');
        //hour
        tmp = calendar.get(Calendar.HOUR_OF_DAY);
        if (tmp < 10) {
            buf.append('0');
        }
        buf.append(tmp).append(':');
        //minute
        tmp = calendar.get(Calendar.MINUTE);
        if (tmp < 10) {
            buf.append('0');
        }
        buf.append(tmp).append(':');
        //second
        tmp = calendar.get(Calendar.SECOND);
        if (tmp < 10) {
            buf.append('0');
        }
        buf.append(tmp).append('.');
        //millis
        tmp = calendar.get(Calendar.MILLISECOND);
        if (tmp < 10) {
            buf.append('0');
        }
        if (tmp < 100) {
            buf.append('0');
        }
        buf.append(tmp);
        if (encodeTzOffset) {
            int offset = calendar.getTimeZone().getOffset(millis);
            if (offset == 0) {
                buf.append('Z');
            } else {
                int hrOff = Math.abs(offset / MILLIS_HOUR);
                int minOff = Math.abs((offset % MILLIS_HOUR) / MILLIS_MINUTE);
                if (offset < 0) {
                    buf.append('-');
                } else {
                    buf.append('+');
                }
                if (hrOff < 10) {
                    buf.append('0');
                }
                buf.append(hrOff);
                buf.append(':');
                if (minOff < 10) {
                    buf.append('0');
                }
                buf.append(minOff);
            }
        }
        return buf;
    }

    /**
     * Converts a Java Calendar into a number safe for file names: YYMMDDHHMMSS. If seconds align to
     * 00, then they will be omitted.  DSTime.alignMinutes can be used to achieve that.
     *
     * @param calendar The calendar representing the timestamp to encode.
     * @param buf      The buffer to append the encoded timestamp and return, can be null.
     * @return The buf argument, or if that was null, a new StringBuilder.
     */
    public static StringBuilder encodeForFiles(Calendar calendar, StringBuilder buf) {
        if (buf == null) {
            buf = new StringBuilder();
        }
        int tmp = calendar.get(Calendar.YEAR) % 100;
        if (tmp < 10) {
            buf.append('0');
        }
        buf.append(tmp);
        //month
        tmp = calendar.get(Calendar.MONTH) + 1;
        if (tmp < 10) {
            buf.append('0');
        }
        buf.append(tmp);
        //date
        tmp = calendar.get(Calendar.DAY_OF_MONTH);
        if (tmp < 10) {
            buf.append('0');
        }
        buf.append(tmp).append('-');
        //hour
        tmp = calendar.get(Calendar.HOUR_OF_DAY);
        if (tmp < 10) {
            buf.append('0');
        }
        buf.append(tmp);
        //minute
        tmp = calendar.get(Calendar.MINUTE);
        if (tmp < 10) {
            buf.append('0');
        }
        buf.append(tmp);
        //second
        tmp = calendar.get(Calendar.SECOND);
        if (tmp > 0) {
            if (tmp < 10) {
                buf.append('0');
            }
            buf.append(tmp);
        }
        return buf;
    }

    /**
     * Converts a Java Calendar into a shorter human readable timestamp for use in logging files.
     *
     * @param calendar The calendar representing the timestamp to encode.
     * @param buf      The buffer to append the encoded timestamp and return, can be null.
     * @return The buf argument, or if that was null, a new StringBuilder.
     */
    public static StringBuilder encodeForLogs(Calendar calendar, StringBuilder buf) {
        if (buf == null) {
            buf = new StringBuilder();
        }
        int tmp = calendar.get(Calendar.YEAR);
        buf.append(tmp).append('-');
        //month
        tmp = calendar.get(Calendar.MONTH) + 1;
        if (tmp < 10) {
            buf.append('0');
        }
        buf.append(tmp).append('-');
        //date
        tmp = calendar.get(Calendar.DAY_OF_MONTH);
        if (tmp < 10) {
            buf.append('0');
        }
        buf.append(tmp).append(' ');
        //hour
        tmp = calendar.get(Calendar.HOUR_OF_DAY);
        if (tmp < 10) {
            buf.append('0');
        }
        buf.append(tmp).append(':');
        //minute
        tmp = calendar.get(Calendar.MINUTE);
        if (tmp < 10) {
            buf.append('0');
        }
        buf.append(tmp).append(':');
        //second
        tmp = calendar.get(Calendar.SECOND);
        if (tmp < 10) {
            buf.append('0');
        }
        buf.append(tmp);
        buf.append(tmp).append(':');
        tmp = calendar.get(Calendar.MILLISECOND);
        if (tmp < 100) {
            buf.append('0');
        }
        if (tmp < 10) {
            buf.append('0');
        }
        buf.append(tmp);
        return buf;
    }

    /**
     * Attempts to reuse a calendar instance, the timezone will be set to TimeZone.getDefault().
     */
    public static Calendar getCalendar() {
        Calendar cal = null;
        synchronized (DSTime.class) {
            if (calendarCache1 != null) {
                cal = calendarCache1;
                calendarCache1 = null;
            } else if (calendarCache2 != null) {
                cal = calendarCache2;
                calendarCache2 = null;
            }
        }
        if (cal == null) {
            cal = Calendar.getInstance();
        } else {
            cal.setTimeZone(TimeZone.getDefault());
        }
        return cal;
    }

    /**
     * Attempts to reuse a calendar instance and sets the time in millis to the argument and the
     * timezone to TimeZone.getDefault().
     */
    public static Calendar getCalendar(long timestamp) {
        Calendar cal = getCalendar();
        cal.setTimeInMillis(timestamp);
        return cal;
    }

    public static long millisToNanos(long millis) {
        return millis * NANOS_IN_MS;
    }

    public static long nanosToMillis(long nanos) {
        return nanos / NANOS_IN_MS;
    }

    /**
     * Return a calendar instance for reuse.
     */
    public static void recycle(Calendar cal) {
        synchronized (DSTime.class) {
            if (calendarCache1 == null) {
                calendarCache1 = cal;
            } else {
                calendarCache2 = cal;
            }
        }
    }

    /**
     * Converts the character to a digit, throws an IllegalStateException if it isn't a valid
     * digit.
     */
    private static int toDigit(char ch) {
        if (('0' <= ch) && (ch <= '9')) {
            return ch - '0';
        }
        throw new IllegalStateException();
    }

    /**
     * Used for decoding timestamp, throws an IllegalStateException if the two characters are not
     * equal.
     */
    private static void validateChar(char c1, char c2) {
        if (c1 != c2) {
            throw new IllegalStateException();
        }
    }


}
