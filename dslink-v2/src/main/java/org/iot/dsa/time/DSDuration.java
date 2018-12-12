package org.iot.dsa.time;

import java.util.Calendar;
import org.iot.dsa.node.DSElement;
import org.iot.dsa.node.DSRegistry;
import org.iot.dsa.node.DSString;
import org.iot.dsa.node.DSValue;
import org.iot.dsa.node.DSValueType;

/**
 * XML Schema compliant relative amount of time represented as a number of years, months, days,
 * hours, minutes, and seconds. The String format is -PnYnMnDTnHnMnS.
 *
 * @author Aaron Hansen
 */
public class DSDuration extends DSValue {

    /////////////////////////////////////////////////////////////////
    // Class Fields
    /////////////////////////////////////////////////////////////////

    public static DSDuration NULL = new DSDuration();
    public static DSDuration DEFAULT = new DSDuration();

    /////////////////////////////////////////////////////////////////
    // Instance Fields
    /////////////////////////////////////////////////////////////////

    private int days = 0;
    private int hours = 0;
    private int millis = 0;
    private int minutes = 0;
    private int months = 0;
    private boolean negative = false;
    private int seconds = 0;
    private DSString string;
    private int years = 0;

    /////////////////////////////////////////////////////////////////
    // Constructors
    /////////////////////////////////////////////////////////////////

    private DSDuration() {
    }

    /////////////////////////////////////////////////////////////////
    // Public Methods
    /////////////////////////////////////////////////////////////////

    /**
     * Applies the duration to the given calendar and returns it.
     */
    public Calendar apply(Calendar cal) {
        if (negative) {
            if (years > 0) {
                DSTime.addYears(-years, cal);
            }
            if (months > 0) {
                DSTime.addMonths(-months, cal);
            }
            if (days > 0) {
                DSTime.addDays(-days, cal);
            }
            if (hours > 0) {
                DSTime.addHours(-hours, cal);
            }
            if (minutes > 0) {
                DSTime.addMinutes(-minutes, cal);
            }
            if (seconds > 0) {
                DSTime.addSeconds(-seconds, cal);
            }
            if (millis > 0) {
                DSTime.addMillis(-millis, cal);
            }
        } else {
            if (years > 0) {
                DSTime.addYears(years, cal);
            }
            if (months > 0) {
                DSTime.addMonths(months, cal);
            }
            if (days > 0) {
                DSTime.addDays(days, cal);
            }
            if (hours > 0) {
                DSTime.addHours(hours, cal);
            }
            if (minutes > 0) {
                DSTime.addMinutes(minutes, cal);
            }
            if (seconds > 0) {
                DSTime.addSeconds(seconds, cal);
            }
            if (millis > 0) {
                DSTime.addMillis(millis, cal);
            }
        }
        return cal;
    }

    /**
     * Returns a new DSDateTime instance with the duration applied to the parameter.
     */
    public DSDateTime apply(DSDateTime timestamp) {
        return DSDateTime.valueOf(apply(timestamp.timeInMillis()));
    }

    /**
     * Applies the duration to the given calendar and returns it.
     */
    public long apply(long timestamp) {
        Calendar cal = DSTime.getCalendar(timestamp);
        apply(cal);
        timestamp = cal.getTimeInMillis();
        DSTime.recycle(cal);
        return timestamp;
    }

    @Override
    public DSDuration copy() {
        DSDuration ret = new DSDuration();
        ret.negative = negative;
        ret.years = years;
        ret.months = months;
        ret.days = days;
        ret.hours = hours;
        ret.minutes = minutes;
        ret.seconds = seconds;
        ret.millis = millis;
        return ret;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof DSDuration) {
            DSDuration d = (DSDuration) obj;
            return (d.negative == negative)
                    && (d.millis == millis)
                    && (d.seconds == seconds)
                    && (d.minutes == minutes)
                    && (d.hours == hours)
                    && (d.days == days)
                    && (d.months == months)
                    && (d.years == years);
        }
        return false;
    }

    public int getDays() {
        return days;
    }

    public int getHours() {
        return hours;
    }

    public int getMillis() {
        return millis;
    }

    public int getMinutes() {
        return minutes;
    }

    public int getMonths() {
        return months;
    }

    public int getSeconds() {
        return seconds;
    }

    /**
     * String.
     */
    @Override
    public DSValueType getValueType() {
        return DSValueType.STRING;
    }

    public int getYears() {
        return years;
    }

    @Override
    public int hashCode() {
        return toString().hashCode();
    }

    /**
     * Defaults to the equals method.
     */
    @Override
    public boolean isEqual(Object obj) {
        return equals(obj);
    }

    public boolean isNegative() {
        return negative;
    }

    @Override
    public boolean isNull() {
        return this == NULL;
    }

    @Override
    public DSElement toElement() {
        if (string == null) {
            toString();
        }
        return string;
    }

    /**
     * String representation of this duration.
     */
    @Override
    public String toString() {
        if (string != null) {
            return string.toString();
        }
        if (this == NULL) {
            return "null";
        }
        StringBuilder buf = new StringBuilder();
        if (negative) {
            buf.append('-');
        }
        buf.append('P');
        if (years > 0) {
            buf.append(years).append('Y');
        }
        if (months > 0) {
            buf.append(months).append('M');
        }
        if (days > 0) {
            buf.append(days).append('D');
        }
        if ((hours > 0) || (minutes > 0) || (seconds > 0) || (millis > 0)) {
            buf.append('T');
            if (hours > 0) {
                buf.append(hours).append('H');
            }
            if (minutes > 0) {
                buf.append(minutes).append('M');
            }
            if (millis == 0) {
                if (seconds > 0) {
                    buf.append(seconds).append('S');
                }
            } else if (seconds == 0) {
                buf.append("0.").append(millis).append('S');
            } else {
                buf.append(seconds).append('.').append(millis).append('S');
            }
        }
        string = DSString.valueOf(buf.toString());
        return string.toString();
    }

    /**
     * Create a new duration object.
     *
     * @param neg    Whether or not the duration is negative.
     * @param hours  Must be positive.
     * @param mins   Must be positive.
     * @param secs   Must be positive.
     * @param millis Must be positive.
     * @return The new duration.
     */
    public static DSDuration valueOf(boolean neg, int hours, int mins, int secs, int millis) {
        DSDuration dur = new DSDuration();
        dur.negative = neg;
        dur.setHours(hours);
        dur.setMinutes(mins);
        dur.setSeconds(secs);
        dur.setMillis(millis);
        return dur;
    }

    /**
     * Create a new duration object.
     *
     * @param neg    Whether or not the duration is negative.
     * @param years  Must be positive.
     * @param months Must be positive.
     * @param days   Must be positive.
     * @return The new duration.
     */
    public static DSDuration valueOf(boolean neg, int years, int months, int days) {
        DSDuration dur = new DSDuration();
        dur.negative = neg;
        dur.setYears(years);
        dur.setMonths(months);
        dur.setDays(days);
        return dur;
    }

    /**
     * Create a new duration object.
     *
     * @param neg    Whether or not the duration is negative.
     * @param years  Must be positive.
     * @param months Must be positive.
     * @param days   Must be positive.
     * @param hours  Must be positive.
     * @param mins   Must be positive.
     * @param secs   Must be positive.
     * @param millis Must be positive.
     * @return The new duration.
     */
    public static DSDuration valueOf(boolean neg, int years, int months, int days,
                                     int hours, int mins, int secs, int millis) {
        DSDuration dur = new DSDuration();
        dur.negative = neg;
        dur.setYears(years);
        dur.setMonths(months);
        dur.setDays(days);
        dur.setHours(hours);
        dur.setMinutes(mins);
        dur.setSeconds(secs);
        dur.setMillis(millis);
        return dur;
    }

    @Override
    public DSDuration valueOf(DSElement element) {
        if ((element == null) || element.isNull()) {
            return NULL;
        }
        return valueOf(element.toString());
    }

    /**
     * Parses a duration using the format -PnYnMnDTnHnMnS.
     */
    public static DSDuration valueOf(String s) {
        if ((s == null) || s.isEmpty() || s.equals("null")) {
            return NULL;
        }
        DSDuration ret = new DSDuration();
        if ((s == null) || (s.length() == 0)) {
            return ret;
        }
        try {
            Parser p = new Parser(s);
            // check for negative
            if (p.is('-')) {
                ret.negative = true;
                p.next();
            } else if (p.is('+')) {
                p.next();
            }
            p.next('P');
            // at least one unit is required, even if it is zero
            if (p.is(-1)) {
                throw new Exception();
            }
            int num;
            if (p.isNot('T')) {
                num = p.num();
                if (p.is('Y')) {
                    p.next();
                    ret.years = num;
                    num = p.num();
                }
                if (p.is('M')) {
                    p.next();
                    ret.months = num;
                    num = p.num();
                }
                if (p.is('D')) {
                    p.next();
                    ret.days = num;
                }
            }
            if (p.is('T')) {
                p.next();
                num = p.num();
                if (p.is('H')) {
                    p.next();
                    ret.hours = num;
                    num = p.num();
                }
                if (p.is('M')) {
                    p.next();
                    ret.minutes = num;
                    num = p.num();
                }
                if (p.is('.')) {
                    p.next();
                    ret.seconds = num;
                    ret.millis = p.num();
                } else if (p.is('S')) {
                    ret.seconds = num;
                }
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid duration: " + s);
        }
        return ret;
    }

    /////////////////////////////////////////////////////////////////
    // Private Methods
    /////////////////////////////////////////////////////////////////

    private void setDays(int arg) {
        days = validate(arg);
    }

    private void setHours(int arg) {
        hours = validate(arg);
    }

    private void setMillis(int arg) {
        millis = validate(arg);
    }

    private void setMinutes(int arg) {
        minutes = validate(arg);
    }

    private void setMonths(int arg) {
        months = validate(arg);
    }

    private void setSeconds(int arg) {
        seconds = validate(arg);
    }

    private void setYears(int arg) {
        years = validate(arg);
    }

    private int validate(int arg) {
        if (arg < 0) {
            throw new IllegalArgumentException("Must be positive");
        }
        return arg;
    }

    /////////////////////////////////////////////////////////////////
    // Inner Classes
    /////////////////////////////////////////////////////////////////

    private static class Parser {

        private boolean isDigit;
        private int off;
        private String s;
        int value;

        Parser(String s) {
            this.s = s;
            this.value = s.charAt(0);
        }

        int digit() {
            return value - '0';
        }

        boolean is(int ch) {
            return value == ch;
        }

        boolean isNot(int ch) {
            return value != ch;
        }

        void next(int ch) {
            if (value != ch) {
                throw new IllegalStateException();
            }
            next();
        }

        void next() {
            off++;
            if (off < s.length()) {
                value = s.charAt(off);
                isDigit = '0' <= value && value <= '9';
            } else {
                value = -1;
                isDigit = false;
            }
        }

        int num() {
            int num = 0;
            while (isDigit) {
                num = num * 10 + digit();
                next();
            }
            return num;
        }
    }

    /////////////////////////////////////////////////////////////////
    // Initialization
    /////////////////////////////////////////////////////////////////

    static {
        DSRegistry.registerDecoder(DSDuration.class, NULL);
    }

}
