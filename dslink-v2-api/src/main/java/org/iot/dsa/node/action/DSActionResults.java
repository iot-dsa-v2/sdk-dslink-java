package org.iot.dsa.node.action;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import org.iot.dsa.dslink.Action.ResultsType;
import org.iot.dsa.dslink.ActionResults;
import org.iot.dsa.node.DSIMetadata;
import org.iot.dsa.node.DSIValue;
import org.iot.dsa.node.DSList;
import org.iot.dsa.node.DSMap;
import org.iot.dsa.node.DSMetadata;

/**
 * A convenience for asynchronously sending results.
 *
 * @author Aaron Hansen
 */
public class DSActionResults implements ActionResults {

    ///////////////////////////////////////////////////////////////////////////
    // Class Fields
    ///////////////////////////////////////////////////////////////////////////

    ///////////////////////////////////////////////////////////////////////////
    // Instance Fields
    ///////////////////////////////////////////////////////////////////////////

    List<DSMap> cols;
    DSIActionRequest req;
    final List<DSList> rows = new LinkedList<>();

    ///////////////////////////////////////////////////////////////////////////
    // Constructors
    ///////////////////////////////////////////////////////////////////////////

    public DSActionResults(DSIActionRequest req) {
        this.req = req;
    }

    ///////////////////////////////////////////////////////////////////////////
    // Public Methods
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Optional, if no columns are added to this object, the columns from the action will be used.
     */
    public void addColumn(DSMap col) {
        if (cols == null) {
            cols = new ArrayList<>();
        }
        cols.add(col);
    }

    /**
     * Optional, if no columns are added to this object, the columns from the action will
     * be used.
     * Creates a DSMetadata, calls setName and setType on it, adds the internal map to
     * the columns and returns the metadata instance for further configuration.
     *
     * @param name  Must not be null.
     * @param value Must not be null.
     * @return Metadata for further configuration.
     */
    public DSMetadata addColumn(String name, DSIValue value) {
        DSMetadata ret = new DSMetadata();
        if (value instanceof DSIMetadata) {
            ((DSIMetadata) value).getMetadata(ret.getMap());
        }
        ret.setName(name).setType(value);
        addColumn(ret.getMap());
        return ret;
    }

    /**
     * Add a row to send to the requester.
     *
     * @param values The cells of a single row
     */
    public void addResults(DSIValue... values) {
        DSList list = new DSList();
        for (DSIValue val : values) {
            list.add(val.toElement());
        }
        addResults(list);
    }

    /**
     * Add a row to send to the requester.
     *
     * @param row Do not modify the row after calling this method.
     */
    public void addResults(DSList row) {
        synchronized (rows) {
            rows.add(row);
        }
        req.sendResults();
    }

    /**
     * Calls close on the request.
     */
    public void close() {
        req.close();
    }

    /**
     * Returns the count defined in the action.
     */
    @Override
    public int getColumnCount() {
        if (cols == null) {
            return req.getAction().getColumnCount();
        }
        return cols.size();
    }

    /**
     * Returns the metadata defined in the action.
     */
    @Override
    public void getColumnMetadata(int idx, DSMap bucket) {
        if (cols == null) {
            req.getAction().getColumnMetadata(idx, bucket);
            return;
        }
        bucket.putAll(cols.get(idx));
    }

    public DSIActionRequest getRequest() {
        return req;
    }

    @Override
    public void getResults(DSList bucket) {
        synchronized (rows) {
            DSList row = rows.remove(0);
            bucket.addAll(row);
        }
    }

    @Override
    public ResultsType getResultsType() {
        return req.getAction().getResultsType();
    }

    @Override
    public boolean next() {
        synchronized (rows) {
            return rows.size() > 0;
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // Protected Methods
    ///////////////////////////////////////////////////////////////////////////

    ///////////////////////////////////////////////////////////////////////////
    // Package Methods
    ///////////////////////////////////////////////////////////////////////////

    ///////////////////////////////////////////////////////////////////////////
    // Private Methods
    ///////////////////////////////////////////////////////////////////////////

    ///////////////////////////////////////////////////////////////////////////
    // Inner Classes
    ///////////////////////////////////////////////////////////////////////////

    ///////////////////////////////////////////////////////////////////////////
    // Initialization
    ///////////////////////////////////////////////////////////////////////////

}
