package com.acuity.iot.dsa.dslink.logging;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import org.iot.dsa.logging.DSILevels;
import org.iot.dsa.time.DSTime;

/**
 * All instances of this handler asynchronously print log records to System.out.
 *
 * @author Aaron Hansen
 */
public class AsyncLogHandler extends Handler implements DSILevels {

    ///////////////////////////////////////////////////////////////////////////
    // Class Fields
    ///////////////////////////////////////////////////////////////////////////

    static final int STATE_CLOSED = 0;
    static final int STATE_OPEN = 1;
    static final int STATE_CLOSE_PENDING = 2;
    static final String lineSeparator;

    private static Level rootLevel;
    private static int maxQueueSize = 10000;
    private static StringBuilder builder = new StringBuilder();
    private static int queueThrottle = (int) (maxQueueSize * .90);

    ///////////////////////////////////////////////////////////////////////////
    // Instance Fields
    ///////////////////////////////////////////////////////////////////////////

    private LogHandlerThread logHandlerThread;
    private LinkedList<LogRecord> queue = new LinkedList<LogRecord>();
    private int state = STATE_CLOSED;

    ///////////////////////////////////////////////////////////////////////////
    // Constructors
    ///////////////////////////////////////////////////////////////////////////

    public AsyncLogHandler() {
        start();
    }

    ///////////////////////////////////////////////////////////////////////////
    // Public Methods
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Waits for the async handler to empty it's queue but won't allow new messages on the queue.
     */
    @Override
    public void close() {
        synchronized (this) {
            if (state != STATE_OPEN) {
                return;
            }
            state = STATE_CLOSE_PENDING;
        }
        synchronized (queue) {
            while (!queue.isEmpty()) {
                queue.notifyAll();
                try {
                    queue.wait(1000);
                } catch (Exception ignore) {
                }
            }
        }
        flush();
        state = STATE_CLOSED;
    }

    /**
     * A convenience for Logger.getLogger("").getLevel().
     */
    public static Level getRootLevel() {
        return Logger.getLogger("").getLevel();
    }

    @Override
    public void flush() {
        System.out.flush();
    }

    /**
     * Enqueues the record for the write thread.
     */
    @Override
    public void publish(LogRecord record) {
        if (state == STATE_OPEN) {
            if (maxQueueSize > 0) {
                int size = queue.size();
                if (size >= queueThrottle) {
                    if (size < maxQueueSize) {
                        if (record.getLevel().intValue() < Level.INFO.intValue()) {
                            return;
                        }
                    } else {
                        return;
                    }
                }
            }
            Object[] params = record.getParameters();
            if ((params != null) && (params.length > 0)) {
                String msg = record.getMessage();
                if ((msg != null) && (msg.length() > 0)) {
                    Object param;
                    for (int i = params.length; --i >= 0; ) {
                        param = params[i];
                        if (param == null) {
                            continue;
                        } else if (param instanceof String) {
                            continue;
                        } else if (param instanceof Integer) {
                            continue;
                        } else if (param instanceof Boolean) {
                            continue;
                        } else if (param instanceof Byte) {
                            continue;
                        } else if (param instanceof Character) {
                            continue;
                        } else if (param instanceof Date) {
                            continue;
                        } else if (param instanceof Double) {
                            continue;
                        } else if (param instanceof Enum) {
                            continue;
                        } else if (param instanceof Float) {
                            continue;
                        } else if (param instanceof Long) {
                            continue;
                        } else if (param instanceof Short) {
                            continue;
                        } else if (param instanceof Calendar) {
                            params[i] = ((Calendar) param).clone();
                        } else if (param instanceof Number) {
                            Formatter formatter = getFormatter();
                            if (formatter != null) {
                                record.setMessage(formatter.formatMessage(record));
                            } else {
                                record.setMessage(String.format(msg, params));
                            }
                            record.setParameters(null);
                            break;
                        } else {
                            params[i] = param.toString();
                        }
                    }
                }
            }
            synchronized (queue) {
                queue.addLast(record);
                queue.notify();
            }
        }
    }

    /**
     * Applies the level to the root logger and it's handlers.
     */
    public static void setRootLevel(Level level) {
        Logger root = Logger.getLogger("");
        root.setLevel(level);
        for (Handler h : root.getHandlers()) {
            h.setLevel(level);
        }
    }

    /**
     * Stringify the log record in the DSA format.
     */
    public static String toString(Handler handler, LogRecord record) {
        StringBuilder buf = new StringBuilder();
        AsyncLogHandler.write(handler, record, buf);
        return buf.toString();
    }

    ///////////////////////////////////////////////////////////////////////////
    // Protected Methods
    ///////////////////////////////////////////////////////////////////////////

    /**
     * This must be called for the handler to actually do anything. Starts the write thread if there
     * isn't already an active write thread.
     */
    protected void start() {
        if (logHandlerThread == null) {
            state = STATE_OPEN;
            logHandlerThread = new LogHandlerThread();
            logHandlerThread.start();
        }
    }

    /**
     * Formats and writes the logging record the underlying stream.
     */
    protected void write(LogRecord record) {
        write(this, record, builder);
        synchronized (AsyncLogHandler.class) {
            System.out.println(builder.toString());
        }
        builder.setLength(0);
    }

    ///////////////////////////////////////////////////////////////////////////
    // Package / Private Methods
    ///////////////////////////////////////////////////////////////////////////

    /**
     * The DSA name mapping.
     */
    static String toString(Level level) {
        switch (level.intValue()) {
            case TRACE: //finest
                return "Trace";
            case DEBUG: //finer
                return "Debug";
            case FINE: //fine
            case CONFIG: //config
                return "Fine ";
            case WARN: //custom
                return "Warn ";
            case INFO: //info
                return "Info ";
            case ERROR: //warn
                return "Error";
            case ADMIN: //custom
                return "Admin";
            case FATAL: //severe
                return "Fatal";
        }
        return level.getLocalizedName();
    }

    /**
     * Formats and writes the logging record the underlying stream.
     */
    static void write(Handler handler, LogRecord record, StringBuilder buf) {
        Formatter formatter = handler.getFormatter();
        if (formatter != null) {
            buf.append(formatter.format(record));
            return;
        }
        // severity
        buf.append('[').append(toString(record.getLevel())).append(' ');
        // timestamp
        Calendar calendar = DSTime.getCalendar(record.getMillis());
        calendar.setTimeInMillis(record.getMillis());
        DSTime.encodeForLogs(calendar, buf);
        DSTime.recycle(calendar);
        buf.append(']');
        // log name
        String name = record.getLoggerName();
        if ((name != null) && !name.isEmpty()) {
            buf.append("[");
            buf.append(record.getLoggerName());
            buf.append(']');
        } else {
            buf.append("[Root]");
        }
        // class
        if (record.getSourceClassName() != null) {
            buf.append(record.getSourceClassName());
            buf.append(" - ");
        }
        // method
        if (record.getSourceMethodName() != null) {
            buf.append(record.getSourceMethodName());
            buf.append(" - ");
        }
        // message
        String msg = record.getMessage();
        if ((msg != null) && (msg.length() > 0)) {
            Object[] params = record.getParameters();
            if ((params != null) && (params.length > 0)) {
                msg = String.format(msg, params);
            }
            buf.append(' ').append(msg);
        }
        Throwable thrown = record.getThrown();
        if (thrown != null) {
            builder.append(lineSeparator);
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            thrown.printStackTrace(pw);
            pw.close();
            builder.append(sw.toString());
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // Inner Classes
    ///////////////////////////////////////////////////////////////////////////

    private class LogHandlerThread extends Thread {

        public LogHandlerThread() {
            super("AsyncLogHandler");
            setDaemon(true);
        }

        public void run() {
            LogRecord record = null;
            while (state != STATE_CLOSED) {
                synchronized (queue) {
                    if (queue.isEmpty()) {
                        try {
                            queue.notifyAll();
                        } catch (Exception ignore) {
                        }
                        if (state == STATE_OPEN) {
                            try {
                                queue.wait(DSTime.MILLIS_SECOND);
                            } catch (Exception ignore) {
                            }
                        }
                    } else {
                        record = queue.removeFirst();
                    }
                }
                if (state != STATE_CLOSED) {
                    if (record != null) {
                        write(record);
                        record = null;
                    }
                }
            }
            flush();
            logHandlerThread = null;
        }

    }

    ///////////////////////////////////////////////////////////////////////////
    // Initialization Classes
    ///////////////////////////////////////////////////////////////////////////

    static {
        Logger rootLogger = Logger.getLogger("");
        rootLevel = rootLogger.getLevel();
        for (Handler handler : rootLogger.getHandlers()) {
            rootLogger.removeHandler(handler);
        }
        AsyncLogHandler async = new AsyncLogHandler();
        async.setLevel(rootLevel);
        rootLogger.addHandler(async);
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        pw.println();
        pw.flush();
        lineSeparator = sw.toString();
    }

}
