package org.iot.dsa.table;

import java.util.ArrayList;
import org.iot.dsa.node.DSIMetadata;
import org.iot.dsa.node.DSIValue;
import org.iot.dsa.node.DSMap;
import org.iot.dsa.node.DSMetadata;
import org.iot.dsa.node.DSValueType;

/**
 * Used for building the SimpleTable and SimpleTrend.
 *
 * @author Aaron Hansen
 * @see SimpleTable
 * @see SimpleTrend
 */
public class AbstractTable {

    ///////////////////////////////////////////////////////////////////////////
    // Instance Fields
    ///////////////////////////////////////////////////////////////////////////

    protected ArrayList<DSMap> columns = new ArrayList<DSMap>();
    protected ArrayList<DSIValue[]> rows = new ArrayList<DSIValue[]>();

    ///////////////////////////////////////////////////////////////////////////
    // Public Methods
    ///////////////////////////////////////////////////////////////////////////

    public int columnCount() {
        return columns.size();
    }

    public DSMap getColumn(int idx) {
        return columns.get(idx);
    }

    public DSIValue[] getRow(int idx) {
        return rows.get(idx);
    }

    public int rowCount() {
        return rows.size();
    }

    ///////////////////////////////////////////////////////////////////////////
    // Protected Methods
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Fully describes a return value when the result type is VALUES.  Must be added in
     * the order that the values will be returned. At the very least, the map should have
     * a unique name and a value type, use the DSMetadata utility class.
     *
     * @see DSMetadata
     */
    protected void appendCol(DSMap metadata) {
        columns.add(metadata);
    }


    /**
     * Creates a DSMetadata, calls setName and setType on it, adds the internal map to
     * the results list and returns the metadata instance for further configuration.
     *
     * @param name  Must not be null.
     * @param value Must not be null.
     * @return Metadata for further configuration.
     */
    protected DSMetadata appendCol(String name, DSIValue value) {
        DSMetadata ret = new DSMetadata();
        if (value instanceof DSIMetadata) {
            ((DSIMetadata) value).getMetadata(ret.getMap());
        }
        ret.setName(name).setType(value);
        appendCol(ret.getMap());
        return ret;
    }

    /**
     * Creates a DSMetadata, calls setName and setType on it, adds the internal map to
     * the results list and returns the metadata instance for further configuration.
     *
     * @param name Must not be null.
     * @param type Must not be null.
     * @return Metadata for further configuration.
     */
    protected DSMetadata appendCol(String name, DSValueType type) {
        DSMetadata ret = new DSMetadata();
        ret.setName(name).setType(type);
        appendCol(ret.getMap());
        return ret;
    }

    protected void appendRow(DSIValue... row) {
        rows.add(row);
    }

    ///////////////////////////////////////////////////////////////////////////
    // Package / Private Methods
    ///////////////////////////////////////////////////////////////////////////

    ///////////////////////////////////////////////////////////////////////////
    // Inner Classes
    ///////////////////////////////////////////////////////////////////////////

    ///////////////////////////////////////////////////////////////////////////
    // Initialization
    ///////////////////////////////////////////////////////////////////////////

}
