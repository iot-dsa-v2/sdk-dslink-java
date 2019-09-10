package org.iot.dsa.table;

import org.iot.dsa.node.DSElement;

/**
 * A time series row cursor, but provides methods for primitive access.  For performance reasons,
 * implementations should not convert the status bits and timestamp into objects unless the column
 * is accessed via the DSIRowCursor methods.
 *
 * @author Aaron Hansen
 */
public interface DSITrend extends DSIResultsCursor {

    public int getStatus();

    public int getStatusColumn();

    public long getTimestamp();

    public int getTimestampColumn();

    public DSElement getValue();

    public int getValueColumn();

}
