package org.iot.dsa.rollup;

import java.util.HashMap;
import java.util.Map;
import org.iot.dsa.node.DSElement;
import org.iot.dsa.node.DSIEnum;
import org.iot.dsa.node.DSIObject;
import org.iot.dsa.node.DSIValue;
import org.iot.dsa.node.DSList;
import org.iot.dsa.node.DSRegistry;
import org.iot.dsa.node.DSString;
import org.iot.dsa.node.DSValueType;

/**
 * Enum representing various types of rollup functions.
 *
 * @author Aaron Hansen
 */
public enum DSRollup implements DSIEnum, DSIValue {

    ///////////////////////////////////////////////////////////////////////////
    // Class Fields
    ///////////////////////////////////////////////////////////////////////////

    AND("And"),
    AVG("Avg"),
    COUNT("Count"),
    FIRST("First"),
    LAST("Last"),
    MAX("Max"),
    MEDIAN("Median"),
    MIN("Min"),
    MODE("Mode"),
    OR("Or"),
    RANGE("Range"),
    SUM("Sum");

    private static final Map<String, DSRollup> rollups = new HashMap<String, DSRollup>();

    ///////////////////////////////////////////////////////////////////////////
    // Instance Fields
    ///////////////////////////////////////////////////////////////////////////

    private DSString element;

    ///////////////////////////////////////////////////////////////////////////
    // Constructors
    ///////////////////////////////////////////////////////////////////////////

    private DSRollup(String display) {
        this.element = DSString.valueOf(display);
    }

    ///////////////////////////////////////////////////////////////////////////
    // Public Methods
    ///////////////////////////////////////////////////////////////////////////

    @Override
    public DSIObject copy() {
        return this;
    }

    @Override
    public DSList getEnums(DSList bucket) {
        if (bucket == null) {
            bucket = new DSList();
        }
        for (DSRollup e : values()) {
            bucket.add(e.toElement());
        }
        return bucket;
    }

    /**
     * Creates a function for rolling up values.
     */
    public RollupFunction getFunction() {
        switch (this) {
            case AND:
                return new AndRollupFunction();
            case AVG:
                return new AvgRollupFunction();
            case COUNT:
                return new CountRollupFunction();
            case FIRST:
                return new FirstRollupFunction();
            case LAST:
                return new LastRollupFunction();
            case MAX:
                return new MaxRollupFunction();
            case MEDIAN:
                return new MedianRollupFunction();
            case MIN:
                return new MinRollupFunction();
            case MODE:
                return new ModeRollupFunction();
            case OR:
                return new OrRollupFunction();
            case RANGE:
                return new RangeRollupFunction();
            case SUM:
                return new SumRollupFunction();
        }
        throw new IllegalStateException("Unexpected function: " + toString());
    }

    @Override
    public DSValueType getValueType() {
        return DSValueType.ENUM;
    }

    @Override
    public boolean isEqual(Object obj) {
        if (obj == this) {
            return true;
        }
        return element.equals(obj);
    }

    @Override
    public boolean isNull() {
        return false;
    }

    @Override
    public DSElement toElement() {
        return element;
    }

    @Override
    public String toString() {
        return element.toString();
    }

    /**
     * Get an instance from a string.
     */
    public static DSRollup valueFor(String display) {
        DSRollup ret = rollups.get(display);
        if (ret == null) {
            ret = rollups.get(display.toLowerCase());
        }
        return ret;
    }

    @Override
    public DSIValue valueOf(DSElement element) {
        return valueFor(element.toString());
    }

    /////////////////////////////////////////////////////////////////
    // Initialization
    /////////////////////////////////////////////////////////////////

    static {
        DSRegistry.registerDecoder(DSRollup.class, AND);
        for (DSRollup e : AND.values()) {
            rollups.put(e.name(), e);
            rollups.put(e.toString(), e);
            rollups.put(e.toString().toLowerCase(), e);
        }
    }

}//DSRollup
