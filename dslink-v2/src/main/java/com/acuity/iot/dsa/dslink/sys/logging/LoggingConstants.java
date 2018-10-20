package com.acuity.iot.dsa.dslink.sys.logging;

import java.util.logging.Level;

public class LoggingConstants {

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

}


