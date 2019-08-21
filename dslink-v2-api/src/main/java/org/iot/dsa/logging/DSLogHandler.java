package org.iot.dsa.logging;

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
import org.iot.dsa.time.Time;

/**
 * All instances of this handler asynchronously print log records to System.out.
 *
 * @author Aaron Hansen
 */
public class DSLogHandler extends Handler {

    ///////////////////////////////////////////////////////////////////////////
    // Class Fields
    ///////////////////////////////////////////////////////////////////////////

    static final int STATE_CLOSED = 0;
    static final int STATE_OPEN = 1;
    static final int STATE_CLOSE_PENDING = 2;
    static final String lineSeparator;
    private static Logger root;
    private static Level rootLevel;
    private StringBuilder builder = new StringBuilder();
    private LogHandlerThread logHandlerThread;
    private int maxQueueSize = 2500;

    ///////////////////////////////////////////////////////////////////////////
    // Instance Fields
    ///////////////////////////////////////////////////////////////////////////
    private LinkedList<LogRecord> queue = new LinkedList<LogRecord>();
    private int queueThrottle = (int) (maxQueueSize * .90);
    private int state = STATE_CLOSED;

    ///////////////////////////////////////////////////////////////////////////
    // Constructors
    ///////////////////////////////////////////////////////////////////////////

    public DSLogHandler() {
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
        long start = System.currentTimeMillis();
        synchronized (queue) {
            while (!queue.isEmpty()) {
                //give up after 15sec
                if ((System.currentTimeMillis() - start) > 15000) {
                    break;
                }
                queue.notifyAll();
                try {
                    queue.wait(1000);
                } catch (Exception ignore) {
                }
            }
        }
        flush();
    }

    @Override
    public void flush() {
        System.out.flush();
    }

    /**
     * A convenience for Logger.getLogger("").getLevel().
     */
    public static Level getRootLevel() {
        return Logger.getLogger("").getLevel();
    }

    /**
     * Installs the root handler.
     */
    public static void init() {
        if (root == null) {
            root = Logger.getLogger("");
        }
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
        if (root == null) {
            root = Logger.getLogger("");
        }
        root.setLevel(level);
    }

    /**
     * Stringify the log record in the DSA format.
     */
    public static String toString(Handler handler, LogRecord record) {
        StringBuilder buf = new StringBuilder();
        DSLogHandler.write(handler, record, buf);
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
     * Formats and writes the logging record to System.out.  Easily overridable if you
     * wish to do something else.
     */
    protected void write(LogRecord record) {
        write(this, record, builder);
        synchronized (DSLogHandler.class) {
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
    static void toString(Level level, StringBuilder buf) {
        String s = DSLevel.valueOf(level).toString();
        buf.append(s);
        int pad = 6 - s.length();
        for (int i = pad; --i >= 0; ) {
            buf.append(' ');
        }
    }

    /**
     * Formats and writes the logging record the underlying stream.
     */
    static void write(Handler handler, LogRecord record, StringBuilder builder) {
        if (builder == null) {
            builder = new StringBuilder();
        }
        builder.append('[');
        // timestamp
        Calendar calendar = Time.getCalendar(record.getMillis());
        Time.encodeForLogs(calendar, builder);
        builder.append(']');
        builder.append(' ');
        // severity
        toString(record.getLevel(), builder);
        // log name
        builder.append(" [");
        builder.append(record.getLoggerName()).append("] ");
        // class
        if (record.getSourceClassName() != null) {
            builder.append(record.getSourceClassName());
            builder.append(" - ");
        }
        // method
        if (record.getSourceMethodName() != null) {
            builder.append(record.getSourceMethodName());
            builder.append(" - ");
        }
        // message
        String msg = record.getMessage();
        if ((msg != null) && (msg.length() > 0)) {
            Object[] params = record.getParameters();
            if (params != null) {
                msg = String.format(msg, params);
            }
            builder.append(msg);
        }
        // exception
        Throwable thrown = record.getThrown();
        if (thrown != null) {
            builder.append(lineSeparator);
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            thrown.printStackTrace(pw);
            pw.close();
            builder.append(sw.toString());
        }
        Time.recycle(calendar);
    }

    /* Old V2 Format, save for now...
     * Formats and writes the logging record the underlying stream.
    static void write(Handler handler, LogRecord record, StringBuilder buf) {
        Formatter formatter = handler.getFormatter();
        if (formatter != null) {
            buf.append(formatter.format(record));
            return;
        }
        // severity
        buf.append('[').append(toString(record.getLevel())).append(' ');
        // timestamp
        Calendar calendar = Time.getCalendar(record.getMillis());
        calendar.setTimeInMillis(record.getMillis());
        Time.encodeForLogs(calendar, buf);
        Time.recycle(calendar);
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
            buf.append(lineSeparator);
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            thrown.printStackTrace(pw);
            pw.close();
            buf.append(sw.toString());
        }
    }
    */

    ///////////////////////////////////////////////////////////////////////////
    // Inner Classes
    ///////////////////////////////////////////////////////////////////////////

    private class LogHandlerThread extends Thread {

        public LogHandlerThread() {
            super("DSLogHandler");
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
                                queue.wait(Time.MILLIS_SECOND);
                            } catch (Exception ignore) {
                            }
                        } else {
                            state = STATE_CLOSED;
                        }
                    } else {
                        record = queue.removeFirst();
                    }
                }
                if (record != null) {
                    write(record);
                    record = null;
                    Thread.yield();
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
        if (root == null) {
            root = Logger.getLogger("");
        }
        rootLevel = root.getLevel();
        for (Handler handler : root.getHandlers()) {
            root.removeHandler(handler);
        }
        DSLogHandler async = new DSLogHandler();
        async.setLevel(rootLevel);
        root.addHandler(async);
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        pw.println();
        pw.flush();
        lineSeparator = sw.toString();
    }

}
