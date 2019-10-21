package com.acuity.iot.dsa.dslink.sys.logging;

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
public enum LoggerNodeLevel implements DSIEnum, DSIValue {

    DEFAULT(null),
    ALL(LoggingConstants.all),
    TRACE(LoggingConstants.trace),
    DEBUG(LoggingConstants.debug),
    INFO(LoggingConstants.info),
    WARN(LoggingConstants.warn),
    ERROR(LoggingConstants.error),
    OFF(LoggingConstants.off);

    ///////////////////////////////////////////////////////////////////////////
    // Instance Fields
    ///////////////////////////////////////////////////////////////////////////

    private final DSString element;
    private final Level level;

    ///////////////////////////////////////////////////////////////////////////
    // Constructors
    ///////////////////////////////////////////////////////////////////////////

    LoggerNodeLevel(Level level) {
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
        for (LoggerNodeLevel e : values()) {
            bucket.add(e.element);
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

    public static LoggerNodeLevel make(String str) {
        for (LoggerNodeLevel e : values()) {
            if (e.element.toString().equalsIgnoreCase(str)) {
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
        for (LoggerNodeLevel e : values()) {
            if (e.element.equals(element)) {
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
    public static LoggerNodeLevel valueOf(Level arg) {
        int argInt = arg.intValue();
        switch (argInt) {
            case LoggingConstants.ALL:
                return ALL;
            case LoggingConstants.FINEST:
                return TRACE;
            case LoggingConstants.FINER: //finer
            case LoggingConstants.FINE: //fine
            case LoggingConstants.CONFIG: //config
                return DEBUG;
            case LoggingConstants.INFO: //info
                return INFO;
            case LoggingConstants.WARN: //warn
                return WARN;
            case LoggingConstants.SEVERE: //error
                return ERROR;
            case LoggingConstants.OFF: //off
                return OFF;
        }
        LoggerNodeLevel closest = LoggerNodeLevel.ALL;
        int closestInt = LoggingConstants.ALL;
        Level dsa;
        int dsaInt;
        for (LoggerNodeLevel e : values()) {
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
        DSRegistry.registerDecoder(LoggerNodeLevel.class, ALL);
    }

}
