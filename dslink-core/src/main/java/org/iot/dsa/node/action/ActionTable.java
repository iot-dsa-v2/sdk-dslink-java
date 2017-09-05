package org.iot.dsa.node.action;

import java.util.Iterator;
import org.iot.dsa.node.DSList;
import org.iot.dsa.node.DSMap;

/**
 * Provides access to the columns and rows of a table.
 *
 * @author Aaron Hansen
 */
public interface ActionTable extends ActionResult {

    /**
     * Column definitions, optional but recommended.
     */
    public Iterator<DSMap> getColumns();

    /**
     * This should return an iterator for the initial set of rows, or null if there aren't any.
     */
    public Iterator<DSList> getRows();

}
