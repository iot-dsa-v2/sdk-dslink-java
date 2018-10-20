package org.iot.dsa.table;

import java.util.Iterator;
import org.iot.dsa.node.DSElement;
import org.iot.dsa.node.DSIMetadata;
import org.iot.dsa.node.DSIValue;
import org.iot.dsa.node.DSMap;
import org.iot.dsa.node.DSMetadata;
import org.iot.dsa.node.DSStatus;
import org.iot.dsa.time.DSDateTime;

/**
 * A very simple trend that holds all rows in memory, primarily for testing but could be used
 * for small trends where the rows are already known. To use, set the value column type, add rows,
 * then call trend().
 *
 * @author Aaron Hansen
 */
public class SimpleTrend extends AbstractTable {

    ///////////////////////////////////////////////////////////////////////////
    // Public Methods
    ///////////////////////////////////////////////////////////////////////////

    public SimpleTrend addRow(DSDateTime ts, DSIValue value, DSStatus status) {
        if (columns.isEmpty()) {
            throw new IllegalStateException("Value column undefined");
        }
        appendRow(ts, value, status);
        return this;
    }

    public DSMetadata setValueType(DSIValue val) {
        appendCol("Timestamp", DSDateTime.NULL.getValueType());
        DSMetadata ret = appendCol("Value", val.getValueType());
        if (val instanceof DSIMetadata) {
            ((DSIMetadata)val).getMetadata(ret.getMap());
        }
        appendCol("Status", DSStatus.ok.getValueType());
        return ret;
    }

    public DSITrend trend() {
        return new MyTrend();
    }

    ///////////////////////////////////////////////////////////////////////////
    // Inner Classes
    ///////////////////////////////////////////////////////////////////////////

    private class MyTrend implements DSITrend {

        private DSIValue[] row;
        private Iterator<DSIValue[]> rowIterator = rows.iterator();

        @Override
        public int getColumnCount() {
            return columns.size();
        }

        @Override
        public void getMetadata(int index, DSMap bucket) {
            bucket.putAll(columns.get(index));
        }

        @Override
        public int getStatus() {
            DSStatus s = (DSStatus) row[2];
            return s.getBits();
        }

        @Override
        public int getStatusColumn() {
            return 2;
        }

        @Override
        public long getTimestamp() {
            DSDateTime dt = (DSDateTime) row[0];
            return dt.timeInMillis();
        }

        @Override
        public int getTimestampColumn() {
            return 0;
        }

        @Override
        public DSElement getValue() {
            return row[1].toElement();
        }

        @Override
        public DSIValue getValue(int index) {
            return row[index];
        }

        @Override
        public int getValueColumn() {
            return 1;
        }

        @Override
        public boolean next() {
            if (rowIterator.hasNext()) {
                row = rowIterator.next();
                return true;
            }
            row = null;
            return false;
        }

    }

}
