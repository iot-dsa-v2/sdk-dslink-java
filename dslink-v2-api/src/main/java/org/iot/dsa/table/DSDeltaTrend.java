package org.iot.dsa.table;

import org.iot.dsa.node.DSDouble;
import org.iot.dsa.node.DSElement;
import org.iot.dsa.node.DSStatus;

/**
 * Numeric trend performs a delta between timestamped values and assigns the delta
 * value to either the leading (by default) or trailing timestamp.
 *
 * @author Aaron Hansen
 */
public class DSDeltaTrend extends DSTrendWrapper {

    /////////////////////////////////////////////////////////////////
    // Instance Fields
    /////////////////////////////////////////////////////////////////

    private int curSts;
    private long curTs;
    private DSElement curVal;
    private int lastSts;
    private long lastTs;
    private double lastVal;
    private boolean leading = true;
    private DSITrend trend;

    /////////////////////////////////////////////////////////////////
    // Constructors - In alphabetical order by method name.
    /////////////////////////////////////////////////////////////////

    public DSDeltaTrend(DSITrend trend) {
        super(trend);
        this.trend = trend;
        if (trend.next()) {
            lastTs = trend.getTimestamp();
            lastVal = trend.getValue().toDouble();
            lastSts = trend.getStatus();
        }
    }

    /////////////////////////////////////////////////////////////////
    // Methods - Public and in alphabetical order by method name.
    /////////////////////////////////////////////////////////////////

    @Override
    public int getStatus() {
        return curSts;
    }

    @Override
    public long getTimestamp() {
        return curTs;
    }

    @Override
    public DSElement getValue() {
        return curVal;
    }

    @Override
    public boolean next() {
        if (!trend.next()) {
            return false;
        }
        int sts = lastSts | trend.getStatus();
        double cur = trend.getValue().toDouble();
        double delta = cur - lastVal;
        //Protect against invalid values, as well as meter rollovers.
        if (!DSStatus.isGood(sts)
                || (cur == 0)  //uncommon when dealing with totalized values
                || Double.isNaN(delta)
                || Double.isInfinite(delta)) {
            delta = 0;
        } else if (delta < 0) { //is this a meter rollover?
            if ((cur > 0) && (cur < Math.abs(delta))) {
                delta = cur;
            }
        }
        if (leading) {
            curTs = lastTs;
            curSts = lastSts;
            curVal = DSDouble.valueOf(delta);
            lastTs = trend.getTimestamp();
            lastSts = trend.getStatus();
        } else {
            curTs = trend.getTimestamp();
            curSts = trend.getStatus();
            curVal = DSDouble.valueOf(delta);
        }
        lastVal = cur;
        return true;
    }

    /**
     * When true, the earlier timestamp is assigned to the delta (true is default).
     */
    public DSDeltaTrend setLeading(boolean leading) {
        this.leading = leading;
        return this;
    }

}
