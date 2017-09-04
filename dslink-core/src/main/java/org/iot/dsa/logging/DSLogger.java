package org.iot.dsa.logging;

import static java.util.logging.Level.CONFIG;
import static java.util.logging.Level.FINE;
import static java.util.logging.Level.FINER;
import static java.util.logging.Level.FINEST;
import static java.util.logging.Level.INFO;
import static java.util.logging.Level.SEVERE;
import static java.util.logging.Level.WARNING;

import java.util.logging.Logger;

/**
 * Logging abstraction and helper.  Enables efficient logging using ternary expressions.
 *
 * <p>
 *
 * For example:  finer(finer() ? complexMsg() : null)
 *
 * <p>
 *
 * Level Guidelines:
 *
 * <ul>
 *
 * <li>finest = verbose or trace
 *
 * <li>finer  = debug
 *
 * <li>fine   = common or frequent event
 *
 * <li>config = configuration info
 *
 * <li>info   = major lifecycle event
 *
 * <li>warn   = unusual and infrequent, but not critical
 *
 * <li>severe = critical / fatal error or event
 *
 * </ul>
 *
 * @author Aaron Hansen
 */
public class DSLogger {

    /////////////////////////////////////////////////////////////////
    // Methods - In alphabetical order by method name.
    /////////////////////////////////////////////////////////////////

    /**
     * True if the level is loggable.
     */
    public boolean config() {
        return getLogger().isLoggable(CONFIG);
    }

    public void config(Object msg) {
        getLogger().log(CONFIG, string(msg));
    }

    /**
     * Override point, returns the runtime logger by default.
     */
    public Logger getLogger() {
        return DSLogging.getDefaultLogger();
    }

    /**
     * True if the level is loggable.
     */
    public boolean fine() {
        return getLogger().isLoggable(FINE);
    }

    /**
     * Log a frequent event.
     */
    public void fine(Object msg) {
        getLogger().log(FINE, string(msg));
    }

    /**
     * Log a frequent event.
     */
    public void fine(Object msg, Throwable x) {
        getLogger().log(FINE, string(msg), x);
    }

    /**
     * True if the level is loggable.
     */
    public boolean finer() {
        return getLogger().isLoggable(FINER);
    }

    /**
     * Log a debug event.
     */
    public void finer(Object msg) {
        getLogger().log(FINER, string(msg));
    }

    /**
     * Log a debug event.
     */
    public void finer(Object msg, Throwable x) {
        getLogger().log(FINER, string(msg), x);
    }

    /**
     * True if the level is loggable.
     */
    public boolean finest() {
        return getLogger().isLoggable(FINEST);
    }

    /**
     * Log a trace or verbose event.
     */
    public void finest(Object msg) {
        getLogger().log(FINEST, string(msg));
    }

    /**
     * Log a trace or verbose event.
     */
    public void finest(Object msg, Throwable x) {
        getLogger().log(FINEST, string(msg), x);
    }

    /**
     * True if the level is loggable.
     */
    public boolean info() {
        return getLogger().isLoggable(INFO);
    }

    /**
     * Log an infrequent major lifecycle event.
     */
    public void info(Object msg) {
        getLogger().log(INFO, string(msg));
    }

    /**
     * Log an infrequent major lifecycle event.
     */
    public void info(Object msg, Throwable x) {
        getLogger().log(INFO, string(msg), x);
    }

    /**
     * True if the level is loggable.
     */
    public boolean severe() {
        return getLogger().isLoggable(SEVERE);
    }

    public void severe(Object msg) {
        getLogger().log(SEVERE, string(msg));
    }

    public void severe(Object msg, Throwable x) {
        getLogger().log(SEVERE, string(msg), x);
    }

    private String string(Object obj) {
        if (obj == null) {
            return null;
        }
        return obj.toString();
    }

    /**
     * True if the level is loggable.
     */
    public boolean warn() {
        return getLogger().isLoggable(WARNING);
    }

    /**
     * Log an unusual but not critical event.
     */
    public void warn(Object msg) {
        getLogger().log(WARNING, string(msg));
    }

    /**
     * Log an unusual but not critical event.
     */
    public void warn(Object msg, Throwable x) {
        getLogger().log(WARNING, string(msg), x);
    }


}
