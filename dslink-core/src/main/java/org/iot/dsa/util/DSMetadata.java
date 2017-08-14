package org.iot.dsa.util;

import java.util.List;
import org.iot.dsa.node.DSElement;
import org.iot.dsa.node.DSIEnum;
import org.iot.dsa.node.DSIValue;
import org.iot.dsa.node.DSList;
import org.iot.dsa.node.DSMap;
import org.iot.dsa.node.DSValueType;

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
    public static final String DEFAULT = "default";
    public static final String ENUM_RANGE = "enumRange";
    public static final String EDITOR = "editor";
    public static final String NAME = "name";
    public static final String MAX_VALUE = "maxValue";
    public static final String MIN_VALUE = "minValue";
    public static final String PLACEHOLDER = "placeholder";
    public static final String TYPE = "type";
    public static final String UNIT = "unit";

    public static final String EDITOR_DATE = "date";
    public static final String EDITOR_DATE_RANGE = "daterange";
    public static final String EDITOR_FILE_INPUT = "fileinput";
    public static final String EDITOR_PASSWORD = "password";
    public static final String EDITOR_TEXT_AREA = "textarea";

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
    public DSList getBooleanText() {
        return (DSList) map.get(BOOLEAN_RANGE);
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

    public DSList getEnumRange() {
        return (DSList) map.get(ENUM_RANGE);
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

    public DSMetadata setBooleanText(String trueText, String falseText) {
        DSList list = new DSList();
        list.add(falseText == null ? "false" : falseText);
        list.add(trueText == null ? "true" : trueText);
        map.put(BOOLEAN_RANGE, list);
        return this;
    }

    /**
     * This will set the TYPE and DEFAULT, and if the arg is an enum, it will also set ENUM_RANGE.
     */
    public DSMetadata setDefault(DSIValue arg) {
        if (arg == null) {
            return this;
        }
        if (arg instanceof DSIEnum) {
            List<String> range = ((DSIEnum) arg).getEnums();
            DSList list = new DSList();
            for (String s : range) {
                list.add(s);
            }
            map.put(ENUM_RANGE, list);
        }
        map.put(TYPE, arg.getValueType().toString());
        map.put(DEFAULT, arg.encode());
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
