package org.iot.dsa.logging;

import java.util.logging.Level;
import org.iot.dsa.node.DSElement;
import org.iot.dsa.node.DSIEnum;
import org.iot.dsa.node.DSIObject;
import org.iot.dsa.node.DSIValue;
import org.iot.dsa.node.DSList;
import org.iot.dsa.node.DSRegistry;
import org.iot.dsa.node.DSString;
import org.iot.dsa.node.DSValueType;

/**
 * Java Enum representing the DSA log levels.
 *
 * @author Aaron Hansen
 */
public enum DSLevel implements DSIEnum, DSIValue {

    ALL(DSLogger.all),
    TRACE(DSLogger.trace),
    DEBUG(DSLogger.debug),
    INFO(DSLogger.info),
    WARN(DSLogger.warn),
    ERROR(DSLogger.error),
    OFF(DSLogger.off);

    ///////////////////////////////////////////////////////////////////////////
    // Instance Fields
    ///////////////////////////////////////////////////////////////////////////

    private DSString element;
    private Level level;

    ///////////////////////////////////////////////////////////////////////////
    // Constructors
    ///////////////////////////////////////////////////////////////////////////

    private DSLevel(Level level) {
        this.element = DSString.valueOf(name());
        this.level = level;
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
        for (DSLevel e : values()) {
            bucket.add(e.toElement());
        }
        return bucket;
    }

    @Override
    public DSValueType getValueType() {
        return DSValueType.ENUM;
    }

    @Override
    public boolean isNull() {
        return false;
    }

    public static DSLevel make(String str) {
        for (DSLevel e : values()) {
            if (e.toElement().toString().equalsIgnoreCase(str)) {
                return e;
            }
        }
        try {
            return valueOf(Level.parse(str));
        } catch (Exception x) {
        }
        return INFO;
    }

    @Override
    public DSElement toElement() {
        return element;
    }

    /**
     * The corresponding JUL level.
     */
    public Level toLevel() {
        return level;
    }

    @Override
    public DSIValue valueOf(DSElement element) {
        for (DSLevel e : values()) {
            if (e.toElement().equals(element)) {
                return e;
            }
        }
        return make(element.toString());
    }

    /**
     * Maps a Java log level to one of the DSA levels.
     *
     * @return Will return a level
     */
    public static DSLevel valueOf(Level arg) {
        int argInt = arg.intValue();
        switch (argInt) {
            case DSLogger.ALL:
                return ALL;
            case DSLogger.FINEST:
                return TRACE;
            case DSLogger.FINER: //finer
            case DSLogger.FINE: //fine
            case DSLogger.CONFIG: //config
                return DEBUG;
            case DSLogger.INFO: //info
                return INFO;
            case DSLogger.WARN: //warn
                return WARN;
            case DSLogger.SEVERE: //error
                return ERROR;
            case DSLogger.OFF: //off
                return OFF;
        }
        DSLevel closest = DSLevel.ALL;
        int closestInt = DSLogger.ALL;
        Level dsa;
        int dsaInt;
        for (DSLevel e : values()) {
            dsa = e.toLevel();
            if (dsa.equals(arg)) {
                return e;
            }
            dsaInt = dsa.intValue();
            if (dsaInt <= argInt) {
                if (dsaInt > closestInt) {
                    closest = e;
                }
            }
        }
        return closest;
    }

    ///////////////////////////////////////////////////////////////////////////
    // Initialization
    ///////////////////////////////////////////////////////////////////////////

    static {
        DSRegistry.registerDecoder(DSLevel.class, ALL);
    }

}
