package org.iot.dsa.node.action;

import org.iot.dsa.node.DSMap;
import org.iot.dsa.table.DSIRowCursor;

/**
 * Extends ActionValues as a row cursor.
 *
 * @author Aaron Hansen
 */
public interface ActionTable extends ActionValues, DSIRowCursor {

    /**
     * Metadata for the entire table, null by default.
     */
    public default DSMap getMetadata() {
        return null;
    }
}
