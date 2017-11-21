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
    private static long nextCycle = -1;
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
        long now = System.currentTimeMillis();
        long nextCycleTmp = now + 60000;
        Timer current = null;
        Timer next = null;
        Timer keepHead = null;
        Timer keepTail = null;
        long tmp;
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
            if (tmp > 0) {
                if (tmp < nextCycleTmp) {
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
            if (nextCycleTmp < nextCycle) {
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
    public static Timer run(Runnable arg, long start, long interval) {
        Timer f = new Timer(arg, start, interval);
        synchronized (DSRuntime.class) {
            if (timerHead == null) {
                timerHead = f;
                timerTail = f;
            } else {
                timerTail.next = f;
                timerTail = f;
            }
            if (start < nextCycle) {
                nextCycle = start;
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
        Timer f = new Timer(arg, at, -1);
        synchronized (DSRuntime.class) {
            if (timerHead == null) {
                timerHead = f;
                timerTail = f;
            } else {
                timerTail.next = f;
                timerTail = f;
            }
            if (at < nextCycle) {
                nextCycle = at;
                DSRuntime.class.notifyAll();
            }
        }
        return f;
    }

    /**
     * Run once after the given delay.
     *
     * @param arg         What to runAt.
     * @param delayMillis The number of millis to wait before running.
     * @return For inspecting and cancel execution.
     */
    public static Timer runDelayed(Runnable arg, long delayMillis) {
        return runAt(arg, System.currentTimeMillis() + delayMillis);
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
        private long lastRun = -1;
        private Timer next; //linked list
        private long nextRun = 1; //0 == canceled, <0 == done
        private Runnable runnable;
        private boolean running = false;

        Timer(Runnable runnable, long start, long interval) {
            this.interval = interval;
            if (start <= 0) {
                start = System.currentTimeMillis();
            }
            this.nextRun = start;
            this.runnable = runnable;
        }

        /**
         * Cancel execution, will not impact current running tasks and will have no effect if
         * already cancelled.
         */
        public void cancel() {
            if (nextRun > 0) {
                nextRun = 0;
            }
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
            return nextRun == 0;
        }

        /**
         * True if cancelled or was a one time execution and that has finished.
         */
        public boolean isFinished() {
            return nextRun <= 0;
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
            return lastRun;
        }

        /**
         * The next scheduled time to run.
         *
         * @return 0 or less when finished.
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
        long run(long now) {
            if (nextRun <= 0) {
                return nextRun;
            }
            if (now < nextRun) {
                return nextRun;
            }
            if (running) {
                return nextRun;
            }
            running = true;
            DSRuntime.run(this);
            count++;
            lastRun = nextRun;
            if (interval > 0) {
                while (nextRun <= now) {
                    nextRun += interval;
                }
            } else {
                nextRun = -1;
            }
            return nextRun;
        }

        /**
         * The number of completed runs.
         */
        public long runCount() {
            return count;
        }

        public String toString() {
            StringBuilder buf = new StringBuilder();
            DSTime.encode(nextRun, false, buf);
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
                    delta = nextCycle - System.currentTimeMillis();
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
        int min = Math.max(1, DSThreadPool.getNumProcessors());
        threadPool.setMinMax(min, DSThreadPool.getNumProcessors() * 25);
        runtimeThread = new RuntimeThread();
        runtimeThread.start();
    }

}
