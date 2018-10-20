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

    /**
     * The current time.
     */
    public static DSDateTime currentTime() {
        return new DSDateTime(System.currentTimeMillis());
    }

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

    /**
     * Defaults to the equals method.
     */
    @Override
    public boolean isEqual(Object obj) {
        return equals(obj);
    }

    @Override
    public boolean isNull() {
        return this == NULL;
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
