package org.iot.dsa.node;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Represent the health, quality or condition of an object.
 *
 * <p>
 *
 * There are two categories of status, good and bad.  Bad status means values can not be trusted and
 * should not be used in decision making / calculations.  When performing aggregations / intervals,
 * bad values should be ignored if good values are present.
 *
 *
 * <p>
 *
 * Remote status means the status is being reported by a foreign system (outside of DSA).  This
 * should greatly help with troubleshooting.
 *
 *
 * <p>
 *
 * Only the highest priority status should be assigned to an object.
 *
 * The status values in order from lowest to highest priority:
 *
 * <ul>
 *
 * <li>ok                = Good, no other status applies. Implied when not present.
 *
 * <li>override          = Good, the value is overridden within DSA.
 *
 * <li>remoteOverride    = Good, the remote system is reporting the value is overridden.
 *
 * <li>stale             = Bad, the value hasn't updated in a reasonable amount of time (user
 * configurable on a per point basis) within DSA.
 *
 * <li>down              = Bad, communications are down in DSA.
 *
 * <li>fault             = Bad, an operational error (exception) has occurred within DSA.
 *
 * <li>configFault       = Bad, a configuration error has been identified within DSA.
 *
 * <li>disabled          = Bad, the object has been disabled within DSA.
 *
 * <li>unknown           = Bad, the status is unknown within DSA, typically the initial state at
 * boot.
 *
 * <li>remoteStale       = Bad, a stale value is being reported by the remote system.
 *
 * <li>remoteDown        = Bad, down communications are being reported by the remote system.
 *
 * <li>remoteFault       = Bad, an operational error is being reported by the remote system.
 *
 * <li>remoteConfigFault = Bad, a configuration error is being reported by the remote system.
 *
 * <li>remoteDisabled    = Bad, the remote system is reporting the object is disabled.
 *
 * <li>remoteUnknown     = Bad, the remote system is reporting the status is unknown.
 *
 * </ul>
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

    /**
     * Good, no other status applies.
     */
    public static final int OK = 0; //"ok"

    /**
     * Good, the value is overridden within DSA.
     */
    public static final int OK_OVERRIDE = 0x00000001; //"override"

    /**
     * Good, the value is overridden outside of DSA.
     */
    public static final int OK_REMOTE_OVERRIDE = 0x00000002; //"remoteOverride"

    /**
     * Bad, the value hasn't updated in a reasonable amount of time (user configurable on a per
     * point basis) within DSA.
     */
    public static final int STALE = 0x00000100; //"stale"

    /**
     * Bad, communications are down within DSA.
     */
    public static final int DOWN = 0x00000200; //"down"

    /**
     * Bad, an operational error (exception) has occurred within DSA.
     */
    public static final int FAULT = 0x00000400; //"fault"

    /**
     * Bad, a configuration error has been indentified within DSA.
     */
    public static final int CONFIG_FAULT = 0x00000800; //"configFault"

    /**
     * Bad, the object has been disabled within DSA.
     */
    public static final int DISABLED = 0x00001000; //"disabled"

    /**
     * Bad, the status is unknown within DSA, typically the initial state at boot.
     */
    public static final int UNKNOWN = 0x00002000; //"unknown"

    /**
     * Bad, a stale value is being reported by the foreign system.
     */
    public static final int REMOTE_STALE = 0x00010000; //"remoteStale"

    /**
     * Bad, down communications are being reported by the foreign system.
     */
    public static final int REMOTE_DOWN = 0x00020000; //"remoteDown"

    /**
     * Bad, an operational error is being reported by the foreign system.
     */
    public static final int REMOTE_FAULT = 0x00040000; //"remoteFault"

    /**
     * Bad, a configuration error is being reported by the foreign system.
     */
    public static final int REMOTE_CONFIG_FAULT = 0x00080000; //"remoteConfigFault"

    /**
     * Bad, the foreign system is reporting the object is disabled.
     */
    public static final int REMOTE_DISABLED = 0x00100000; //"remoteDisabled"

    /**
     * Bad, the foreign system is reporting the status is unknown.
     */
    public static final int REMOTE_UNKNOWN = 0x00200000; //"remoteUnknown"

    //The string for each unique status.
    public static final String OK_STR = "ok";
    public static final String OK_OVERRIDE_STR = "override";
    public static final String OK_REMOTE_OVERRIDE_STR = "remoteOverride";
    public static final String STALE_STR = "stale";
    public static final String DOWN_STR = "down";
    public static final String CONFIG_FAULT_STR = "configFault";
    public static final String FAULT_STR = "fault";
    public static final String DISABLED_STR = "disabled";
    public static final String UNKNOWN_STR = "unknown";
    public static final String REMOTE_STALE_STR = "remoteStale";
    public static final String REMOTE_DOWN_STR = "remoteDown";
    public static final String REMOTE_FAULT_STR = "remoteFault";
    public static final String REMOTE_CONFIG_FAULT_STR = "remoteConfigFault";
    public static final String REMOTE_DISABLED_STR = "remoteDisabled";
    public static final String REMOTE_UNKNOWN_STR = "remoteUnknown";

    //The instance for each unique status.
    public static final DSStatus ok = new DSStatus(0);
    public static final DSStatus okOverride = valueOf(OK_OVERRIDE);
    public static final DSStatus okRemoteOverride = valueOf(OK_REMOTE_OVERRIDE);
    public static final DSStatus stale = valueOf(STALE);
    public static final DSStatus down = valueOf(DOWN);
    public static final DSStatus fault = valueOf(FAULT);
    public static final DSStatus configFault = valueOf(CONFIG_FAULT);
    public static final DSStatus disabled = valueOf(DISABLED);
    public static final DSStatus unknown = valueOf(UNKNOWN);
    public static final DSStatus remoteStale = valueOf(REMOTE_STALE);
    public static final DSStatus remoteDown = valueOf(REMOTE_DOWN);
    public static final DSStatus remoteFault = valueOf(REMOTE_FAULT);
    public static final DSStatus remoteConfigFault = valueOf(REMOTE_CONFIG_FAULT);
    public static final DSStatus remoteDisabled = valueOf(REMOTE_DISABLED);
    public static final DSStatus remoteUnknown = valueOf(REMOTE_UNKNOWN);

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

    private void append(StringBuilder buf, String status) {
        if (buf.length() > 0) {
            buf.append(",");
        }
        buf.append(status);
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
    int getBits() {
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
        return (bits & 0x11111100) != 0;
    }

    /**
     * True if the associate bit is set.
     */
    public boolean isConfigFault() {
        return (CONFIG_FAULT & bits) != 0;
    }

    /**
     * True if the associate bit is set.
     */
    public boolean isDisabled() {
        return (DISABLED & bits) != 0;
    }

    /**
     * True if the associate bit is set.
     */
    public boolean isDown() {
        return (DOWN & bits) != 0;
    }

    /**
     * True if the associate bit is set.
     */
    public boolean isFault() {
        return (FAULT & bits) != 0;
    }

    /**
     * If true, any associate object / value can be trusted.
     */
    public boolean isGood() {
        return !isBad();
    }

    @Override
    public boolean isNull() {
        return this == NULL;
    }

    public boolean isOk() {
        return bits == 0;
    }

    /**
     * True if the associate bit is set.
     */
    public boolean isOverride() {
        return (OK_OVERRIDE & bits) != 0;
    }

    public boolean isRemoteConfigFault() {
        return (REMOTE_CONFIG_FAULT & bits) != 0;
    }

    public boolean isRemoteDisabled() {
        return (REMOTE_DISABLED & bits) != 0;
    }

    public boolean isRemoteDown() {
        return (REMOTE_DOWN & bits) != 0;
    }

    public boolean isRemoteFault() {
        return (REMOTE_FAULT & bits) != 0;
    }

    public boolean isRemoteOverride() {
        return (OK_REMOTE_OVERRIDE & bits) != 0;
    }

    public boolean isRemoteStale() {
        return (REMOTE_STALE & bits) != 0;
    }

    public boolean isRemoteUnknown() {
        return (REMOTE_UNKNOWN & bits) != 0;
    }

    public boolean isStale() {
        return (STALE & bits) != 0;
    }

    public boolean isUnknown() {
        return (UNKNOWN & bits) != 0;
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
        if (isOk()) {
            return OK_STR;
        }
        StringBuilder buf = new StringBuilder();
        if (isOverride()) {
            append(buf, OK_OVERRIDE_STR);
        }
        if (isRemoteOverride()) {
            append(buf, OK_REMOTE_OVERRIDE_STR);
        }
        if (isStale()) {
            append(buf, STALE_STR);
        }
        if (isDown()) {
            append(buf, DOWN_STR);
        }
        if (isFault()) {
            append(buf, FAULT_STR);
        }
        if (isConfigFault()) {
            append(buf, CONFIG_FAULT_STR);
        }
        if (isDisabled()) {
            append(buf, DISABLED_STR);
        }
        if (isUnknown()) {
            append(buf, UNKNOWN_STR);
        }
        if (isRemoteStale()) {
            append(buf, REMOTE_STALE_STR);
        }
        if (isRemoteDown()) {
            append(buf, REMOTE_DOWN_STR);
        }
        if (isRemoteFault()) {
            append(buf, REMOTE_FAULT_STR);
        }
        if (isRemoteConfigFault()) {
            append(buf, REMOTE_CONFIG_FAULT_STR);
        }
        if (isRemoteDisabled()) {
            append(buf, REMOTE_DISABLED_STR);
        }
        if (isRemoteUnknown()) {
            append(buf, REMOTE_UNKNOWN_STR);
        }
        return buf.toString();
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

    static DSStatus valueOf(int bits) {
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

    static DSStatus valueOf(String... strings) {
        int tmp = 0;
        int max = 0;
        for (String s : strings) {
            tmp = getBit(s);
            if (tmp > max) {
                max = tmp;
            }
        }
        return valueOf(max);
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
        stringCache.put("disconnected", DOWN); //DSAv1
        stringCache.put(OK_STR, OK);
        stringCache.put(OK_OVERRIDE_STR, OK_OVERRIDE);
        stringCache.put(OK_REMOTE_OVERRIDE_STR, OK_REMOTE_OVERRIDE);
        stringCache.put(STALE_STR, STALE);
        stringCache.put(DOWN_STR, DOWN);
        stringCache.put(FAULT_STR, FAULT);
        stringCache.put(CONFIG_FAULT_STR, CONFIG_FAULT);
        stringCache.put(DISABLED_STR, DISABLED);
        stringCache.put(UNKNOWN_STR, UNKNOWN);
        stringCache.put(REMOTE_STALE_STR, REMOTE_STALE);
        stringCache.put(REMOTE_DOWN_STR, REMOTE_DOWN);
        stringCache.put(REMOTE_FAULT_STR, REMOTE_FAULT);
        stringCache.put(REMOTE_CONFIG_FAULT_STR, REMOTE_CONFIG_FAULT);
        stringCache.put(REMOTE_DISABLED_STR, REMOTE_DISABLED);
        stringCache.put(REMOTE_UNKNOWN_STR, REMOTE_UNKNOWN);
    }

}
