package org.iot.dsa.logging;

import java.io.File;
import java.io.PrintStream;
import java.util.Enumeration;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

/**
 * Static utilities for configuring the logging subsystem.
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
     * Closes all async log handlers.
     */
    public static void close() {
        Enumeration<String> logs = LogManager.getLogManager().getLoggerNames();
        while (logs.hasMoreElements()) {
            Logger l = Logger.getLogger(logs.nextElement());
            for (Handler h : l.getHandlers()) {
                if (h instanceof AsyncLogHandler) {
                    h.close();
                }
            }
        }
    }

    /**
     * The default logger.
     */
    public static Logger getDefaultLogger() {
        return defaultLogger;
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
    static Logger getLogger(String name, PrintStream out) {
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

    public static void setDefaultLevel(Level level) {
        defaultLevel = level;
        defaultLogger.setLevel(level);
    }

    ///////////////////////////////////////////////////////////////////////////
    // Initialization
    ///////////////////////////////////////////////////////////////////////////

    static {
        defaultLogger = Logger.getLogger("");
        for (Handler handler : defaultLogger.getHandlers()) {
            defaultLogger.removeHandler(handler);
        }
        defaultLogger.addHandler(new PrintStreamLogHandler("Root Logger", System.out));
    }

}
