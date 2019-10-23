package org.iot.dsa;

import org.iot.dsa.time.Time;

/**
 * DSA thread pool and timers.
 *
 * @author Aaron Hansen
 */
public class DSRuntime {

    ///////////////////////////////////////////////////////////////////////////
    // Fields
    ///////////////////////////////////////////////////////////////////////////

    private static boolean alive = true;
    private static boolean hasStarted = false;
    private static long nextCycle = 0;
    private static RuntimeThread runtimeThread;
    private static DSThreadPool threadPool;
    private static Timer timerHead;
    private static Timer timerTail;

    ///////////////////////////////////////////////////////////////////////////
    // Constructors
    ///////////////////////////////////////////////////////////////////////////

    private DSRuntime() {
    }

    ///////////////////////////////////////////////////////////////////////////
    // Methods
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Run as soon as possible on the application's thread pool and run only once.
     */
    public static void run(Runnable arg) {
        threadPool.enqueue(arg);
    }

    /**
     * Run periodically starting at the given time and repeat at the given millisecond interval.
     *
     * @param arg            What to runAt.
     * @param start          First absolute execution time, or if less or equal to 0, start immediately.
     * @param intervalMillis The millisecond interval at which to run.
     * @return For inspecting and cancel execution.
     */
    public static Timer run(Runnable arg, long start, long intervalMillis) {
        long delayMillis = start - System.currentTimeMillis();
        return runAfterDelay(arg, delayMillis < 0 ? 0 : delayMillis, intervalMillis);
    }

    /**
     * Run periodically starting after the given millisecond delay and repeat at the given millisecond interval.
     *
     * @param arg            What to runAt.
     * @param delayMillis    The number of millis to wait before first execution.
     * @param intervalMillis The millisecond interval at which to run.
     * @return For inspecting and cancel execution.
     */
    public static Timer runAfterDelay(Runnable arg, long delayMillis, long intervalMillis) {
        long intervalNanos = intervalMillis * Time.NANOS_IN_MS;
        long delayNanos = delayMillis * Time.NANOS_IN_MS;
        long startNanos = System.nanoTime() + delayNanos;
        Timer f = new Timer(arg, startNanos, intervalNanos);
        synchronized (DSRuntime.class) {
            if (timerHead == null) {
                timerHead = f;
                timerTail = f;
            } else {
                timerTail.next = f;
                timerTail = f;
            }
            if (!hasStarted || startNanos - nextCycle < 0) {
                nextCycle = startNanos;
                hasStarted = true;
                DSRuntime.class.notifyAll();
            }
        }
        return f;
    }

    /**
     * Run once at the given time.
     *
     * @param arg What to runAt.
     * @param at  Execution time.  If the time is past, it'll run right away.
     * @return For inspecting and cancel execution.
     */
    public static Timer runAt(Runnable arg, long at) {
        long delayMillis = at - System.currentTimeMillis();
        return runDelayed(arg, delayMillis < 0 ? 0 : delayMillis);
    }

    /**
     * Run once after the given delay.
     *
     * @param arg         What to runAt.
     * @param delayMillis The number of millis to wait before running.
     * @return For inspecting and cancel execution.
     */
    public static Timer runDelayed(Runnable arg, long delayMillis) {
        long delayNanos = delayMillis * Time.NANOS_IN_MS;
        long startNanos = System.nanoTime() + delayNanos;
        Timer f = new Timer(arg, startNanos, -1);
        synchronized (DSRuntime.class) {
            if (timerHead == null) {
                timerHead = f;
                timerTail = f;
            } else {
                timerTail.next = f;
                timerTail = f;
            }
            if (!hasStarted || startNanos - nextCycle < 0) {
                nextCycle = startNanos;
                hasStarted = true;
                DSRuntime.class.notifyAll();
            }
        }
        return f;
    }

    /**
     * Returns the next time execution is needed.
     */
    private static void executeTimers() {
        long now = System.nanoTime();
        long nextCycleTmp = now + 60000000000L;
        Timer current;
        Timer next;
        Timer keepHead = null;
        Timer keepTail = null;
        long tmp;
        boolean futureWork;
        //Take the task link list.
        synchronized (DSRuntime.class) {
            nextCycle = nextCycleTmp;
            hasStarted = true;
            current = timerHead;
            timerHead = null;
            timerTail = null;
        }
        //Execute items (if needed) and retains tasks with future work.
        while (alive && (current != null)) {
            futureWork = current.run(now);
            next = current.next;
            current.next = null;
            if (futureWork) {
                tmp = current.nextRunNanos();
                if (tmp - nextCycleTmp < 0) {
                    nextCycleTmp = tmp;
                }
                if (keepHead == null) {
                    keepHead = current;
                    keepTail = current;
                } else {
                    keepTail.next = current;
                    keepTail = current;
                }
            }
            current = next;
        }
        //Add the tasks that have future work back to the main linked list.
        synchronized (DSRuntime.class) {
            if (nextCycleTmp - nextCycle < 0) {
                nextCycle = nextCycleTmp;
            }
            if (keepHead != null) {
                if (timerHead == null) {
                    timerHead = keepHead;
                    timerTail = keepTail;
                } else {
                    timerTail.next = keepHead;
                    timerTail = keepTail;
                }
            }
        }
    }

    private static long nanoTimeToSystemTimeMillis(long nanoTime) {
        long nowNanos = System.nanoTime();
        long nowMillis = System.currentTimeMillis();
        long nanosTillTime = nanoTime - nowNanos;
        long millisTillTime = nanosTillTime / Time.NANOS_IN_MS;
        return nowMillis + millisTillTime;
    }

    private static void shutdown() {
        synchronized (DSRuntime.class) {
            alive = false;
            threadPool.shutdown();
            DSRuntime.class.notifyAll();
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // Inner Classes
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Executes timers.
     */
    private static class RuntimeThread extends Thread {

        public RuntimeThread() {
            super("DSRuntime");
            setDaemon(true);
        }

        @Override
        public void run() {
            long delta;
            while (alive) {
                executeTimers();
                synchronized (DSRuntime.class) {
                    delta = (nextCycle - System.nanoTime()) / Time.NANOS_IN_MS;
                    if (delta > 0) {
                        try {
                            DSRuntime.class.wait(delta);
                        } catch (Exception ignore) {
                        }
                    }
                }
                if (delta <= 0) {
                    Thread.yield();
                }
            }
        }
    } //RuntimeThread

    private static class ShutdownThread extends Thread {

        ShutdownThread() {
            super("DSRuntime Shutdown Hook");
        }

        @Override
        public void run() {
            shutdown();
        }

    }

    /**
     * Can be used to inspect and cancel tasks passed to the run methods in DSRuntime.
     */
    public static class Timer implements Runnable {

        boolean cancelled = false;
        private long count = 0;
        boolean done = false;
        boolean hasRun = false;
        private long interval;
        private long lastRun = 0;
        private Timer next; //linked list
        private long nextRun;
        private Runnable runnable;
        private boolean running = false;
        private boolean skipMissed = true;

        private Timer(Runnable runnable, long start, long interval) {
            this.interval = interval;
//            if (start <= 0) {
//                start = System.currentTimeMillis();
//            }
            this.nextRun = start;
            this.runnable = runnable;
        }

        /**
         * Cancel execution, will not impact current running tasks and will have no effect if
         * already cancelled.
         */
        public void cancel() {
            if (!done) {
                done = true;
                cancelled = true;
            }
        }

        /**
         * The interval between runs, zero or less for no interval.
         */
        public long getInterval() {
            return interval / Time.NANOS_IN_MS;
        }

        /**
         * The runnable being managed by this timer.
         */
        public Runnable getRunnable() {
            return runnable;
        }

        public boolean isCancelled() {
            return cancelled;
        }

        /**
         * True if cancelled or was a one time execution and that has finished.
         */
        public boolean isFinished() {
            return done;
        }

        /**
         * True when the runnable is being actually being executed.
         */
        public boolean isRunning() {
            return running;
        }

        /**
         * The lastRun run or -1 if it hasn't run yet.
         */
        public long lastRun() {
            return hasRun ? nanoTimeToSystemTimeMillis(lastRun) : -1;
        }

        /**
         * The next scheduled time to run.
         *
         * @return 0 or less when finished.
         */
        public long nextRun() {
            return done ? cancelled ? 0 : -1 : nanoTimeToSystemTimeMillis(nextRun);
        }

        /**
         * Do not call.
         */
        @Override
        public void run() {
            try {
                runnable.run();
            } finally {
                running = false;
            }
        }

        /**
         * The number of completed runs.
         */
        public long runCount() {
            return count;
        }

        /**
         * The default is true, set this to false if all intervals should be run, even if they run
         * later than scheduled.
         *
         * @param skipMissed False if intervals should be run after they were scheduled to.
         * @return this
         */
        public Timer setSkipMissedIntervals(boolean skipMissed) {
            this.skipMissed = skipMissed;
            return this;
        }

        @Override
        public String toString() {
            StringBuilder buf = new StringBuilder();
            Time.encode(nextRun(), false, buf);
            buf.append(" - ").append(runnable.toString());
            return buf.toString();
        }

        /**
         * Executes the task if it is time.
         *
         * @param now The current time, just an efficiency.
         * @return Whether the task should be run at some point in the future
         */
        boolean run(long now) {
            if (done) {
                return false;
            }
            if (now - nextRun < 0) {
                return true;
            }
            if (running) {
                if (skipMissed) {
                    return computeNextRun(now);
                }
                return true;
            }
            running = true;
            DSRuntime.run(this);
            count++;
            lastRun = nextRun;
            hasRun = true;
            return computeNextRun(now);
        }

        long nextRunNanos() {
            return nextRun;
        }

        private boolean computeNextRun(long now) {
            if (interval <= 0) {
                done = true;
                return false;
            }
            if (skipMissed) {
                while (nextRun - now <= 0) {
                    nextRun += interval;
                }
            } else {
                nextRun += interval;
            }
            return true;
        }

    } //Timer

    ///////////////////////////////////////////////////////////////////////////
    // Initialization
    ///////////////////////////////////////////////////////////////////////////

    static {
        Runtime.getRuntime().addShutdownHook(new ShutdownThread());
        threadPool = new DSThreadPool("DSRuntime");
        int min = Math.max(4, DSThreadPool.getNumProcessors());
        threadPool.setMinMax(min, -1);
        //threadPool.setMinMax(min, DSThreadPool.getNumProcessors() * 25);
        runtimeThread = new RuntimeThread();
        runtimeThread.start();
    }
}
