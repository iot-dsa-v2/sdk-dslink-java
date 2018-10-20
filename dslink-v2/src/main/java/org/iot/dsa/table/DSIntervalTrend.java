package org.iot.dsa.table;

import org.iot.dsa.node.DSElement;
import org.iot.dsa.rollup.DSRollup;
import org.iot.dsa.rollup.RollupFunction;
import org.iot.dsa.time.DSDuration;

/**
 * Rolls up values in a series to intervals.  Do not use if the interval
 * is INTERVAL_NONE.
 *
 * @author Aaron Hansen
 */
public class DSIntervalTrend extends DSTrendWrapper {

    /////////////////////////////////////////////////////////////////
    // Instance Fields
    /////////////////////////////////////////////////////////////////

    private boolean cov = false;
    private long curTs;
    private DSDuration interval;
    private long nextTs = -1;
    private RollupFunction rollup;
    private DSITrend trend;

    /////////////////////////////////////////////////////////////////
    // Constructors
    /////////////////////////////////////////////////////////////////

    /**
     * @param trend    Required, the trend to convert.
     * @param interval Required, duration representing the interval.
     * @param rollup   Required, how to aggregate multiple values in an interval.
     */
    public DSIntervalTrend(DSITrend trend,
                           DSDuration interval,
                           DSRollup rollup) {
        super(trend);
        this.trend = trend;
        this.interval = interval;
        this.rollup = rollup.getFunction();
        if (trend.next()) {
            nextTs = trend.getTimestamp();
            this.rollup.update(trend.getValue(), trend.getStatus());
        }
    }

    /////////////////////////////////////////////////////////////////
    // Public Methods
    /////////////////////////////////////////////////////////////////

    @Override
    public int getStatus() {
        return rollup.getStatus();
    }

    @Override
    public long getTimestamp() {
        return curTs;
    }

    @Override
    public DSElement getValue() {
        return rollup.getValue();
    }

    @Override
    public boolean next() {
        if (nextTs < 0) {
            return false;
        }
        long rowTs = trend.getTimestamp();
        curTs = nextTs;
        nextTs = interval.apply(nextTs);
        if (nextTs <= rowTs) {
            if (cov) {
                return true;
            }
            //find the ivl for the next timestamp
            while (nextTs <= rowTs) {
                curTs = nextTs;
                nextTs = interval.apply(nextTs);
            }
        }
        rollup.reset();
        while (trend.getTimestamp() < nextTs) {
            rollup.update(trend.getValue(), trend.getStatus());
            if (!trend.next()) {
                nextTs = -1;
                break;
            }
        }
        return true;
    }

    /**
     * When true, missing rows are interpolated using the last value (false by default).  This
     * must be called before the first call to next().
     */
    public DSIntervalTrend setCov(boolean arg) {
        cov = arg;
        return this;
    }

}
