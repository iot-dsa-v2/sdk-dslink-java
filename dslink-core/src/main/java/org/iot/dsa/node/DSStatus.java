package org.iot.dsa.node;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Can be used to express the quality / health / condition of something.
 *
 * @author Aaron Hansen
 */
public class DSStatus extends DSValue implements DSIStatus {

    ///////////////////////////////////////////////////////////////////////////
    // Constants
    ///////////////////////////////////////////////////////////////////////////

    private static ConcurrentHashMap<Integer, DSStatus> intCache =
            new ConcurrentHashMap<Integer, DSStatus>();
    private static ConcurrentHashMap<String, Integer> stringCache =
            new ConcurrentHashMap<String, Integer>();

    public static final DSStatus NULL = new DSStatus(0xFFFF);

    //The bit for each unique status
    public static final int OK = 0;
    public static final int GOOD_OVERRIDE = 0x0001;
    public static final int GOOD_ALARM = 0x0002;
    public static final int BAD_COMMS_DOWN = 0x0004;
    public static final int BAD_CONFIG_FAULT = 0x0008;
    public static final int BAD_FAILURE = 0x0010;
    public static final int BAD_DISABLED = 0x0020;
    public static final int BAD_UNKNOWN = 0x0040;

    //The string for each unique status.
    public static final String OK_STR = "OK";
    public static final String GOOD_OVERRIDE_STR = "Override";
    public static final String GOOD_ALARM_STR = "Alarm";
    public static final String BAD_COMMS_DOWN_STR = "Comms Down";
    public static final String BAD_CONFIG_FAULT_STR = "Config Fault";
    public static final String BAD_FAILURE_STR = "Failure";
    public static final String BAD_DISABLED_STR = "Disabled";
    public static final String BAD_UNKNOWN_STR = "Unknown";

    //The instance for each unique status.
    public static final DSStatus ok = new DSStatus(0);
    public static final DSStatus goodOverride = valueOf(GOOD_OVERRIDE);
    public static final DSStatus goodAlarm = valueOf(GOOD_ALARM);
    public static final DSStatus badCommsDown = valueOf(BAD_COMMS_DOWN);
    public static final DSStatus badConfigFault = valueOf(BAD_CONFIG_FAULT);
    public static final DSStatus badFailure = valueOf(BAD_FAILURE);
    public static final DSStatus badDisabled = valueOf(BAD_DISABLED);
    public static final DSStatus badUnknown = valueOf(BAD_UNKNOWN);

    ///////////////////////////////////////////////////////////////////////////
    // Fields
    ///////////////////////////////////////////////////////////////////////////

    private int bits;

    ///////////////////////////////////////////////////////////////////////////
    // Constructors
    ///////////////////////////////////////////////////////////////////////////

    DSStatus(int bits) {
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
            throw new IllegalArgumentException("Unknown status: " + orig);
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
    public boolean isCommsDown() {
        return (BAD_COMMS_DOWN & bits) != 0;
    }

    /**
     * True if the associate bit is set.
     */
    public boolean isConfigFault() {
        return (BAD_CONFIG_FAULT & bits) != 0;
    }

    /**
     * True if the associate bit is set.
     */
    public boolean isFailure() {
        return (BAD_FAILURE & bits) != 0;
    }

    /**
     * True if the associate bit is set.
     */
    public boolean isDisabled() {
        return (BAD_DISABLED & bits) != 0;
    }

    /**
     * True if the associate bit is set.
     */
    public boolean isUnknown() {
        return (BAD_UNKNOWN & bits) != 0;
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
    public boolean isOverride() {
        return (GOOD_OVERRIDE & bits) != 0;
    }

    /**
     * True if the associate bit is set.
     */
    public boolean isAlarm() {
        return (GOOD_ALARM & bits) != 0;
    }

    @Override
    public boolean isNull() {
        return this == NULL;
    }

    public boolean isOk() {
        return bits == 0;
    }

    @Override
    public DSLong store() {
        return DSLong.valueOf(bits);
    }

    @Override
    public DSStatus restore(DSElement arg) {
        return valueOf(arg.toInt());
    }

    @Override
    public DSString toElement() {
        return DSString.valueOf(toString());
    }

    @Override
    public String toString() {
        if (isNull()) {
            return "null";
        }
        StringBuilder buf = new StringBuilder();
        if (isBad()) {
            if (isUnknown()) {
                append(buf, BAD_UNKNOWN_STR);
            }
            if (isDisabled()) {
                append(buf, BAD_DISABLED_STR);
            }
            if (isFailure()) {
                append(buf, BAD_FAILURE_STR);
            }
            if (isConfigFault()) {
                append(buf, BAD_CONFIG_FAULT_STR);
            }
            if (isCommsDown()) {
                append(buf, BAD_COMMS_DOWN_STR);
            }
        }
        if (isGood()) {
            if (isAlarm()) {
                append(buf, GOOD_ALARM_STR);
            }
            if (isOverride()) {
                append(buf, GOOD_OVERRIDE_STR);
            }
        }
        return null;
    }

    @Override
    public DSStatus toStatus() {
        return this;
    }

    @Override
    public DSStatus valueOf(DSElement element) {
        if ((element == null) || element.isNull()) {
            return NULL;
        }
        return valueOf(element.toString());
    }

    public static DSStatus valueOf(int bits) {
        if (bits == NULL.bits) {
            return NULL;
        }
        if (bits == 0) {
            return ok;
        }
        DSStatus ret = intCache.get(bits);
        if (ret != null) {
            return ret;
        }
        ret = new DSStatus(bits);
        intCache.put(bits, ret);
        return ret;
    }

    public static DSStatus valueOf(String string) {
        return valueOf(string.split(","));
    }

    public static DSStatus valueOf(String[] strings) {
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
        DSRegistry.registerDecoder(DSStatus.class, NULL);
        stringCache.put("null", NULL.bits);
        stringCache.put(OK_STR, OK);
        stringCache.put(GOOD_OVERRIDE_STR, GOOD_OVERRIDE);
        stringCache.put(GOOD_ALARM_STR, GOOD_ALARM);
        stringCache.put(BAD_COMMS_DOWN_STR, BAD_COMMS_DOWN);
        stringCache.put(BAD_CONFIG_FAULT_STR, BAD_CONFIG_FAULT);
        stringCache.put(BAD_FAILURE_STR, BAD_FAILURE);
        stringCache.put(BAD_DISABLED_STR, BAD_DISABLED);
        stringCache.put(BAD_UNKNOWN_STR, BAD_UNKNOWN);
        //toLowerCase
        stringCache.put(OK_STR.toLowerCase(), OK);
        stringCache.put(GOOD_OVERRIDE_STR.toLowerCase(), GOOD_OVERRIDE);
        stringCache.put(GOOD_ALARM_STR.toLowerCase(), GOOD_ALARM);
        stringCache.put(BAD_COMMS_DOWN_STR.toLowerCase(), BAD_COMMS_DOWN);
        stringCache.put(BAD_CONFIG_FAULT_STR.toLowerCase(), BAD_CONFIG_FAULT);
        stringCache.put(BAD_FAILURE_STR.toLowerCase(), BAD_FAILURE);
        stringCache.put(BAD_DISABLED_STR.toLowerCase(), BAD_DISABLED);
        stringCache.put(BAD_UNKNOWN_STR.toLowerCase(), BAD_UNKNOWN);
    }

}
