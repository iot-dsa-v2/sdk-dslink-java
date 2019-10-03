package org.iot.dsa.node;

import org.iot.dsa.util.DSUtil;

/**
 * Combines a boolean with true text and false text.
 *
 * @author Aaron Hansen
 */
public class DSBoolRange extends DSValue implements DSIBoolean, DSIMetadata, DSIStorable {

    ///////////////////////////////////////////////////////////////////////////
    // Class Fields
    ///////////////////////////////////////////////////////////////////////////

    public static final DSBoolRange ACTIVE_INACTIVE = new DSBoolRange(
            DSBool.NULL, DSList.valueOf("Inactive", "Active"));
    public static final DSBoolRange ENABLED_DISABLED = new DSBoolRange(
            DSBool.NULL, DSList.valueOf("Disabled", "Enabled"));
    public static final DSBoolRange NULL = new DSBoolRange(
            DSBool.NULL, DSList.valueOf("false", "true"));
    public static final DSBoolRange ON_OFF = new DSBoolRange(
            DSBool.NULL, DSList.valueOf("Off", "On"));

    ///////////////////////////////////////////////////////////////////////////
    // Instance Fields
    ///////////////////////////////////////////////////////////////////////////

    private DSList range;
    private DSBool val;

    ///////////////////////////////////////////////////////////////////////////
    // Constructors
    ///////////////////////////////////////////////////////////////////////////

    private DSBoolRange(DSBool val, DSList range) {
        if (val == null) {
            throw new NullPointerException("Null value");
        }
        if (range == null) {
            throw new NullPointerException("Null range");
        }
        if (range.size() != 2) {
            throw new NullPointerException("Invalid range: " + range);
        }
        this.val = val;
        this.range = range;
    }

    ///////////////////////////////////////////////////////////////////////////
    // Methods in alphabetical order
    ///////////////////////////////////////////////////////////////////////////

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof DSBoolRange) {
            DSBoolRange dt = (DSBoolRange) obj;
            return val.equals(dt.val) && DSUtil.equal(range, dt.range);
        }
        return false;
    }

    public String getFalseText() {
        return range.getString(0);
    }

    @Override
    public void getMetadata(DSMap bucket) {
        bucket.put(DSMetadata.BOOLEAN_RANGE, range.copy());
    }

    public String getTrueText() {
        return range.getString(1);
    }

    /**
     * Bool.
     */
    @Override
    public DSValueType getValueType() {
        return DSValueType.BOOL;
    }

    @Override
    public int hashCode() {
        return toString().hashCode();
    }

    @Override
    public boolean isNull() {
        return this == NULL;
    }

    /**
     * Restores the value and range from a map created by store()
     */
    @Override
    public DSIValue restore(DSElement map) {
        if (!map.isMap()) {
            throw new IllegalStateException("Not a map");
        }
        DSMap tmp = map.toMap();
        DSBool b = (DSBool) tmp.get("v");
        if (val.isNull()) {
            return NULL;
        }
        DSList r = (DSList) tmp.get("r");
        return new DSBoolRange(b, r);
    }

    /**
     * Stores the value and range in a map.
     */
    @Override
    public DSElement store() {
        return new DSMap().put("v", val).put("r", range);
    }

    @Override
    public boolean toBoolean() {
        return val.toBoolean();
    }

    /**
     * Returns the boolean value.
     */
    @Override
    public DSBool toElement() {
        return val;
    }

    @Override
    public String toString() {
        if (isNull()) {
            return "null";
        }
        if (toBoolean()) {
            return getTrueText();
        }
        return getFalseText();
    }

    public static DSBoolRange valueOf(boolean val, String falseText, String trueText) {
        return valueOf(DSBool.valueOf(val), falseText, trueText);
    }

    public static DSBoolRange valueOf(DSBool val, String falseText, String trueText) {
        if (trueText == null) {
            throw new NullPointerException("trueText cannot be null");
        }
        if (falseText == null) {
            throw new NullPointerException("falseText cannot be null");
        }
        return new DSBoolRange(val, new DSList().add(falseText).add(trueText));
    }

    /**
     * Can handle a boolean or string.
     */
    @Override
    public DSBoolRange valueOf(DSElement element) {
        if (element instanceof DSBool) {
            if (DSUtil.equal(element, val)) {
                return this;
            }
            return valueOf((DSBool)element, range);
        }
        return valueOf(element.toString());
    }

    /**
     * Must equal the true or false text.
     */
    public DSBoolRange valueOf(String text) {
        if (getTrueText().equals(text)) {
            return valueOf(DSBool.TRUE);
        }
        if (getFalseText().equals(text)) {
            return valueOf(DSBool.TRUE);
        }
        throw new IllegalArgumentException("Unknown boolean tag: " + text);
    }

    ///////////////////////////////////////////////////////////////////////////
    // Private Methods
    ///////////////////////////////////////////////////////////////////////////

    public static DSBoolRange valueOf(DSBool val, DSList range) {
        if ((val == null) || val.isNull()) {
            return NULL;
        }
        if (ACTIVE_INACTIVE.equals(val, range)) {
            return ACTIVE_INACTIVE;
        }
        if (ENABLED_DISABLED.equals(val, range)) {
            return ENABLED_DISABLED;
        }
        if (ON_OFF.equals(val, range)) {
            return ON_OFF;
        }
        return new DSBoolRange(val, range);
    }

    private boolean equals(DSBool val, DSList range) {
        return this.val.equals(val) && this.range.equals(range);
    }

    ///////////////////////////////////////////////////////////////////////////
    // Initialization
    ///////////////////////////////////////////////////////////////////////////

    static {
        DSRegistry.registerDecoder(DSBoolRange.class, NULL);
    }

}
