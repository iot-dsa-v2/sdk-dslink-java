package org.iot.dsa;

import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.iot.dsa.time.Time;

/**
 * A simple thread pool with the option for an unbounded maximum number of threads.
 *
 * @author Aaron Hansen
 */
class DSThreadPool {

    /////////////////////////////////////////////////////////////////
    // Constants
    /////////////////////////////////////////////////////////////////

    /////////////////////////////////////////////////////////////////
    // Fields
    /////////////////////////////////////////////////////////////////

    private boolean alive = true;
    private int idleThreads = 0;
    private int max;
    private int min;
    private int numThreads = 0;
    private LinkedList queue = new LinkedList();
    private String threadName;
    private int totalCreated = 0;

    /////////////////////////////////////////////////////////////////
    // Constructors
    /////////////////////////////////////////////////////////////////

    /**
     * Constructs an a thread pool with the min number of threads equal to the number of processors
     * and an unbounded max.
     *
     * @param name Thread name prefix, the total number of threads created in the pool will be
     *             appended.
     */
    public DSThreadPool(String name) {
        this.threadName = name;
        this.min = Math.max(1, getNumProcessors());
        this.max = -1;
    }

    /**
     * @param name Thread name prefix, the total number of threads created in the pool will be
     *             appended.
     * @param min  Must be 0 or more.
     * @param max  Use less than zero to indicate an unbounded max.
     */
    public DSThreadPool(String name, int min, int max) {
        this.threadName = name;
        if (min < 0) {
            throw new IllegalArgumentException("Min threads less than 0");
        } else if ((max >= 0) && (max < min)) {
            throw new IllegalArgumentException("Max threads less than min");
        }
        this.min = min;
        this.max = max;
    }

    /////////////////////////////////////////////////////////////////
    // Methods - Public and in alphabetical order by method threadName.
    /////////////////////////////////////////////////////////////////

    /**
     * Enqueue the runnable to be executed by a thread in the pool.  If the thread pool has been
     * destroyed this does nothing.
     */
    public synchronized void enqueue(Runnable arg) {
        if (alive) {
            queue.add(arg);
            updateThreads();
            notify();
        }
    }

    /**
     * The maximum number of threads to keep alive.  If 0 or less, the thread pool is unbounded.
     */
    public int getMaxThreads() {
        return min;
    }

    /**
     * The minimum number of threads to keep alive.
     */
    public int getMinThreads() {
        return min;
    }

    /**
     * Can be used for setting a max number of threads.
     */
    public static int getNumProcessors() {
        try {
            return Runtime.getRuntime().availableProcessors();
        } catch (Throwable t) {
        }
        return 2;
    }

    /**
     * Sets the min and max number of threads.
     *
     * @param min Must be zero or more.
     * @param max If zero or less, the thread pool will be unbounded.  If greater than zero, must be
     *            larger than min.
     */
    public void setMinMax(int min, int max) {
        if (min < 0) {
            throw new IllegalArgumentException("Min threads cannot be < 0");
        }
        if ((max > 0) && (max < min)) {
            throw new IllegalArgumentException("Max threads cannot be < min");
        }
        this.min = min;
        this.max = max;
    }

    /**
     * Permanently terminates the thread pool.
     */
    public void shutdown() {
        alive = false;
        synchronized (this) {
            notifyAll();
        }
    }

    /////////////////////////////////////////////////////////////////
    // Methods - Protected and in alphabetical order by method threadName.
    /////////////////////////////////////////////////////////////////

    @Override
    protected void finalize() throws Throwable {
        alive = false;
        synchronized (this) {
            notifyAll();
        }
    }

    protected void updateThreads() {
        if (idleThreads == 0) {
            if ((max <= 0) || (numThreads < max)) {
                numThreads++;
                totalCreated++;
                StringBuilder buf = new StringBuilder(threadName).append('-')
                                                                 .append(totalCreated);
                new DSThread(buf.toString()).start();
            }
        }
    }

    /////////////////////////////////////////////////////////////////
    // Methods - Package and in alphabetical order by method threadName.
    /////////////////////////////////////////////////////////////////

    /////////////////////////////////////////////////////////////////
    // Methods - Private and in alphabetical order by method threadName.
    /////////////////////////////////////////////////////////////////

    private Runnable dequeue() {
        if (queue.size() == 0) {
            return null;
        }
        return (Runnable) queue.remove(0);
    }

    /////////////////////////////////////////////////////////////////
    // Inner Classes - in alphabetical order by class threadName.
    /////////////////////////////////////////////////////////////////

    private class DSThread extends Thread {

        public DSThread(String name) {
            super(name);
            setDaemon(true);
        }

        public void run() {
            long start;
            Runnable r = null;
            try {
                while (alive) {
                    start = System.currentTimeMillis();
                    synchronized (DSThreadPool.this) {
                        idleThreads++;
                        while ((r == null) && alive) {
                            r = dequeue();
                            if (r == null) {
                                if (((System.currentTimeMillis() - start) > Time.MILLIS_MINUTE) &&
                                        (numThreads > min)) {
                                    idleThreads--;
                                    return;
                                } else {
                                    try {
                                        DSThreadPool.this.wait(Time.MILLIS_FIVE_SECONDS);
                                    } catch (Exception ignore) {
                                    }
                                }
                            }
                        }
                        idleThreads--;
                        DSThreadPool.this.notify();
                        updateThreads();
                    }
                    if (r != null) {
                        try {
                            r.run();
                        } catch (Exception x) {
                            Logger.getLogger("").log(Level.FINER, r.toString(), x);
                        }
                        r = null;
                    }
                }
            } finally {
                synchronized (DSThreadPool.this) {
                    numThreads--;
                }
            }
        }
    }

    /////////////////////////////////////////////////////////////////
    // Initialization
    /////////////////////////////////////////////////////////////////

}
