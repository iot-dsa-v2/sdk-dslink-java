package org.iot.dsa.io.json;

/**
 * Useful constants.
 *
 * @author Aaron Hansen
 */
public interface JsonConstants {

    // Constants
    // ---------

    /**
     * How Double.NaN is encoded: "\\u001BNaN"
     */
    public static String DBL_NAN = "\u001BNaN";

    /**
     * How Double.NEGATIVE_INFINITY is encoded: "\\u001B-Infinity"
     */
    public static String DBL_NEG_INF = "\u001B-Infinity";

    /**
     * How Double.POSITIVE_INFINITY is encoded: "\\u001BInfinity"
     */
    public static String DBL_POS_INF = "\u001BInfinity";

}//DSIObject
