package org.iot.dsa.node.action;

import java.util.Iterator;
import org.iot.dsa.node.DSList;
import org.iot.dsa.node.DSMap;
import org.iot.dsa.node.DSMetadata;

/**
 * Provides access to the columns and rows of a table.
 *
 * @author Aaron Hansen
 */
public interface ActionTable extends ActionResult {

    /**
     * Column definitions, optional but highly recommended. The map should have a
     * unique name and a value type, use the metadata utility class to build the map.
     *
     * @see DSMetadata
     */
    public Iterator<DSMap> getColumns();

    /**
     * This should return an iterator for the initial set of rows, or null if there aren't any.
     */
    public Iterator<DSList> getRows();

}
