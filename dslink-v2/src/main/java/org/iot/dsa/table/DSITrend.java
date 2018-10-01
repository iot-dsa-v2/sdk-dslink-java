package org.iot.dsa.table;

import org.iot.dsa.node.DSElement;

/**
 * A time series row cursor, but provides methods for primitive access.  For performance reasons,
 * implementations should not convert the status bits and timestamp into object unless the column
 * is accessed via the DSIRowCursor methods.
 *
 * @author Aaron Hansen
 */
public interface DSITrend extends DSIRowCursor {

    public int getStatus();

    public long getTimestamp();

    public DSElement getValue();

}
