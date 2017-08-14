package org.iot.dsa.node.action;

import java.util.Iterator;
import org.iot.dsa.node.DSList;

/**
 * Provides access to the columns and rows of a table.
 *
 * @author Aaron Hansen
 */
public interface ActionTable extends ActionResult {

    /**
     * Column definitions, optional but recommended.
     */
    public Iterator<ActionResultSpec> getColumns();

    /**
     * This should return an iterator for the initial set of rows, or null if there aren't any.
     */
    public Iterator<DSList> getRows();

}
