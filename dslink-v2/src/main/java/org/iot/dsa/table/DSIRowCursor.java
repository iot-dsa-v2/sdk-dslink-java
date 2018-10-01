package org.iot.dsa.table;

/**
 * Interface for walking rows of a table, stream, trend, etc.
 *
 * @author Aaron Hansen
 */
public interface DSIRowCursor extends DSIRow {

    /**
     * Returns true when the cursor
     */
    public boolean next();

}
