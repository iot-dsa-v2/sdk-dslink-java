package org.iot.dsa.table;

import org.iot.dsa.node.DSIMetadata;
import org.iot.dsa.node.DSIValue;
import org.iot.dsa.node.DSList;
import org.iot.dsa.node.DSMap;
import org.iot.dsa.node.DSMetadata;

/**
 * Simple implementation of DSIResultsCursor.  Add columns in the order they will be in the rows,
 * then add rows.  Row values are limited to DSElements.
 *
 * @author Aaron Hansen
 */
public class BasicResultsCursor implements DSIResultsCursor {

    ///////////////////////////////////////////////////////////////////////////
    // Instance Fields
    ///////////////////////////////////////////////////////////////////////////

    protected DSList columns;
    protected DSList next;
    protected DSList rows;

    ///////////////////////////////////////////////////////////////////////////
    // Public Methods
    ///////////////////////////////////////////////////////////////////////////

    public DSMetadata addColumn(String name, DSIValue prototype) {
        DSMetadata meta = new DSMetadata();
        meta.setName(name);
        meta.setType(prototype);
        if (prototype instanceof DSIMetadata) {
            ((DSIMetadata) prototype).getMetadata(meta.getMap());
        }
        addColumn(meta.getMap());
        return meta;
    }

    public BasicResultsCursor addColumn(DSMap col) {
        if (columns == null) {
            columns = new DSList();
        }
        columns.add(col);
        return this;
    }

    public BasicResultsCursor addRow(DSIValue... values) {
        DSList list = new DSList();
        for (DSIValue value : values) {
            list.add(value.toElement());
        }
        return addRow(list);
    }

    public BasicResultsCursor addRow(DSList row) {
        if (rows == null) {
            rows = new DSList();
        }
        rows.add(row);
        return this;
    }

    @Override
    public int getColumnCount() {
        if ((columns == null) || columns.isEmpty()) {
            return 0;
        }
        return columns.size();
    }

    @Override
    public void getColumnMetadata(int index, DSMap bucket) {
        bucket.putAll(columns.getMap(index));
    }

    @Override
    public DSIValue getValue(int index) {
        return next.get(index);
    }

    @Override
    public boolean next() {
        next = null;
        if ((rows == null) || rows.isEmpty()) {
            return false;
        }
        next = (DSList) rows.removeFirst();
        return true;
    }

}
