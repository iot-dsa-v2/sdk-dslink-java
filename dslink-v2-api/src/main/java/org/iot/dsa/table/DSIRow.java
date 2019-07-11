package org.iot.dsa.table;

import org.iot.dsa.node.DSIValue;
import org.iot.dsa.node.DSMap;

/**
 * Represents an index accessible list of object.
 *
 * @author Aaron Hansen
 */
public interface DSIRow {

    /**
     * The number of columns in the row.
     */
    public int getColumnCount();

    /**
     * Adds the meta data for the given column/index to the given bucket.  Only needs to be
     * called once, not for each row in a cursor.
     *
     * @param index  0 based column identifier.
     * @param bucket Must not be null and the caller is responsible for clearing it.
     */
    public void getMetadata(int index, DSMap bucket);

    /**
     * Retrieves the value at the given index.  In the cursor subclass, this must only
     * be called after next() returns true.
     *
     * @param index 0 based column identifier.
     */
    public DSIValue getValue(int index);

}
