package org.iot.dsa.node;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Can be used to express the status / health / condition of something.
 *
 * @author Aaron Hansen
 */
public class DSQuality implements DSIQuality, DSIValue {

    ///////////////////////////////////////////////////////////////////////////
    // Constants
    ///////////////////////////////////////////////////////////////////////////

    private static ConcurrentHashMap<Integer, DSQuality> intCache =
            new ConcurrentHashMap<Integer, DSQuality>();
    private static ConcurrentHashMap<String, Integer> stringCache =
            new ConcurrentHashMap<String, Integer>();

    public static final DSQuality NULL = new DSQuality(0xFFFF);

    //The bit for each unique quality
    public static final int OK = 0;
    public static final int GOOD_OVERRIDE = 0x0001;
    public static final int GOOD_REMOTE_OVERRIDE = 0x0002;
    public static final int GOOD_ALARM = 0x0004;
    public static final int GOOD_REMOTE_ALARM = 0x0008;
    public static final int BAD_COMMS_DOWN = 0x0010;
    public static final int BAD_REMOTE_COMMS_DOWN = 0x0020;
    public static final int BAD_CONFIG_FAULT = 0x0040;
    public static final int BAD_REMOTE_CONFIG_FAULT = 0x0080;
    public static final int BAD_FAILURE = 0x0100;
    public static final int BAD_REMOTE_FAILURE = 0x0200;
    public static final int BAD_DISABLED = 0x0400;
    public static final int BAD_REMOTE_DISABLED = 0x0800;
    public static final int BAD_UNKNOWN = 0x1000;
    public static final int BAD_REMOTE_UNKNOWN = 0x2000;

    //The string for each unique quality.
    public static final String OK_STR = "OK";
    public static final String GOOD_OVERRIDE_STR = "Override";
    public static final String GOOD_REMOTE_OVERRIDE_STR = "Remote Override";
    public static final String GOOD_ALARM_STR = "Alarm";
    public static final String GOOD_REMOTE_ALARM_STR = "Remote Alarm";
    public static final String BAD_COMMS_DOWN_STR = "Comms Down";
    public static final String BAD_REMOTE_COMMS_DOWN_STR = "Remote Comms Down";
    public static final String BAD_CONFIG_FAULT_STR = "Config Fault";
    public static final String BAD_REMOTE_CONFIG_FAULT_STR = "Remote Config Fault";
    public static final String BAD_FAILURE_STR = "Failure";
    public static final String BAD_REMOTE_FAILURE_STR = "Remote Failure";
    public static final String BAD_DISABLED_STR = "Disabled";
    public static final String BAD_REMOTE_DISABLED_STR = "Remote Disabled";
    public static final String BAD_UNKNOWN_STR = "Unknown";
    public static final String BAD_REMOTE_UNKNOWN_STR = "Remote Unknown";

    //The instance for each unique quality.
    public static final DSQuality ok = new DSQuality(0);
    public static final DSQuality goodOverride = valueOf(GOOD_OVERRIDE);
    public static final DSQuality goodRemoteOverride = valueOf(GOOD_REMOTE_OVERRIDE);
    public static final DSQuality goodAlarm = valueOf(GOOD_ALARM);
    public static final DSQuality goodRemoteAlarm = valueOf(GOOD_REMOTE_ALARM);
    public static final DSQuality badCommsDown = valueOf(BAD_COMMS_DOWN);
    public static final DSQuality badRemoteCommsDown = valueOf(BAD_REMOTE_COMMS_DOWN);
    public static final DSQuality badConfigFault = valueOf(BAD_CONFIG_FAULT);
    public static final DSQuality badRemoteConfigFault = valueOf(BAD_REMOTE_CONFIG_FAULT);
    public static final DSQuality badFailure = valueOf(BAD_FAILURE);
    public static final DSQuality badRemoteFailure = valueOf(BAD_REMOTE_FAILURE);
    public static final DSQuality badDisabled = valueOf(BAD_DISABLED);
    public static final DSQuality badRemoteDisabled = valueOf(BAD_REMOTE_DISABLED);
    public static final DSQuality badUnknown = valueOf(BAD_UNKNOWN);
    public static final DSQuality badRemoteUnknown = valueOf(BAD_REMOTE_UNKNOWN);

    ///////////////////////////////////////////////////////////////////////////
    // Fields
    ///////////////////////////////////////////////////////////////////////////

    private int bits;

    ///////////////////////////////////////////////////////////////////////////
    // Constructors
    ///////////////////////////////////////////////////////////////////////////

    DSQuality(int bits) {
        this.bits = bits;
    }

    ///////////////////////////////////////////////////////////////////////////
    // Methods in alphabetical order
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Inserts commas and spaces if necessary.
     */
    private void append(StringBuilder buf, String str) {
        if (buf.length() > 0) {
            buf.append(',').append(' ');
        }
        buf.append(str);
    }

    @Override
    public DSQuality copy() {
        return this;
    }

    @Override
    public DSQuality decode(DSElement element) {
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
     * Finds the bit representation for the given status string.
     *
     * @throws IllegalArgumentException If the string is unknown.
     */
    static Integer getBit(String s) {
        String orig = s;
        s = s.trim();
        Integer i = stringCache.get(s);
        if (i == null) {
            s = s.toLowerCase();
            i = stringCache.get(s);
        }
        if (i == null) {
            throw new IllegalArgumentException("Unknown quality: " + orig);
        }
        return i;
    }

    /**
     * The bitset representing the all the set qualities.
     */
    public int getBits() {
        return bits;
    }

    /**
     * String.
     */
    @Override
    public DSValueType getValueType() {
        return DSValueType.STRING;
    }

    /**
     * If any of the bad flags are set, or is null.
     */
    public boolean isBad() {
        return (bits & 0x1110) != 0;
    }

    /**
     * True if the associate bit is set.
     */
    public boolean isBadLocalCommsDown() {
        return (BAD_COMMS_DOWN & bits) != 0;
    }

    /**
     * True if the associate bit is set.
     */
    public boolean isBadRemoteCommsDown() {
        return (BAD_REMOTE_COMMS_DOWN & bits) != 0;
    }

    /**
     * True if the associate bit is set.
     */
    public boolean isBadLocalConfigFault() {
        return (BAD_CONFIG_FAULT & bits) != 0;
    }

    /**
     * True if the associate bit is set.
     */
    public boolean isBadRemoteConfigFault() {
        return (BAD_REMOTE_CONFIG_FAULT & bits) != 0;
    }

    /**
     * True if the associate bit is set.
     */
    public boolean isBadLocalFailure() {
        return (BAD_FAILURE & bits) != 0;
    }

    /**
     * True if the associate bit is set.
     */
    public boolean isBadRemoteFailure() {
        return (BAD_REMOTE_FAILURE & bits) != 0;
    }

    /**
     * True if the associate bit is set.
     */
    public boolean isBadLocalDisabled() {
        return (BAD_DISABLED & bits) != 0;
    }

    /**
     * True if the associate bit is set.
     */
    public boolean isBadRemoteDisabled() {
        return (BAD_REMOTE_DISABLED & bits) != 0;
    }

    /**
     * True if the associate bit is set.
     */
    public boolean isBadLocalUnknown() {
        return (BAD_UNKNOWN & bits) != 0;
    }

    /**
     * True if the associate bit is set.
     */
    public boolean isBadRemoteUnknown() {
        return (BAD_REMOTE_UNKNOWN & bits) != 0;
    }

    /**
     * If true, any associate object / value can be trusted.
     */
    public boolean isGood() {
        return !isBad();
    }

    /**
     * True if the associate bit is set.
     */
    public boolean isGoodLocalOverride() {
        return (GOOD_OVERRIDE & bits) != 0;
    }

    /**
     * True if the associate bit is set.
     */
    public boolean isGoodRemoteOverride() {
        return (GOOD_REMOTE_OVERRIDE & bits) != 0;
    }

    /**
     * True if the associate bit is set.
     */
    public boolean isGoodLocalAlarm() {
        return (GOOD_ALARM & bits) != 0;
    }

    /**
     * True if the associate bit is set.
     */
    public boolean isGoodRemoteAlarm() {
        return (GOOD_REMOTE_ALARM & bits) != 0;
    }

    @Override
    public boolean isNull() {
        return this == NULL;
    }

    public boolean isOk() {
        return bits == 0;
    }

    @Override
    public String toString() {
        if (isNull()) {
            return "null";
        }
        StringBuilder buf = new StringBuilder();
        if (isBad()) {
            if (isBadRemoteUnknown()) {
                append(buf, BAD_REMOTE_UNKNOWN_STR);
            }
            if (isBadLocalUnknown()) {
                append(buf, BAD_UNKNOWN_STR);
            }
            if (isBadRemoteDisabled()) {
                append(buf, BAD_REMOTE_DISABLED_STR);
            }
            if (isBadLocalDisabled()) {
                append(buf, BAD_DISABLED_STR);
            }
            if (isBadRemoteFailure()) {
                append(buf, BAD_REMOTE_FAILURE_STR);
            }
            if (isBadLocalFailure()) {
                append(buf, BAD_FAILURE_STR);
            }
            if (isBadRemoteConfigFault()) {
                append(buf, BAD_REMOTE_CONFIG_FAULT_STR);
            }
            if (isBadLocalConfigFault()) {
                append(buf, BAD_CONFIG_FAULT_STR);
            }
            if (isBadRemoteCommsDown()) {
                append(buf, BAD_REMOTE_COMMS_DOWN_STR);
            }
            if (isBadLocalCommsDown()) {
                append(buf, BAD_COMMS_DOWN_STR);
            }
        }
        if (isGood()) {
            if (isGoodRemoteAlarm()) {
                append(buf, GOOD_REMOTE_ALARM_STR);
            }
            if (isGoodLocalAlarm()) {
                append(buf, GOOD_ALARM_STR);
            }
            if (isGoodRemoteOverride()) {
                append(buf, GOOD_REMOTE_OVERRIDE_STR);
            }
            if (isGoodLocalOverride()) {
                append(buf, GOOD_OVERRIDE_STR);
            }
        }
        return null;
    }

    @Override
    public DSQuality toQuality() {
        return this;
    }

    public static DSQuality valueOf(int bits) {
        if (bits == NULL.bits) {
            return NULL;
        }
        if (bits == 0) {
            return ok;
        }
        DSQuality ret = intCache.get(bits);
        if (ret != null) {
            return ret;
        }
        ret = new DSQuality(bits);
        intCache.put(bits, ret);
        return ret;
    }

    public static DSQuality valueOf(String string) {
        String[] strings = string.split(",");
        int tmp = 0;
        for (String s : strings) {
            tmp = tmp | getBit(s);
        }
        return valueOf(tmp);
    }

    public static DSQuality valueOf(String[] strings) {
        int tmp = 0;
        for (String s : strings) {
            tmp = tmp | getBit(s);
        }
        return valueOf(tmp);
    }

    ///////////////////////////////////////////////////////////////////////////
    // Inner Classes
    ///////////////////////////////////////////////////////////////////////////

    ///////////////////////////////////////////////////////////////////////////
    // Initialization
    ///////////////////////////////////////////////////////////////////////////

    static {
        DSRegistry.registerNull(DSQuality.class, NULL);
        stringCache.put("null", NULL.bits);
        stringCache.put(OK_STR, OK);
        stringCache.put(GOOD_OVERRIDE_STR, GOOD_OVERRIDE);
        stringCache.put(GOOD_REMOTE_OVERRIDE_STR, GOOD_REMOTE_OVERRIDE);
        stringCache.put(GOOD_ALARM_STR, GOOD_ALARM);
        stringCache.put(GOOD_REMOTE_ALARM_STR, GOOD_REMOTE_ALARM);
        stringCache.put(BAD_COMMS_DOWN_STR, BAD_COMMS_DOWN);
        stringCache.put(BAD_REMOTE_COMMS_DOWN_STR, BAD_REMOTE_COMMS_DOWN);
        stringCache.put(BAD_CONFIG_FAULT_STR, BAD_CONFIG_FAULT);
        stringCache.put(BAD_REMOTE_CONFIG_FAULT_STR, BAD_REMOTE_CONFIG_FAULT);
        stringCache.put(BAD_FAILURE_STR, BAD_FAILURE);
        stringCache.put(BAD_REMOTE_FAILURE_STR, BAD_REMOTE_FAILURE);
        stringCache.put(BAD_DISABLED_STR, BAD_DISABLED);
        stringCache.put(BAD_REMOTE_DISABLED_STR, BAD_REMOTE_DISABLED);
        stringCache.put(BAD_UNKNOWN_STR, BAD_UNKNOWN);
        stringCache.put(BAD_REMOTE_UNKNOWN_STR, BAD_REMOTE_UNKNOWN);
        //toLowerCase
        stringCache.put(OK_STR.toLowerCase(), OK);
        stringCache.put(GOOD_OVERRIDE_STR.toLowerCase(), GOOD_OVERRIDE);
        stringCache.put(GOOD_REMOTE_OVERRIDE_STR.toLowerCase(), GOOD_REMOTE_OVERRIDE);
        stringCache.put(GOOD_ALARM_STR.toLowerCase(), GOOD_ALARM);
        stringCache.put(GOOD_REMOTE_ALARM_STR.toLowerCase(), GOOD_REMOTE_ALARM);
        stringCache.put(BAD_COMMS_DOWN_STR.toLowerCase(), BAD_COMMS_DOWN);
        stringCache.put(BAD_REMOTE_COMMS_DOWN_STR.toLowerCase(), BAD_REMOTE_COMMS_DOWN);
        stringCache.put(BAD_CONFIG_FAULT_STR.toLowerCase(), BAD_CONFIG_FAULT);
        stringCache.put(BAD_REMOTE_CONFIG_FAULT_STR.toLowerCase(), BAD_REMOTE_CONFIG_FAULT);
        stringCache.put(BAD_FAILURE_STR.toLowerCase(), BAD_FAILURE);
        stringCache.put(BAD_REMOTE_FAILURE_STR.toLowerCase(), BAD_REMOTE_FAILURE);
        stringCache.put(BAD_DISABLED_STR.toLowerCase(), BAD_DISABLED);
        stringCache.put(BAD_REMOTE_DISABLED_STR.toLowerCase(), BAD_REMOTE_DISABLED);
        stringCache.put(BAD_UNKNOWN_STR.toLowerCase(), BAD_UNKNOWN);
        stringCache.put(BAD_REMOTE_UNKNOWN_STR.toLowerCase(), BAD_REMOTE_UNKNOWN);
    }

}
