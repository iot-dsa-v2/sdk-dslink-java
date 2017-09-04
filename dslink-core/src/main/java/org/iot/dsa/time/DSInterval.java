package org.iot.dsa.time;

/**
 * Enum representing periods of time.
 *
 * @author Aaron Hansen
 */
public enum DSInterval {

    none,
    second,
    fiveSeconds,
    tenSeconds,
    fifteenSeconds,
    thirtySeconds,
    minute,
    fiveMinutes,
    tenMinutes,
    fifteenMinutes,
    twentyMinutes,
    thirtyMinutes,
    hour,
    twoHours,
    threeHours,
    fourHours,
    sixHours,
    twelveHours,
    day,
    week,
    month,
    quarter,
    year;

    /////////////////////////////////////////////////////////////////
    // Methods
    /////////////////////////////////////////////////////////////////

    /**
     * Returns the number of intervals for the given time range, or -1
     * if indeterminate.
     */
    public int count(long start, long end) {
        if ((start < 0) || (end < 0)) {
            return -1;
        }
        long delta = end - start;
        switch (this) {
            case none:
                return (int) delta;
            case second:
            case fiveSeconds:
            case tenSeconds:
            case fifteenSeconds:
            case thirtySeconds:
            case minute:
            case fiveMinutes:
            case tenMinutes:
            case fifteenMinutes:
            case twentyMinutes:
            case thirtyMinutes:
            case hour:
            case twoHours:
            case threeHours:
            case fourHours:
            case sixHours:
            case twelveHours:
            case day:
            case week:
                return (int) (delta / millis());
        }
        int ret = 0;
        if (start < end) {
            while (start < end) {
                ret++;
                start = next(start);
            }
        } else {
            while (end < start) {
                ret++;
                end = next(end);
            }
        }
        return ret;
    }

    /**
     * The approximate number of ms in the interval.
     */
    public long millis() {
        switch (this) {
            case second:
                return DSTime.MILLIS_SECOND;
            case fiveSeconds:
                return DSTime.MILLIS_FIVE_SECONDS;
            case tenSeconds:
                return DSTime.MILLIS_TEN_SECONDS;
            case fifteenSeconds:
                return DSTime.MILLIS_FIFTEEN_SECONDS;
            case thirtySeconds:
                return DSTime.MILLIS_THIRTY_SECONDS;
            case minute:
                return DSTime.MILLIS_MINUTE;
            case fiveMinutes:
                return DSTime.MILLIS_FIVE_MINUTES;
            case tenMinutes:
                return DSTime.MILLIS_TEN_MINUTES;
            case fifteenMinutes:
                return DSTime.MILLIS_FIFTEEN_MINUTES;
            case twentyMinutes:
                return DSTime.MILLIS_TWENTY_MINUTES;
            case thirtyMinutes:
                return DSTime.MILLIS_THIRTY_MINUTES;
            case hour:
                return DSTime.MILLIS_HOUR;
            case twoHours:
                return DSTime.MILLIS_TWO_HOURS;
            case threeHours:
                return DSTime.MILLIS_THREE_HOURS;
            case fourHours:
                return DSTime.MILLIS_FOUR_HOURS;
            case sixHours:
                return DSTime.MILLIS_SIX_HOURS;
            case twelveHours:
                return DSTime.MILLIS_TWELVE_HOURS;
            case day:
                return DSTime.MILLIS_DAY;
            case week:
                return DSTime.MILLIS_WEEK;
            case month:
                return DSTime.MILLIS_MONTH;
            case quarter:
                return DSTime.MILLIS_QUARTER;
            case year:
                return DSTime.MILLIS_YEAR;
            default: //none
                return 1;
        }
    }

    /**
     * Returns the nextRun interval for the previously aligned timestamp.
     */
    public long next(long timestamp) {
        long ts = timestamp;
        switch (this) {
            case second:
                return ts + DSTime.MILLIS_SECOND;
            case fiveSeconds:
                return ts + DSTime.MILLIS_FIVE_SECONDS;
            case tenSeconds:
                return ts + DSTime.MILLIS_TEN_SECONDS;
            case fifteenSeconds:
                return ts + DSTime.MILLIS_FIFTEEN_SECONDS;
            case thirtySeconds:
                return ts + DSTime.MILLIS_THIRTY_SECONDS;
            case minute:
                return ts + DSTime.MILLIS_MINUTE;
            case fiveMinutes:
                return ts + DSTime.MILLIS_FIVE_MINUTES;
            case tenMinutes:
                return ts + DSTime.MILLIS_TEN_MINUTES;
            case fifteenMinutes:
                return ts + DSTime.MILLIS_FIFTEEN_MINUTES;
            case twentyMinutes:
                return ts + DSTime.MILLIS_TWENTY_MINUTES;
            case thirtyMinutes:
                return ts + DSTime.MILLIS_THIRTY_MINUTES;
            case hour:
                return DSTime.addHours(1, ts);
            case twoHours:
                return DSTime.addHours(2, ts);
            case threeHours:
                return DSTime.addHours(3, ts);
            case fourHours:
                return DSTime.addHours(4, ts);
            case sixHours:
                return DSTime.addHours(6, ts);
            case twelveHours:
                return DSTime.addHours(12, ts);
            case day:
                return DSTime.addDays(1, ts);
            case week:
                return DSTime.addWeeks(1, ts);
            case month:
                return DSTime.addMonths(1, ts);
            case quarter:
                return DSTime.addMonths(3, ts);
            case year:
                return DSTime.addYears(1, ts);
            default: //none
                return ts + 1;
        }
    }

}
