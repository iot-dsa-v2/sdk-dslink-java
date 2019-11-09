package org.iot.dsa.dslink.requester;

import org.iot.dsa.node.DSList;
import org.iot.dsa.node.DSMap;

/**
 * Callback mechanism passed to the invoke method on DSIRequester.
 *
 * @author Daniel Shapiro, Aaron Hansen
 */
public interface OutboundInvokeHandler extends OutboundRequestHandler {

    /**
     * Called whenever columns are received.  Columns should be appended to any existing columns
     * unless a refresh is called, in which case any new columns replace the existing columns.
     *
     * @param list A list of maps.
     */
    void onColumns(DSList list);

    /**
     * Called when the given rows should be inserted at the given index.
     *
     * @param index Where to insert the given rows.
     * @param rows  What to insert at the given index.
     */
    void onInsert(int index, DSList rows);

    /**
     * Called whenever a mode is received.
     */
    void onMode(Mode mode);

    /**
     * The rows starting and ending with the given indexes should be removed and the given rows
     * inserted at the start index.
     *
     * @param start First inclusive of rows to be replaced.
     * @param end   Last inclusive index of rows to be replaced.
     * @param rows  What to insert at the starting index.
     */
    void onReplace(int start, int end, DSList rows);

    /**
     * Called whenever metadata for the entire table is received.
     */
    void onTableMeta(DSMap map);

    /**
     * Called for every row.
     */
    void onUpdate(DSList row);

    enum Mode {
        APPEND,
        REFRESH,
        STREAM,
    }
}
