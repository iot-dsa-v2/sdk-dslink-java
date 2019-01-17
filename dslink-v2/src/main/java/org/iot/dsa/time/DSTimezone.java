package org.iot.dsa.time;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;
import org.iot.dsa.node.DSElement;
import org.iot.dsa.node.DSFlexEnum;
import org.iot.dsa.node.DSISetAction;
import org.iot.dsa.node.DSInfo;
import org.iot.dsa.node.DSList;
import org.iot.dsa.node.DSMap;
import org.iot.dsa.node.DSMetadata;
import org.iot.dsa.node.DSRegistry;
import org.iot.dsa.node.DSString;
import org.iot.dsa.node.DSValue;
import org.iot.dsa.node.DSValueType;
import org.iot.dsa.node.action.ActionInvocation;
import org.iot.dsa.node.action.ActionResult;
import org.iot.dsa.node.action.DSAction;

/**
 * Wrapper for a Java timezone.
 *
 * @author Aaron Hansen
 */
public class DSTimezone extends DSValue implements DSISetAction {

    ///////////////////////////////////////////////////////////////////////////
    // Class Fields
    ///////////////////////////////////////////////////////////////////////////

    public static final DSTimezone DEFAULT = new DSTimezone(DSString.valueOf("Default"), null);
    public static final DSTimezone NULL = new DSTimezone(DSString.valueOf("null"), null);
    private static final Map<String, DSTimezone> zones = new HashMap<>();

    ///////////////////////////////////////////////////////////////////////////
    // Fields
    ///////////////////////////////////////////////////////////////////////////

    private DSString id;
    private TimeZone timeZone;

    ///////////////////////////////////////////////////////////////////////////
    // Constructors
    ///////////////////////////////////////////////////////////////////////////

    private DSTimezone(DSString id, TimeZone timeZone) {
        this.id = id;
        this.timeZone = timeZone;
    }

    ///////////////////////////////////////////////////////////////////////////
    // Methods in alphabetical order
    ///////////////////////////////////////////////////////////////////////////

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof DSTimezone) {
            return toString().equals(obj.toString());
        }
        return false;
    }

    @Override
    public DSAction getSetAction() {
        return SetAction.INSTANCE;
    }

    /**
     * The Java TimeZone, NULL and DEFAULT will return TimeZone.getDefault()
     */
    public TimeZone getTimeZone() {
        if (timeZone == null) {
            return TimeZone.getDefault();
        }
        return timeZone;
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

    /**
     * Defaults to the equals method.
     */
    @Override
    public boolean isEqual(Object obj) {
        return equals(obj);
    }

    @Override
    public boolean isNull() {
        return this == NULL;
    }

    /**
     * String representing the zone ID.
     */
    @Override
    public DSElement toElement() {
        return id;
    }

    /**
     * The zone ID.
     */
    @Override
    public String toString() {
        return id.toString();
    }

    /**
     * Expects a string representing the zone ID.
     */
    @Override
    public DSTimezone valueOf(DSElement element) {
        if ((element == null) || element.isNull()) {
            return NULL;
        }
        return valueOf(element.toString());
    }

    /**
     * Returns the timezone for the given zone ID.
     */
    public static DSTimezone valueOf(String string) {
        if (string == null) {
            return NULL;
        }
        if (string.isEmpty()) {
            return NULL;
        }
        if ("default".equalsIgnoreCase(string)) {
            return DEFAULT;
        }
        if ("null".equalsIgnoreCase(string)) {
            return NULL;
        }
        DSTimezone ret = zones.get(string);
        if (ret == null) {
            TimeZone zone = TimeZone.getTimeZone(string);
            if (zone == null) {
                throw new IllegalArgumentException("Unknown timezone: " + string);
            }
            ret = new DSTimezone(DSString.valueOf(string), zone);
            zones.put(string, ret);
        }
        return ret;
    }

    ///////////////////////////////////////////////////////////////////////////
    // Inner Classes
    ///////////////////////////////////////////////////////////////////////////

    public static class SetAction extends DSAction {

        public static final SetAction INSTANCE = new SetAction();

        /**
         * The name of the single parameter.
         */
        public static final String ZONEID = "Zone ID";

        @Override
        public ActionResult invoke(DSInfo target, ActionInvocation invocation) {
            String id = invocation.getParameters().getString(ZONEID);
            if (id == null) {
                throw new IllegalArgumentException("Missing Zone ID");
            }
            target.getParent().put(target, DSTimezone.valueOf(id));
            return null;
        }

        @Override
        public void prepareParameter(DSInfo target, DSMap parameter) {
            parameter.put(DSMetadata.DEFAULT, target.getValue().toElement());
        }

        {
            DSList list = new DSList();
            list.add("Default");
            String[] ary = TimeZone.getAvailableIDs();
            Arrays.sort(ary);
            for (String s : ary) {
                list.add(s);
            }
            DSFlexEnum e = DSFlexEnum.valueOf("Default", list);
            addParameter(ZONEID, e, "For example: America/Los_Angeles");
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // Initialization
    ///////////////////////////////////////////////////////////////////////////

    static {
        DSRegistry.registerDecoder(DSTimezone.class, NULL);
    }

}
