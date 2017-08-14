package org.iot.dsa.time;

import org.iot.dsa.node.DSElement;
import org.iot.dsa.node.DSIValue;
import org.iot.dsa.node.DSRegistry;
import org.iot.dsa.node.DSString;
import org.iot.dsa.node.DSValueType;

/**
 * Wrapper for Java time.
 *
 * @author Aaron Hansen
 */
public class DSDateTime implements DSIValue {

    ///////////////////////////////////////////////////////////////////////////
    // Constants
    ///////////////////////////////////////////////////////////////////////////

    public static final DSDateTime NULL = new DSDateTime("null", 0l);

    ///////////////////////////////////////////////////////////////////////////
    // Fields
    ///////////////////////////////////////////////////////////////////////////

    private Long millis;
    private String string;

    ///////////////////////////////////////////////////////////////////////////
    // Constructors
    ///////////////////////////////////////////////////////////////////////////

    DSDateTime(long millis) {
        this.millis = millis;
    }

    DSDateTime(String string) {
        this.string = string;
    }

    DSDateTime(String string, Long millis) {
        this.string = string;
        this.millis = millis;
    }

    ///////////////////////////////////////////////////////////////////////////
    // Methods in alphabetical order
    ///////////////////////////////////////////////////////////////////////////

    @Override
    public DSDateTime copy() {
        return this;
    }

    @Override
    public DSDateTime decode(DSElement element) {
        if ((element == null) || element.isNull()) {
            return NULL;
        }
        return valueOf(element.toString());
    }

    @Override
    public DSElement encode() {
        return DSString.valueOf(toString());
    }

    /**
     * String.
     */
    @Override
    public DSValueType getValueType() {
        return DSValueType.STRING;
    }

    @Override
    public boolean isNull() {
        return this == NULL;
    }

    /**
     * The Java time represented by this object.
     */
    public long timeInMillis() {
        if (millis == null) {
            millis = DSTime.decode(string);
        }
        return millis;
    }

    /**
     * ISO 8601 standard format of "yyyy-mm-ddThh:mm:ss.mmm[+/-]hh:mm".
     */
    @Override
    public String toString() {
        if (string == null) {
            string = DSTime.encode(millis, true).toString();
        }
        return string;
    }

    public static DSDateTime valueOf(long millis) {
        return new DSDateTime(millis);
    }

    /**
     * Decodes an ISO 8601 standard format of "yyyy-mm-ddThh:mm:ss.mmm[+/-]hh:mm".
     */
    public static DSDateTime valueOf(String string) {
        if (string == null) {
            return NULL;
        }
        if ("null".equals(string)) {
            return NULL;
        }
        return new DSDateTime(string);
    }

    ///////////////////////////////////////////////////////////////////////////
    // Inner Classes
    ///////////////////////////////////////////////////////////////////////////

    ///////////////////////////////////////////////////////////////////////////
    // Initialization
    ///////////////////////////////////////////////////////////////////////////

    static {
        DSRegistry.registerNull(DSDateTime.class, NULL);
    }

}
