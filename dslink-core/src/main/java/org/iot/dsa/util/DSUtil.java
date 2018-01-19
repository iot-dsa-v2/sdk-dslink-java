package org.iot.dsa.util;

/**
 * Common utilities.
 *
 * @author Aaron Hansen
 */
public class DSUtil {

    private DSUtil() {
    }

    /**
     * Comparison that takes null into account.  Null == null.
     */
    public static boolean equal(Object o1, Object o2) {
        if (o1 == o2) {
            return true;
        } else if (o1 == null) {
            return false;
        } else if (o2 == null) {
            return false;
        }
        return o1.equals(o2);
    }

    /**
     * Returns true if the bit at the given index is set.
     *
     * @param bits  The bitset the check.
     * @param index The bit position in the bits argument; 0 is the lowest order bit and 31 is the
     *              highest order.
     * @return True if the target bit is set.
     */
    public static boolean getBit(int bits, int index) {
        return (bits & (1 << index)) != 0;
    }

    /**
     * Set or unset a bit at the given index.
     *
     * @param bits  The bitset to modify.
     * @param index Which bit to set/unset; 0 is the lowest order bit and 31 is the highest.
     * @param set   True to set, false to unset.
     * @return The adjusted bits.
     */
    public static int setBit(int bits, int index, boolean set) {
        if (set) {
            bits |= (1 << index);
        } else {
            bits &= ~(1 << index);
        }
        return bits;
    }

}
