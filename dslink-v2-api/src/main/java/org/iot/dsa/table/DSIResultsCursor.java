package org.iot.dsa.table;

/**
 * Interface for walking rows of a table, stream, trend, etc.
 *
 * @author Aaron Hansen
 */
public interface DSIResultsCursor extends DSIResults {

    /**
     * The cursor must start before the first row of results.  If there is another row, this
     * should advance the cursor and return true.  Otherwise return false.
     */
    boolean next();

}
