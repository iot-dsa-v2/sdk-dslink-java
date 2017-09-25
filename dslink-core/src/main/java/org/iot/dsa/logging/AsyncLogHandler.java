package org.iot.dsa.logging;

import java.io.PrintStream;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import org.iot.dsa.time.DSTime;

/**
 * Enqueues logging records which are then processed by separate thread.
 *
 * @author Aaron Hansen
 */
public abstract class AsyncLogHandler extends Handler {

    ///////////////////////////////////////////////////////////////////////////
    // Constants
    ///////////////////////////////////////////////////////////////////////////

    ///////////////////////////////////////////////////////////////////////////
    // Fields
    ///////////////////////////////////////////////////////////////////////////

    private StringBuilder builder = new StringBuilder();
    private Calendar calendar = Calendar.getInstance();
    private LogHandlerThread logHandlerThread;
    private int maxQueueSize = DSLogging.DEFAULT_MAX_QUEUE;
    private boolean open = false;
    private PrintStream out;
    private LinkedList<LogRecord> queue = new LinkedList<LogRecord>();
    private int queueThrottle = (int) (DSLogging.DEFAULT_MAX_QUEUE * .90);

    ///////////////////////////////////////////////////////////////////////////
    // Constructors
    ///////////////////////////////////////////////////////////////////////////

    ///////////////////////////////////////////////////////////////////////////
    // Methods
    ///////////////////////////////////////////////////////////////////////////

    /**
     * The number of items on the queue.
     */
    public int backlog() {
        synchronized (queue) {
            return queue.size();
        }
    }

    /**
     * Closes the PrintStream, terminates the write thread and performs houseKeeping.
     */
    @Override
    public void close() {
        open = false;
        synchronized (queue) {
            queue.notifyAll();
        }
        houseKeeping();
        out.close();
    }

    @Override
    public void flush() {
        out.flush();
    }

    /**
     * Ten seconds by default, this is a guideline more than anything else.  Housekeeping can be
     * called sooner during low activity periods.
     */
    public long getHouseKeepingIntervalMillis() {
        return DSTime.MILLIS_TEN_SECONDS;
    }

    public int getMaxQueueSize() {
        return maxQueueSize;
    }

    /**
     * The sink for formatted messages.
     */
    protected PrintStream getOut() {
        return out;
    }

    /**
     * Used to name the thread that processes logging records.
     */
    protected abstract String getThreadName();

    /**
     * Subclass hook for activities such as rolling files and cleaning up old garbage. Called during
     * periods of inactivity or after the houseKeepingInterval is exceeded. Does nothing by default
     * and flush will be called just prior to this.
     */
    protected void houseKeeping() {
    }

    /**
     * Enqueues the record for the write thread.
     */
    @Override
    public void publish(LogRecord record) {
        if (open) {
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

    public AsyncLogHandler setMaxQueueSize(int maxQueueSize) {
        this.maxQueueSize = maxQueueSize;
        this.queueThrottle = (int) (maxQueueSize * .75);
        return this;
    }

    /**
     * Sets the sink for formatted messages.
     */
    protected AsyncLogHandler setOut(PrintStream out) {
        this.out = out;
        return this;
    }

    /**
     * This must be called for the handler to actually do anything. Starts the write thread if there
     * isn't already an active write thread.
     */
    protected void start() {
        if (logHandlerThread == null) {
            open = true;
            logHandlerThread = new LogHandlerThread();
            logHandlerThread.start();
        }
    }

    /**
     * Formats and writes the logging record the underlying stream.
     */
    protected void write(LogRecord record) {
        Formatter formatter = getFormatter();
        if (formatter != null) {
            out.println(formatter.format(record));
            return;
        }
        // timestamp
        calendar.setTimeInMillis(record.getMillis());
        DSTime.encodeForLogs(calendar, builder);
        // log name
        builder.append(" [");
        builder.append(record.getLoggerName());
        builder.append("] ");
        // severity
        builder.append(record.getLevel().getLocalizedName());
        // class
        if (record.getSourceClassName() != null) {
            builder.append(" - ");
            builder.append(record.getSourceClassName());
        }
        // method
        if (record.getSourceMethodName() != null) {
            builder.append(" - ");
            builder.append(record.getSourceMethodName());
        }
        // message
        String msg = record.getMessage();
        if ((msg != null) && (msg.length() > 0)) {
            Object[] params = record.getParameters();
            if (params != null) {
                msg = String.format(msg, params);
            }
            builder.append(" - ");
            builder.append(msg);
        }
        out.println(builder.toString());
        builder.setLength(0);
        // exception
        Throwable thrown = record.getThrown();
        if (thrown != null) {
            thrown.printStackTrace(out);
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // Inner Classes
    ///////////////////////////////////////////////////////////////////////////

    private class LogHandlerThread extends Thread {

        public LogHandlerThread() {
            super(AsyncLogHandler.this.getThreadName());
            setDaemon(true);
        }

        public void run() {
            long lastHouseKeeping = System.nanoTime();
            long now;
            LogRecord record;
            boolean emptyQueue;
            while (open) {
                record = null;
                synchronized (queue) {
                    emptyQueue = queue.isEmpty();
                    if (emptyQueue) {
                        try {
                            queue.wait(DSTime.MILLIS_SECOND);
                        } catch (Exception ignore) {
                        }
                        emptyQueue = queue.isEmpty(); //housekeeping opportunity flag
                    } else {
                        record = queue.removeFirst();
                    }
                }
                if (open) {
                    if (record != null) {
                        write(record);
                        Thread.yield();
                    }
                    if (emptyQueue) {
                        //housekeeping opportunity
                        now = System.currentTimeMillis();
                        long min = getHouseKeepingIntervalMillis() / 2;
                        if ((now - lastHouseKeeping) > min) {
                            flush();
                            houseKeeping();
                            lastHouseKeeping = System.nanoTime();
                        }
                    } else {
                        now = System.currentTimeMillis();
                        if ((now - lastHouseKeeping) > getHouseKeepingIntervalMillis()) {
                            flush();
                            houseKeeping();
                            lastHouseKeeping = System.nanoTime();
                        }
                    }
                }
            }
            logHandlerThread = null;
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // Initialization
    ///////////////////////////////////////////////////////////////////////////

} //class
