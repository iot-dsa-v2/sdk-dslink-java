package org.iot.dsa;

import org.iot.dsa.time.DSTime;

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
    private static Long nextCycle = null;
    private static RuntimeThread runtimeThread;
    private static Timer timerHead;
    private static Timer timerTail;
    private static DSThreadPool threadPool;

    ///////////////////////////////////////////////////////////////////////////
    // Constructors
    ///////////////////////////////////////////////////////////////////////////

    private DSRuntime() {
    }

    ///////////////////////////////////////////////////////////////////////////
    // Methods
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Returns the next time execution is needed.
     */
    private static void executeTimers() {
        long now = System.nanoTime();
        long nextCycleTmp = now + 60000000000l;
        Timer current = null;
        Timer next = null;
        Timer keepHead = null;
        Timer keepTail = null;
        Long tmp;
        //Take the task link list.
        synchronized (DSRuntime.class) {
            nextCycle = nextCycleTmp;
            current = timerHead;
            timerHead = null;
            timerTail = null;
        }
        //Execute items (if needed) and retains tasks with future work.
        while (alive && (current != null)) {
            tmp = current.run(now);
            next = current.next;
            current.next = null;
            if (tmp != null) {
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

    /**
     * Run as soon as possible on the application's thread pool and run only once.
     */
    public static void run(Runnable arg) {
        threadPool.enqueue(arg);
    }

    /**
     * Run periodically starting at the given time and repeat at the given millisecond interval.
     *
     * @param arg      What to runAt.
     * @param start    First absolute execution time, or if less or equal to 0, start immediately.
     * @param interval The millisecond interval at which to run.
     * @return For inspecting and cancel execution.
     */
    public static Timer run(Runnable arg, long start, long intervalMillis) {
        long delayMillis = start - System.currentTimeMillis();
        return runAfterDelay(arg, delayMillis, intervalMillis);
    }
    
    /**
     * Run periodically starting after the given millisecond delay and repeat at the given millisecond interval.
     *
     * @param arg      What to runAt.
     * @param start    First absolute execution time, or if less or equal to 0, start immediately.
     * @param interval The millisecond interval at which to run.
     * @return For inspecting and cancel execution.
     */
    public static Timer runAfterDelay(Runnable arg, long delayMillis, long intervalMillis) {
        long intervalNanos = intervalMillis * 1000000;
        long delyNanos = delayMillis * 1000000;
        long startNanos = System.nanoTime() + delyNanos;
        Timer f = new Timer(arg, startNanos, intervalNanos);
        synchronized (DSRuntime.class) {
            if (timerHead == null) {
                timerHead = f;
                timerTail = f;
            } else {
                timerTail.next = f;
                timerTail = f;
            }
            if (startNanos - nextCycle < 0) {
                nextCycle = startNanos;
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
        return runDelayed(arg, delayMillis);
    }

    /**
     * Run once after the given delay.
     *
     * @param arg         What to runAt.
     * @param delayMillis The number of millis to wait before running.
     * @return For inspecting and cancel execution.
     */
    public static Timer runDelayed(Runnable arg, long delayMillis) {
//        return runAt(arg, System.currentTimeMillis() + delayMillis);
        long delyNanos = delayMillis * 1000000;
        long startNanos = System.nanoTime() + delyNanos;
        Timer f = new Timer(arg, startNanos, -1);
        synchronized (DSRuntime.class) {
            if (timerHead == null) {
                timerHead = f;
                timerTail = f;
            } else {
                timerTail.next = f;
                timerTail = f;
            }
            if (startNanos - nextCycle < 0) {
                nextCycle = startNanos;
                DSRuntime.class.notifyAll();
            }
        }
        return f;
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
     * Can be used to inspect and cancel tasks passed to the run methods in DSRuntime.
     */
    public static class Timer implements Runnable {

        private long count = 0;
        private long interval = 0;
        private Long lastRun = null;
        private Timer next; //linked list
        private Long nextRun = null;
        boolean cancelled = false;
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
            if (nextRun != null) {
                nextRun = null;
                cancelled = true;
            }
        }

        private Long computeNextRun(long now) {
            if (interval <= 0) {
                return nextRun = null;
            }
            if (skipMissed) {
                while (nextRun - now <= 0) {
                    nextRun += interval;
                }
            } else {
                nextRun += interval;
            }
            return nextRun;
        }

        /**
         * The interval between runs, zero or less for no interval.
         */
        public long getInterval() {
            return interval;
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
            return nextRun == null;
        }

        /**
         * True when the runnable is being actually being executed.
         */
        public boolean isRunning() {
            return running;
        }

        /**
         * The lastRun run or null if it hasn't run yet.
         */
        public Long lastRun() {
            return lastRun;
        }

        /**
         * The next scheduled time to run.
         *
         * @return null when finished.
         */
        public long nextRun() {
            return nextRun;
        }

        /**
         * Do not call.
         */
        public void run() {
            try {
                runnable.run();
            } finally {
                running = false;
            }
        }

        /**
         * Executes the task if it is time.
         *
         * @param now The current time, just an efficiency.
         * @return The next update time, or 0 or less if done.
         */
        Long run(long now) {
            if (nextRun == null) {
                return nextRun;
            }
            if (now - nextRun < 0) {
                return nextRun;
            }
            if (running) {
                if (skipMissed) {
                    return computeNextRun(now);
                }
                return nextRun;
            }
            running = true;
            DSRuntime.run(this);
            count++;
            lastRun = nextRun;
            return computeNextRun(now);
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
        
        public long nextRunInSystemTimeMillis() {
            if (nextRun == null) {
                return cancelled ? 0 : -1;
            }
            long nowNanos = System.nanoTime();
            long nowMillis = System.currentTimeMillis();
            long nanosToRun = nextRun - nowNanos;
            long millisToRun = nanosToRun / 1000000;
            return nowMillis + millisToRun;
        }

        public String toString() {
            StringBuilder buf = new StringBuilder();
            DSTime.encode(nextRunInSystemTimeMillis(), false, buf);
            buf.append(" - ").append(runnable.toString());
            return buf.toString();
        }

    } //Timer

    /**
     * Executes timers.
     */
    private static class RuntimeThread extends Thread {

        public RuntimeThread() {
            super("DSRuntime");
            setDaemon(true);
        }

        public void run() {
            long delta;
            while (alive) {
                executeTimers();
                synchronized (DSRuntime.class) {
                    delta = nextCycle - System.nanoTime();
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

        public void run() {
            shutdown();
        }

    }

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
