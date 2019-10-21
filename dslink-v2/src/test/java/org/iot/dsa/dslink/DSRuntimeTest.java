package org.iot.dsa.dslink;

import org.iot.dsa.DSRuntime;
import org.iot.dsa.DSRuntime.Timer;
import org.testng.Assert;
import org.testng.annotations.Test;

public class DSRuntimeTest {

    private static Object lock1 = new Object();
    private static Object lock2 = new Object();
    private static Object lock3 = new Object();
    private static Object lock4 = new Object();
    private static Object lock5 = new Object();
    private static int runs = 0;
    private static int runsImmed = 0;
    private static int runsWithDelay = 0;
    private static boolean success = false;
    private static boolean successWithDelay = false;

    @Test
    public void test1() throws Exception {
        runs = 0;
        Timer tim = DSRuntime.run(() -> {
            runs += 1;
            if (runs >= 5) {
                synchronized (lock1) {
                    lock1.notifyAll();
                }
            }
        }, System.currentTimeMillis() + 400, 100);

//        System.out.println("test1    now: " + System.nanoTime());
//        System.out.println("test1nextrun: " + tim.nextRun());

        synchronized (lock1) {
            lock1.wait(1000);
        }
        Assert.assertTrue(runs >= 5);
    }

    @Test
    public void test2() throws Exception {
        runsWithDelay = 0;
        Timer tim = DSRuntime.runAfterDelay(() -> {
            runsWithDelay += 1;
            if (runsWithDelay >= 5) {
                synchronized (lock2) {
                    lock2.notifyAll();
                }
            }
        }, 400, 100);

//        System.out.println("test2    now: " + System.nanoTime());
//        System.out.println("test2nextrun: " + tim.nextRun());

        synchronized (lock2) {
            lock2.wait(1000);
        }
        Assert.assertTrue(runsWithDelay >= 5);
    }

    @Test
    public void test3() throws Exception {
        success = false;
        Timer tim = DSRuntime.runAt(() -> {
            success = true;
            synchronized (lock3) {
                lock3.notifyAll();
            }
        }, System.currentTimeMillis() + 400);

//        System.out.println("test3    now: " + System.nanoTime());
//        System.out.println("test3nextrun: " + tim.nextRun());

        synchronized (lock3) {
            lock3.wait(500);
        }
        Assert.assertTrue(success);
    }

    @Test
    public void test4() throws Exception {
        successWithDelay = false;
        Timer tim = DSRuntime.runDelayed(() -> {
            successWithDelay = true;
            synchronized (lock4) {
                lock4.notifyAll();
            }
        }, 400);

//        System.out.println("test4    now: " + System.nanoTime());
//        System.out.println("test4nextrun: " + tim.nextRun());

        synchronized (lock4) {
            lock4.wait(500);
        }
        Assert.assertTrue(successWithDelay);
    }

    @Test
    public void test5() throws Exception {
        runsImmed = 0;
        Timer tim = DSRuntime.run(() -> {
            runsImmed += 1;
            if (runsImmed >= 5) {
                synchronized (lock5) {
                    lock5.notifyAll();
                }
            }
        }, 0, 100);

//        System.out.println("test1    now: " + System.nanoTime());
//        System.out.println("test1nextrun: " + tim.nextRun());

        synchronized (lock5) {
            lock5.wait(600);
        }
        Assert.assertTrue(runsImmed >= 5);
    }

}
