package org.iot.dsa.logging;

import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Adds an abstraction layer on Java Util Logging for two purposes:
 * <ul>
 * <li>To use the DSA levels.
 * <li>So JUL can be replaced with other logging frameworks if needed.
 * </ul>
 * <p>
 * This has methods that enables conditional logging (most efficient) using ternary statements.
 * <p>
 * Without this class:
 * <pre>
 * {@code
 * if (myLogger.isLoggable(Level.INFO))
 *     myLogger.info(someMessage());
 * }
 * </pre>
 * <p>
 * With this class:
 * <pre>
 * {@code
 * info(info() ? someMessage() : null);
 * }
 * </pre>
 * <p>
 * DSA defines levels differently than JUL, however, all JUL levels will be mapped / formatted
 * for DSA.  Level guidelines:
 * <ul>
 * <li>trace() = JUL finest
 * <li>debug() = JUL finer
 * <li>info()  = JUL info
 * <li>warn()  = JUL warning
 * <li>error() = JUL severe
 * </ul>
 *
 * @author Aaron Hansen
 */
public class DSLogger {

    static final int ALL = Integer.MIN_VALUE;
    static final int FINEST = 300;
    static final int FINER = 400;
    static final int FINE = 500;
    static final int CONFIG = 700;
    static final int INFO = 800;
    static final int WARN = 900;
    static final int SEVERE = 1000;
    static final int OFF = Integer.MAX_VALUE;

    public static final Level all = Level.ALL;
    public static final Level trace = Level.FINEST;
    public static final Level debug = Level.FINER;
    public static final Level info = Level.INFO;
    public static final Level warn = Level.WARNING;
    public static final Level error = Level.SEVERE;
    public static final Level off = Level.OFF;

    public static final Logger DSA = Logger.getLogger("dsa");

    /////////////////////////////////////////////////////////////////
    // Instance Fields
    /////////////////////////////////////////////////////////////////

    private Logger logger;

    /////////////////////////////////////////////////////////////////
    // Public Methods
    /////////////////////////////////////////////////////////////////

    /**
     * True if the level is loggable.
     */
    public boolean debug() {
        return getLogger().isLoggable(debug);
    }

    /**
     * Log a finer message.
     */
    public void debug(Object msg) {
        if (msg != null) {
            getLogger().log(debug, string(msg));
        }
    }

    /**
     * Log a finer message.
     */
    public void debug(Supplier<String> msg) {
        if (msg != null) {
            getLogger().log(debug, msg);
        }
    }

    /**
     * Log a parameterized message.
     */
    public void debug(Object msg, Object... params) {
        if (msg != null) {
            getLogger().log(debug, string(msg), params);
        }
    }

    /**
     * Log a finer message.
     */
    public void debug(Object msg, Throwable x) {
        getLogger().log(debug, string(msg), x);
    }

    /**
     * True if the level is loggable.
     */
    public boolean error() {
        return getLogger().isLoggable(error);
    }

    /**
     * Log a severe message.
     */
    public void error(Object msg) {
        if (msg != null) {
            getLogger().log(error, string(msg));
        }
    }

    /**
     * Log a parameterized message.
     */
    public void error(Object msg, Object... params) {
        if (msg != null) {
            getLogger().log(error, string(msg), params);
        }
    }

    public void error(Object msg, Throwable x) {
        getLogger().log(error, string(msg), x);
    }

    /**
     * Log a severe message.
     */
    public void error(Supplier<String> msg) {
        if (msg != null) {
            getLogger().log(error, msg);
        }
    }

    /**
     * Override point, returns the console logger by default.
     */
    public Logger getLogger() {
        if (logger == null) {
            logger = Logger.getLogger(getLogName());
        }
        return logger;
    }

    /**
     * True if the level is loggable.
     */
    public boolean info() {
        return getLogger().isLoggable(info);
    }

    /**
     * Log an infrequent major lifecycle event.
     */
    public void info(Object msg) {
        if (msg != null) {
            getLogger().log(info, string(msg));
        }
    }

    /**
     * Log a parameterized message.
     */
    public void info(Object msg, Object... params) {
        if (msg != null) {
            getLogger().log(info, string(msg), params);
        }
    }

    /**
     * Log an infrequent major lifecycle event.
     */
    public void info(Object msg, Throwable x) {
        getLogger().log(info, string(msg), x);
    }

    /**
     * Log an infrequent major lifecycle event.
     */
    public void info(Supplier<String> msg) {
        if (msg != null) {
            getLogger().log(info, msg);
        }
    }

    /**
     * True if the level is loggable.
     */
    public boolean trace() {
        return getLogger().isLoggable(trace);
    }

    /**
     * Log a trace or verbose event.
     */
    public void trace(Object msg) {
        if (msg != null) {
            getLogger().log(trace, string(msg));
        }
    }

    /**
     * Log a parameterized message.
     */
    public void trace(Object msg, Object... params) {
        if (msg != null) {
            getLogger().log(trace, string(msg), params);
        }
    }

    /**
     * Log a trace or verbose event.
     */
    public void trace(Object msg, Throwable x) {
        getLogger().log(trace, string(msg), x);
    }

    /**
     * Log a trace or verbose event.
     */
    public void trace(Supplier<String> msg) {
        if (msg != null) {
            getLogger().log(trace, msg);
        }
    }

    /**
     * Log a trace or verbose event.
     */
    public void warn(Object msg, Throwable x) {
        getLogger().log(warn, string(msg), x);
    }

    /**
     * True if the level is loggable.
     */
    public boolean warn() {
        return getLogger().isLoggable(warn);
    }

    /**
     * Log a trace or verbose event.
     */
    public void warn(Object msg) {
        if (msg != null) {
            getLogger().log(warn, string(msg));
        }
    }

    /**
     * Log a parameterized message.
     */
    public void warn(Object msg, Object... params) {
        if (msg != null) {
            getLogger().log(warn, string(msg), params);
        }
    }

    /**
     * Log a trace or verbose event.
     */
    public void warn(Supplier<String> msg) {
        if (msg != null) {
            getLogger().log(warn, msg);
        }
    }

    /////////////////////////////////////////////////////////////////
    // Protected Methods
    /////////////////////////////////////////////////////////////////

    /**
     * Override point, returns the simple class name by default.
     */
    protected String getLogName() {
        return "dsa";
    }

    protected void setLogger(Logger logger) {
        this.logger = logger;
    }

    /////////////////////////////////////////////////////////////////
    // Package / Privates Methods
    /////////////////////////////////////////////////////////////////

    private String string(Object obj) {
        if (obj == null) {
            return null;
        }
        return obj.toString();
    }

    /////////////////////////////////////////////////////////////////
    // Initialization
    /////////////////////////////////////////////////////////////////

    static {
        DSLogHandler.init();
    }


}
