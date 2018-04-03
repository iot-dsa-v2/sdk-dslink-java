package org.iot.dsa.node;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Represents the health, quality or condition of an object.  A DSStatus instance is a
 * can represent multiple conditions.
 * <p>
 * There are three categories of status: good, bad and uncertain.  Bad status means values
 * can not be trusted and should not be used in decision making / calculations.
 * Uncertain means the value might be good, but is the result of an operational or
 * configuration error such as out of range.  When performing aggregations and rollups,
 * bad and uncertain values should be ignored if any good values are present.
 * <p>
 * Remote status means the status is being reported by a foreign system (outside of DSA).
 * This should help with troubleshooting.
 * <p>
 * The status values:
 * <p>
 * <ul>
 * <li>ok                = Good, no other status applies. Always implied when not present.
 * <li>override          = Good, the value is overridden within DSA.
 * <li>remoteOverride    = Good, the remote system is reporting the value an override.
 * <li>stale             = Uncertain, the value hasn't updated in a reasonable amount of
 *                         time (usually configurable) within DSA.
 * <li>remoteStale       = Uncertain, the remote system is reporting stale.
 * <li>fault             = Uncertain, an operational or configuration error
 *                         has occurred within DSA.
 * <li>remoteFault       = Uncertain, the remote system is report fault.
 * <li>down              = Bad, a communication failure has occurred in DSA.
 * <li>remoteDown        = Bad, the remote system is reporting a communications failure.
 * <li>disabled          = Bad, an object has been disabled within DSA.
 * <li>remoteDisabled    = Bad, the remote system is reporting disabled.
 * <li>unknown           = Bad, the status is unknown within DSA.
 * <li>remoteUnknown     = Bad, the remote system is reporting the status is unknown.
 * </ul>
 *
 * @author Aaron Hansen
 */
public class DSStatus extends DSValue implements DSIStatus, DSIStorable {

    ///////////////////////////////////////////////////////////////////////////
    // Constants
    ///////////////////////////////////////////////////////////////////////////

    private static ConcurrentHashMap<Integer, DSStatus> intCache =
            new ConcurrentHashMap<Integer, DSStatus>();
    private static ConcurrentHashMap<String, Integer> stringCache =
            new ConcurrentHashMap<String, Integer>();

    private static final int GOOD_MASK = 0x000000FF;
    private static final int UNCERTAIN_MASK = 0x0000FF00;
    private static final int BAD_MASK = 0x00FF0000;

    /**
     * Good, no other status applies. Always implied when not present.
     */
    public static final int OK = 0; //"ok"

    /**
     * Good, the value is overridden within DSA.
     */
    public static final int OVERRIDE = 0x00000001; //"override"

    /**
     * Good, the remote system is reporting the value is overridden.
     */
    public static final int REMOTE_OVERRIDE = 0x00000002; //"remoteOverride"

    /**
     * Uncertain, the value hasn't updated in a reasonable amount of time (usually
     * configurable) within DSA.
     */
    public static final int STALE = 0x00000100; //"stale"

    /**
     * Uncertain, the remote system is reporting the value is stale.
     */
    public static final int REMOTE_STALE = 0x00000200; //"remoteStale"

    /**
     * Uncertain, an operational or configuration error has occurred within DSA.
     */
    public static final int FAULT = 0x00000400; //"fault"

    /**
     * Uncertain, the remote system is reporting a fault.
     */
    public static final int REMOTE_FAULT = 0x00000800; //"remoteFault"

    /**
     * Bad, a communication failure has occurred in DSA.
     */
    public static final int DOWN = 0x00010000; //"down"

    /**
     * Bad, the remote system is reporting down.
     */
    public static final int REMOTE_DOWN = 0x00020000; //"remoteDown"

    /**
     * Bad, the object has been disabled within DSA.
     */
    public static final int DISABLED = 0x00040000; //"disabled"

    /**
     * Bad, the remote system is reporting disabled.
     */
    public static final int REMOTE_DISABLED = 0x00080000; //"remoteDisabled"

    /**
     * Bad, the status is unknown within DSA.  This will typically be reported for
     * invalid paths.
     */
    public static final int UNKNOWN = 0x00100000; //"unknown"

    /**
     * Bad, the remote system is reporting unknown.
     */
    public static final int REMOTE_UNKNOWN = 0x00200000; //"remoteUnknown"

    //The string for each unique status.
    public static final String OK_STR = "ok";
    public static final String OVERRIDE_STR = "override";
    public static final String STALE_STR = "stale";
    public static final String DOWN_STR = "down";
    public static final String FAULT_STR = "fault";
    public static final String DISABLED_STR = "disabled";
    public static final String UNKNOWN_STR = "unknown";
    public static final String REMOTE_OVERRIDE_STR = "remoteOverride";
    public static final String REMOTE_STALE_STR = "remoteStale";
    public static final String REMOTE_FAULT_STR = "remoteFault";
    public static final String REMOTE_DOWN_STR = "remoteDown";
    public static final String REMOTE_DISABLED_STR = "remoteDisabled";
    public static final String REMOTE_UNKNOWN_STR = "remoteUnknown";

    //The instance for each unique status.
    public static final DSStatus ok = new DSStatus(0);
    public static final DSStatus override = valueOf(OVERRIDE);
    public static final DSStatus remoteOverride = valueOf(REMOTE_OVERRIDE);
    public static final DSStatus stale = valueOf(STALE);
    public static final DSStatus remoteStale = valueOf(REMOTE_STALE);
    public static final DSStatus fault = valueOf(FAULT);
    public static final DSStatus remoteFault = valueOf(REMOTE_FAULT);
    public static final DSStatus down = valueOf(DOWN);
    public static final DSStatus remoteDown = valueOf(REMOTE_DOWN);
    public static final DSStatus disabled = valueOf(DISABLED);
    public static final DSStatus remoteDisabled = valueOf(REMOTE_DISABLED);
    public static final DSStatus unknown = valueOf(UNKNOWN);
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

    @Override
    public boolean equals(Object obj) {
        return isEqual(obj);
    }

    /**
     * Finds the bit representation for the given status string.
     *
     * @throws IllegalArgumentException If the string is unknown.
     */
    private static Integer getBit(String s) {
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

    @Override
    public int hashCode() {
        return bits;
    }

    /**
     * If any of the bad flags are set, or is null.
     */
    public boolean isBad() {
        return (bits & BAD_MASK) != 0;
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

    @Override
    public boolean isEqual(Object obj) {
        if (obj instanceof DSStatus) {
            return hashCode() == obj.hashCode();
        }
        return false;
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
        if (bits == 0) {
            return true;
        }
        return (bits & GOOD_MASK) != 0;
    }

    @Override
    public boolean isNull() {
        return false;
    }

    public boolean isOk() {
        return bits == 0;
    }

    /**
     * True if the associate bit is set.
     */
    public boolean isOverride() {
        return (OVERRIDE & bits) != 0;
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
        return (REMOTE_OVERRIDE & bits) != 0;
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

    /**
     * If true, any associate object / value might be bad.
     */
    public boolean isUncertain() {
        return (bits & UNCERTAIN_MASK) != 0;
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
            append(buf, OVERRIDE_STR);
        }
        if (isRemoteOverride()) {
            append(buf, REMOTE_OVERRIDE_STR);
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
            return ok;
        }
        return valueOf(element.toString());
    }

    /**
     * Returns a status representing all of the given bits.
     */
    public static DSStatus valueOf(int... bits) {
        int all = 0;
        for (int bit : bits) {
            all |= bit;
        }
        return valueOf(bits);
    }

    /**
     * Returns a status representing the given bitset.
     */
    public static DSStatus valueOf(int bits) {
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

    /**
     * Returns a status representing all of the status strings (comma separated).
     *
     * @param string CSV of status names.
     */
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
        DSRegistry.registerDecoder(DSStatus.class, ok);
        stringCache.put("disconnected", DOWN); //DSAv1
        stringCache.put(OK_STR, OK);
        stringCache.put(OVERRIDE_STR, OVERRIDE);
        stringCache.put(REMOTE_OVERRIDE_STR, REMOTE_OVERRIDE);
        stringCache.put(STALE_STR, STALE);
        stringCache.put(REMOTE_STALE_STR, REMOTE_STALE);
        stringCache.put(FAULT_STR, FAULT);
        stringCache.put(REMOTE_FAULT_STR, REMOTE_FAULT);
        stringCache.put(DOWN_STR, DOWN);
        stringCache.put(REMOTE_DOWN_STR, REMOTE_DOWN);
        stringCache.put(DISABLED_STR, DISABLED);
        stringCache.put(REMOTE_DISABLED_STR, REMOTE_DISABLED);
        stringCache.put(UNKNOWN_STR, UNKNOWN);
        stringCache.put(REMOTE_UNKNOWN_STR, REMOTE_UNKNOWN);
    }

}
