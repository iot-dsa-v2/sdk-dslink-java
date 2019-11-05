package org.iot.dsa.time;

import java.util.Calendar;
import java.util.TimeZone;
import org.iot.dsa.dslink.ActionResults;
import org.iot.dsa.node.DSElement;
import org.iot.dsa.node.DSInfo;
import org.iot.dsa.node.DSLong;
import org.iot.dsa.node.DSMap;
import org.iot.dsa.node.DSMetadata;
import org.iot.dsa.node.DSRegistry;
import org.iot.dsa.node.DSString;
import org.iot.dsa.node.DSValue;
import org.iot.dsa.node.DSValueType;
import org.iot.dsa.node.action.DSAction;
import org.iot.dsa.node.action.DSIActionRequest;
import org.iot.dsa.node.action.DSISetAction;

/**
 * Wrapper for Java time.
 *
 * @author Aaron Hansen
 */
public class DSDateTime extends DSValue implements DSISetAction {

    ///////////////////////////////////////////////////////////////////////////
    // Constants
    ///////////////////////////////////////////////////////////////////////////

    public static final DSDateTime NULL = new DSDateTime("null", 0L);

    public static final String YEAR = "Year";
    public static final String MONTH = "Month";
    public static final String DAY = "Day";
    public static final String HOUR = "Hour";
    public static final String MINUTE = "Minute";
    public static final String SECOND = "Second";
    public static final String TIMEZONE = "Timezone";

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
        Calendar cal = Time.decode(string, null);
        this.millis = cal.getTimeInMillis();
        this.timeZone = cal.getTimeZone();
        Time.recycle(cal);
    }

    DSDateTime(String string, Long millis) {
        this.string = string;
        this.millis = millis;
    }

    ///////////////////////////////////////////////////////////////////////////
    // Methods in alphabetical order
    ///////////////////////////////////////////////////////////////////////////

    public DSDateTime apply(DSDuration duration) {
        return duration.apply(this);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof DSDateTime) {
            DSDateTime dt = (DSDateTime) obj;
            return dt.timeInMillis() == timeInMillis();
        }
        return false;
    }

    /**
     * 1-31
     */
    public int getDay() {
        Calendar c = Time.getCalendar(millis);
        int ret = Time.getDay(c);
        Time.recycle(c);
        return ret;
    }

    /**
     * 0-23
     */
    public int getHour() {
        Calendar c = Time.getCalendar(millis);
        int ret = Time.getHour(c);
        Time.recycle(c);
        return ret;
    }

    /**
     * 0-59
     */
    public int getMinute() {
        Calendar c = Time.getCalendar(millis);
        int ret = Time.getMinute(c);
        Time.recycle(c);
        return ret;
    }

    /**
     * 1-12
     */
    public int getMonth() {
        Calendar c = Time.getCalendar(millis);
        int ret = Time.getMonth(c);
        Time.recycle(c);
        return ret;
    }

    /**
     * 0-59
     */
    public int getSecond() {
        Calendar c = Time.getCalendar(millis);
        int ret = Time.getSecond(c);
        Time.recycle(c);
        return ret;
    }

    @Override
    public DSAction getSetAction() {
        return SetAction.INSTANCE;
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

    public int getYear() {
        Calendar c = Time.getCalendar(millis);
        int ret = Time.getYear(c);
        Time.recycle(c);
        return ret;
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
        Calendar cal = Time.getCalendar(timeInMillis(), getTimeZone());
        Time.addDays(1, cal);
        DSDateTime ret = DSDateTime.valueOf(cal);
        Time.recycle(cal);
        return ret;
    }

    public DSDateTime nextHour() {
        Calendar cal = Time.getCalendar(timeInMillis(), getTimeZone());
        Time.addHours(1, cal);
        DSDateTime ret = DSDateTime.valueOf(cal);
        Time.recycle(cal);
        return ret;
    }

    public DSDateTime nextMonth() {
        Calendar cal = Time.getCalendar(timeInMillis(), getTimeZone());
        Time.addMonths(1, cal);
        DSDateTime ret = DSDateTime.valueOf(cal);
        Time.recycle(cal);
        return ret;
    }

    public DSDateTime nextWeek() {
        Calendar cal = Time.getCalendar(timeInMillis(), getTimeZone());
        Time.addWeeks(1, cal);
        DSDateTime ret = DSDateTime.valueOf(cal);
        Time.recycle(cal);
        return ret;
    }

    public DSDateTime nextYear() {
        Calendar cal = Time.getCalendar(timeInMillis(), getTimeZone());
        Time.addYears(1, cal);
        DSDateTime ret = DSDateTime.valueOf(cal);
        Time.recycle(cal);
        return ret;
    }

    /**
     * The current time.
     */
    public static DSDateTime now() {
        return new DSDateTime(System.currentTimeMillis(), DSTimezone.DEFAULT.getTimeZone());
    }

    public DSDateTime prevDay() {
        Calendar cal = Time.getCalendar(timeInMillis(), getTimeZone());
        Time.addDays(-1, cal);
        DSDateTime ret = DSDateTime.valueOf(cal);
        Time.recycle(cal);
        return ret;
    }

    public DSDateTime prevHour() {
        Calendar cal = Time.getCalendar(timeInMillis(), getTimeZone());
        Time.addHours(-1, cal);
        DSDateTime ret = DSDateTime.valueOf(cal);
        Time.recycle(cal);
        return ret;
    }

    public DSDateTime prevMonth() {
        Calendar cal = Time.getCalendar(timeInMillis(), getTimeZone());
        Time.addMonths(-1, cal);
        DSDateTime ret = DSDateTime.valueOf(cal);
        Time.recycle(cal);
        return ret;
    }

    public DSDateTime prevWeek() {
        Calendar cal = Time.getCalendar(timeInMillis(), getTimeZone());
        Time.addWeeks(-1, cal);
        DSDateTime ret = DSDateTime.valueOf(cal);
        Time.recycle(cal);
        return ret;
    }

    public DSDateTime prevYear() {
        Calendar cal = Time.getCalendar(timeInMillis(), getTimeZone());
        Time.addYears(-1, cal);
        DSDateTime ret = DSDateTime.valueOf(cal);
        Time.recycle(cal);
        return ret;
    }

    public DSDateTime startOfDay() {
        long cur = timeInMillis();
        Calendar cal = Time.getCalendar(timeInMillis(), getTimeZone());
        Time.alignDay(cal);
        long neu = cal.getTimeInMillis();
        if (cur == neu) {
            Time.recycle(cal);
            return this;
        }
        DSDateTime ret = DSDateTime.valueOf(cal);
        Time.recycle(cal);
        return ret;
    }

    public DSDateTime startOfHour() {
        long cur = timeInMillis();
        Calendar cal = Time.getCalendar(timeInMillis(), getTimeZone());
        Time.alignHour(cal);
        long neu = cal.getTimeInMillis();
        if (cur == neu) {
            Time.recycle(cal);
            return this;
        }
        DSDateTime ret = DSDateTime.valueOf(cal);
        Time.recycle(cal);
        return ret;
    }

    public DSDateTime startOfMinute() {
        long cur = timeInMillis();
        Calendar cal = Time.getCalendar(timeInMillis(), getTimeZone());
        Time.alignMinute(cal);
        long neu = cal.getTimeInMillis();
        if (cur == neu) {
            Time.recycle(cal);
            return this;
        }
        DSDateTime ret = DSDateTime.valueOf(cal);
        Time.recycle(cal);
        return ret;
    }

    public DSDateTime startOfMonth() {
        long cur = timeInMillis();
        Calendar cal = Time.getCalendar(timeInMillis(), getTimeZone());
        Time.alignMonth(cal);
        long neu = cal.getTimeInMillis();
        if (cur == neu) {
            Time.recycle(cal);
            return this;
        }
        DSDateTime ret = DSDateTime.valueOf(cal);
        Time.recycle(cal);
        return ret;
    }

    public DSDateTime startOfWeek() {
        long cur = timeInMillis();
        Calendar cal = Time.getCalendar(timeInMillis(), getTimeZone());
        Time.alignWeek(cal);
        long neu = cal.getTimeInMillis();
        if (cur == neu) {
            Time.recycle(cal);
            return this;
        }
        DSDateTime ret = DSDateTime.valueOf(cal);
        Time.recycle(cal);
        return ret;
    }

    public DSDateTime startOfYear() {
        long cur = timeInMillis();
        Calendar cal = Time.getCalendar(timeInMillis(), getTimeZone());
        Time.alignYear(cal);
        long neu = cal.getTimeInMillis();
        if (cur == neu) {
            Time.recycle(cal);
            return this;
        }
        DSDateTime ret = DSDateTime.valueOf(cal);
        Time.recycle(cal);
        return ret;
    }

    /**
     * The Java time represented by this object.
     */
    public long timeInMillis() {
        if (millis == null) {
            millis = Time.decode(string);
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
            if (timeZone != null) {
                string = Time.encode(millis, timeZone).toString();
            } else {
                string = Time.encode(millis, true).toString();
            }
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
        Calendar cal = Time.getCalendar();
        if (tz != null) {
            cal.setTimeZone(tz);
        }
        cal.set(year, --month, day, hour, min, sec);
        cal.set(Calendar.MILLISECOND, 0);
        DSDateTime ret = new DSDateTime(cal.getTimeInMillis(), tz);
        Time.recycle(cal);
        return ret;
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
                                     DSTimezone tz) {
        return valueOf(year, month, day, hour, min, sec, tz.getTimeZone());
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

    public static class SetAction extends DSAction {

        public static final SetAction INSTANCE = new SetAction();

        @Override
        public ActionResults invoke(DSIActionRequest request) {
            DSMap params = request.getParameters();
            int year = params.get(YEAR, 1970);
            int month = params.get(MONTH, 0);
            int day = params.get(DAY, 0);
            int hr = params.get(HOUR, 0);
            int min = params.get(MINUTE, 0);
            int sec = params.get(SECOND, 0);
            DSTimezone tz = DSTimezone.NULL.valueOf(params.get(TIMEZONE));
            DSInfo<?> info = request.getTargetInfo();
            info.getParent().put(info, valueOf(year, month, day, hr, min, sec, tz));
            return null;
        }

        @Override
        public void prepareParameter(DSInfo<?> target, DSMap parameter) {
            DSDateTime dt = (DSDateTime) target.get();
            String name = parameter.get(DSMetadata.NAME, "");
            switch (name) {
                case YEAR:
                    parameter.put(DSMetadata.DEFAULT, dt.getYear());
                    break;
                case MONTH:
                    parameter.put(DSMetadata.DEFAULT, dt.getMonth());
                    break;
                case DAY:
                    parameter.put(DSMetadata.DEFAULT, dt.getDay());
                    break;
                case HOUR:
                    parameter.put(DSMetadata.DEFAULT, dt.getHour());
                    break;
                case MINUTE:
                    parameter.put(DSMetadata.DEFAULT, dt.getMinute());
                    break;
                case SECOND:
                    parameter.put(DSMetadata.DEFAULT, dt.getSecond());
                    break;
                case TIMEZONE:
                    if (dt.timeZone != null) {
                        DSTimezone tz = DSTimezone.valueOf(dt.getTimeZone());
                        if ((tz != null) && !tz.isDefault()) {
                            parameter.put(DSMetadata.DEFAULT, tz.toElement());
                        }
                    }
                    break;
            }
        }

        {
            DSLong zero = DSLong.valueOf(0);
            addParameter(YEAR, zero, null);
            addParameter(MONTH, zero, null);
            addParameter(DAY, zero, null);
            addParameter(HOUR, zero, null);
            addParameter(MINUTE, zero, null);
            addParameter(SECOND, zero, null);
            addParameter(TIMEZONE, DSTimezone.ALL_ZONES, "For example: America/Los_Angeles");
        }

    }

    ///////////////////////////////////////////////////////////////////////////
    // Initialization
    ///////////////////////////////////////////////////////////////////////////

    static {
        DSRegistry.registerDecoder(DSDateTime.class, NULL);
    }

}
