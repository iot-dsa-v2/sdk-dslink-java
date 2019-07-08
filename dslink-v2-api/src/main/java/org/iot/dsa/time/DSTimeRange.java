package org.iot.dsa.time;

import org.iot.dsa.node.DSElement;
import org.iot.dsa.node.DSIMetadata;
import org.iot.dsa.node.DSMap;
import org.iot.dsa.node.DSMetadata;
import org.iot.dsa.node.DSRegistry;
import org.iot.dsa.node.DSString;
import org.iot.dsa.node.DSValue;
import org.iot.dsa.node.DSValueType;

/**
 * Two timestamps representing a time range.  The start is inclusive and the end is exclusive.
 * If either is null, that represents a wildcard.
 *
 * @author Aaron Hansen
 */
public class DSTimeRange extends DSValue implements DSIMetadata {

    ///////////////////////////////////////////////////////////////////////////
    // Constants
    ///////////////////////////////////////////////////////////////////////////

    /**
     * The null time range can be used as a wild card for all time.
     */
    public static final DSTimeRange NULL = new DSTimeRange(DSDateTime.NULL, DSDateTime.NULL);

    ///////////////////////////////////////////////////////////////////////////
    // Fields
    ///////////////////////////////////////////////////////////////////////////

    private DSDateTime from;
    private String string;
    private DSDateTime to;

    ///////////////////////////////////////////////////////////////////////////
    // Constructors
    ///////////////////////////////////////////////////////////////////////////

    DSTimeRange(DSDateTime from, DSDateTime to, String string) {
        this(from, to);
        this.string = string;
    }

    DSTimeRange(DSDateTime from, DSDateTime to) {
        if (to.isBefore(from)) {
            throw new IllegalArgumentException(String.format("Start %s is after end %s", from, to));
        }
        this.from = from;
        this.to = to;
    }

    ///////////////////////////////////////////////////////////////////////////
    // Methods in alphabetical order
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Whether or not the given timestamp is in this range.
     */
    public boolean contains(long timestamp) {
        if (!from.isNull()) {
            if (from.timeInMillis() > timestamp) {
                return false;
            }
        }
        if (!to.isNull()) {
            if (to.timeInMillis() < timestamp) {
                return false;
            }
        }
        return true;
    }

    /**
     * Whether or not the given timestamp is in this range.
     */
    public boolean contains(DSDateTime timestamp) {
        return contains(timestamp.timeInMillis());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof DSTimeRange) {
            DSTimeRange dt = (DSTimeRange) obj;
            return from.equals(dt.from) && to.equals(dt.to);
        }
        return false;
    }

    public DSDateTime getEnd() {
        return to;
    }

    public DSDateTime getFrom() {
        return from;
    }

    @Override
    public void getMetadata(DSMap bucket) {
        bucket.put(DSMetadata.EDITOR, DSMetadata.STR_EDITOR_DATE_RANGE);
    }

    public DSDateTime getStart() {
        return from;
    }

    public DSDateTime getTo() {
        return to;
    }

    /**
     * String.
     */
    @Override
    public DSValueType getValueType() {
        return DSValueType.STRING;
    }

    @Override
    public int hashCode() {
        return toString().hashCode();
    }

    @Override
    public boolean isNull() {
        return this == NULL;
    }

    @Override
    public DSElement toElement() {
        return DSString.valueOf(toString());
    }

    /**
     * Two ISO 8601 standard formats of "yyyy-mm-ddThh:mm:ss.mmm[+/-]hh:mm" separated with
     * a forward slash.
     */
    @Override
    public String toString() {
        if (isNull()) {
            return "null";
        }
        if (string == null) {
            string = from.toString() + '/' + to.toString();
        }
        return string;
    }

    /**
     * Creates a DSDateTime for the given range.
     */
    public static DSTimeRange valueOf(DSDateTime from, DSDateTime to) {
        if (from == null) {
            from = DSDateTime.NULL;
        }
        if (to == null) {
            to = DSDateTime.NULL;
        }
        if (from.isNull() && to.isNull()) {
            return NULL;
        }
        return new DSTimeRange(from, to);
    }

    @Override
    public DSTimeRange valueOf(DSElement element) {
        return valueOf(element.toString());
    }

    /**
     * Decodes 2 ISO 8601 standard format "yyyy-mm-ddThh:mm:ss.mmm[+/-]hh:mm" timestamps separated
     * by a forward slash '/'.
     */
    public static DSTimeRange valueOf(String string) {
        if (string == null) {
            return NULL;
        }
        if ("null".equals(string)) {
            return NULL;
        }
        String[] split = string.split("/");
        DSDateTime from = DSDateTime.valueOf(split[0]);
        DSDateTime to = DSDateTime.valueOf(split[1]);
        if (from.isNull() && to.isNull()) {
            return NULL;
        }
        return new DSTimeRange(from, to, string);
    }

    ///////////////////////////////////////////////////////////////////////////
    // Inner Classes
    ///////////////////////////////////////////////////////////////////////////

    ///////////////////////////////////////////////////////////////////////////
    // Initialization
    ///////////////////////////////////////////////////////////////////////////

    static {
        DSRegistry.registerDecoder(DSTimeRange.class, NULL);
    }

}
