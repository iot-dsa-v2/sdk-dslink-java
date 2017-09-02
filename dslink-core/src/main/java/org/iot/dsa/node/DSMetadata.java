package org.iot.dsa.node;

/**
 * Utility fon constructing metadata maps.
 *
 * @author Aaron Hansen
 */
public class DSMetadata {

    ///////////////////////////////////////////////////////////////////////////
    // Constants
    ///////////////////////////////////////////////////////////////////////////

    public static final String BOOLEAN_RANGE = "booleanRange";
    public static final String DESCRIPTION = "description";
    public static final String DECIMAL_PLACES = "decimalPlaces";
    public static final String DEFAULT = "default";
    public static final String EDITOR = "editor";
    public static final String ENUM_RANGE = "enumRange";
    public static final String NAME = "name";
    public static final String MAX_VALUE = "maxValue";
    public static final String MIN_VALUE = "minValue";
    public static final String PLACEHOLDER = "placeholder";
    public static final String TYPE = "type";
    public static final String UNIT = "unit";

    //public static final String EDITOR_DATE = "date";
    //public static final String EDITOR_DATE_RANGE = "daterange";
    //public static final String EDITOR_FILE_INPUT = "fileinput";
    //public static final String EDITOR_PASSWORD = "password";
    //public static final String EDITOR_TEXT_AREA = "textarea";

    ///////////////////////////////////////////////////////////////////////////
    // Fields
    ///////////////////////////////////////////////////////////////////////////

    private DSMap map;

    ///////////////////////////////////////////////////////////////////////////
    // Constructors
    ///////////////////////////////////////////////////////////////////////////

    public DSMetadata() {
        this.map = new DSMap();
    }

    public DSMetadata(DSMap map) {
        this.map = map;
    }

    ///////////////////////////////////////////////////////////////////////////
    // Methods in alphabetical order
    ///////////////////////////////////////////////////////////////////////////

    public DSMetadata clear() {
        map.clear();
        return this;
    }

    public DSMetadata clear(String key) {
        map.remove(key);
        return this;
    }

    public DSMap getMap() {
        return map;
    }

    /**
     * The boolean range, or null.  If not null, the length will be 2, index 0 will be the false
     * text and index 1 the true text.
     */
    public DSList getBooleanRange() {
        return (DSList) map.get(BOOLEAN_RANGE);
    }

    /**
     * The decimal precision or null.
     */
    public DSLong getDecimalPlaces() {
        return (DSLong) map.get(DECIMAL_PLACES);
    }

    /**
     * The default value for an action parameter, or null.
     */
    public DSElement getDefault() {
        return map.get(DEFAULT);
    }

    /**
     * The description, or null.
     */
    public String getDescription() {
        return map.getString(DESCRIPTION);
    }

    /**
     * The editor, or null.
     */
    public String getEditor() {
        return map.getString(EDITOR);
    }

    /**
     * The editor, or null.
     */
    public DSList getEnumRange() {
        return map.getList(ENUM_RANGE);
    }

    /**
     * The max value, or null.
     */
    public DSElement getMaxValue() {
        return map.get(MAX_VALUE);
    }

    /**
     * The min value, or null.
     */
    public DSElement getMinValue() {
        return map.get(MIN_VALUE);
    }

    /**
     * The name, or null.
     */
    public String getName() {
        return map.getString(NAME);
    }

    /**
     * Placeholder text for text fields, or null.
     */
    public String getPlaceHolder() {
        return map.getString(PLACEHOLDER);
    }

    /**
     * The type for action parameters, can be used to override types in the responder api.
     */
    public DSValueType getType() {
        String s = map.getString(TYPE);
        if (s == null) {
            return null;
        }
        return DSValueType.valueOf(s);
    }

    /**
     * Value if defined, otherwise null.
     */
    public String getUnit() {
        return map.getString(UNIT);
    }

    public boolean isEmpty() {
        return map.isEmpty();
    }

    /**
     * Set arbitrary keys.
     */
    public DSMetadata set(String key, DSElement value) {
        if (value != null) {
            map.put(key, value);
        }
        return this;
    }

    /**
     * The list must be size 2 and the entries must not be null.
     */
    public DSMetadata setBooleanRange(DSList range) {
        if (range.size() != 2) {
            throw new IllegalStateException();
        }
        if (range.get(0).isNull() || range.get(1).isNull()) {
            throw new NullPointerException();
        }
        map.put(BOOLEAN_RANGE, range);
        return this;
    }

    /**
     * The parameters can be null, which will result in the default text (false/true).
     */
    public DSMetadata setBooleanRange(String falseText, String trueText) {
        DSList list = new DSList();
        list.add(falseText == null ? "false" : falseText);
        list.add(trueText == null ? "true" : trueText);
        map.put(BOOLEAN_RANGE, list);
        return this;
    }

    /**
     * Sets the default value only, does not set type information.
     */
    public DSMetadata setDefault(DSIValue arg) {
        if (arg == null) {
            return this;
        }
        map.put(DEFAULT, arg.encode());
        return this;
    }

    public DSMetadata setDecimalPlaces(DSLong arg) {
        if (arg != null) {
            map.put(DECIMAL_PLACES, arg);
        }
        return this;
    }

    public DSMetadata setDescription(String arg) {
        if (arg != null) {
            map.put(DESCRIPTION, arg);
        }
        return this;
    }

    /**
     * See the EDITOR_ constants.
     */
    public DSMetadata setEditor(String arg) {
        if (arg != null) {
            map.put(EDITOR, arg);
        }
        return this;
    }

    /**
     * See the EDITOR_ constants.
     */
    public DSMetadata setEnumRange(DSList arg) {
        if (arg != null) {
            map.put(ENUM_RANGE, arg);
        }
        return this;
    }

    /**
     * Change the underlying map so the metadata instance can be reused.
     */
    public DSMetadata setMap(DSMap arg) {
        if (arg == null) {
            this.map = new DSMap();
        } else {
            this.map = arg;
        }
        return this;
    }

    /**
     * The arg should be a number.
     */
    public DSMetadata setMaxValue(DSElement arg) {
        if (arg != null) {
            map.put(MIN_VALUE, arg);
        }
        return this;
    }

    /**
     * The arg should be a number.
     */
    public DSMetadata setMinValue(DSElement arg) {
        if (arg != null) {
            map.put(MIN_VALUE, arg);
        }
        return this;
    }

    public DSMetadata setName(String arg) {
        if (arg != null) {
            map.put(NAME, arg);
        }
        return this;
    }

    /**
     * Place holder text for text fields.
     */
    public DSMetadata setPlaceHolder(String arg) {
        if (arg != null) {
            map.put(PLACEHOLDER, arg);
        }
        return this;
    }

    /**
     * Sets the type and if the given is an enum, sets the enum range as well.
     */
    public DSMetadata setType(DSIValue arg) {
        if (arg != null) {
            map.put(TYPE, arg.getValueType().toString());
        }
        if (arg instanceof DSIEnum) {
            setEnumRange(((DSIEnum) arg).getEnums(new DSList()));
        }
        return this;
    }

    /**
     * The type for action parameters, can be used to override types in the responder api.
     */
    public DSMetadata setType(DSValueType arg) {
        if (arg != null) {
            map.put(TYPE, arg.toString());
        }
        return this;
    }

    /**
     * The unit identifier.
     */
    public DSMetadata setUnit(String arg) {
        if (arg != null) {
            map.put(UNIT, arg);
        }
        return this;
    }

    ///////////////////////////////////////////////////////////////////////////
    // Inner Classes
    ///////////////////////////////////////////////////////////////////////////

    ///////////////////////////////////////////////////////////////////////////
    // Initialization
    ///////////////////////////////////////////////////////////////////////////

}
