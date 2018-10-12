package org.iot.dsa.table;

import org.iot.dsa.node.DSElement;
import org.iot.dsa.node.DSIValue;
import org.iot.dsa.node.DSMap;

/**
 * A convenience for a trend that wraps another trend.
 *
 * @author Aaron Hansen
 */
public class TrendWrapper implements DSITrend {

    ///////////////////////////////////////////////////////////////////////////
    // Instance Fields
    ///////////////////////////////////////////////////////////////////////////

    private DSITrend inner;

    ///////////////////////////////////////////////////////////////////////////
    // Constructors
    ///////////////////////////////////////////////////////////////////////////

    public TrendWrapper(DSITrend inner) {
        this.inner = inner;
    }

    ///////////////////////////////////////////////////////////////////////////
    // Public Methods
    ///////////////////////////////////////////////////////////////////////////

    @Override
    public int getStatus() {
        return inner.getStatus();
    }

    public long getTimestamp() {
        return inner.getTimestamp();
    }

    @Override
    public DSElement getValue() {
        return inner.getValue();
    }

    @Override
    public boolean next() {
        return false;
    }

    @Override
    public int getColumnCount() {
        return inner.getColumnCount();
    }

    public DSITrend getInner() {
        return inner;
    }

    @Override
    public void getMetadata(int index, DSMap bucket) {
        inner.getMetadata(index, bucket);
    }

    @Override
    public DSIValue getValue(int index) {
        return inner.getValue(index);
    }

}
