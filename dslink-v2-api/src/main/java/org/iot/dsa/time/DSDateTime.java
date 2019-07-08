package org.iot.dsa.time;

import java.util.Calendar;
import java.util.TimeZone;
import org.iot.dsa.node.DSElement;
import org.iot.dsa.node.DSRegistry;
import org.iot.dsa.node.DSString;
import org.iot.dsa.node.DSValue;
import org.iot.dsa.node.DSValueType;

/**
 * Wrapper for Java time.
 *
 * @author Aaron Hansen
 */
public class DSDateTime extends DSValue {

    ///////////////////////////////////////////////////////////////////////////
    // Constants
    ///////////////////////////////////////////////////////////////////////////

    public static final DSDateTime NULL = new DSDateTime("null", 0l);

    ///////////////////////////////////////////////////////////////////////////
    // Fields
    ///////////////////////////////////////////////////////////////////////////

    private Long millis;
    private String string;
    private TimeZone timeZone;

    ///////////////////////////////////////////////////////////////////////////
    // Constructors
    ///////////////////////////////////////////////////////////////////////////

    private DSDateTime(long millis) {
        this.millis = millis;
    }

    private DSDateTime(long millis, TimeZone timeZone) {
        this.millis = millis;
        this.timeZone = timeZone;
    }

    DSDateTime(String string) {
        this.string = string;
        Calendar cal = DSTime.decode(string, null);
        this.millis = cal.getTimeInMillis();
        this.timeZone = cal.getTimeZone();
        DSTime.recycle(cal);
    }

    DSDateTime(String string, Long millis) {
        this.string = string;
        this.millis = millis;
    }

    ///////////////////////////////////////////////////////////////////////////
    // Methods in alphabetical order
    ///////////////////////////////////////////////////////////////////////////

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof DSDateTime) {
            DSDateTime dt = (DSDateTime) obj;
            return dt.timeInMillis() == timeInMillis();
        }
        return false;
    }

    public TimeZone getTimeZone() {
        if (timeZone == null) {
            return TimeZone.getDefault();
        }
        return timeZone;
    }

    /**
     * String.
     */
    @Override
    public DSValueType getValueType() {
        return DSValueType.STRING;
    }

    @Override
    public int hashCode() {
        return millis.hashCode();
    }

    public boolean isAfter(DSDateTime dt) {
        return timeInMillis() > dt.timeInMillis();
    }

    public boolean isBefore(DSDateTime dt) {
        return timeInMillis() < dt.timeInMillis();
    }

    @Override
    public boolean isNull() {
        return this == NULL;
    }

    public DSDateTime nextDay() {
        Calendar cal = DSTime.getCalendar(timeInMillis(), getTimeZone());
        DSTime.addDays(1, cal);
        DSDateTime ret = DSDateTime.valueOf(cal);
        DSTime.recycle(cal);
        return ret;
    }

    public DSDateTime nextHour() {
        Calendar cal = DSTime.getCalendar(timeInMillis(), getTimeZone());
        DSTime.addHours(1, cal);
        DSDateTime ret = DSDateTime.valueOf(cal);
        DSTime.recycle(cal);
        return ret;
    }

    public DSDateTime nextMonth() {
        Calendar cal = DSTime.getCalendar(timeInMillis(), getTimeZone());
        DSTime.addMonths(1, cal);
        DSDateTime ret = DSDateTime.valueOf(cal);
        DSTime.recycle(cal);
        return ret;
    }

    public DSDateTime nextWeek() {
        Calendar cal = DSTime.getCalendar(timeInMillis(), getTimeZone());
        DSTime.addWeeks(1, cal);
        DSDateTime ret = DSDateTime.valueOf(cal);
        DSTime.recycle(cal);
        return ret;
    }

    public DSDateTime nextYear() {
        Calendar cal = DSTime.getCalendar(timeInMillis(), getTimeZone());
        DSTime.addYears(1, cal);
        DSDateTime ret = DSDateTime.valueOf(cal);
        DSTime.recycle(cal);
        return ret;
    }

    /**
     * The current time.
     */
    public static DSDateTime now() {
        return new DSDateTime(System.currentTimeMillis());
    }

    public DSDateTime prevDay() {
        Calendar cal = DSTime.getCalendar(timeInMillis(), getTimeZone());
        DSTime.addDays(-1, cal);
        DSDateTime ret = DSDateTime.valueOf(cal);
        DSTime.recycle(cal);
        return ret;
    }

    public DSDateTime prevHour() {
        Calendar cal = DSTime.getCalendar(timeInMillis(), getTimeZone());
        DSTime.addHours(-1, cal);
        DSDateTime ret = DSDateTime.valueOf(cal);
        DSTime.recycle(cal);
        return ret;
    }

    public DSDateTime prevMonth() {
        Calendar cal = DSTime.getCalendar(timeInMillis(), getTimeZone());
        DSTime.addMonths(-1, cal);
        DSDateTime ret = DSDateTime.valueOf(cal);
        DSTime.recycle(cal);
        return ret;
    }

    public DSDateTime prevWeek() {
        Calendar cal = DSTime.getCalendar(timeInMillis(), getTimeZone());
        DSTime.addWeeks(-1, cal);
        DSDateTime ret = DSDateTime.valueOf(cal);
        DSTime.recycle(cal);
        return ret;
    }

    public DSDateTime prevYear() {
        Calendar cal = DSTime.getCalendar(timeInMillis(), getTimeZone());
        DSTime.addYears(-1, cal);
        DSDateTime ret = DSDateTime.valueOf(cal);
        DSTime.recycle(cal);
        return ret;
    }

    public DSDateTime startOfDay() {
        long cur = timeInMillis();
        Calendar cal = DSTime.getCalendar(timeInMillis(), getTimeZone());
        DSTime.alignDay(cal);
        long neu = cal.getTimeInMillis();
        if (cur == neu) {
            DSTime.recycle(cal);
            return this;
        }
        DSDateTime ret = DSDateTime.valueOf(cal);
        DSTime.recycle(cal);
        return ret;
    }

    public DSDateTime startOfHour() {
        long cur = timeInMillis();
        Calendar cal = DSTime.getCalendar(timeInMillis(), getTimeZone());
        DSTime.alignHour(cal);
        long neu = cal.getTimeInMillis();
        if (cur == neu) {
            DSTime.recycle(cal);
            return this;
        }
        DSDateTime ret = DSDateTime.valueOf(cal);
        DSTime.recycle(cal);
        return ret;
    }

    public DSDateTime startOfMinute() {
        long cur = timeInMillis();
        Calendar cal = DSTime.getCalendar(timeInMillis(), getTimeZone());
        DSTime.alignMinute(cal);
        long neu = cal.getTimeInMillis();
        if (cur == neu) {
            DSTime.recycle(cal);
            return this;
        }
        DSDateTime ret = DSDateTime.valueOf(cal);
        DSTime.recycle(cal);
        return ret;
    }

    public DSDateTime startOfMonth() {
        long cur = timeInMillis();
        Calendar cal = DSTime.getCalendar(timeInMillis(), getTimeZone());
        DSTime.alignMonth(cal);
        long neu = cal.getTimeInMillis();
        if (cur == neu) {
            DSTime.recycle(cal);
            return this;
        }
        DSDateTime ret = DSDateTime.valueOf(cal);
        DSTime.recycle(cal);
        return ret;
    }

    public DSDateTime startOfWeek() {
        long cur = timeInMillis();
        Calendar cal = DSTime.getCalendar(timeInMillis(), getTimeZone());
        DSTime.alignWeek(cal);
        long neu = cal.getTimeInMillis();
        if (cur == neu) {
            DSTime.recycle(cal);
            return this;
        }
        DSDateTime ret = DSDateTime.valueOf(cal);
        DSTime.recycle(cal);
        return ret;
    }

    public DSDateTime startOfYear() {
        long cur = timeInMillis();
        Calendar cal = DSTime.getCalendar(timeInMillis(), getTimeZone());
        DSTime.alignYear(cal);
        long neu = cal.getTimeInMillis();
        if (cur == neu) {
            DSTime.recycle(cal);
            return this;
        }
        DSDateTime ret = DSDateTime.valueOf(cal);
        DSTime.recycle(cal);
        return ret;
    }

    /**
     * The Java time represented by this object.
     */
    public long timeInMillis() {
        if (millis == null) {
            millis = DSTime.decode(string);
        }
        return millis;
    }

    @Override
    public DSElement toElement() {
        return DSString.valueOf(toString());
    }

    /**
     * ISO 8601 standard format of "yyyy-mm-ddThh:mm:ss.mmm[+/-]hh:mm".
     */
    @Override
    public String toString() {
        if (string == null) {
            string = DSTime.encode(millis, true).toString();
        }
        return string;
    }

    @Override
    public DSDateTime valueOf(DSElement element) {
        if ((element == null) || element.isNull()) {
            return NULL;
        }
        return valueOf(element.toString());
    }

    /**
     * Create a DSDateTime for the given time fields.
     *
     * @param month 1-12
     * @param day   1-31
     * @return Value representing the given time fields.
     */
    public static DSDateTime valueOf(int year, int month, int day) {
        return valueOf(year, month, day, 0, 0, 0);
    }

    /**
     * Create a DSDateTime for the given time fields.
     *
     * @param month 1-12
     * @param day   1-31
     * @param tz    Optional
     * @return Value representing the given time fields.
     */
    public static DSDateTime valueOf(int year, int month, int day, TimeZone tz) {
        return valueOf(year, month, day, 0, 0, 0, tz);
    }

    /**
     * Create a DSDateTime for the given time fields.
     *
     * @param month 1-12
     * @param day   1-31
     * @param hour  0-23
     * @return Value representing the given time fields.
     */
    public static DSDateTime valueOf(int year, int month, int day, int hour) {
        return valueOf(year, month, day, hour, 0, 0);
    }

    /**
     * Create a DSDateTime for the given time fields.
     *
     * @param month 1-12
     * @param day   1-31
     * @param hour  0-23
     * @param tz    Optional
     * @return Value representing the given time fields.
     */
    public static DSDateTime valueOf(int year, int month, int day, int hour, TimeZone tz) {
        return valueOf(year, month, day, hour, 0, 0, tz);
    }

    /**
     * Create a DSDateTime for the given time fields.
     *
     * @param month 1-12
     * @param day   1-31
     * @param hour  0-23
     * @param min   0-59
     * @return Value representing the given time fields.
     */
    public static DSDateTime valueOf(int year, int month, int day, int hour, int min) {
        return valueOf(year, month, day, hour, min, 0);
    }

    /**
     * Create a DSDateTime for the given time fields.
     *
     * @param month 1-12
     * @param day   1-31
     * @param hour  0-23
     * @param min   0-59
     * @param tz    Optional
     * @return Value representing the given time fields.
     */
    public static DSDateTime valueOf(int year, int month, int day, int hour, int min,
                                     TimeZone tz) {
        return valueOf(year, month, day, hour, min, 0, tz);
    }

    /**
     * Create a DSDateTime for the given time fields.
     *
     * @param month 1-12
     * @param day   1-31
     * @param hour  0-23
     * @param min   0-59
     * @param sec   0-59
     * @return Value representing the given time fields.
     */
    public static DSDateTime valueOf(int year, int month, int day, int hour, int min, int sec) {
        return valueOf(year, month, day, hour, min, sec, TimeZone.getDefault());
    }

    /**
     * Create a DSDateTime for the given time fields.
     *
     * @param month 1-12
     * @param day   1-31
     * @param hour  0-23
     * @param min   0-59
     * @param sec   0-59
     * @param tz    Optional
     * @return Value representing the given time fields.
     */
    public static DSDateTime valueOf(int year, int month, int day, int hour, int min, int sec,
                                     TimeZone tz) {
        Calendar cal = DSTime.getCalendar();
        if (tz != null) {
            cal.setTimeZone(tz);
        }
        cal.set(year, --month, day, hour, min, sec);
        cal.set(Calendar.MILLISECOND, 0);
        DSDateTime ret = new DSDateTime(cal.getTimeInMillis(), tz);
        DSTime.recycle(cal);
        return ret;
    }

    /**
     * Creates a DSDateTime for the given calendar.
     */
    public static DSDateTime valueOf(Calendar calendar) {
        return new DSDateTime(calendar.getTimeInMillis(), calendar.getTimeZone());
    }

    /**
     * Creates a DSDateTime for the given Java time.
     */
    public static DSDateTime valueOf(long millis) {
        return new DSDateTime(millis);
    }

    /**
     * Creates a DSDateTime for the given Java time and timezone.
     */
    public static DSDateTime valueOf(long millis, TimeZone tz) {
        return new DSDateTime(millis, tz);
    }

    /**
     * Decodes an ISO 8601 standard format of "yyyy-mm-ddThh:mm:ss.mmm[+/-]hh:mm".
     */
    public static DSDateTime valueOf(String string) {
        if (string == null) {
            return NULL;
        }
        if ("null".equals(string)) {
            return NULL;
        }
        return new DSDateTime(string);
    }

    ///////////////////////////////////////////////////////////////////////////
    // Inner Classes
    ///////////////////////////////////////////////////////////////////////////

    ///////////////////////////////////////////////////////////////////////////
    // Initialization
    ///////////////////////////////////////////////////////////////////////////

    static {
        DSRegistry.registerDecoder(DSDateTime.class, NULL);
    }

}
