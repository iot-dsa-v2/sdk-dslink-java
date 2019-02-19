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
 *
 * <ul>
 * <li>ok                = Good, no other status applies. Always implied when not present.
 * <li>override          = Good, the value is overridden within DSA.
 * <li>remoteOverride    = Good, the remote system is reporting the value an override.
 * <li>stale             = Uncertain, the value hasn't updated in a reasonable amount of
 * time (usually configurable) within DSA.
 * <li>remoteStale       = Uncertain, the remote system is reporting stale.
 * <li>offnormal         = Uncertain, the value is outside the bounds of normal operation.
 * <li>remoteOffnormal   = Uncertain, the remote system is report offnormal.
 * <li>fault             = Bad, an operational or configuration error
 * has occurred within DSA.
 * <li>remoteFault       = Bad, the remote system is report fault.
 * <li>down              = Bad, a communication failure has occurred in DSA.
 * <li>remoteDown        = Bad, the remote system is reporting a communications failure.
 * <li>disabled          = Bad, an object has been disabled within DSA.
 * <li>remoteDisabled    = Bad, the remote system is reporting disabled.
 * <li>unknown           = Bad, the status is unknown within DSA.
 * <li>remoteUnknown     = Bad, the remote system is reporting the status is unknown.
 * <li>start             = Good, indicates the beginning of history collection in trends.
 * </ul>
 *
 * @author Aaron Hansen
 */
public class DSStatus extends DSValue implements DSIStatus, DSIStorable {

    ///////////////////////////////////////////////////////////////////////////
    // Constants
    ///////////////////////////////////////////////////////////////////////////

    private static final int UNCERTAIN_MASK = 0x0000FF00;
    private static final int BAD_MASK = 0x00FF0000;
    private static final int HIS_MASK = 0xFF000000; //for history flags
    private static final int NOT_GOOD_MASK = 0x00FFFF00;
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
     * Uncertain, the value is outside the bounds of normal operation.
     */
    public static final int OFFNORMAL = 0x00000400; //"offnormal"
    /**
     * Uncertain, the remote system is remote reporting the value is offnormal.
     */
    public static final int REMOTE_OFFNORMAL = 0x00000800; //"remoteOffnormal"
    /**
     * Bad, an operational or configuration error has occurred within DSA.
     */
    public static final int FAULT = 0x00001000; //"fault"
    /**
     * Bad, the remote system is reporting a fault.
     */
    public static final int REMOTE_FAULT = 0x00002000; //"remoteFault"
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
    /**
     * Good, used to indicate the start of history collection.
     */
    public static final int START = 0x01000000; //"start"

    //The string for each unique status.
    public static final String OK_STR = "ok";
    public static final String OVERRIDE_STR = "override";
    public static final String REMOTE_OVERRIDE_STR = "remoteOverride";
    public static final String STALE_STR = "stale";
    public static final String REMOTE_STALE_STR = "remoteStale";
    public static final String OFFNORMAL_STR = "offnormal";
    public static final String REMOTE_OFFNORMAL_STR = "remoteOffnormal";
    public static final String FAULT_STR = "fault";
    public static final String REMOTE_FAULT_STR = "remoteFault";
    public static final String DOWN_STR = "down";
    public static final String REMOTE_DOWN_STR = "remoteDown";
    public static final String DISABLED_STR = "disabled";
    public static final String REMOTE_DISABLED_STR = "remoteDisabled";
    public static final String UNKNOWN_STR = "unknown";
    public static final String REMOTE_UNKNOWN_STR = "remoteUnknown";
    public static final String START_STR = "start";

    //The instance for each unique status.
    public static final DSStatus ok = new DSStatus(0);
    private int bits;
    private static ConcurrentHashMap<Integer, DSStatus> intCache = new ConcurrentHashMap<>();
    public static final DSStatus override = valueOf(OVERRIDE);
    public static final DSStatus remoteOverride = valueOf(REMOTE_OVERRIDE);
    public static final DSStatus stale = valueOf(STALE);
    public static final DSStatus remoteStale = valueOf(REMOTE_STALE);
    public static final DSStatus offnormal = valueOf(OFFNORMAL);
    public static final DSStatus remoteOffnormal = valueOf(REMOTE_OFFNORMAL);
    public static final DSStatus fault = valueOf(FAULT);
    public static final DSStatus remoteFault = valueOf(REMOTE_FAULT);
    public static final DSStatus down = valueOf(DOWN);
    public static final DSStatus remoteDown = valueOf(REMOTE_DOWN);
    public static final DSStatus disabled = valueOf(DISABLED);
    public static final DSStatus remoteDisabled = valueOf(REMOTE_DISABLED);
    public static final DSStatus unknown = valueOf(UNKNOWN);
    public static final DSStatus remoteUnknown = valueOf(REMOTE_UNKNOWN);
    public static final DSStatus start = valueOf(STALE);

    ///////////////////////////////////////////////////////////////////////////
    // Fields
    ///////////////////////////////////////////////////////////////////////////
    private static ConcurrentHashMap<String, Integer> stringCache =
            new ConcurrentHashMap<String, Integer>();

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
     * Add the given bits and either return a new status if there is a change, or this status
     * if no change.
     */
    public DSStatus add(int flags) {
        int newBits = bits | flags;
        if (newBits == bits) {
            return this;
        }
        return valueOf(newBits);
    }

    /**
     * Add the given bits and either return a new status if there is a change, or this status
     * if no change.
     */
    public DSStatus add(DSStatus flags) {
        return add(flags.getBits());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof DSStatus) {
            return hashCode() == obj.hashCode();
        }
        return false;
    }

    /**
     * The bitset representing the all the set qualities.
     */
    public int getBits() {
        return bits;
    }

    @Override
    public DSStatus getStatus() {
        return this;
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
     * True if either the local or remote status is true.
     */
    public boolean isAnyDisabled() {
        return isAnyDisabled(bits);
    }

    /**
     * True if either the local or remote status is true.
     */
    public static boolean isAnyDisabled(int bits) {
        return isDisabled(bits) || isRemoteDisabled(bits);
    }

    /**
     * True if either the local or remote status is true.
     */
    public boolean isAnyDown() {
        return isAnyDown(bits);
    }

    /**
     * True if either the local or remote status is true.
     */
    public static boolean isAnyDown(int bits) {
        return isDown(bits) || isRemoteDown(bits);
    }

    /**
     * True if either the local or remote status is true.
     */
    public boolean isAnyFault() {
        return isAnyFault(bits);
    }

    /**
     * True if either the local or remote status is true.
     */
    public static boolean isAnyFault(int bits) {
        return isFault(bits) || isRemoteFault(bits);
    }

    /**
     * True if either the local or remote status is true.
     */
    public boolean isAnyOffnormal() {
        return isAnyOffnormal(bits);
    }

    /**
     * True if either the local or remote status is true.
     */
    public static boolean isAnyOffnormal(int bits) {
        return isOffnormal(bits) || isRemoteOffnormal(bits);
    }

    /**
     * True if either the local or remote status is true.
     */
    public boolean isAnyOverride() {
        return isAnyOverride(bits);
    }

    /**
     * True if either the local or remote status is true.
     */
    public static boolean isAnyOverride(int bits) {
        return isOverride(bits) || isRemoteOverride(bits);
    }

    /**
     * True if either the local or remote status is true.
     */
    public boolean isAnyStale() {
        return isAnyStale(bits);
    }

    /**
     * True if either the local or remote status is true.
     */
    public static boolean isAnyStale(int bits) {
        return isStale(bits) || isRemoteStale(bits);
    }

    /**
     * True if either the local or remote status is true.
     */
    public boolean isAnyUnknown() {
        return isAnyUnknown(bits);
    }

    /**
     * True if either the local or remote status is true.
     */
    public static boolean isAnyUnknown(int bits) {
        return isUnknown(bits) || isRemoteUnknown(bits);
    }

    /**
     * If any of the bad flags are set.
     */
    public boolean isBad() {
        return isBad(bits);
    }

    /**
     * If any of the bad flags are set.
     */
    public static boolean isBad(int bits) {
        return (bits & BAD_MASK) != 0;
    }

    public boolean isDisabled() {
        return isDisabled(bits);
    }

    public static boolean isDisabled(int bits) {
        return (DISABLED & bits) != 0;
    }

    public boolean isDown() {
        return isDown(bits);
    }

    public static boolean isDown(int bits) {
        return (DOWN & bits) != 0;
    }

    public boolean isFault() {
        return isFault(bits);
    }

    public static boolean isFault(int bits) {
        return (FAULT & bits) != 0;
    }

    /**
     * If true, any associate object / value can be trusted.
     */
    public boolean isGood() {
        return isGood(bits);
    }

    /**
     * If true, any associate object / value can be trusted.
     */
    public static boolean isGood(int bits) {
        return (bits & NOT_GOOD_MASK) == 0;
    }

    /**
     * False, there is no null instance, use ok instead.
     */
    @Override
    public boolean isNull() {
        return false;
    }

    public boolean isOffnormal() {
        return isOffnormal(bits);
    }

    public static boolean isOffnormal(int bits) {
        return (OFFNORMAL & bits) != 0;
    }

    public boolean isOk() {
        return isOk(bits);
    }

    public static boolean isOk(int bits) {
        return bits == 0;
    }

    public boolean isOverride() {
        return isOverride(bits);
    }

    public static boolean isOverride(int bits) {
        return (OVERRIDE & bits) != 0;
    }

    public boolean isRemoteDisabled() {
        return isRemoteDisabled(bits);

    }

    public static boolean isRemoteDisabled(int bits) {
        return (REMOTE_DISABLED & bits) != 0;
    }

    public boolean isRemoteDown() {
        return isRemoteDown(bits);
    }

    public static boolean isRemoteDown(int bits) {
        return (REMOTE_DOWN & bits) != 0;
    }

    public boolean isRemoteFault() {
        return isRemoteFault(bits);
    }

    public static boolean isRemoteFault(int bits) {
        return (REMOTE_FAULT & bits) != 0;
    }

    public boolean isRemoteOffnormal() {
        return isRemoteOffnormal(bits);
    }

    public static boolean isRemoteOffnormal(int bits) {
        return (REMOTE_OFFNORMAL & bits) != 0;
    }

    public boolean isRemoteOverride() {
        return isRemoteOverride(bits);
    }

    public static boolean isRemoteOverride(int bits) {
        return (REMOTE_OVERRIDE & bits) != 0;
    }

    public boolean isRemoteStale() {
        return isRemoteStale(bits);
    }

    public static boolean isRemoteStale(int bits) {
        return (REMOTE_STALE & bits) != 0;
    }

    public boolean isRemoteUnknown() {
        return isRemoteUnknown(bits);
    }

    public static boolean isRemoteUnknown(int bits) {
        return (REMOTE_UNKNOWN & bits) != 0;
    }

    public boolean isStale() {
        return isStale(bits);
    }

    public static boolean isStale(int bits) {
        return (STALE & bits) != 0;
    }

    public boolean isStart() {
        return isStart(bits);
    }

    public static boolean isStart(int bits) {
        return (START & bits) != 0;
    }

    /**
     * If true, any associate object / value might be bad.
     */
    public boolean isUncertain() {
        return isUncertain(bits);
    }

    /**
     * If true, any associate object / value might be bad.
     */
    public static boolean isUncertain(int bits) {
        return (bits & UNCERTAIN_MASK) != 0;
    }

    public boolean isUnknown() {
        return isUnknown(bits);
    }

    public static boolean isUnknown(int bits) {
        return (UNKNOWN & bits) != 0;
    }

    /**
     * Remove the given bits and either return a new status if there is a change, or this status
     * if no change.
     */
    public DSStatus remove(int flags) {
        int newBits = bits & ~flags;
        if (newBits == bits) {
            return this;
        }
        return valueOf(newBits);
    }

    /**
     * Remove the given bits and either return a new status if there is a change, or this status
     * if no change.
     */
    public DSStatus remove(DSStatus flags) {
        return remove(flags.getBits());
    }

    @Override
    public DSStatus restore(DSElement arg) {
        return valueOf(arg.toInt());
    }

    @Override
    public DSLong store() {
        return DSLong.valueOf(bits);
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
        if (isRemoteStale()) {
            append(buf, REMOTE_STALE_STR);
        }
        if (isOffnormal()) {
            append(buf, OFFNORMAL_STR);
        }
        if (isRemoteOffnormal()) {
            append(buf, REMOTE_OFFNORMAL_STR);
        }
        if (isFault()) {
            append(buf, FAULT_STR);
        }
        if (isRemoteFault()) {
            append(buf, REMOTE_FAULT_STR);
        }
        if (isDown()) {
            append(buf, DOWN_STR);
        }
        if (isRemoteDown()) {
            append(buf, REMOTE_DOWN_STR);
        }
        if (isDisabled()) {
            append(buf, DISABLED_STR);
        }
        if (isRemoteDisabled()) {
            append(buf, REMOTE_DISABLED_STR);
        }
        if (isUnknown()) {
            append(buf, UNKNOWN_STR);
        }
        if (isRemoteUnknown()) {
            append(buf, REMOTE_UNKNOWN_STR);
        }
        if (isStart()) {
            append(buf, START_STR);
        }
        return buf.toString();
    }

    @Override
    public DSStatus valueOf(DSElement element) {
        if ((element == null) || element.isNull()) {
            return ok;
        }
        return valueOf(element.toString());
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
     * Returns a status representing all of the given bits.
     */
    public static DSStatus valueOf(int... bits) {
        int all = 0;
        for (int bit : bits) {
            all |= bit;
        }
        return valueOf(all);
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

    ///////////////////////////////////////////////////////////////////////////
    // Initialization
    ///////////////////////////////////////////////////////////////////////////

    static {
        DSRegistry.registerDecoder(DSStatus.class, ok);
        stringCache.put("disconnected", DOWN); //DSA version 1
        stringCache.put(OK_STR, OK);
        stringCache.put(OVERRIDE_STR, OVERRIDE);
        stringCache.put(REMOTE_OVERRIDE_STR, REMOTE_OVERRIDE);
        stringCache.put(STALE_STR, STALE);
        stringCache.put(REMOTE_STALE_STR, REMOTE_STALE);
        stringCache.put(OFFNORMAL_STR, OFFNORMAL);
        stringCache.put(REMOTE_OFFNORMAL_STR, REMOTE_OFFNORMAL);
        stringCache.put(FAULT_STR, FAULT);
        stringCache.put(REMOTE_FAULT_STR, REMOTE_FAULT);
        stringCache.put(DOWN_STR, DOWN);
        stringCache.put(REMOTE_DOWN_STR, REMOTE_DOWN);
        stringCache.put(DISABLED_STR, DISABLED);
        stringCache.put(REMOTE_DISABLED_STR, REMOTE_DISABLED);
        stringCache.put(UNKNOWN_STR, UNKNOWN);
        stringCache.put(REMOTE_UNKNOWN_STR, REMOTE_UNKNOWN);
        stringCache.put(START_STR, START);
    }

}
