package org.iot.dsa.logging;

import java.util.logging.Level;

/**
 * The DSA logging levels.
 */
public interface DSILevels {

    int TRACE = 300; //finest
    int DEBUG = 400; //finer
    int FINE = 500; //fine
    int CONFIG = 700; //config
    int WARN = 750; //custom
    int INFO = 800; //info
    int ERROR = 900; //warning
    int ADMIN = 950; //custom
    int FATAL = 1000; //severe

    Level all = Level.ALL;
    Level trace = Level.FINEST;
    Level debug = Level.FINER;
    Level fine = Level.FINE;
    Level warn = new DSLevel("Warn", WARN);
    Level info = Level.INFO;
    Level error = Level.WARNING;
    Level admin = new DSLevel("Admin", ADMIN);
    Level fatal = Level.SEVERE;
    Level off = Level.OFF;

}


