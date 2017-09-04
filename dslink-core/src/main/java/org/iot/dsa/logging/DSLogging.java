package org.iot.dsa.logging;

import java.io.File;
import java.io.PrintStream;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Acquire loggers here.  Multiple logs may share the same file, there will be a single file handler
 * per absolute file path and it is thread safe.
 *
 * @author Aaron Hansen
 */
public class DSLogging {

    ///////////////////////////////////////////////////////////////////////////
    // Constants
    ///////////////////////////////////////////////////////////////////////////

    /**
     * This is the threshold, not a hard limit: 10 megs by default.
     */
    public static int DEFAULT_BACKUP_THRESHOLD = 10 * 1024 * 1024;

    /**
     * The default number of backups to retain: 10 by default.
     */
    public static int DEFAULT_MAX_BACKUPS = 10;

    /**
     * Max async queue size: 2500 by default.
     */
    public static int DEFAULT_MAX_QUEUE = 2500;

    ///////////////////////////////////////////////////////////////////////////
    // Fields
    ///////////////////////////////////////////////////////////////////////////

    private static File defaultFile;
    private static Level defaultLevel = Level.INFO;
    private static Logger defaultLogger = Logger.getLogger("");

    ///////////////////////////////////////////////////////////////////////////
    // Constructors
    ///////////////////////////////////////////////////////////////////////////

    // Prevent instantiation.
    private DSLogging() {
    }

    ///////////////////////////////////////////////////////////////////////////
    // Methods
    ///////////////////////////////////////////////////////////////////////////

    /**
     * The default logger.
     */
    public static Logger getDefaultLogger() {
        return defaultLogger;
    }

    /**
     * Adds a handler for the default log file to the named logger, if there isn't one already. This
     * can be used repeatedly to acquire the same logger, but doing so would be inefficient.  Use
     * Logger.getLogger after this has installed the handler.
     *
     * @param name Log name.
     */
    public static Logger getLogger(String name) {
        if (defaultFile == null) {
            return getLogger(name, System.out);
        }
        Logger ret = Logger.getLogger(name);
        FileLogHandler fileLogHandler = FileLogHandler.getHandler(defaultFile);
        for (Handler handler : ret.getHandlers()) {
            if (handler == fileLogHandler) {
                return ret;
            }
        }
        ret.addHandler(fileLogHandler);
        ret.setLevel(defaultLevel);
        return ret;
    }

    /**
     * Adds a FileLogHandler to the named logger, if there isn't one already. This can be used
     * repeatedly to acquire the same logger, but doing so would be inefficient.  Use
     * Logger.getLogger after this has installed the handler.
     *
     * @param name    Log name.
     * @param logFile Where record the logging, may be null.  Multiple logs can safely share the
     *                same file.  If null, will route to System.out.
     */
    public static Logger getLogger(String name, File logFile) {
        Logger ret = Logger.getLogger(name);
        FileLogHandler fileLogHandler = FileLogHandler.getHandler(logFile);
        for (Handler handler : ret.getHandlers()) {
            if (handler == fileLogHandler) {
                return ret;
            }
        }
        ret.addHandler(fileLogHandler);
        ret.setLevel(defaultLevel);
        return ret;
    }

    /**
     * Adds a PrintStreamLogHandler to the named logger, if there isn't one already. This can be
     * used repeatedly to acquire the same logger, but doing so would be inefficient.  Use
     * Logger.getLogger after this has installed the handler.
     *
     * @param name Log name.
     * @param out  Where to print the logging.
     */
    public static Logger getLogger(String name, PrintStream out) {
        Logger ret = Logger.getLogger(name);
        for (Handler handler : ret.getHandlers()) {
            if (handler instanceof PrintStreamLogHandler) {
                PrintStreamLogHandler pslh = (PrintStreamLogHandler) handler;
                if (pslh.getOut() == out) {
                    return ret;
                }
            }
        }
        ret.addHandler(new PrintStreamLogHandler(name, out));
        ret.setLevel(defaultLevel);
        return ret;
    }

    /**
     * Removes existing handlers from the root logger and installs a PrintStreamLogHandler for
     * System.out.
     */
    public static void replaceRootHandler() {
        Logger global = Logger.getLogger("");
        for (Handler handler : global.getHandlers()) {
            global.removeHandler(handler);
        }
        global.addHandler(new PrintStreamLogHandler("Root Logger", System.out));
    }

    public static void setDefaultFile(File file) {
        defaultFile = file;
    }

    public static void setDefaultLevel(Level level) {
        defaultLevel = level;
    }

    public static void setDefaultLogger(Logger logger) {
        defaultLogger = logger;
    }

    ///////////////////////////////////////////////////////////////////////////
    // Inner Classes
    ///////////////////////////////////////////////////////////////////////////

    ///////////////////////////////////////////////////////////////////////////
    // Initialization
    ///////////////////////////////////////////////////////////////////////////

} //class
