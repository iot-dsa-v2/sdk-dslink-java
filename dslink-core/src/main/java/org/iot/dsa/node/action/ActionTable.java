package org.iot.dsa.node.action;

import java.util.Iterator;
import org.iot.dsa.node.DSList;
import org.iot.dsa.node.DSMap;
import org.iot.dsa.node.DSMetadata;

/**
 * Provides access to the columns and rows of a table.  Not thread safe and only represents
 * the result of a single action invocation.
 *
 * @author Aaron Hansen
 */
public interface ActionTable extends ActionResult {

    /**
     * Column definitions, optional but highly recommended. The map should have a unique name and a
     * value type, use the metadata utility class to build the map.
     *
     * @see DSMetadata
     */
    public Iterator<DSMap> getColumns();

    /**
     * This should return an iterator for the initial set of rows, or null if there aren't any.
     * Subsequent calls should just return null unless documented otherwise.
     */
    public Iterator<DSList> getRows();

}
