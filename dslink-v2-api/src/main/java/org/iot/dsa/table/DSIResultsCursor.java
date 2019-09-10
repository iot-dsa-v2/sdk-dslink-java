package org.iot.dsa.table;

/**
 * Interface for walking rows of a table, stream, trend, etc.
 *
 * @author Aaron Hansen
 */
public interface DSIResultsCursor extends DSIResults {

    /**
     * Returns true when the cursor
     */
    public boolean next();

}
