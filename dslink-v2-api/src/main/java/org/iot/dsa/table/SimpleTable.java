package org.iot.dsa.table;

import java.util.Iterator;
import org.iot.dsa.node.DSIValue;
import org.iot.dsa.node.DSMap;
import org.iot.dsa.node.DSMetadata;
import org.iot.dsa.node.DSValueType;

/**
 * A very simple table that holds all rows in memory, primarily for testing but could be used
 * for small tables where the rows are already known. To use, add columns and rows, then call
 * cursor().
 *
 * @author Aaron Hansen
 */
public class SimpleTable extends AbstractTable {

    ///////////////////////////////////////////////////////////////////////////
    // Public Methods
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Fully describes a return value when the result type is VALUES.  Must be added in
     * the order that the values will be returned. At the very least, the map should have
     * a unique name and a value type, use the DSMetadata utility class.
     *
     * @return This.
     * @see DSMetadata
     */
    public SimpleTable addColumn(DSMap metadata) {
        appendCol(metadata);
        return this;
    }


    /**
     * Creates a DSMetadata, calls setName and setType on it, adds the internal map to
     * the column list and returns the metadata instance for further configuration.
     *
     * @param name  Must not be null.
     * @param value Must not be null.
     * @return Metadata for further configuration.
     */
    public DSMetadata addColumn(String name, DSIValue value) {
        return appendCol(name, value);
    }

    /**
     * Creates a DSMetadata, calls setName and setType on it, adds the internal map to
     * the columns list and returns the metadata instance for further configuration.
     *
     * @param name Must not be null.
     * @param type Must not be null.
     * @return Metadata for further configuration.
     */
    public DSMetadata addColumn(String name, DSValueType type) {
        return appendCol(name, type);
    }

    public SimpleTable addRow(DSIValue... row) {
        appendRow(row);
        return this;
    }

    public DSIResultsCursor cursor() {
        return new MyCursor();
    }

    ///////////////////////////////////////////////////////////////////////////
    // Inner Classes
    ///////////////////////////////////////////////////////////////////////////

    private class MyCursor implements DSIResultsCursor {

        private DSIValue[] row;
        private Iterator<DSIValue[]> rowIterator = rows.iterator();

        @Override
        public int getColumnCount() {
            return columns.size();
        }

        @Override
        public void getColumnMetadata(int index, DSMap bucket) {
            bucket.putAll(columns.get(index));
        }

        @Override
        public DSIValue getValue(int index) {
            return row[index];
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

    ///////////////////////////////////////////////////////////////////////////
    // Initialization
    ///////////////////////////////////////////////////////////////////////////

}
