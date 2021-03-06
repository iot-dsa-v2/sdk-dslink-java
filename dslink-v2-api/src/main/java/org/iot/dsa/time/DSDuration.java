package org.iot.dsa.time;

import java.util.Calendar;
import org.iot.dsa.dslink.ActionResults;
import org.iot.dsa.node.DSBool;
import org.iot.dsa.node.DSElement;
import org.iot.dsa.node.DSInfo;
import org.iot.dsa.node.DSInt;
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
 * XML Schema compliant relative amount of time represented as a number of years, months, days,
 * hours, minutes, and seconds. The String format is -PnYnMnDTnHnMnS.
 *
 * @author Aaron Hansen
 */
public class DSDuration extends DSValue implements DSISetAction {

    /////////////////////////////////////////////////////////////////
    // Class Fields
    /////////////////////////////////////////////////////////////////

    public static DSDuration NULL = new DSDuration();

    public static final String NEGATIVE = "Negative";
    public static final String YEARS = "Years";
    public static final String MONTHS = "Months";
    public static final String DAYS = "Days";
    public static final String HOURS = "Hours";
    public static final String MINUTES = "Minutes";
    public static final String SECONDS = "Seconds";
    public static final String MILLIS = "Milliseconds";

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
                Time.addYears(-years, cal);
            }
            if (months > 0) {
                Time.addMonths(-months, cal);
            }
            if (days > 0) {
                Time.addDays(-days, cal);
            }
            if (hours > 0) {
                Time.addHours(-hours, cal);
            }
            if (minutes > 0) {
                Time.addMinutes(-minutes, cal);
            }
            if (seconds > 0) {
                Time.addSeconds(-seconds, cal);
            }
            if (millis > 0) {
                Time.addMillis(-millis, cal);
            }
        } else {
            if (years > 0) {
                Time.addYears(years, cal);
            }
            if (months > 0) {
                Time.addMonths(months, cal);
            }
            if (days > 0) {
                Time.addDays(days, cal);
            }
            if (hours > 0) {
                Time.addHours(hours, cal);
            }
            if (minutes > 0) {
                Time.addMinutes(minutes, cal);
            }
            if (seconds > 0) {
                Time.addSeconds(seconds, cal);
            }
            if (millis > 0) {
                Time.addMillis(millis, cal);
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
        Calendar cal = Time.getCalendar(timestamp);
        apply(cal);
        timestamp = cal.getTimeInMillis();
        Time.recycle(cal);
        return timestamp;
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

    private void setDays(int arg) {
        days = validate(arg);
    }

    public int getHours() {
        return hours;
    }

    private void setHours(int arg) {
        hours = validate(arg);
    }

    public int getMillis() {
        return millis;
    }

    private void setMillis(int arg) {
        millis = validate(arg);
    }

    public int getMinutes() {
        return minutes;
    }

    private void setMinutes(int arg) {
        minutes = validate(arg);
    }

    public int getMonths() {
        return months;
    }

    private void setMonths(int arg) {
        months = validate(arg);
    }

    public int getSeconds() {
        return seconds;
    }

    private void setSeconds(int arg) {
        seconds = validate(arg);
    }

    @Override
    public DSAction getSetAction() {
        return SetAction.INSTANCE;
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

    private void setYears(int arg) {
        years = validate(arg);
    }

    @Override
    public int hashCode() {
        return toString().hashCode();
    }

    public boolean isNegative() {
        return negative;
    }

    @Override
    public boolean isNull() {
        return this == NULL;
    }

    /////////////////////////////////////////////////////////////////
    // Private Methods
    /////////////////////////////////////////////////////////////////

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

    public static class SetAction extends DSAction {

        public static final SetAction INSTANCE = new SetAction();

        @Override
        public ActionResults invoke(DSIActionRequest request) {
            DSMap params = request.getParameters();
            boolean neg = params.get(NEGATIVE, false);
            int years = params.get(YEARS, 0);
            int months = params.get(MONTHS, 0);
            int days = params.get(DAYS, 0);
            int hrs = params.get(HOURS, 0);
            int mins = params.get(MINUTES, 0);
            int secs = params.get(SECONDS, 0);
            int ms = params.get(MILLIS, 0);
            DSInfo<?> target = request.getTargetInfo();
            target.getParent().put(target, valueOf(neg, years, months, days, hrs, mins, secs, ms));
            return null;
        }

        @Override
        public void prepareParameter(DSInfo<?> target, DSMap parameter) {
            DSDuration dur = (DSDuration) target.get();
            String name = parameter.get(DSMetadata.NAME, "");
            switch (name) {
                case NEGATIVE:
                    parameter.put(name, dur.isNegative());
                    break;
                case YEARS:
                    parameter.put(name, dur.getYears());
                    break;
                case MONTHS:
                    parameter.put(name, dur.getMonths());
                    break;
                case DAYS:
                    parameter.put(name, dur.getDays());
                    break;
                case HOURS:
                    parameter.put(name, dur.getHours());
                    break;
                case MINUTES:
                    parameter.put(name, dur.getMinutes());
                    break;
                case SECONDS:
                    parameter.put(name, dur.getSeconds());
                    break;
                case MILLIS:
                    parameter.put(name, dur.getMillis());
                    break;
            }
        }

        {
            addParameter(NEGATIVE, DSBool.NULL, "Whether or not the duration is negative");
            addParameter(YEARS, DSInt.NULL, null);
            addParameter(MONTHS, DSInt.NULL, null);
            addParameter(DAYS, DSInt.NULL, null);
            addParameter(HOURS, DSInt.NULL, null);
            addParameter(MINUTES, DSInt.NULL, null);
            addParameter(SECONDS, DSInt.NULL, null);
            addParameter(MILLIS, DSInt.NULL, null);
        }

    }

    /////////////////////////////////////////////////////////////////
    // Initialization
    /////////////////////////////////////////////////////////////////

    static {
        DSRegistry.registerDecoder(DSDuration.class, NULL);
    }

}
